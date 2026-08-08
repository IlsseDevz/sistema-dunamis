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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoginIntegrationTest {

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
    }

    @Test
    void shouldLoginMemberAndRedirectToDashboard() throws Exception {
        saveUser("923888001", Set.of(RoleName.ROLE_MEMBER));

        mockMvc.perform(formLogin("/login")
                        .user("923888001")
                        .password("segredo123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void shouldLoginAdminAndRedirectToAdminPanel() throws Exception {
        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_ADMIN));
        }

        saveUser("923888002", Set.of(RoleName.ROLE_ADMIN, RoleName.ROLE_MEMBER));

        mockMvc.perform(formLogin("/login")
                        .user("923888002")
                        .password("segredo123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        saveUser("923888003", Set.of(RoleName.ROLE_MEMBER));

        mockMvc.perform(formLogin("/login")
                        .user("923888003")
                        .password("errada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    private void saveUser(String contacto, Set<RoleName> roleNames) {
        ChurchFunction churchFunction = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name))))
                .collect(java.util.stream.Collectors.toSet());

        User user = new User();
        user.setFullName("Login Teste");
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
