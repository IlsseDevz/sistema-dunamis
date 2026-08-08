package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.AdminMemberUpdateRequest;
import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.exception.DuplicateContactException;
import com.dunamis.sistema.exception.DuplicateEmailException;
import com.dunamis.sistema.exception.OperationNotAllowedException;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.UserRepository;
import com.dunamis.sistema.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminMemberService {

    private static final int PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final ChurchFunctionRepository churchFunctionRepository;
    private final InscricaoRepository inscricaoRepository;
    private final SecurityUtils securityUtils;

    public AdminMemberService(
            UserRepository userRepository,
            ChurchFunctionRepository churchFunctionRepository,
            InscricaoRepository inscricaoRepository,
            SecurityUtils securityUtils
    ) {
        this.userRepository = userRepository;
        this.churchFunctionRepository = churchFunctionRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public Page<User> searchMembers(
            String search,
            ChurchSituation situation,
            String bairro,
            Boolean baptized,
            Long functionId,
            AccountStatus accountStatus,
            int page
    ) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("fullName").ascending());
        return userRepository.searchMembers(
                emptyToNull(search),
                situation,
                emptyToNull(bairro),
                baptized,
                functionId,
                accountStatus,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public User getMemberById(Long id) {
        return userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado."));
    }

    @Transactional(readOnly = true)
    public boolean isStudent(Long userId) {
        User user = getMemberById(userId);
        return inscricaoRepository.findFirstByUserAndStatusOrderByEnrollmentDateDesc(
                user,
                com.dunamis.sistema.entity.enums.EnrollmentStatus.ACTIVE
        ).isPresent();
    }

    @Transactional(readOnly = true)
    public AdminMemberUpdateRequest toUpdateRequest(User user) {
        AdminMemberUpdateRequest request = new AdminMemberUpdateRequest();
        request.setFullName(user.getFullName());
        request.setContacto(user.getContacto());
        request.setEmail(user.getEmail());
        request.setBairro(user.getBairro());
        request.setChurchSituation(user.getChurchSituation());
        request.setBaptized(user.isBaptized());
        request.setChurchFunctionId(user.getChurchFunction().getId());
        request.setAccountStatus(user.getAccountStatus());
        return request;
    }

    @Transactional
    public User updateMember(Long id, AdminMemberUpdateRequest request) {
        User user = getMemberById(id);

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
        user.setAccountStatus(request.getAccountStatus());

        return userRepository.save(user);
    }

    @Transactional
    public void deactivateMember(Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new OperationNotAllowedException("Não pode desactivar a sua própria conta.");
        }

        User user = getMemberById(id);
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void activateMember(Long id) {
        User user = getMemberById(id);
        user.setAccountStatus(AccountStatus.ACTIVE);
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

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
