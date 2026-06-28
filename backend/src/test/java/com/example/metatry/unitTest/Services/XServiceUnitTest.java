package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Config.XConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class XServiceUnitTest {

    private XService xService;

    @BeforeEach
    void setUp() {
        XConfig.XCredentials credentials = new XConfig.XCredentials(
                "test-api-key", "test-api-secret",
                "test-access-token", "test-access-token-secret"
        );
        xService = new XService(credentials);
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
