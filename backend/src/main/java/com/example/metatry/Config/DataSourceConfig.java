package com.example.metatry.Config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        StringBuilder sb = new StringBuilder("=== DataSourceConfig DEBUG === | ");
        sb.append("env=").append(env.getClass().getName()).append(" | ");
        if (env instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
            sb.append("ps_count=").append(ce.getPropertySources().size()).append(" | ");
            for (PropertySource<?> ps : ce.getPropertySources()) {
                Object val = ps.getProperty("spring.datasource.url");
                sb.append("[").append(ps.getName()).append("=").append(val).append("] ");
            }
        }
        sb.append("| PROP:url=").append(env.getProperty("spring.datasource.url"));
        sb.append("|username=").append(env.getProperty("spring.datasource.username"));
        sb.append("|password=").append(env.getProperty("spring.datasource.password"));
        sb.append("|driver=").append(env.getProperty("spring.datasource.driver-class-name"));
        throw new RuntimeException(sb.toString());
    }
}
