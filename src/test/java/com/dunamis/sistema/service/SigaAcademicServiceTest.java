package com.dunamis.sistema.service;

import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Nota;
import com.dunamis.sistema.entity.Presenca;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.entity.enums.GradeType;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.DisciplinaRepository;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.NotaRepository;
import com.dunamis.sistema.repository.PresencaRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SigaAcademicServiceTest {

    @Autowired
    private SigaAcademicService sigaAcademicService;

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
    private NotaRepository notaRepository;

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityUtils securityUtils;

    private Inscricao inscricao;

    @BeforeEach
    void setUp() {
        Role memberRole = roleRepository.findByName(RoleName.ROLE_MEMBER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_MEMBER)));
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_STUDENT)));

        ChurchFunction fn = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        User user = new User();
        user.setFullName("Aluno SIGA");
        user.setContacto("923555001");
        user.setPassword(passwordEncoder.encode("segredo123"));
        user.setBairro("Centro");
        user.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        user.setChurchFunction(fn);
        user.setBaptized(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRoles(Set.of(memberRole, studentRole));
        user = userRepository.save(user);

        Turma turma = turmaRepository.findFirstByStatusOrderByCreatedAtDesc(TurmaStatus.ACTIVE)
                .orElseGet(() -> {
                    Turma t = new Turma();
                    t.setName("Turma SIGA");
                    t.setYear(2026);
                    return turmaRepository.save(t);
                });

        inscricao = new Inscricao();
        inscricao.setUser(user);
        inscricao.setTurma(turma);
        inscricao.setStatus(EnrollmentStatus.ACTIVE);
        inscricao = inscricaoRepository.save(inscricao);

        Disciplina disciplina = new Disciplina();
        disciplina.setTurma(turma);
        disciplina.setName("Doutrina");
        disciplina = disciplinaRepository.save(disciplina);

        Nota nota = new Nota();
        nota.setInscricao(inscricao);
        nota.setDisciplina(disciplina);
        nota.setGradeType(GradeType.NOTA_UNICA);
        nota.setValue(new BigDecimal("15.00"));
        notaRepository.save(nota);

        Presenca presenca = new Presenca();
        presenca.setInscricao(inscricao);
        presenca.setAttendanceDate(LocalDate.now());
        presenca.setPresent(true);
        presencaRepository.save(presenca);

        securityUtils.refreshAuthentication(user.getId());
    }

    @Test
    void shouldBuildSummaryForCurrentStudentOnly() {
        var summary = sigaAcademicService.buildSummary();

        assertThat(summary.getTurmaName()).isEqualTo("Turma SIGA");
        assertThat(summary.getOverallAverage()).isEqualByComparingTo("15.00");
        assertThat(summary.getAttendancePercentage()).isEqualTo(100.0);
        assertThat(summary.getDisciplinaGrades()).hasSize(1);
    }
}
