package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.AttendanceBatchRequest;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Presenca;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.exception.ResourceNotFoundException;
import com.dunamis.sistema.repository.InscricaoRepository;
import com.dunamis.sistema.repository.PresencaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminAttendanceService {

    private final AdminBibleSchoolService adminBibleSchoolService;
    private final PresencaRepository presencaRepository;
    private final InscricaoRepository inscricaoRepository;

    public AdminAttendanceService(
            AdminBibleSchoolService adminBibleSchoolService,
            PresencaRepository presencaRepository,
            InscricaoRepository inscricaoRepository
    ) {
        this.adminBibleSchoolService = adminBibleSchoolService;
        this.presencaRepository = presencaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Presenca> listTurmaAttendances(LocalDate date) {
        Long turmaId = adminBibleSchoolService.getActiveTurma().getId();
        if (date != null) {
            return presencaRepository.findByTurmaIdAndDateWithDetails(turmaId, date);
        }
        return presencaRepository.findByTurmaIdWithDetails(turmaId);
    }

    @Transactional(readOnly = true)
    public List<Inscricao> listActiveEnrollments() {
        return inscricaoRepository.findByTurmaAndStatusWithUser(
                adminBibleSchoolService.getActiveTurma(),
                EnrollmentStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public AttendanceBatchRequest buildBatchRequest(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<Inscricao> enrollments = inscricaoRepository.findByTurmaAndStatusWithUser(
                adminBibleSchoolService.getActiveTurma(),
                EnrollmentStatus.ACTIVE
        );

        AttendanceBatchRequest batch = new AttendanceBatchRequest();
        batch.setAttendanceDate(targetDate);

        List<AttendanceBatchRequest.AttendanceItemRequest> items = new ArrayList<>();
        for (Inscricao inscricao : enrollments) {
            AttendanceBatchRequest.AttendanceItemRequest item = new AttendanceBatchRequest.AttendanceItemRequest();
            item.setInscricaoId(inscricao.getId());

            presencaRepository.findByInscricaoAndAttendanceDate(inscricao, targetDate).ifPresentOrElse(
                    presenca -> {
                        item.setPresent(presenca.isPresent());
                        item.setObservation(presenca.getObservation());
                    },
                    () -> item.setPresent(true)
            );
            items.add(item);
        }
        batch.setItems(items);
        return batch;
    }

    @Transactional
    public void saveBatch(AttendanceBatchRequest request) {
        LocalDate date = request.getAttendanceDate();
        if (date == null) {
            throw new IllegalArgumentException("A data é obrigatória.");
        }

        for (AttendanceBatchRequest.AttendanceItemRequest item : request.getItems()) {
            if (item.getInscricaoId() == null) {
                continue;
            }

            Inscricao inscricao = inscricaoRepository.findById(item.getInscricaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inscrição não encontrada."));

            if (inscricao.getStatus() != EnrollmentStatus.ACTIVE
                    || !inscricao.getTurma().getId().equals(adminBibleSchoolService.getActiveTurma().getId())) {
                throw new ResourceNotFoundException("Inscrição inválida para a turma activa.");
            }

            Presenca presenca = presencaRepository.findByInscricaoAndAttendanceDate(inscricao, date)
                    .orElseGet(() -> {
                        Presenca newPresenca = new Presenca();
                        newPresenca.setInscricao(inscricao);
                        newPresenca.setAttendanceDate(date);
                        return newPresenca;
                    });

            presenca.setPresent(item.isPresent());
            presenca.setObservation(trimToNull(item.getObservation()));
            presencaRepository.save(presenca);
        }
    }

    @Transactional
    public void delete(Long id) {
        Presenca presenca = presencaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presença não encontrada."));

        if (!presenca.getInscricao().getTurma().getId().equals(adminBibleSchoolService.getActiveTurma().getId())) {
            throw new ResourceNotFoundException("Presença inválida para a turma activa.");
        }
        presencaRepository.delete(presenca);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
