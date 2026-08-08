package com.dunamis.sistema.service;

import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.exception.AlreadyEnrolledException;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.TurmaRepository;
import com.dunamis.sistema.repository.UserRepository;
import com.dunamis.sistema.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BibleSchoolEnrollmentService {

    private final CurrentUserService currentUserService;
    private final InscricaoRepository inscricaoRepository;
    private final TurmaRepository turmaRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public BibleSchoolEnrollmentService(
            CurrentUserService currentUserService,
            InscricaoRepository inscricaoRepository,
            TurmaRepository turmaRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            SecurityUtils securityUtils
    ) {
        this.currentUserService = currentUserService;
        this.inscricaoRepository = inscricaoRepository;
        this.turmaRepository = turmaRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserEnrolled() {
        User user = currentUserService.getCurrentUserEntity();
        return inscricaoRepository.findFirstByUserAndStatusOrderByEnrollmentDateDesc(user, EnrollmentStatus.ACTIVE)
                .isPresent();
    }

    @Transactional
    public Inscricao enrollCurrentUser() {
        User user = currentUserService.getCurrentUserEntity();

        if (inscricaoRepository.findFirstByUserAndStatusOrderByEnrollmentDateDesc(user, EnrollmentStatus.ACTIVE).isPresent()) {
            throw new AlreadyEnrolledException();
        }

        Turma turma = turmaRepository.findFirstByStatusOrderByCreatedAtDesc(TurmaStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma turma activa disponível."));

        Inscricao inscricao = new Inscricao();
        inscricao.setUser(user);
        inscricao.setTurma(turma);
        inscricao.setStatus(EnrollmentStatus.ACTIVE);
        Inscricao saved = inscricaoRepository.save(inscricao);

        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de aluno não configurado."));

        if (user.getRoles().stream().noneMatch(r -> r.getName() == RoleName.ROLE_STUDENT)) {
            user.addRole(studentRole);
            userRepository.save(user);
            securityUtils.refreshAuthentication(user.getId());
        }

        return saved;
    }
}
