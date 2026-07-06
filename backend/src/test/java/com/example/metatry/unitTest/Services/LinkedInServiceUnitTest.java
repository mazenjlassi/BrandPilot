package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

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
class LinkedInServiceUnitTest {

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
    void postText_returnsSuccess_whenAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-RestLi-Id", "urn:li:ugcPost:456");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(Map.of(), responseHeaders, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = linkedInService.postText("Hello LinkedIn");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("postId")).isEqualTo("urn:li:ugcPost:456");
        assertThat(result.get("linkedinUrl")).isEqualTo("https://www.linkedin.com/feed/update/urn:li:ugcPost:456");
    }

    @Test
    void postText_returnsError_whenNotAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.postText("Hello");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated with LinkedIn");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void postArticleWithImage_returnsSuccess_whenAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-RestLi-Id", "urn:li:ugcPost:789");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(Map.of(), responseHeaders, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = linkedInService.postArticleWithImage("Check this", "https://img.url", "My Title");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    void postArticleWithVideo_returnsSuccess_whenAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-RestLi-Id", "urn:li:ugcPost:101");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(Map.of(), responseHeaders, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = linkedInService.postArticleWithVideo("Video post", "https://video.url", "Video");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    void postText_returnsError_whenPostIdMissing() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(Map.of(), responseHeaders, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = linkedInService.postText("No ID");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("LinkedIn did not return post ID");
    }

    @Test
    void postText_returnsError_onHttpClientError() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad request",
                        "{error:invalid}".getBytes(), StandardCharsets.UTF_8));

        Map<String, Object> result = linkedInService.postText("Error post");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isNotNull();
    }

    @Test
    void postText_returnsError_onGenericException() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");
        when(tokenService.getPersonUrn()).thenReturn("urn:li:person:123");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Network failure"));

        Map<String, Object> result = linkedInService.postText("Network fail");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Network failure");
    }

    @Test
    void getUserProfile_returnsProfile_whenAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");

        Map<String, Object> profileData = Map.of("sub", "abc123", "name", "John");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(profileData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("profile")).isEqualTo(profileData);
    }

    @Test
    void getUserProfile_returnsError_whenNotAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated");
    }

    @Test
    void getUserProfile_returnsError_onException() {
        when(tokenService.isAuthenticated()).thenReturn(true);
        when(tokenService.getAccessToken()).thenReturn("fake-token");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Profile fetch failed"));

        Map<String, Object> result = linkedInService.getUserProfile();

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Profile fetch failed");
    }

    @Test
    void escape_handlesNullInput() {
        String result = linkedInService.postArticleWithImage("test", "https://img.url", "null").toString();
        assertThat(result).contains("success");
    }

    @Test
    void postArticleWithImage_returnsError_whenNotAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.postArticleWithImage("text", "https://img.url", "title");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated with LinkedIn");
    }

    @Test
    void postArticleWithVideo_returnsError_whenNotAuthenticated() {
        when(tokenService.isAuthenticated()).thenReturn(false);

        Map<String, Object> result = linkedInService.postArticleWithVideo("text", "https://vid.url", "title");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Not authenticated with LinkedIn");
    }
}
