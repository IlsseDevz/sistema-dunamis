package com.dunamis.sistema.security;

import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationEventListenerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChurchFunctionRepository churchFunctionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(RoleName.ROLE_MEMBER).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_MEMBER));
        }
        saveUser("923800001", Set.of(RoleName.ROLE_MEMBER));
    }

    @Test
    void failedAttemptsShouldLockAccount() throws Exception {
        // perform failed attempts (listener marks account inactive after 5 attempts)
        for (int i = 0; i < 6; i++) {
            final String remote = "10.0.0." + i; // vary IP to bypass IP-based rate limiter
            mockMvc.perform(post("/login")
                    .param("username", "923800001")
                    .param("password", "wrongpass")
                    .with(csrf())
                    .with(request -> { request.setRemoteAddr(remote); return request; }));
        }

        User user = userRepository.findByContacto("923800001").orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isGreaterThanOrEqualTo(5);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    private void saveUser(String contacto, Set<RoleName> roleNames) {
        ChurchFunction churchFunction = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name))))
                .collect(java.util.stream.Collectors.toSet());

        User user = new User();
        user.setFullName("AuthEvent Teste");
        user.setContacto(contacto);
        user.setPassword(passwordEncoder.encode("segredo123"));
        user.setBairro("Centro");
        user.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        user.setChurchFunction(churchFunction);
        user.setBaptized(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRoles(roles);
        userRepository.save(user);
    }
}
