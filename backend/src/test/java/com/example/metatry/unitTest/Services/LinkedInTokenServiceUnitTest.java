package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Config.LinkedInConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LinkedInTokenServiceUnitTest {

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
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> tokenService.getAccessToken()
        );
    }
}
