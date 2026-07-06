package com.example.metatry.Config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.ManagedType;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Component
public class SchemaDiagnostic {

    private static final Logger log = LoggerFactory.getLogger(SchemaDiagnostic.class);

    private final Environment environment;
    private final LocalContainerEntityManagerFactoryBean emfBean;

    public SchemaDiagnostic(Environment environment, LocalContainerEntityManagerFactoryBean emfBean) {
        this.environment = environment;
        this.emfBean = emfBean;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void dumpSchemaDiagnostics() {
        log.info("========== SCHEMA DIAGNOSTIC ==========");

        // Phase 1 — Verify Inputs
        log.info("--- Phase 1: Inputs ---");
        log.info("spring.jpa.hibernate.ddl-auto = [{}]", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        log.info("spring.jpa.generate-ddl = [{}]", environment.getProperty("spring.jpa.generate-ddl"));

        String hbm2ddl = environment.getProperty("spring.jpa.properties.hibernate.hbm2ddl.auto");
        if (hbm2ddl == null) {
            hbm2ddl = environment.getProperty("hibernate.hbm2ddl.auto");
        }
        log.info("hibernate.hbm2ddl.auto = [{}]", hbm2ddl);

        log.info("spring.jpa.properties = [{}]", environment.getProperty("spring.jpa.properties"));

        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Default profiles: {}", Arrays.toString(environment.getDefaultProfiles()));

        log.info("--- Property Sources ---");
        AbstractEnvironment env = (AbstractEnvironment) environment;
        for (PropertySource<?> ps : env.getPropertySources()) {
            log.info("  source: {} (class: {})", ps.getName(), ps.getClass().getSimpleName());
            if (ps instanceof EnumerablePropertySource eps) {
                for (String key : eps.getPropertyNames()) {
                    if (key.contains("ddl") || key.contains("hbm2ddl") || key.contains("schema") || key.contains("generate-ddl")) {
                        log.info("    {} = [{}]", key, eps.getProperty(key));
                    }
                }
            }
        }

        // Phase 2 — Verify Metadata
        log.info("--- Phase 2: Metadata ---");
        EntityManagerFactory emf = emfBean.getObject();
        if (emf != null) {
            Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
            log.info("Number of managed types (entities): {}", managedTypes.size());
            for (ManagedType<?> type : managedTypes) {
                log.info("  Entity: {}", type.getJavaType().getName());
            }
        } else {
            log.warn("EntityManagerFactory is null — metadata not available");
        }

        // Attempt to trigger schema action logging by inspecting Hibernate config
        if (emf instanceof SessionFactoryImplementor sfi) {
            Map<String, Object> properties = sfi.getProperties();
            log.info("--- Hibernate Runtime Properties ---");
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getKey().contains("hbm2ddl") || entry.getKey().contains("schema")) {
                    log.info("  {} = [{}]", entry.getKey(), entry.getValue());
                }
            }
            log.info("Hibernate properties count: {}", properties.size());
            log.info("hibernate.hbm2ddl.auto in session: [{}]", properties.get("hibernate.hbm2ddl.auto"));
        }

        log.info("========== END SCHEMA DIAGNOSTIC ==========");
    }
}
