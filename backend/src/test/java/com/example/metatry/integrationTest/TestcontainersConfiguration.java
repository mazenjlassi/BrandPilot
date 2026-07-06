package com.example.metatry.integrationTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.*;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public MySQLContainer mysqlContainer() {
        return new MySQLContainer(DockerImageName.parse("mysql:8.0"));
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }

}
