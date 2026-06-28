package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.LinkedInService;
import com.example.metatry.Services.LinkedInTokenService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, LinkedInIntegrationTest.MockConfig.class})
class LinkedInIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        LinkedInService linkedInService() {
            return Mockito.mock(LinkedInService.class);
        }

        @Bean
        @Primary
        LinkedInTokenService linkedInTokenService() {
            return Mockito.mock(LinkedInTokenService.class);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LinkedInService linkedInService;

    @Autowired
    private LinkedInTokenService linkedInTokenService;

    private String token;
    private final String testName = "li_" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setUp() {
        doNothing().when(linkedInTokenService).exchangeAuthorizationCode(anyString());

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
        when(linkedInService.postText(anyString()))
                .thenReturn(Map.of("success", true, "id", "li_123"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", "Hello LinkedIn"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("success")).isEqualTo(true);
    }

    @Test
    void postText_missingText_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of(), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postText_serviceFailure_returns400() {
        when(linkedInService.postText(anyString()))
                .thenReturn(Map.of("success", false, "error", "API error"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/post/text", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", "Hello"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postImageFromUrl_success() {
        when(linkedInService.postArticleWithImage(anyString(), anyString(), anyString()))
                .thenReturn(Map.of("success", true));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/post/image/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("imageUrl", "https://example.com/img.jpg", "text", "Nice image"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postImageFromUrl_missingParams_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/post/image/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("imageUrl", "https://example.com/img.jpg"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void postVideoFromUrl_success() {
        when(linkedInService.postArticleWithVideo(anyString(), anyString(), anyString()))
                .thenReturn(Map.of("success", true));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/post/video/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("videoUrl", "https://example.com/vid.mp4", "text", "Great video"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postVideoFromUrl_missingParams_returns400() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/post/video/url", HttpMethod.POST,
                new HttpEntity<>(Map.of("text", "No video URL"), authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void authUrl_success() {
        when(linkedInTokenService.getAuthorizationUrl())
                .thenReturn("https://linkedin.com/oauth/auth");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/auth-url", HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("authUrl")).isEqualTo("https://linkedin.com/oauth/auth");
    }

    @Test
    void callback_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/callback?code=testcode", HttpMethod.GET,
                null, Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("success")).isEqualTo(true);
    }

    @Test
    void callback_exchangeFails_returns500() {
        doThrow(new RuntimeException("Invalid code"))
                .when(linkedInTokenService).exchangeAuthorizationCode(anyString());

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/callback?code=badcode", HttpMethod.GET,
                null, Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void profile_success() {
        when(linkedInService.getUserProfile())
                .thenReturn(Map.of("name", "Test User", "headline", "Developer"));

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/profile", HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("name")).isEqualTo("Test User");
    }

    @Test
    void status_success() {
        when(linkedInTokenService.isAuthenticated()).thenReturn(true);
        when(linkedInTokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/linkedin/status", HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("authenticated")).isEqualTo(true);
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/linkedin/post/text", HttpMethod.POST,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
