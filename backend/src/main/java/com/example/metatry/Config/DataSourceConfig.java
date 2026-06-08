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
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String[] paths = {
            "application.properties",
            "/application.properties",
            "BOOT-INF/classes/application.properties",
            "classpath:application.properties"
        };
        Exception lastEx = null;
        for (String path : paths) {
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is != null) {
                    props.load(is);
                    break;
                }
            } catch (Exception e) {
                lastEx = e;
            }
        }
        if (props.isEmpty()) {
            StringBuilder sb = new StringBuilder("Cannot find application.properties. Tried: ");
            for (String p : paths) sb.append(p).append(" ");
            sb.append(" CL=").append(cl);
            if (lastEx != null) sb.append(" ex=").append(lastEx);
            throw new RuntimeException(sb.toString());
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
