package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Nota;
import com.dunamis.sistema.entity.enums.GradeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    List<Nota> findByInscricao(Inscricao inscricao);

    List<Nota> findByInscricaoId(Long inscricaoId);

    @Query("""
            SELECT n FROM Nota n
            JOIN FETCH n.disciplina d
            WHERE n.inscricao.id = :inscricaoId
            ORDER BY d.name ASC, n.gradeType ASC
            """)
    List<Nota> findByInscricaoIdWithDisciplina(@Param("inscricaoId") Long inscricaoId);

    List<Nota> findByDisciplinaId(Long disciplinaId);

    Optional<Nota> findByInscricaoAndDisciplinaAndGradeType(
            Inscricao inscricao,
            Disciplina disciplina,
            GradeType gradeType
    );

    @Query("""
            SELECT n FROM Nota n
            JOIN FETCH n.inscricao i
            JOIN FETCH i.user u
            JOIN FETCH n.disciplina d
            WHERE i.turma.id = :turmaId
            ORDER BY u.fullName ASC, d.name ASC, n.gradeType ASC
            """)
    List<Nota> findByTurmaIdWithDetails(@Param("turmaId") Long turmaId);

    @Query("""
            SELECT AVG(n.value) FROM Nota n
            WHERE n.inscricao.turma.id = :turmaId
              AND n.recordedAt BETWEEN :start AND :end
            """)
    BigDecimal averageByTurmaAndRecordedAtBetween(
            @Param("turmaId") Long turmaId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
