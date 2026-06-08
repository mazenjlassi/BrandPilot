package com.example.metatry.Config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupDiagnostic {
    private static final Logger log = LoggerFactory.getLogger(StartupDiagnostic.class);
    private final Environment env;

    public StartupDiagnostic(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void printDiagnostics() {
        log.info("=== STARTUP DIAGNOSTIC ===");
        log.info("Active profiles: {}", java.util.Arrays.toString(env.getActiveProfiles()));
        log.info("spring.datasource.url: {}", env.getProperty("spring.datasource.url"));
        log.info("spring.datasource.username: {}", env.getProperty("spring.datasource.username"));
        log.info("spring.jpa.hibernate.ddl-auto: {}", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        log.info("spring.jpa.properties.hibernate.hbm2ddl.auto: {}", env.getProperty("spring.jpa.properties.hibernate.hbm2ddl.auto"));
        log.info("hibernate.hbm2ddl.auto: {}", env.getProperty("hibernate.hbm2ddl.auto"));
        log.info("=== END STARTUP DIAGNOSTIC ===");
    }
}
