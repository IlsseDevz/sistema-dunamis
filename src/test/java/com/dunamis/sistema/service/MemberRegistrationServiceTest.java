package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.request.MemberRegistrationRequest;
import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.exception.DuplicateContactException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberRegistrationServiceTest {

    @Autowired
    private MemberRegistrationService memberRegistrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChurchFunctionRepository churchFunctionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long churchFunctionId;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(RoleName.ROLE_MEMBER).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_MEMBER));
        }

        churchFunctionId = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .map(ChurchFunction::getId)
                .orElseGet(() -> churchFunctionRepository.save(
                        new ChurchFunction("SEM_FUNCAO", "Sem função", 9)
                ).getId());
    }

    @Test
    void shouldRegisterMemberWithHashedPasswordAndActiveAccount() {
        MemberRegistrationRequest request = validRequest("Ana Costa", "923111222");

        User saved = memberRegistrationService.register(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getRoles()).extracting(role -> role.getName().name())
                .containsExactly("ROLE_MEMBER");
        assertThat(passwordEncoder.matches("segredo123", saved.getPassword())).isTrue();
        assertThat(saved.getEmail()).isNull();
    }

    @Test
    void shouldRegisterMemberWithOptionalEmail() {
        MemberRegistrationRequest request = validRequest("Pedro Lima", "923111333");
        request.setEmail("pedro@email.com");

        User saved = memberRegistrationService.register(request);

        assertThat(saved.getEmail()).isEqualTo("pedro@email.com");
    }

    @Test
    void shouldRejectDuplicateContacto() {
        memberRegistrationService.register(validRequest("Maria Silva", "923111444"));

        assertThatThrownBy(() ->
                memberRegistrationService.register(validRequest("Outra Pessoa", "923111444"))
        ).isInstanceOf(DuplicateContactException.class);
    }

    private MemberRegistrationRequest validRequest(String fullName, String contacto) {
        MemberRegistrationRequest request = new MemberRegistrationRequest();
        request.setFullName(fullName);
        request.setContacto(contacto);
        request.setBairro("Centro");
        request.setPassword("segredo123");
        request.setConfirmPassword("segredo123");
        request.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        request.setBaptized(true);
        request.setChurchFunctionId(churchFunctionId);
        return request;
    }
}
