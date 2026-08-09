package com.dunamis.sistema.config;

import com.dunamis.sistema.entity.ChurchFunction;
import com.dunamis.sistema.entity.Role;
import com.dunamis.sistema.entity.Turma;
import com.dunamis.sistema.entity.enums.RoleName;
import com.dunamis.sistema.entity.enums.TurmaStatus;
import com.dunamis.sistema.repository.ChurchFunctionRepository;
import com.dunamis.sistema.repository.RoleRepository;
import com.dunamis.sistema.repository.TurmaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.Year;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedReferenceData(
            RoleRepository roleRepository,
            ChurchFunctionRepository churchFunctionRepository,
            TurmaRepository turmaRepository,
            org.springframework.core.env.Environment env
    ) {
        return args -> {
            seedRoles(roleRepository);
            seedChurchFunctions(churchFunctionRepository);
            seedDefaultTurma(turmaRepository, env);
        };
    }

    private void seedRoles(RoleRepository roleRepository) {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(new Role(roleName));
                log.info("Role criada: {}", roleName);
            }
        }
    }

    private void seedChurchFunctions(ChurchFunctionRepository churchFunctionRepository) {
        String[][] functions = {
                {"PASTOR", "Pastor", "1"},
                {"DIACONO", "Diácono", "2"},
                {"LOUVOR", "Louvor", "3"},
                {"INTERCESSAO", "Intercessão", "4"},
                {"EVANGELISMO", "Evangelismo", "5"},
                {"JOVENS", "Jovens", "6"},
                {"CRIANCAS", "Crianças", "7"},
                {"OUTRO", "Outro", "8"},
                {"SEM_FUNCAO", "Sem função", "9"}
        };

        for (String[] fn : functions) {
            if (!churchFunctionRepository.existsByCode(fn[0])) {
                churchFunctionRepository.save(
                        new ChurchFunction(fn[0], fn[1], Integer.parseInt(fn[2]))
                );
                log.info("Função na igreja criada: {}", fn[1]);
            }
        }
    }

    private void seedDefaultTurma(TurmaRepository turmaRepository, org.springframework.core.env.Environment env) {
        // Skip creating a default turma during tests to avoid interfering with test data
        for (String profile : env.getActiveProfiles()) {
            if ("test".equals(profile)) {
                return;
            }
        }

        if (turmaRepository.findByStatus(TurmaStatus.ACTIVE).isEmpty()) {
            Turma turma = new Turma();
            turma.setName("Turma " + Year.now().getValue());
            turma.setDescription("Turma principal da Escola Bíblica do Pregador Dunamis");
            turma.setYear(Year.now().getValue());
            turma.setStatus(TurmaStatus.ACTIVE);
            turma.setCreatedAt(LocalDateTime.now());
            turmaRepository.save(turma);
            log.info("Turma padrão criada: {}", turma.getName());
        }
    }
}
