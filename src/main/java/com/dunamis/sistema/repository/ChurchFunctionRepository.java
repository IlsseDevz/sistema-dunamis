package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.ChurchFunction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChurchFunctionRepository extends JpaRepository<ChurchFunction, Long> {

    Optional<ChurchFunction> findByCode(String code);

    List<ChurchFunction> findAllByActiveTrueOrderByDisplayOrderAsc();

    boolean existsByCode(String code);
}
