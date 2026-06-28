package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private final String testName = "inttest_" + UUID.randomUUID().toString().substring(0, 8);

    @AfterEach
    void tearDown() {
        userRepository.findByName(testName).ifPresent(u -> userRepository.delete(u));
    }

    @Test
    void registerAndLogin() {
        var register = new RegisterRequest();
        register.setName(testName);
        register.setEmail(testName + "@test.com");
        register.setPassword("password123");

        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                "/auth/register", register, String.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResponse.getBody()).isEqualTo("User registered successfully");

        assertThat(userRepository.findByName(testName)).isPresent();

        var login = new AuthRequest();
        login.setUsername(testName);
        login.setPassword("password123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/auth/login", login, Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsKeys("token", "role", "name");
        assertThat((String) loginResponse.getBody().get("token")).isNotBlank();
        assertThat((String) loginResponse.getBody().get("role")).isEqualTo("MARKETING");
    }

    @Test
    void registerDuplicateReturnsError() {
        var register = new RegisterRequest();
        register.setName(testName);
        register.setEmail(testName + "@test.com");
        register.setPassword("password123");

        restTemplate.postForEntity("/auth/register", register, String.class);

        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
                "/auth/register", register, String.class);

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginWithWrongPasswordReturnsError() {
        var register = new RegisterRequest();
        register.setName(testName);
        register.setEmail(testName + "@test.com");
        register.setPassword("correctpass");

        restTemplate.postForEntity("/auth/register", register, String.class);

        var login = new AuthRequest();
        login.setUsername(testName);
        login.setPassword("wrongpass");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/auth/login", login, String.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
