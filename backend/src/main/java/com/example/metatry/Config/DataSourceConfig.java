package com.example.metatry.Config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        String driver = env.getProperty("spring.datasource.driver-class-name");
        return DataSourceBuilder.create()
                .url(url != null ? url : "jdbc:mysql://localhost:3306/metatry?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC")
                .username(username != null ? username : "root")
                .password(password != null ? password : "rootpassword")
                .driverClassName(driver != null ? driver : "com.mysql.cj.jdbc.Driver")
                .type(HikariDataSource.class)
                .build();
    }
}
