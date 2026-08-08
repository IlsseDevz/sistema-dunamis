package com.dunamis.sistema.service;

import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.User;
import com.dunamis.sistema.entity.enums.AccountStatus;
import com.dunamis.sistema.entity.enums.ChurchSituation;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Set;

@Configuration
public class AdminBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    @Bean
    CommandLineRunner bootstrapAdminUser(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ChurchFunctionRepository churchFunctionRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-contacto:}") String adminContacto,
            @Value("${app.bootstrap.admin-password:}") String adminPassword,
            @Value("${app.bootstrap.admin-name:Administrador}") String adminName
    ) {
        return args -> {
            if (!StringUtils.hasText(adminContacto) || !StringUtils.hasText(adminPassword)) {
                return;
            }

            String contacto = adminContacto.trim().replaceAll("\\s+", "");
            if (userRepository.existsByContacto(contacto)) {
                return;
            }

            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN não encontrada."));
            Role memberRole = roleRepository.findByName(RoleName.ROLE_MEMBER)
                    .orElseThrow(() -> new IllegalStateException("ROLE_MEMBER não encontrada."));

            ChurchFunction churchFunction = churchFunctionRepository.findByCode("SEM_FUNCAO")
                    .orElseThrow(() -> new IllegalStateException("Função SEM_FUNCAO não encontrada."));

            User admin = new User();
            admin.setFullName(adminName.trim());
            admin.setContacto(contacto);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setBairro("Sede");
            admin.setChurchSituation(ChurchSituation.MEMBRO_ATIVO);
            admin.setChurchFunction(churchFunction);
            admin.setBaptized(true);
            admin.setAccountStatus(AccountStatus.ACTIVE);
            admin.setRoles(Set.of(adminRole, memberRole));

            userRepository.save(admin);
            log.info("Utilizador administrador criado com contacto: {}", contacto);
        };
    }
}
