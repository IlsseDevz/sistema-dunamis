package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresencaRepository extends JpaRepository<Presenca, Long> {

    List<Presenca> findByInscricaoOrderByAttendanceDateDesc(Inscricao inscricao);

    List<Presenca> findByInscricaoIdOrderByAttendanceDateDesc(Long inscricaoId);

    Optional<Presenca> findByInscricaoAndAttendanceDate(Inscricao inscricao, LocalDate attendanceDate);

    @Query("""
            SELECT COUNT(p) FROM Presenca p
            WHERE p.inscricao.id = :inscricaoId AND p.present = true
            """)
    long countPresentByInscricaoId(@Param("inscricaoId") Long inscricaoId);

    @Query("""
            SELECT COUNT(p) FROM Presenca p
            WHERE p.inscricao.id = :inscricaoId
            """)
    long countTotalByInscricaoId(@Param("inscricaoId") Long inscricaoId);

    @Query("""
            SELECT p FROM Presenca p
            JOIN FETCH p.inscricao i
            JOIN FETCH i.user u
            WHERE i.turma.id = :turmaId
            ORDER BY p.attendanceDate DESC, u.fullName ASC
            """)
    List<Presenca> findByTurmaIdWithDetails(@Param("turmaId") Long turmaId);

    @Query("""
            SELECT p FROM Presenca p
            JOIN FETCH p.inscricao i
            JOIN FETCH i.user u
            WHERE i.turma.id = :turmaId AND p.attendanceDate = :date
            ORDER BY u.fullName ASC
            """)
    List<Presenca> findByTurmaIdAndDateWithDetails(
            @Param("turmaId") Long turmaId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT COUNT(DISTINCT p.inscricao.id) FROM Presenca p
            WHERE p.inscricao.turma.id = :turmaId
              AND p.attendanceDate BETWEEN :start AND :end
              AND p.present = true
            """)
    long countDistinctStudentsPresentInPeriod(
            @Param("turmaId") Long turmaId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COUNT(p) FROM Presenca p
            WHERE p.inscricao.turma.id = :turmaId
              AND p.attendanceDate BETWEEN :start AND :end
              AND p.present = true
            """)
    long countPresentRecordsInPeriod(
            @Param("turmaId") Long turmaId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COUNT(p) FROM Presenca p
            WHERE p.inscricao.turma.id = :turmaId
              AND p.attendanceDate BETWEEN :start AND :end
              AND p.present = false
            """)
    long countAbsentRecordsInPeriod(
            @Param("turmaId") Long turmaId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            SELECT COUNT(p) FROM Presenca p
            WHERE p.inscricao.turma.id = :turmaId
              AND p.attendanceDate BETWEEN :start AND :end
            """)
    long countTotalRecordsInPeriod(
            @Param("turmaId") Long turmaId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
