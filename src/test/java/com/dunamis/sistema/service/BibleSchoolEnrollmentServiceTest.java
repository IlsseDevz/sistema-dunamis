package com.dunamis.sistema.service;

import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.exception.AlreadyEnrolledException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.TurmaRepository;
import com.dunamis.sistema.repository.UserRepository;
import com.dunamis.sistema.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BibleSchoolEnrollmentServiceTest {

    @Autowired
    private BibleSchoolEnrollmentService bibleSchoolEnrollmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChurchFunctionRepository churchFunctionRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityUtils securityUtils;

    private User user;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(RoleName.ROLE_MEMBER).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_MEMBER));
        }
        if (roleRepository.findByName(RoleName.ROLE_STUDENT).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_STUDENT));
        }

        if (turmaRepository.findByStatus(TurmaStatus.ACTIVE).isEmpty()) {
            Turma turma = new Turma();
            turma.setName("Turma 2026");
            turma.setYear(2026);
            turmaRepository.save(turma);
        }

        user = saveMember("923777001");
        securityUtils.refreshAuthentication(user.getId());
    }

    @Test
    void shouldEnrollMemberAndAddStudentRole() {
        bibleSchoolEnrollmentService.enrollCurrentUser();

        User updated = userRepository.findByIdWithDetails(user.getId()).orElseThrow();
        assertThat(updated.getRoles()).extracting(r -> r.getName().name()).contains("ROLE_STUDENT");
        assertThat(inscricaoRepository.findFirstByUserAndStatusOrderByEnrollmentDateDesc(updated, EnrollmentStatus.ACTIVE))
                .isPresent();
        assertThat(bibleSchoolEnrollmentService.isCurrentUserEnrolled()).isTrue();
    }

    @Test
    void shouldRejectDuplicateEnrollment() {
        bibleSchoolEnrollmentService.enrollCurrentUser();

        assertThatThrownBy(() -> bibleSchoolEnrollmentService.enrollCurrentUser())
                .isInstanceOf(AlreadyEnrolledException.class);
    }

    private User saveMember(String contacto) {
        Role memberRole = roleRepository.findByName(RoleName.ROLE_MEMBER).orElseThrow();
        ChurchFunction fn = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        User u = new User();
        u.setFullName("Membro Teste");
        u.setContacto(contacto);
        u.setPassword(passwordEncoder.encode("segredo123"));
        u.setBairro("Centro");
        u.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        u.setChurchFunction(fn);
        u.setBaptized(true);
        u.setAccountStatus(AccountStatus.ACTIVE);
        u.setRoles(Set.of(memberRole));
        return userRepository.save(u);
    }
}
