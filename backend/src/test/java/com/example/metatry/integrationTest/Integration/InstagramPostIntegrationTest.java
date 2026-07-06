package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.InstagramService;
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
@Import({TestcontainersConfiguration.class, InstagramPostIntegrationTest.MockConfig.class})
class InstagramPostIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        InstagramService instagramService() {
            return Mockito.mock(InstagramService.class);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstagramService instagramService;

    private String token;
    private final String testName = "ig_" + UUID.randomUUID().toString().substring(0, 8);

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
    void postUrl_success() {
        when(instagramService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("id", "ig_123"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/instagram/post/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("imageUrl", "https://example.com/photo.jpg", "caption", "Great shot"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postUrl_missingImageUrl_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/instagram/post/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("caption", "No image"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testEndpoint_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/instagram/test", HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("status")).isEqualTo("OK");
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/instagram/post/url", HttpMethod.POST,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
