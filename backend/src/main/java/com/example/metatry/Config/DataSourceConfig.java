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
        StringBuilder sb = new StringBuilder();
        sb.append("=== DataSourceConfig DEBUG ===\n");
        sb.append("Environment: ").append(env).append('\n');
        if (env instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
            sb.append("PropertySources:\n");
            for (PropertySource<?> ps : ce.getPropertySources()) {
                Object val = ps.getProperty("spring.datasource.url");
                sb.append("  ").append(ps.getName()).append(" -> ").append(val).append('\n');
            }
        }
        sb.append("url=").append(env.getProperty("spring.datasource.url")).append('\n');
        sb.append("username=").append(env.getProperty("spring.datasource.username")).append('\n');
        sb.append("password=").append(env.getProperty("spring.datasource.password")).append('\n');
        sb.append("driver=").append(env.getProperty("spring.datasource.driver-class-name")).append('\n');
        throw new RuntimeException(sb.toString());
    }
}
