package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.TurmaUpdateRequest;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final AdminBibleSchoolService adminBibleSchoolService;

    public TurmaService(TurmaRepository turmaRepository, AdminBibleSchoolService adminBibleSchoolService) {
        this.turmaRepository = turmaRepository;
        this.adminBibleSchoolService = adminBibleSchoolService;
    }

    @Transactional(readOnly = true)
    public Turma getCurrentTurma() {
        return adminBibleSchoolService.getActiveTurma();
    }

    @Transactional(readOnly = true)
    public TurmaUpdateRequest toUpdateRequest(Turma turma) {
        TurmaUpdateRequest request = new TurmaUpdateRequest();
        request.setName(turma.getName());
        request.setDescription(turma.getDescription());
        request.setYear(turma.getYear());
        request.setStatus(turma.getStatus());
        return request;
    }

    @Transactional
    public Turma updateTurma(Long id, TurmaUpdateRequest request) {
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new com.dunamis.sistema.exception.ResourceNotFoundException("Turma não encontrada."));

        turma.setName(request.getName().trim());
        turma.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        turma.setYear(request.getYear());
        turma.setStatus(request.getStatus());

        return turmaRepository.save(turma);
    }
}
