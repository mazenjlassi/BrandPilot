package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Config.LinkedInConfig;
import com.example.metatry.Services.LinkedInTokenService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class LinkedInTokenServiceIntegrationTest {

    @Autowired
    private LinkedInTokenService linkedInTokenService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private LinkedInConfig.LinkedInAuthProperties authProps;

    @Test
    void exchangeAuthorizationCode_setsTokenAndUrn() {
        when(restTemplate.exchange(
                eq("https://www.linkedin.com/oauth/v2/accessToken"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(Map.class)
        )).thenReturn(new org.springframework.http.ResponseEntity<>(
                Map.of("access_token", "test-token", "expires_in", 3600),
                org.springframework.http.HttpStatus.OK));

        when(restTemplate.exchange(
                eq("https://api.linkedin.com/v2/userinfo"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenReturn(new org.springframework.http.ResponseEntity<>(
                Map.of("sub", "abc123"),
                org.springframework.http.HttpStatus.OK));

        linkedInTokenService.exchangeAuthorizationCode("test-code");

        assertThat(linkedInTokenService.getAccessToken()).isEqualTo("test-token");
        assertThat(linkedInTokenService.getPersonUrn()).isEqualTo("urn:li:person:abc123");
        assertThat(linkedInTokenService.isAuthenticated()).isTrue();
    }

    @Test
    void exchangeAuthorizationCode_fetchUserInfoFails_throws() {
        when(restTemplate.exchange(
                eq("https://www.linkedin.com/oauth/v2/accessToken"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(Map.class)
        )).thenReturn(new org.springframework.http.ResponseEntity<>(
                Map.of("access_token", "test-token", "expires_in", 3600),
                org.springframework.http.HttpStatus.OK));

        when(restTemplate.exchange(
                eq("https://api.linkedin.com/v2/userinfo"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(Map.class)
        )).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> linkedInTokenService.exchangeAuthorizationCode("test-code"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erreur lors de la récupération du profil");
    }

    @Test
    void getAccessToken_expired_throws() {
        assertThatThrownBy(() -> linkedInTokenService.getAccessToken())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token LinkedIn expiré");
    }

    @Test
    void isAuthenticated_noToken_returnsFalse() {
        assertThat(linkedInTokenService.isAuthenticated()).isFalse();
    }

    @Test
    void getAuthorizationUrl_containsClientId() {
        String url = linkedInTokenService.getAuthorizationUrl();

        assertThat(url).contains("client_id=" + authProps.clientId());
        assertThat(url).contains("redirect_uri=" + authProps.redirectUri());
        assertThat(url).contains("response_type=code");
        assertThat(url).contains("w_member_social");
    }
}
