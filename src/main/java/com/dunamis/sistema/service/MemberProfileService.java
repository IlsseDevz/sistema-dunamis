package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.ChangePasswordRequest;
import com.dunamis.sistema.dto.request.ProfileUpdateRequest;
import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.exception.DuplicateContactException;
import com.dunamis.sistema.exception.DuplicateEmailException;
import com.dunamis.sistema.exception.InvalidPasswordException;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.UserRepository;
import com.dunamis.sistema.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MemberProfileService {

    private final UserRepository userRepository;
    private final ChurchFunctionRepository churchFunctionRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    public MemberProfileService(
            UserRepository userRepository,
            ChurchFunctionRepository churchFunctionRepository,
            CurrentUserService currentUserService,
            PasswordEncoder passwordEncoder,
            SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.churchFunctionRepository = churchFunctionRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public ProfileUpdateRequest toProfileUpdateRequest(User user) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName(user.getFullName());
        request.setContacto(user.getContacto());
        request.setEmail(user.getEmail());
        request.setBairro(user.getBairro());
        request.setChurchSituation(user.getChurchSituation());
        request.setBaptized(user.isBaptized());
        request.setChurchFunctionId(user.getChurchFunction().getId());
        return request;
    }

    @Transactional
    public User updateProfile(ProfileUpdateRequest request) {
        User user = currentUserService.getCurrentUserEntity();

        String contacto = normalizeContacto(request.getContacto());
        String email = normalizeEmail(request.getEmail());

        if (!user.getContacto().equals(contacto) && userRepository.existsByContacto(contacto)) {
            throw new DuplicateContactException();
        }

        if (email != null && !email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        ChurchFunction churchFunction = churchFunctionRepository.findById(request.getChurchFunctionId())
                .filter(ChurchFunction::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Função na igreja não encontrada."));

        user.setFullName(request.getFullName().trim());
        user.setContacto(contacto);
        user.setEmail(email);
        user.setBairro(request.getBairro().trim());
        user.setChurchSituation(request.getChurchSituation());
        user.setBaptized(Boolean.TRUE.equals(request.getBaptized()));
        user.setChurchFunction(churchFunction);

        User saved = userRepository.save(user);
        securityUtils.refreshAuthentication(saved.getId());
        return saved;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private String normalizeContacto(String contacto) {
        if (!StringUtils.hasText(contacto)) {
            return contacto;
        }
        return contacto.trim().replaceAll("\\s+", "");
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
