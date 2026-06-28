package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.FacebookService;
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
@Import({TestcontainersConfiguration.class, FacebookPostIntegrationTest.MockConfig.class})
class FacebookPostIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        FacebookService facebookService() {
            return Mockito.mock(FacebookService.class);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacebookService facebookService;

    private String token;
    private final String testName = "fb_" + UUID.randomUUID().toString().substring(0, 8);

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
        when(facebookService.postText(anyString())).thenReturn(Map.of("id", "fb_123"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/facebook/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of("message", "Hello Facebook"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postText_missingMessage_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/facebook/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of(), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postUrl_success() {
        when(facebookService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("id", "fb_photo_789"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/facebook/post/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("imageUrl", "https://example.com/img.jpg", "caption", "Nice pic"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postUrl_missingImageUrl_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/facebook/post/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("caption", "No image"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/facebook/post/text", HttpMethod.POST,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
