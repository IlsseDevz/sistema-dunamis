package com.dunamis.sistema.service;

import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAlunoService {

    private static final int PAGE_SIZE = 10;

    private final AdminBibleSchoolService adminBibleSchoolService;
    private final com.dunamis.sistema.repository.InscricaoRepository inscricaoRepository;

    public AdminAlunoService(
            AdminBibleSchoolService adminBibleSchoolService,
            com.dunamis.sistema.repository.InscricaoRepository inscricaoRepository
    ) {
        this.adminBibleSchoolService = adminBibleSchoolService;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Inscricao> listActiveStudents(String search, int page) {
        Turma turma = adminBibleSchoolService.getActiveTurma();
        return inscricaoRepository.searchActiveByTurma(
                turma.getId(),
                EnrollmentStatus.ACTIVE,
                emptyToNull(search),
                org.springframework.data.domain.PageRequest.of(
                        page,
                        PAGE_SIZE,
                        org.springframework.data.domain.Sort.by("user.fullName").ascending()
                )
        );
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
