package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.GradeRequest;
import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Nota;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.exception.DuplicateGradeException;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.DisciplinaRepository;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.NotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminGradeService {

    private final AdminBibleSchoolService adminBibleSchoolService;
    private final NotaRepository notaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public AdminGradeService(
            AdminBibleSchoolService adminBibleSchoolService,
            NotaRepository notaRepository,
            InscricaoRepository inscricaoRepository,
            DisciplinaRepository disciplinaRepository
    ) {
        this.adminBibleSchoolService = adminBibleSchoolService;
        this.notaRepository = notaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    @Transactional(readOnly = true)
    public List<Nota> listTurmaGrades() {
        Long turmaId = adminBibleSchoolService.getActiveTurma().getId();
        return notaRepository.findByTurmaIdWithDetails(turmaId);
    }

    @Transactional(readOnly = true)
    public List<Inscricao> listActiveEnrollments() {
        return inscricaoRepository.findByTurmaAndStatusWithUser(
                adminBibleSchoolService.getActiveTurma(),
                EnrollmentStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public Nota getById(Long id) {
        return notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota não encontrada."));
    }

    @Transactional(readOnly = true)
    public GradeRequest toRequest(Nota nota) {
        GradeRequest request = new GradeRequest();
        request.setInscricaoId(nota.getInscricao().getId());
        request.setDisciplinaId(nota.getDisciplina().getId());
        request.setGradeType(nota.getGradeType());
        request.setValue(nota.getValue());
        request.setObservation(nota.getObservation());
        return request;
    }

    @Transactional
    public Nota create(GradeRequest request) {
        Inscricao inscricao = getActiveEnrollment(request.getInscricaoId());
        Disciplina disciplina = getDisciplinaForTurma(request.getDisciplinaId());

        if (notaRepository.findByInscricaoAndDisciplinaAndGradeType(
                inscricao, disciplina, request.getGradeType()).isPresent()) {
            throw new DuplicateGradeException();
        }

        Nota nota = new Nota();
        nota.setInscricao(inscricao);
        nota.setDisciplina(disciplina);
        nota.setGradeType(request.getGradeType());
        nota.setValue(request.getValue());
        nota.setObservation(trimToNull(request.getObservation()));
        return notaRepository.save(nota);
    }

    @Transactional
    public Nota update(Long id, GradeRequest request) {
        Nota nota = getById(id);
        Inscricao inscricao = getActiveEnrollment(request.getInscricaoId());
        Disciplina disciplina = getDisciplinaForTurma(request.getDisciplinaId());

        notaRepository.findByInscricaoAndDisciplinaAndGradeType(inscricao, disciplina, request.getGradeType())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateGradeException();
                });

        nota.setInscricao(inscricao);
        nota.setDisciplina(disciplina);
        nota.setGradeType(request.getGradeType());
        nota.setValue(request.getValue());
        nota.setObservation(trimToNull(request.getObservation()));
        return notaRepository.save(nota);
    }

    @Transactional
    public void delete(Long id) {
        Nota nota = getById(id);
        notaRepository.delete(nota);
    }

    private Inscricao getActiveEnrollment(Long inscricaoId) {
        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscrição não encontrada."));

        if (inscricao.getStatus() != EnrollmentStatus.ACTIVE
                || !inscricao.getTurma().getId().equals(adminBibleSchoolService.getActiveTurma().getId())) {
            throw new ResourceNotFoundException("Inscrição inválida para a turma activa.");
        }
        return inscricao;
    }

    private Disciplina getDisciplinaForTurma(Long disciplinaId) {
        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));

        if (!disciplina.getTurma().getId().equals(adminBibleSchoolService.getActiveTurma().getId())) {
            throw new ResourceNotFoundException("Disciplina inválida para a turma activa.");
        }
        return disciplina;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
