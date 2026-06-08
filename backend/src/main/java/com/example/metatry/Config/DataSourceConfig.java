package com.example.metatry.Config;

import java.io.InputStream;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
            if (is == null) {
                throw new RuntimeException("Cannot find application.properties on classpath");
            }
            props.load(is);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
        String url = props.getProperty("spring.datasource.url");
        String username = props.getProperty("spring.datasource.username");
        String password = props.getProperty("spring.datasource.password");
        String driverClassName = props.getProperty("spring.datasource.driver-class-name");
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .type(HikariDataSource.class)
                .build();
    }
}
