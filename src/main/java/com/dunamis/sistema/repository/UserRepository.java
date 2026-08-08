package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByContacto(String contacto);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.roles
            WHERE u.contacto = :contacto
            """)
    Optional<User> findByContactoWithRoles(@Param("contacto") String contacto);

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.roles
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.churchFunction
            LEFT JOIN FETCH u.roles
            WHERE u.id = :id
            """)
    Optional<User> findByIdWithDetails(@Param("id") Long id);

    boolean existsByContacto(String contacto);

    boolean existsByEmail(String email);

    long countByAccountStatus(AccountStatus accountStatus);

    long countByChurchSituation(ChurchSituation churchSituation);

    long countByBaptizedTrue();

    long countByBaptizedFalse();

    long countByRegisteredAtLessThanEqual(java.time.LocalDateTime end);

    long countByRegisteredAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    long countByChurchSituationAndRegisteredAtLessThanEqual(
            ChurchSituation churchSituation,
            java.time.LocalDateTime end
    );

    long countByBaptizedTrueAndRegisteredAtLessThanEqual(java.time.LocalDateTime end);

    long countByBaptizedFalseAndRegisteredAtLessThanEqual(java.time.LocalDateTime end);

    @Query("""
            SELECT cf.label, COUNT(u) FROM User u
            JOIN u.churchFunction cf
            WHERE u.registeredAt <= :end
            GROUP BY cf.id, cf.label
            ORDER BY cf.displayOrder ASC, cf.label ASC
            """)
    java.util.List<Object[]> countUsersByFunctionUntil(@Param("end") java.time.LocalDateTime end);

    @Query(value = """
            SELECT u FROM User u
            WHERE (:situation IS NULL OR u.churchSituation = :situation)
              AND (:bairro IS NULL OR LOWER(u.bairro) LIKE LOWER(CONCAT('%', :bairro, '%')))
              AND (:baptized IS NULL OR u.baptized = :baptized)
              AND (:functionId IS NULL OR u.churchFunction.id = :functionId)
              AND (:accountStatus IS NULL OR u.accountStatus = :accountStatus)
              AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR u.contacto LIKE CONCAT('%', :search, '%'))
            """,
            countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE (:situation IS NULL OR u.churchSituation = :situation)
              AND (:bairro IS NULL OR LOWER(u.bairro) LIKE LOWER(CONCAT('%', :bairro, '%')))
              AND (:baptized IS NULL OR u.baptized = :baptized)
              AND (:functionId IS NULL OR u.churchFunction.id = :functionId)
              AND (:accountStatus IS NULL OR u.accountStatus = :accountStatus)
              AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR u.contacto LIKE CONCAT('%', :search, '%'))
            """)
    Page<User> searchMembers(
            @Param("search") String search,
            @Param("situation") ChurchSituation situation,
            @Param("bairro") String bairro,
            @Param("baptized") Boolean baptized,
            @Param("functionId") Long functionId,
            @Param("accountStatus") AccountStatus accountStatus,
            Pageable pageable
    );
}
