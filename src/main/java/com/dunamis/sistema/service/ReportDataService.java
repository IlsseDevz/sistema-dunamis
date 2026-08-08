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
public class ReportDataService {

    private final AppProperties appProperties;
    private final TurmaService turmaService;
    private final InscricaoRepository inscricaoRepository;
    private final PresencaRepository presencaRepository;
    private final NotaRepository notaRepository;
    private final UserRepository userRepository;
    private final ReportPeriodHelper reportPeriodHelper;

    public ReportDataService(
            AppProperties appProperties,
            TurmaService turmaService,
            InscricaoRepository inscricaoRepository,
            PresencaRepository presencaRepository,
            NotaRepository notaRepository,
            UserRepository userRepository,
            ReportPeriodHelper reportPeriodHelper
    ) {
        this.appProperties = appProperties;
        this.turmaService = turmaService;
        this.inscricaoRepository = inscricaoRepository;
        this.presencaRepository = presencaRepository;
        this.notaRepository = notaRepository;
        this.userRepository = userRepository;
        this.reportPeriodHelper = reportPeriodHelper;
    }

    @Transactional(readOnly = true)
    public BibleSchoolReportData buildBibleSchoolReport(int month, int year) {
        YearMonth yearMonth = reportPeriodHelper.toYearMonth(month, year);
        LocalDate start = reportPeriodHelper.startDate(yearMonth);
        LocalDate end = reportPeriodHelper.endDate(yearMonth);
        Turma turma = turmaService.getCurrentTurma();

        int totalStudents = (int) inscricaoRepository.countByTurmaAndStatus(turma, EnrollmentStatus.ACTIVE);
        long studentsPresent = presencaRepository.countDistinctStudentsPresentInPeriod(turma.getId(), start, end);
        long presentRecords = presencaRepository.countPresentRecordsInPeriod(turma.getId(), start, end);
        long totalRecords = presencaRepository.countTotalRecordsInPeriod(turma.getId(), start, end);
        long absentRecords = totalRecords - presentRecords;
        long studentsAbsent = Math.max(0, totalStudents - studentsPresent);

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
        data.setAttendancePercentage(round(attendancePercentage));
        data.setAverageGrade(averageGrade);
        data.setTotalAttendanceRecords(totalRecords);
        data.setPresentRecords(presentRecords);
        data.setAbsentRecords(absentRecords);
        return data;
    }

    @Transactional(readOnly = true)
    public ChurchReportData buildChurchReport(int month, int year) {
        YearMonth yearMonth = reportPeriodHelper.toYearMonth(month, year);
        LocalDateTime start = reportPeriodHelper.startDateTime(yearMonth);
        LocalDateTime end = reportPeriodHelper.endDateTime(yearMonth);

        ChurchReportData data = new ChurchReportData();
        data.setChurchName(appProperties.getChurch().getName());
        data.setPeriodLabel(reportPeriodHelper.formatPeriod(yearMonth));
        data.setTotalRegistered(userRepository.countByRegisteredAtLessThanEqual(end));
        data.setActiveMembers(userRepository.countByChurchSituationAndRegisteredAtLessThanEqual(
                ChurchSituation.MEMBRO_ATIVO, end));
        data.setNewConverts(userRepository.countByChurchSituationAndRegisteredAtLessThanEqual(
                ChurchSituation.NOVO_CONVERTIDO, end));
        data.setVisitors(userRepository.countByChurchSituationAndRegisteredAtLessThanEqual(
                ChurchSituation.VISITANTE, end));
        data.setBaptized(userRepository.countByBaptizedTrueAndRegisteredAtLessThanEqual(end));
        data.setNotBaptized(userRepository.countByBaptizedFalseAndRegisteredAtLessThanEqual(end));
        data.setNewRegistrationsInPeriod(userRepository.countByRegisteredAtBetween(start, end));

        List<Object[]> functionRows = userRepository.countUsersByFunctionUntil(end);
        for (Object[] row : functionRows) {
            data.getFunctionDistribution().add(
                    new ChurchReportData.FunctionCount((String) row[0], (Long) row[1])
            );
        }
        return data;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
