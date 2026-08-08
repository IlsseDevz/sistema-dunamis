package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.SigaAcademicSummary;
import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Nota;
import com.dunamis.sistema.entity.Presenca;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.exception.StudentNotEnrolledException;
import com.dunamis.sistema.repository.DisciplinaRepository;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.NotaRepository;
import com.dunamis.sistema.repository.PresencaRepository;
import com.dunamis.sistema.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SigaAcademicService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SecurityUtils securityUtils;
    private final InscricaoRepository inscricaoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final NotaRepository notaRepository;
    private final PresencaRepository presencaRepository;

    public SigaAcademicService(
            SecurityUtils securityUtils,
            InscricaoRepository inscricaoRepository,
            DisciplinaRepository disciplinaRepository,
            NotaRepository notaRepository,
            PresencaRepository presencaRepository
    ) {
        this.securityUtils = securityUtils;
        this.inscricaoRepository = inscricaoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.notaRepository = notaRepository;
        this.presencaRepository = presencaRepository;
    }

    @Transactional(readOnly = true)
    public Inscricao getCurrentEnrollment() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new StudentNotEnrolledException();
        }

        return inscricaoRepository.findActiveEnrollmentsByUserIdWithTurma(userId, EnrollmentStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(StudentNotEnrolledException::new);
    }

    @Transactional(readOnly = true)
    public List<Disciplina> getCurrentDisciplinas() {
        Inscricao inscricao = getCurrentEnrollment();
        return disciplinaRepository.findByTurmaIdOrderByNameAsc(inscricao.getTurma().getId());
    }

    @Transactional(readOnly = true)
    public List<Nota> getCurrentGrades() {
        Inscricao inscricao = getCurrentEnrollment();
        return notaRepository.findByInscricaoIdWithDisciplina(inscricao.getId());
    }

    @Transactional(readOnly = true)
    public List<Presenca> getCurrentAttendances() {
        Inscricao inscricao = getCurrentEnrollment();
        return presencaRepository.findByInscricaoOrderByAttendanceDateDesc(inscricao);
    }

    @Transactional(readOnly = true)
    public SigaAcademicSummary buildSummary() {
        Inscricao inscricao = getCurrentEnrollment();
        List<Disciplina> disciplinas = getCurrentDisciplinas();
        List<Nota> notas = getCurrentGrades();
        List<Presenca> presencas = getCurrentAttendances();

        SigaAcademicSummary summary = new SigaAcademicSummary();
        summary.setTurmaName(inscricao.getTurma().getName());
        summary.setTurmaYear(inscricao.getTurma().getYear());
        summary.setTotalDisciplinas(disciplinas.size());
        summary.setDisciplinaGrades(buildDisciplinaGrades(notas));
        summary.setOverallAverage(calculateOverallAverage(summary.getDisciplinaGrades()));

        long present = presencas.stream().filter(Presenca::isPresent).count();
        summary.setPresentCount(present);
        summary.setAbsentCount(presencas.size() - present);
        summary.setTotalAttendanceRecords(presencas.size());
        summary.setAttendancePercentage(calculateAttendancePercentage(present, presencas.size()));

        return summary;
    }

    private List<SigaAcademicSummary.DisciplinaGradeSummary> buildDisciplinaGrades(List<Nota> notas) {
        Map<Long, SigaAcademicSummary.DisciplinaGradeSummary> grouped = new LinkedHashMap<>();

        for (Nota nota : notas) {
            Disciplina disciplina = nota.getDisciplina();
            SigaAcademicSummary.DisciplinaGradeSummary item = grouped.computeIfAbsent(
                    disciplina.getId(),
                    id -> {
                        SigaAcademicSummary.DisciplinaGradeSummary summary =
                                new SigaAcademicSummary.DisciplinaGradeSummary();
                        summary.setDisciplinaId(disciplina.getId());
                        summary.setDisciplinaName(disciplina.getName());
                        return summary;
                    }
            );

            SigaAcademicSummary.GradeItem gradeItem = new SigaAcademicSummary.GradeItem();
            gradeItem.setType(nota.getGradeType().getLabel());
            gradeItem.setValue(nota.getValue());
            gradeItem.setRecordedAt(nota.getRecordedAt().format(DATE_FORMAT));
            item.getGrades().add(gradeItem);
        }

        grouped.values().forEach(item -> item.setAverage(calculateDisciplinaAverage(item.getGrades())));
        return new ArrayList<>(grouped.values());
    }

    private BigDecimal calculateDisciplinaAverage(List<SigaAcademicSummary.GradeItem> grades) {
        if (grades.isEmpty()) {
            return null;
        }
        BigDecimal sum = grades.stream()
                .map(SigaAcademicSummary.GradeItem::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOverallAverage(List<SigaAcademicSummary.DisciplinaGradeSummary> disciplinaGrades) {
        List<BigDecimal> averages = disciplinaGrades.stream()
                .map(SigaAcademicSummary.DisciplinaGradeSummary::getAverage)
                .filter(a -> a != null)
                .toList();

        if (averages.isEmpty()) {
            return null;
        }

        BigDecimal sum = averages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(averages.size()), 2, RoundingMode.HALF_UP);
    }

    private double calculateAttendancePercentage(long present, long total) {
        if (total == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(present * 100.0 / total)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
