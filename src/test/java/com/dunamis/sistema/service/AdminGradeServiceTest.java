package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.GradeRequest;
import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.entity.enums.GradeType;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.exception.DuplicateGradeException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.DisciplinaRepository;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.TurmaRepository;
import com.dunamis.sistema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminGradeServiceTest {

    @Autowired
    private AdminGradeService adminGradeService;

    @Autowired
    private AdminAttendanceService adminAttendanceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChurchFunctionRepository churchFunctionRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Inscricao inscricao;
    private Disciplina disciplina;

    @BeforeEach
    void setUp() {
        Role memberRole = roleRepository.findByName(RoleName.ROLE_MEMBER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_MEMBER)));
        ChurchFunction fn = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        User user = new User();
        user.setFullName("Aluno Notas");
        user.setContacto("923444001");
        user.setPassword(passwordEncoder.encode("segredo123"));
        user.setBairro("Centro");
        user.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        user.setChurchFunction(fn);
        user.setBaptized(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRoles(Set.of(memberRole));
        user = userRepository.save(user);

        Turma turma = turmaRepository.findFirstByStatusOrderByCreatedAtDesc(TurmaStatus.ACTIVE)
                .orElseGet(() -> {
                    Turma t = new Turma();
                    t.setName("Turma Notas");
                    t.setYear(2026);
                    return turmaRepository.save(t);
                });

        inscricao = new Inscricao();
        inscricao.setUser(user);
        inscricao.setTurma(turma);
        inscricao.setStatus(EnrollmentStatus.ACTIVE);
        inscricao = inscricaoRepository.save(inscricao);

        disciplina = new Disciplina();
        disciplina.setTurma(turma);
        disciplina.setName("Teologia");
        disciplina = disciplinaRepository.save(disciplina);
    }

    @Test
    void shouldCreateAndRejectDuplicateGrade() {
        GradeRequest request = new GradeRequest();
        request.setInscricaoId(inscricao.getId());
        request.setDisciplinaId(disciplina.getId());
        request.setGradeType(GradeType.NOTA_UNICA);
        request.setValue(new BigDecimal("16.00"));

        adminGradeService.create(request);

        assertThat(adminGradeService.listTurmaGrades()).hasSize(1);

        assertThatThrownBy(() -> adminGradeService.create(request))
                .isInstanceOf(DuplicateGradeException.class);
    }

    @Test
    void shouldSaveAttendanceBatch() {
        var batch = adminAttendanceService.buildBatchRequest(LocalDate.now());
        batch.getItems().get(0).setPresent(false);

        adminAttendanceService.saveBatch(batch);

        assertThat(adminAttendanceService.listTurmaAttendances(LocalDate.now())).hasSize(1);
    }
}
