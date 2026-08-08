package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.DisciplinaRequest;
import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.exception.DisciplinaInUseException;
import com.dunamis.sistema.exception.DuplicateDisciplinaException;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.DisciplinaRepository;
import com.dunamis.sistema.repository.NotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final NotaRepository notaRepository;
    private final AdminBibleSchoolService adminBibleSchoolService;

    public DisciplinaService(
            DisciplinaRepository disciplinaRepository,
            NotaRepository notaRepository,
            AdminBibleSchoolService adminBibleSchoolService
    ) {
        this.disciplinaRepository = disciplinaRepository;
        this.notaRepository = notaRepository;
        this.adminBibleSchoolService = adminBibleSchoolService;
    }

    @Transactional(readOnly = true)
    public List<Disciplina> listCurrentTurmaDisciplinas() {
        Turma turma = adminBibleSchoolService.getActiveTurma();
        return disciplinaRepository.findByTurmaOrderByNameAsc(turma);
    }

    @Transactional(readOnly = true)
    public Disciplina getById(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
    }

    @Transactional(readOnly = true)
    public DisciplinaRequest toRequest(Disciplina disciplina) {
        DisciplinaRequest request = new DisciplinaRequest();
        request.setName(disciplina.getName());
        request.setDescription(disciplina.getDescription());
        request.setWorkloadHours(disciplina.getWorkloadHours());
        return request;
    }

    @Transactional
    public Disciplina create(DisciplinaRequest request) {
        Turma turma = adminBibleSchoolService.getActiveTurma();
        String name = request.getName().trim();

        if (disciplinaRepository.existsByTurmaIdAndNameIgnoreCase(turma.getId(), name)) {
            throw new DuplicateDisciplinaException();
        }

        Disciplina disciplina = new Disciplina();
        disciplina.setTurma(turma);
        disciplina.setName(name);
        disciplina.setDescription(trimToNull(request.getDescription()));
        disciplina.setWorkloadHours(request.getWorkloadHours());
        return disciplinaRepository.save(disciplina);
    }

    @Transactional
    public Disciplina update(Long id, DisciplinaRequest request) {
        Disciplina disciplina = getById(id);
        String name = request.getName().trim();

        if (disciplinaRepository.existsByTurmaIdAndNameIgnoreCaseAndIdNot(
                disciplina.getTurma().getId(), name, id)) {
            throw new DuplicateDisciplinaException();
        }

        disciplina.setName(name);
        disciplina.setDescription(trimToNull(request.getDescription()));
        disciplina.setWorkloadHours(request.getWorkloadHours());
        return disciplinaRepository.save(disciplina);
    }

    @Transactional
    public void delete(Long id) {
        Disciplina disciplina = getById(id);
        if (!notaRepository.findByDisciplinaId(id).isEmpty()) {
            throw new DisciplinaInUseException();
        }
        disciplinaRepository.delete(disciplina);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
