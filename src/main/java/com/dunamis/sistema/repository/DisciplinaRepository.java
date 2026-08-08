package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.Disciplina;
import com.dunamis.sistema.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    List<Disciplina> findByTurmaOrderByNameAsc(Turma turma);

    List<Disciplina> findByTurmaIdOrderByNameAsc(Long turmaId);

    long countByTurmaId(Long turmaId);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM Disciplina d
            WHERE d.turma.id = :turmaId AND LOWER(d.name) = LOWER(:name)
            """)
    boolean existsByTurmaIdAndNameIgnoreCase(@Param("turmaId") Long turmaId, @Param("name") String name);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM Disciplina d
            WHERE d.turma.id = :turmaId AND LOWER(d.name) = LOWER(:name) AND d.id <> :id
            """)
    boolean existsByTurmaIdAndNameIgnoreCaseAndIdNot(
            @Param("turmaId") Long turmaId,
            @Param("name") String name,
            @Param("id") Long id
    );
}
