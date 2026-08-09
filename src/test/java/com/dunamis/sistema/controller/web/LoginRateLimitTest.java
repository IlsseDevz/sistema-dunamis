package com.dunamis.sistema.controller.web;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = "app.security.rateLimiter.enabled=true")
@Transactional
public class LoginRateLimitTest {

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
        // create several users for rate limit test
        for (int i = 1; i <= 6; i++) {
            saveUser(String.format("923700%03d", i), Set.of(RoleName.ROLE_MEMBER));
        }
    }

    @Test
    void shouldRateLimitAfterMaxAttempts() throws Exception {
        // perform failed login attempts for different users from same IP; limiter allows 5 per minute
        for (int i = 1; i <= 5; i++) {
            String contacto = String.format("923700%03d", i);
            mockMvc.perform(formLogin("/login").user(contacto).password("wrongpass"))
                    .andExpect(status().is3xxRedirection());
        }

        // 6th distinct user attempt from same IP should be rate limited
        mockMvc.perform(formLogin("/login").user("923700006").password("wrongpass"))
                .andExpect(status().isTooManyRequests());
    }

    private void saveUser(String contacto, Set<RoleName> roleNames) {
        ChurchFunction churchFunction = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name))))
                .collect(java.util.stream.Collectors.toSet());

        User user = new User();
        user.setFullName("RateLimit Teste");
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
