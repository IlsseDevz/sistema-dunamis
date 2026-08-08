package com.dunamis.sistema.service;

import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.UserRepository;
import com.dunamis.sistema.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public CurrentUserService(UserRepository userRepository, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("Utilizador não autenticado.");
        }

        return userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado."));
    }
}
