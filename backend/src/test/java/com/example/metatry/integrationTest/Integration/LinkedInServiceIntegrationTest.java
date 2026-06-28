package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Services.LinkedInService;
import com.example.metatry.Services.LinkedInTokenService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class LinkedInServiceIntegrationTest {

    @Autowired
    private LinkedInService linkedInService;

    @MockitoBean
    private LinkedInTokenService tokenService;

    @MockitoBean
    private RestTemplate restTemplate;

    // ================= postText =================

    @Test
    void postText_success() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:test");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RestLi-Id", "urn:li:ugcPost:123");

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(), headers, HttpStatus.CREATED));

        Map<String, Object> result = linkedInService.postText("Test message");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("postId")).isEqualTo("urn:li:ugcPost:123");
    }

    @Test
    void postText_notAuthenticated_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.postText("Test");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).toString().contains("Not authenticated");
    }

    // ================= postArticleWithImage =================

    @Test
    void postArticleWithImage_success() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:test");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RestLi-Id", "urn:li:ugcPost:456");

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(), headers, HttpStatus.CREATED));

        Map<String, Object> result = linkedInService.postArticleWithImage("Text", "http://img.com/a.jpg", "Title");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("postId")).isEqualTo("urn:li:ugcPost:456");
    }

    // ================= postArticleWithVideo =================

    @Test
    void postArticleWithVideo_success() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:test");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RestLi-Id", "urn:li:ugcPost:789");

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(), headers, HttpStatus.CREATED));

        Map<String, Object> result = linkedInService.postArticleWithVideo("Text", "http://video.com/v.mp4", "Video");

        assertThat(result.get("success")).isEqualTo(true);
    }

    // ================= getUserProfile =================

    @Test
    void getUserProfile_success() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(
                        Map.of("sub", "abc", "name", "Test User"),
                        HttpStatus.OK));

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(true);
        assertThat((Map<String, Object>) result.get("profile")).containsEntry("sub", "abc");
    }

    @Test
    void getUserProfile_notAuthenticated_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(false);
    }

    @Test
    void getUserProfile_apiError_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(false);
    }
}
