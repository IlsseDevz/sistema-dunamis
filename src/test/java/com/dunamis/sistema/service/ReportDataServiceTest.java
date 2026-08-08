package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.BibleSchoolReportData;
import com.dunamis.sistema.dto.ChurchReportData;
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
import java.time.YearMonth;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportDataServiceTest {

    @Autowired
    private ReportDataService reportDataService;

    @Autowired
    private PdfReportService pdfReportService;

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

    private YearMonth period;

    @BeforeEach
    void setUp() {
        period = YearMonth.now();
        Role memberRole = roleRepository.findByName(RoleName.ROLE_MEMBER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_MEMBER)));
        ChurchFunction fn = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        User user = new User();
        user.setFullName("Aluno Relatório");
        user.setContacto("923555001");
        user.setPassword(passwordEncoder.encode("segredo123"));
        user.setBairro("Centro");
        user.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        user.setChurchFunction(fn);
        user.setBaptized(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRoles(Set.of(memberRole));
        userRepository.save(user);

        Turma turma = turmaRepository.findFirstByStatusOrderByCreatedAtDesc(TurmaStatus.ACTIVE)
                .orElseGet(() -> {
                    Turma t = new Turma();
                    t.setName("Turma Relatório");
                    t.setYear(period.getYear());
                    return turmaRepository.save(t);
                });

        Inscricao inscricao = new Inscricao();
        inscricao.setUser(user);
        inscricao.setTurma(turma);
        inscricao.setStatus(EnrollmentStatus.ACTIVE);
        inscricao = inscricaoRepository.save(inscricao);

        Disciplina disciplina = new Disciplina();
        disciplina.setTurma(turma);
        disciplina.setName("Doutrina");
        disciplina = disciplinaRepository.save(disciplina);

        var gradeRequest = new com.dunamis.sistema.dto.request.GradeRequest();
        gradeRequest.setInscricaoId(inscricao.getId());
        gradeRequest.setDisciplinaId(disciplina.getId());
        gradeRequest.setGradeType(GradeType.NOTA_UNICA);
        gradeRequest.setValue(new BigDecimal("18.00"));
        adminGradeService.create(gradeRequest);

        var batch = adminAttendanceService.buildBatchRequest(period.atDay(12));
        batch.getItems().get(0).setPresent(true);
        adminAttendanceService.saveBatch(batch);
    }

    @Test
    void shouldBuildBibleSchoolReportForPeriod() {
        BibleSchoolReportData data = reportDataService.buildBibleSchoolReport(
                period.getMonthValue(),
                period.getYear()
        );

        assertThat(data.getTotalStudents()).isGreaterThanOrEqualTo(1);
        assertThat(data.getStudentsPresent()).isGreaterThanOrEqualTo(1);
        assertThat(data.getAverageGrade()).isEqualByComparingTo("18.00");
        assertThat(data.getChurchName()).isNotBlank();
        assertThat(data.getBibleSchoolName()).isNotBlank();
    }

    @Test
    void shouldBuildChurchReportForPeriod() {
        ChurchReportData data = reportDataService.buildChurchReport(
                period.getMonthValue(),
                period.getYear()
        );

        assertThat(data.getTotalRegistered()).isGreaterThanOrEqualTo(1);
        assertThat(data.getActiveMembers()).isGreaterThanOrEqualTo(1);
        assertThat(data.getFunctionDistribution()).isNotEmpty();
    }

    @Test
    void shouldGeneratePdfBytes() {
        BibleSchoolReportData bibleSchoolData = reportDataService.buildBibleSchoolReport(
                period.getMonthValue(),
                period.getYear()
        );
        ChurchReportData churchData = reportDataService.buildChurchReport(
                period.getMonthValue(),
                period.getYear()
        );

        assertThat(pdfReportService.generateBibleSchoolReport(bibleSchoolData)).startsWith("%PDF".getBytes());
        assertThat(pdfReportService.generateChurchReport(churchData)).startsWith("%PDF".getBytes());
    }
}
