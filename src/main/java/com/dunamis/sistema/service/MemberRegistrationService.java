package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.MemberRegistrationRequest;
import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.exception.DuplicateContactException;
import com.dunamis.sistema.exception.DuplicateEmailException;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class MemberRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ChurchFunctionRepository churchFunctionRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberRegistrationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ChurchFunctionRepository churchFunctionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.churchFunctionRepository = churchFunctionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(MemberRegistrationRequest request) {
        String contacto = normalizeContacto(request.getContacto());
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByContacto(contacto)) {
            throw new DuplicateContactException();
        }

        if (email != null && userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        ChurchFunction churchFunction = churchFunctionRepository.findById(request.getChurchFunctionId())
                .filter(ChurchFunction::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Função na igreja não encontrada."));

        Role memberRole = roleRepository.findByName(RoleName.ROLE_MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de membro não configurado."));

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setContacto(contacto);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBairro(request.getBairro().trim());
        user.setChurchSituation(request.getChurchSituation());
        user.setChurchFunction(churchFunction);
        user.setBaptized(Boolean.TRUE.equals(request.getBaptized()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRoles(Set.of(memberRole));

        return userRepository.save(user);
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
