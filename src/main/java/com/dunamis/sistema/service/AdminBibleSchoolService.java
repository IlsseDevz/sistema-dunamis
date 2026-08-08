package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.BibleSchoolOverview;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.DisciplinaRepository;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBibleSchoolService {

    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final InscricaoRepository inscricaoRepository;

    public AdminBibleSchoolService(
            TurmaRepository turmaRepository,
            DisciplinaRepository disciplinaRepository,
            InscricaoRepository inscricaoRepository
    ) {
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional(readOnly = true)
    public Turma getActiveTurma() {
        return turmaRepository.findFirstByStatusOrderByCreatedAtDesc(TurmaStatus.ACTIVE)
                .orElseGet(this::getLatestTurma);
    }

    @Transactional(readOnly = true)
    public BibleSchoolOverview getOverview() {
        Turma turma = getActiveTurma();

        BibleSchoolOverview overview = new BibleSchoolOverview();
        BibleSchoolOverview.TurmaSummary summary = new BibleSchoolOverview.TurmaSummary();
        summary.setId(turma.getId());
        summary.setName(turma.getName());
        summary.setYear(turma.getYear());
        summary.setStatus(turma.getStatus().getLabel());
        overview.setTurma(summary);
        overview.setTotalDisciplinas(disciplinaRepository.countByTurmaId(turma.getId()));
        overview.setTotalAlunos(inscricaoRepository.countByTurmaAndStatus(turma, EnrollmentStatus.ACTIVE));
        return overview;
    }

    private Turma getLatestTurma() {
        return turmaRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma turma configurada."));
    }
}
