package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    List<Turma> findByStatus(TurmaStatus status);

    Optional<Turma> findFirstByStatusOrderByCreatedAtDesc(TurmaStatus status);
}
