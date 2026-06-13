package com.example.metatry.Services;

import com.example.metatry.Config.LinkedInConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkedInTokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private LinkedInTokenService tokenService;
    private LinkedInConfig.LinkedInAuthProperties authProps;

    @BeforeEach
    void setUp() {
        authProps = new LinkedInConfig.LinkedInAuthProperties(
                "test-client-id", "test-client-secret", "https://test.com/callback"
        );
        tokenService = new LinkedInTokenService(restTemplate, authProps);
    }

    @Test
    void getAuthorizationUrl_containsClientIdAndRedirectUri() {
        String url = tokenService.getAuthorizationUrl();

        assertThat(url).startsWith("https://www.linkedin.com/oauth/v2/authorization?");
        assertThat(url).contains("response_type=code");
        assertThat(url).contains("client_id=test-client-id");
        assertThat(url).contains("redirect_uri=https://test.com/callback");
        assertThat(url).contains("scope=openid%20profile%20w_member_social");
    }

    @Test
    void isAuthenticated_whenNotInitialized_returnsFalse() {
        assertThat(tokenService.isAuthenticated()).isFalse();
    }

    @Test
    void getAccessToken_whenNotInitialized_throwsException() {
        assertThrows(RuntimeException.class, () -> tokenService.getAccessToken());
    }

    @Test
    void getPersonUrn_whenNotInitialized_returnsNull() {
        assertThat(tokenService.getPersonUrn()).isNull();
    }

    @Test
    void exchangeAuthorizationCode_setsTokenAndPersonUrn() {
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "test-access-token",
                "expires_in", 3600
        );
        Map<String, Object> userInfoResponse = Map.of("sub", "user123");

        when(restTemplate.exchange(
                eq("https://www.linkedin.com/oauth/v2/accessToken"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                eq("https://api.linkedin.com/v2/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(userInfoResponse, HttpStatus.OK));

        tokenService.exchangeAuthorizationCode("authcode");

        assertThat(tokenService.getAccessToken()).isEqualTo("test-access-token");
        assertThat(tokenService.getPersonUrn()).isEqualTo("urn:li:person:user123");
        assertThat(tokenService.isAuthenticated()).isTrue();
    }

    @Test
    void exchangeAuthorizationCode_whenUserInfoFails_throwsException() {
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "test-access-token",
                "expires_in", 3600
        );

        when(restTemplate.exchange(
                eq("https://www.linkedin.com/oauth/v2/accessToken"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                eq("https://api.linkedin.com/v2/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RuntimeException("API error"));

        assertThrows(RuntimeException.class, () -> tokenService.exchangeAuthorizationCode("authcode"));
    }

    @Test
    void isAuthenticated_whenTokenExpired_returnsFalse() throws InterruptedException {
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "test-token",
                "expires_in", 0
        );
        Map<String, Object> userInfoResponse = Map.of("sub", "user1");

        when(restTemplate.exchange(
                eq("https://www.linkedin.com/oauth/v2/accessToken"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(
                eq("https://api.linkedin.com/v2/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(userInfoResponse, HttpStatus.OK));

        tokenService.exchangeAuthorizationCode("code");
        Thread.sleep(100);

        assertThat(tokenService.isAuthenticated()).isFalse();
    }

    @Test
    void getAccessToken_whenExpired_throwsException() throws InterruptedException {
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "test-token",
                "expires_in", 0
        );
        Map<String, Object> userInfoResponse = Map.of("sub", "user1");

        when(restTemplate.exchange(
                eq("https://www.linkedin.com/oauth/v2/accessToken"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(
                eq("https://api.linkedin.com/v2/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(userInfoResponse, HttpStatus.OK));

        tokenService.exchangeAuthorizationCode("code");
        Thread.sleep(100);

        assertThrows(RuntimeException.class, () -> tokenService.getAccessToken());
    }
}
