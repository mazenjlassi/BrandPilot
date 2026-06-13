package com.example.metatry.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkedInServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LinkedInTokenService tokenService;

    private LinkedInService linkedInService;

    @BeforeEach
    void setUp() {
        linkedInService = new LinkedInService(restTemplate, tokenService);
    }

    @Test
    void postText_returnsPostId() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RestLi-Id", "urn:li:share:456");
        ResponseEntity<Map> response = new ResponseEntity<>(Map.of(), headers, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = linkedInService.postText("Hello");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("postId")).isEqualTo("urn:li:share:456");
    }

    @Test
    void postText_notAuthenticated_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.postText("Hello");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated with LinkedIn");
    }

    @Test
    void postArticleWithImage_returnsPostId() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RestLi-Id", "urn:li:share:789");
        ResponseEntity<Map> response = new ResponseEntity<>(Map.of(), headers, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = linkedInService.postArticleWithImage("Text", "https://img.url", "Title");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("postId")).isEqualTo("urn:li:share:789");
    }

    @Test
    void postArticleWithImage_notAuthenticated_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.postArticleWithImage("Text", "https://img.url", "Title");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated with LinkedIn");
    }

    @Test
    void postArticleWithVideo_returnsPostId() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RestLi-Id", "urn:li:share:101");
        ResponseEntity<Map> response = new ResponseEntity<>(Map.of(), headers, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = linkedInService.postArticleWithVideo("Text", "https://vid.url", "Title");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("postId")).isEqualTo("urn:li:share:101");
    }

    @Test
    void postArticleWithVideo_notAuthenticated_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.postArticleWithVideo("Text", "https://vid.url", "Title");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated with LinkedIn");
    }

    @Test
    void executeRequest_whenHttpClientError_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", "{\"error\":\"invalid\"}".getBytes(), StandardCharsets.UTF_8));

        Map<String, Object> result = linkedInService.postText("Hello");

        assertThat(result.get("success")).isEqualTo(false);
    }

    @Test
    void executeRequest_whenException_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        Map<String, Object> result = linkedInService.postText("Hello");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Connection refused");
    }

    @Test
    void executeRequest_whenNoPostId_throwsException() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        ResponseEntity<Map> response = new ResponseEntity<>(Map.of(), HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = linkedInService.postText("Hello");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat((String) result.get("error")).contains("LinkedIn did not return post ID");
    }

    @Test
    void getUserProfile_returnsProfile() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");

        Map<String, Object> profileData = Map.of("name", "John Doe", "sub", "123");
        ResponseEntity<Map> response = new ResponseEntity<>(profileData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("profile")).isEqualTo(profileData);
    }

    @Test
    void getUserProfile_notAuthenticated_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated");
    }

    @Test
    void getUserProfile_whenException_returnsError() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("test-token");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("API error"));

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("API error");
    }
}
