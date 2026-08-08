package com.dunamis.sistema.security;

import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUserByLoginIdentifier(username)
                .orElseThrow(() -> new UsernameNotFoundException("Contacto ou password inválidos."));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new DisabledException("A sua conta está inativa. Contacte a administração.");
        }

        return UserDetailsImpl.from(user);
    }

    /**
     * Login principal por contacto. Se o identificador contiver '@', tenta também por email
     * (preparado para login futuro por email).
     */
    private java.util.Optional<User> findUserByLoginIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return java.util.Optional.empty();
        }

        String trimmed = identifier.trim();

        java.util.Optional<User> byContacto = userRepository.findByContactoWithRoles(normalizeContacto(trimmed));
        if (byContacto.isPresent()) {
            return byContacto;
        }

        if (trimmed.contains("@")) {
            return userRepository.findByEmailWithRoles(trimmed.toLowerCase());
        }

        return java.util.Optional.empty();
    }

    private String normalizeContacto(String contacto) {
        return contacto.replaceAll("\\s+", "");
    }
}
