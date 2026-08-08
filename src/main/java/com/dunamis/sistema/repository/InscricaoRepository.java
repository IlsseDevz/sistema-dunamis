package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    List<Inscricao> findByUser(User user);

    List<Inscricao> findByTurma(Turma turma);

    List<Inscricao> findByTurmaAndStatus(Turma turma, EnrollmentStatus status);

    @Query("""
            SELECT i FROM Inscricao i
            JOIN FETCH i.user u
            WHERE i.turma = :turma AND i.status = :status
            ORDER BY u.fullName ASC
            """)
    List<Inscricao> findByTurmaAndStatusWithUser(
            @Param("turma") Turma turma,
            @Param("status") EnrollmentStatus status
    );

    Optional<Inscricao> findByUserAndTurma(User user, Turma turma);

    Optional<Inscricao> findFirstByUserAndStatusOrderByEnrollmentDateDesc(User user, EnrollmentStatus status);

    @Query("""
            SELECT i FROM Inscricao i
            JOIN FETCH i.turma t
            JOIN FETCH i.user u
            WHERE u.id = :userId AND i.status = :status
            ORDER BY i.enrollmentDate DESC
            """)
    List<Inscricao> findActiveEnrollmentsByUserIdWithTurma(
            @Param("userId") Long userId,
            @Param("status") EnrollmentStatus status
    );

    boolean existsByUserAndTurmaAndStatus(User user, Turma turma, EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);

    long countByTurmaAndStatus(Turma turma, EnrollmentStatus status);

    @Query(value = """
            SELECT i FROM Inscricao i
            JOIN FETCH i.user u
            WHERE i.turma.id = :turmaId
              AND i.status = :status
              AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR u.contacto LIKE CONCAT('%', :search, '%'))
            """,
            countQuery = """
            SELECT COUNT(i) FROM Inscricao i
            JOIN i.user u
            WHERE i.turma.id = :turmaId
              AND i.status = :status
              AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR u.contacto LIKE CONCAT('%', :search, '%'))
            """)
    Page<Inscricao> searchActiveByTurma(
            @Param("turmaId") Long turmaId,
            @Param("status") EnrollmentStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
