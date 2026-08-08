package com.dunamis.sistema;

import com.dunamis.sistema.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SistemaDunamisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaDunamisApplication.class, args);
    }
}
