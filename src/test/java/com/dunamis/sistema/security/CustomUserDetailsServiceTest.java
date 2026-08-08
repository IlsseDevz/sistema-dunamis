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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

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
    void shouldLoadUserByContacto() {
        saveUser("923999001", AccountStatus.ACTIVE);

        UserDetailsImpl details = (UserDetailsImpl) customUserDetailsService.loadUserByUsername("923999001");

        assertThat(details.getContacto()).isEqualTo("923999001");
        assertThat(details.getAuthorities()).extracting("authority").contains("ROLE_MEMBER");
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void shouldNormalizeContactoWithSpaces() {
        saveUser("923999002", AccountStatus.ACTIVE);

        UserDetailsImpl details = (UserDetailsImpl) customUserDetailsService.loadUserByUsername("923 999 002");

        assertThat(details.getContacto()).isEqualTo("923999002");
    }

    @Test
    void shouldLoadUserByEmailWhenIdentifierContainsAt() {
        User user = saveUser("923999003", AccountStatus.ACTIVE);
        user.setEmail("aluno@test.com");
        userRepository.save(user);

        UserDetailsImpl details = (UserDetailsImpl) customUserDetailsService.loadUserByUsername("aluno@test.com");

        assertThat(details.getEmail()).isEqualTo("aluno@test.com");
    }

    @Test
    void shouldRejectInactiveAccount() {
        saveUser("923999004", AccountStatus.INACTIVE);

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("923999004"))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void shouldRejectUnknownUser() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("000000000"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private User saveUser(String contacto, AccountStatus status) {
        Role memberRole = roleRepository.findByName(RoleName.ROLE_MEMBER).orElseThrow();
        ChurchFunction churchFunction = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        User user = new User();
        user.setFullName("Utilizador Teste");
        user.setContacto(contacto);
        user.setPassword(passwordEncoder.encode("segredo123"));
        user.setBairro("Centro");
        user.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        user.setChurchFunction(churchFunction);
        user.setBaptized(true);
        user.setAccountStatus(status);
        user.setRoles(Set.of(memberRole));
        return userRepository.save(user);
    }
}
