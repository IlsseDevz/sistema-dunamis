package com.dunamis.sistema.repository;

import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Inscricao;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.EnrollmentStatus;
import com.dunamis.sistema.entity.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EntityRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChurchFunctionRepository churchFunctionRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    private ChurchFunction semFuncao;
    private Role memberRole;
    private Turma turma;

    @BeforeEach
    void setUp() {
        semFuncao = new ChurchFunction("SEM_FUNCAO", "Sem função", 9);
        entityManager.persist(semFuncao);

        memberRole = new Role(RoleName.ROLE_MEMBER);
        entityManager.persist(memberRole);

        turma = new Turma();
        turma.setName("Turma Teste");
        turma.setYear(2026);
        entityManager.persist(turma);

        entityManager.flush();
    }

    @Test
    void shouldPersistUserWithUniqueContacto() {
        User user = buildUser("João Silva", "923000001");
        user.setRoles(Set.of(memberRole));

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findByContacto("923000001")).isPresent();
        assertThat(userRepository.existsByContacto("923000001")).isTrue();
    }

    @Test
    void shouldCreateInscricaoLinkingUserToTurmaWithoutDuplicatingUser() {
        User user = userRepository.save(buildUser("Maria Santos", "923000002"));

        Inscricao inscricao = new Inscricao();
        inscricao.setUser(user);
        inscricao.setTurma(turma);
        inscricao.setStatus(EnrollmentStatus.ACTIVE);

        Inscricao saved = inscricaoRepository.save(inscricao);

        assertThat(saved.getId()).isNotNull();
        assertThat(inscricaoRepository.findFirstByUserAndStatusOrderByEnrollmentDateDesc(user, EnrollmentStatus.ACTIVE))
                .isPresent();
        assertThat(inscricaoRepository.existsByUserAndTurmaAndStatus(user, turma, EnrollmentStatus.ACTIVE))
                .isTrue();
    }

    @Test
    void shouldSeedChurchFunctionsViaRepository() {
        ChurchFunction pastor = new ChurchFunction("PASTOR", "Pastor", 1);
        churchFunctionRepository.save(pastor);

        assertThat(churchFunctionRepository.findByCode("PASTOR")).isPresent();
        assertThat(churchFunctionRepository.findAllByActiveTrueOrderByDisplayOrderAsc())
                .hasSizeGreaterThanOrEqualTo(1);
    }

    private User buildUser(String fullName, String contacto) {
        User user = new User();
        user.setFullName(fullName);
        user.setContacto(contacto);
        user.setPassword("$2a$10$hashedpasswordplaceholder");
        user.setBairro("Centro");
        user.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        user.setChurchFunction(semFuncao);
        user.setBaptized(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return user;
    }
}
