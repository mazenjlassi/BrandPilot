package com.example.metatry.Services;

import com.example.metatry.Config.XConfig;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XServiceTest {

    @Mock
    private OAuth10aService oAuthService;

    private XService xService;

    @BeforeEach
    void setUp() throws Exception {
        XConfig.XCredentials credentials = new XConfig.XCredentials(
                "test-api-key", "test-api-secret",
                "test-access-token", "test-access-token-secret"
        );
        xService = new XService(credentials);

        Field serviceField = XService.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(xService, oAuthService);

        Field tokenField = XService.class.getDeclaredField("accessToken");
        tokenField.setAccessible(true);
        tokenField.set(xService, new OAuth1AccessToken("test-token", "test-secret"));
    }

    @Test
    void postText_returnsSuccess() throws Exception {
        String jsonBody = "{\"id_str\": \"123456789\"}";
        Response response = new Response(200, "OK", Map.of(), jsonBody);
        when(oAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        Map<String, Object> result = xService.postText("Hello X");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("tweetId")).isEqualTo("123456789");
    }

    @Test
    void postText_whenNon200_returnsError() throws Exception {
        Response response = new Response(403, "Forbidden", Map.of(), "Rate limit exceeded");
        when(oAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        Map<String, Object> result = xService.postText("Hello X");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat((String) result.get("error")).contains("403");
    }

    @Test
    void postText_whenException_returnsError() throws Exception {
        when(oAuthService.execute(any(OAuthRequest.class))).thenThrow(new RuntimeException("Network error"));

        Map<String, Object> result = xService.postText("Hello X");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Network error");
    }

    @Test
    void test_returnsSuccess() throws Exception {
        Response response = new Response(200, "OK", Map.of(), "{}");
        when(oAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        Map<String, Object> result = xService.test();

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    void test_whenException_returnsError() throws Exception {
        when(oAuthService.execute(any(OAuthRequest.class))).thenThrow(new RuntimeException("Network error"));

        Map<String, Object> result = xService.test();

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isEqualTo("Network error");
    }

    @Test
    void getLimits_returnsConstantMap() {
        Map<String, Object> limits = xService.getLimits();

        assertThat(limits.get("success")).isEqualTo(true);
        assertThat(limits.get("maxTweetLength")).isEqualTo(280);
        assertThat(limits.get("postingEndpoint")).isEqualTo("v1.1/statuses/update.json");
        assertThat(limits.get("authentication")).isEqualTo("OAuth 1.0a");
    }

    @Test
    void health_returnsUpStatus() {
        Map<String, Object> health = xService.health();

        assertThat(health.get("status")).isEqualTo("UP");
        assertThat(health.get("service")).isEqualTo("X Service (v1.1 + OAuth1.0a)");
        assertThat(health).containsKey("timestamp");
    }
}
