package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.DisciplinaRequest;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.exception.DuplicateDisciplinaException;
import com.dunamis.sistema.repository.DisciplinaRepository;
import com.dunamis.sistema.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DisciplinaServiceTest {

    @Autowired
    private DisciplinaService disciplinaService;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @BeforeEach
    void setUp() {
        if (turmaRepository.findByStatus(TurmaStatus.ACTIVE).isEmpty()) {
            Turma turma = new Turma();
            turma.setName("Turma Teste");
            turma.setYear(2026);
            turmaRepository.save(turma);
        }
    }

    @Test
    void shouldCreateDisciplina() {
        DisciplinaRequest request = new DisciplinaRequest();
        request.setName("Doutrina");
        request.setWorkloadHours(20);

        disciplinaService.create(request);

        assertThat(disciplinaRepository.findAll()).anyMatch(d -> "Doutrina".equals(d.getName()));
    }

    @Test
    void shouldRejectDuplicateDisciplinaName() {
        DisciplinaRequest request = new DisciplinaRequest();
        request.setName("Teologia");
        disciplinaService.create(request);

        assertThatThrownBy(() -> disciplinaService.create(request))
                .isInstanceOf(DuplicateDisciplinaException.class);
    }
}
