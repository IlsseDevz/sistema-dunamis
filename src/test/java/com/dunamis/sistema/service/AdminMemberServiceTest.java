package com.dunamis.sistema.service;

import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.exception.OperationNotAllowedException;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.UserRepository;
import com.dunamis.sistema.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminMemberServiceTest {

    @Autowired
    private AdminMemberService adminMemberService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChurchFunctionRepository churchFunctionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityUtils securityUtils;

    private User adminUser;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_ADMIN));
        }
        if (roleRepository.findByName(RoleName.ROLE_MEMBER).isEmpty()) {
            roleRepository.save(new Role(RoleName.ROLE_MEMBER));
        }

        adminUser = saveUser("923600001", RoleName.ROLE_ADMIN);
        securityUtils.refreshAuthentication(adminUser.getId());
    }

    @Test
    void shouldDeactivateMember() {
        User member = saveUser("923600002", RoleName.ROLE_MEMBER);

        adminMemberService.deactivateMember(member.getId());

        User updated = userRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void shouldPreventSelfDeactivation() {
        assertThatThrownBy(() -> adminMemberService.deactivateMember(adminUser.getId()))
                .isInstanceOf(OperationNotAllowedException.class);
    }

    private User saveUser(String contacto, RoleName roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        ChurchFunction fn = churchFunctionRepository.findByCode("SEM_FUNCAO")
                .orElseGet(() -> churchFunctionRepository.save(new ChurchFunction("SEM_FUNCAO", "Sem função", 9)));

        User user = new User();
        user.setFullName("Utilizador " + contacto);
        user.setContacto(contacto);
        user.setPassword(passwordEncoder.encode("segredo123"));
        user.setBairro("Centro");
        user.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
        user.setChurchFunction(fn);
        user.setBaptized(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }
}
