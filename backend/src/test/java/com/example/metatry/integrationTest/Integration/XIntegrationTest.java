package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.XService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, XIntegrationTest.MockConfig.class})
class XIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        XService xService() {
            return Mockito.mock(XService.class);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private XService xService;

    private String token;
    private final String testName = "xint_" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setUp() {
        var register = new RegisterRequest();
        register.setName(testName);
        register.setEmail(testName + "@test.com");
        register.setPassword("password123");
        restTemplate.postForEntity("/auth/register", register, String.class);

        var login = new AuthRequest();
        login.setUsername(testName);
        login.setPassword("password123");
        ResponseEntity<Map> loginResp = restTemplate.postForEntity("/auth/login", login, Map.class);
        token = "Bearer " + loginResp.getBody().get("token");
    }

    @AfterEach
    void tearDown() {
        userRepository.findByName(testName).ifPresent(u -> userRepository.delete(u));
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return h;
    }

    @Test
    void postText_success() {
        when(xService.postText(anyString())).thenReturn(Map.of("success", true, "id", "12345"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", "Hello world"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("success")).isEqualTo(true);
    }

    @Test
    void postText_empty_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", ""), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postText_null_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of(), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postText_tooLong_returns400() {
        String longText = "x".repeat(281);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", longText), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postText_serviceFailure_returns400() {
        when(xService.postText(anyString())).thenReturn(Map.of("success", false, "error", "API error"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", "Hello"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_endpoint_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x/test", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("success")).isEqualTo(true);
    }

    @Test
    void limits_endpoint_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x/limits", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("success")).isEqualTo(true);
    }

    @Test
    void docs_endpoint_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/x", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("service")).isEqualTo("X/Twitter API");
    }

    @Test
    void health_endpoint_requiresAuth() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/x/health", HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/x/test", HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
