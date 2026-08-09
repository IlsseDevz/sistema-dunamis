package com.dunamis.sistema.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final Environment env;

    public LoginRateLimitFilter(Environment env) {
        this.env = env;
    }

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private static class Attempt {
        final AtomicInteger count = new AtomicInteger(0);
        volatile Instant windowStart = Instant.now();
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private Attempt resolveAttempt(String key) {
        return attempts.computeIfAbsent(key, k -> new Attempt());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Rate limiter can be disabled via property (useful for tests)
        String enabled = env.getProperty("app.security.rateLimiter.enabled", "true");
        if ("false".equalsIgnoreCase(enabled)) {
            return true;
        }
        // Filter only POST requests targeting the /login endpoint
        return !("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI() != null && request.getRequestURI().endsWith("/login"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        Attempt attempt = resolveAttempt(ip);
        Instant now = Instant.now();
        synchronized (attempt) {
            if (Duration.between(attempt.windowStart, now).compareTo(WINDOW) > 0) {
                attempt.windowStart = now;
                attempt.count.set(0);
            }
            int current = attempt.count.incrementAndGet();
            if (current <= MAX_ATTEMPTS) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Too many login attempts. Please try again later.");
    }
}
