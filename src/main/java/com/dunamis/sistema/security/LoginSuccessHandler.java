package com.dunamis.sistema.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String redirectUrl = resolveRedirectUrl(roles);
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    private String resolveRedirectUrl(Set<String> roles) {
        if (roles.contains("ROLE_ADMIN")) {
            return "/admin";
        }
        if (roles.contains("ROLE_STUDENT") && !roles.contains("ROLE_MEMBER")) {
            return "/siga";
        }
        return "/dashboard";
    }
}
