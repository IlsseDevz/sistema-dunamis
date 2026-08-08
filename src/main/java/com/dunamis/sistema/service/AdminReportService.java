package com.dunamis.sistema.service;

import com.dunamis.sistema.config.AppProperties;
import com.dunamis.sistema.dto.BibleSchoolReportData;
import com.dunamis.sistema.dto.ChurchReportData;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.NotaRepository;
import com.dunamis.sistema.repository.PresencaRepository;
import com.dunamis.sistema.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class AdminReportService {

    private final AppProperties appProperties;
    private final TurmaService turmaService;
    private final ReportPeriodHelper reportPeriodHelper;
    private final InscricaoRepository inscricaoRepository;
    private final PresencaRepository presencaRepository;
    private final NotaRepository notaRepository;
    private final UserRepository userRepository;

    public AdminReportService(
            AppProperties appProperties,
            TurmaService turmaService,
            ReportPeriodHelper reportPeriodHelper,
            InscricaoRepository inscricaoRepository,
            PresencaRepository presencaRepository,
            NotaRepository notaRepository,
            UserRepository userRepository
    ) {
        this.appProperties = appProperties;
        this.turmaService = turmaService;
        this.reportPeriodHelper = reportPeriodHelper;
        this.inscricaoRepository = inscricaoRepository;
        this.presencaRepository = presencaRepository;
        this.notaRepository = notaRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BibleSchoolReportData buildBibleSchoolReport(int month, int year) {
        YearMonth yearMonth = reportPeriodHelper.toYearMonth(month, year);
        LocalDate start = reportPeriodHelper.startDate(yearMonth);
        LocalDate end = reportPeriodHelper.endDate(yearMonth);

        Turma turma = turmaService.getCurrentTurma();
        int totalStudents = (int) inscricaoRepository.countByTurmaAndStatus(turma, EnrollmentStatus.ACTIVE);

        long studentsPresent = presencaRepository.countDistinctStudentsPresentInPeriod(turma.getId(), start, end);
        long studentsAbsent = Math.max(0, totalStudents - studentsPresent);

        long presentRecords = presencaRepository.countPresentRecordsInPeriod(turma.getId(), start, end);
        long absentRecords = presencaRepository.countAbsentRecordsInPeriod(turma.getId(), start, end);
        long totalRecords = presencaRepository.countTotalRecordsInPeriod(turma.getId(), start, end);

        double attendancePercentage = totalRecords == 0
                ? 0.0
                : (presentRecords * 100.0) / totalRecords;

        BigDecimal averageGrade = notaRepository.averageByTurmaAndRecordedAtBetween(turma.getId(), start, end);
        if (averageGrade != null) {
            averageGrade = averageGrade.setScale(2, RoundingMode.HALF_UP);
        }

        BibleSchoolReportData data = new BibleSchoolReportData();
        data.setChurchName(appProperties.getChurch().getName());
        data.setBibleSchoolName(appProperties.getBibleSchool().getName());
        data.setPeriodLabel(reportPeriodHelper.formatPeriod(yearMonth));
        data.setTotalStudents(totalStudents);
        data.setStudentsPresent(studentsPresent);
        data.setStudentsAbsent(studentsAbsent);
        data.setAttendancePercentage(attendancePercentage);
        data.setAverageGrade(averageGrade);
        data.setTotalAttendanceRecords(totalRecords);
        data.setPresentRecords(presentRecords);
        data.setAbsentRecords(absentRecords);
        return data;
    }

    @Transactional(readOnly = true)
    public ChurchReportData buildChurchReport(int month, int year) {
        YearMonth yearMonth = reportPeriodHelper.toYearMonth(month, year);
        LocalDateTime periodStart = reportPeriodHelper.startDateTime(yearMonth);
        LocalDateTime periodEnd = reportPeriodHelper.endDateTime(yearMonth);

        ChurchReportData data = new ChurchReportData();
        data.setChurchName(appProperties.getChurch().getName());
        data.setPeriodLabel(reportPeriodHelper.formatPeriod(yearMonth));
        data.setTotalRegistered(userRepository.countByRegisteredAtLessThanEqual(periodEnd));
        data.setActiveMembers(userRepository.countByChurchSituationAndRegisteredAtLessThanEqual(
                ChurchSituation.MEMBRO_ATIVO, periodEnd));
        data.setNewConverts(userRepository.countByChurchSituationAndRegisteredAtLessThanEqual(
                ChurchSituation.NOVO_CONVERTIDO, periodEnd));
        data.setVisitors(userRepository.countByChurchSituationAndRegisteredAtLessThanEqual(
                ChurchSituation.VISITANTE, periodEnd));
        data.setBaptized(userRepository.countByBaptizedTrueAndRegisteredAtLessThanEqual(periodEnd));
        data.setNotBaptized(userRepository.countByBaptizedFalseAndRegisteredAtLessThanEqual(periodEnd));
        data.setNewRegistrationsInPeriod(userRepository.countByRegisteredAtBetween(periodStart, periodEnd));

        List<Object[]> functionCounts = userRepository.countUsersByFunctionUntil(periodEnd);
        for (Object[] row : functionCounts) {
            String label = (String) row[0];
            long count = (Long) row[1];
            data.getFunctionDistribution().add(new ChurchReportData.FunctionCount(label, count));
        }

        return data;
    }
}
