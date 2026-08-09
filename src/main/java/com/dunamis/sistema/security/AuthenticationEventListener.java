package com.dunamis.sistema.security;

import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationEventListener {

    private final UserRepository userRepository;
    private final int MAX_ATTEMPTS = 5;

    public AuthenticationEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof String) {
            String username = (String) principal;
            Optional<User> userOpt = userRepository.findByContacto(username);
            userOpt.ifPresent(user -> {
                Integer attempts = user.getFailedLoginAttempts();
                if (attempts == null) attempts = 0;
                user.setFailedLoginAttempts(attempts + 1);
                if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
                    user.setAccountStatus(com.dunamis.sistema.entity.enums.AccountStatus.INACTIVE);
                }
                userRepository.save(user);
            });
        }
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            Optional<User> userOpt = userRepository.findByContacto(username);
            userOpt.ifPresent(user -> {
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            });
        }
    }
}
