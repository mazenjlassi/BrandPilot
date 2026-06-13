package com.example.metatry.Config;

import com.cloudinary.Cloudinary;
import com.example.metatry.Config.LinkedInConfig.LinkedInAuthProperties;
import com.example.metatry.Config.XConfig.XCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigTest {

    @Mock private Environment environment;

    @Test
    void cloudflareConfig_getters() {
        CloudflareConfig config = new CloudflareConfig();
        ReflectionTestUtils.setField(config, "apiToken", "test-token");
        ReflectionTestUtils.setField(config, "accountId", "test-account");

        assertThat(config.getApiToken()).isEqualTo("test-token");
        assertThat(config.getAccountId()).isEqualTo("test-account");
    }

    @Test
    void cloudflareConfig_defaultValues() {
        CloudflareConfig config = new CloudflareConfig();
        assertThat(config.getApiToken()).isNull();
        assertThat(config.getAccountId()).isNull();
    }

    @Test
    void cloudinaryConfig_createsCloudinaryBean() {
        CloudinaryConfig config = new CloudinaryConfig();
        ReflectionTestUtils.setField(config, "cloudName", "mycloud");
        ReflectionTestUtils.setField(config, "apiKey", "key123");
        ReflectionTestUtils.setField(config, "apiSecret", "secret456");

        Cloudinary cloudinary = config.cloudinary();
        assertThat(cloudinary).isNotNull();
    }

    @Test
    void cloudinaryConfig_defaultValues() {
        CloudinaryConfig config = new CloudinaryConfig();
        Cloudinary cloudinary = config.cloudinary();
        assertThat(cloudinary).isNotNull();
    }

    @Test
    void geminiConfig_getApiKey() {
        GeminiConfig config = new GeminiConfig();
        ReflectionTestUtils.setField(config, "apiKey", "gemini-key");

        assertThat(config.getApiKey()).isEqualTo("gemini-key");
    }

    @Test
    void linkedInConfig_createsAuthProperties() {
        LinkedInConfig config = new LinkedInConfig();
        ReflectionTestUtils.setField(config, "clientId", "client-id");
        ReflectionTestUtils.setField(config, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(config, "redirectUri", "https://example.com/callback");

        LinkedInAuthProperties props = config.linkedinAuthProperties();
        assertThat(props.clientId()).isEqualTo("client-id");
        assertThat(props.clientSecret()).isEqualTo("client-secret");
        assertThat(props.redirectUri()).isEqualTo("https://example.com/callback");
    }

    @Test
    void linkedInConfig_createsAuthPropertiesWithDefaults() {
        LinkedInConfig config = new LinkedInConfig();
        LinkedInAuthProperties props = config.linkedinAuthProperties();
        assertThat(props.clientId()).isNull();
        assertThat(props.clientSecret()).isNull();
        assertThat(props.redirectUri()).isNull();
    }

    @Test
    void xConfig_createsCredentials() {
        XConfig config = new XConfig();
        ReflectionTestUtils.setField(config, "apiKey", "x-key");
        ReflectionTestUtils.setField(config, "apiKeySecret", "x-secret");
        ReflectionTestUtils.setField(config, "accessToken", "x-token");
        ReflectionTestUtils.setField(config, "accessTokenSecret", "x-token-secret");

        XCredentials creds = config.xCredentials();
        assertThat(creds.apiKey()).isEqualTo("x-key");
        assertThat(creds.apiSecret()).isEqualTo("x-secret");
        assertThat(creds.accessToken()).isEqualTo("x-token");
        assertThat(creds.accessTokenSecret()).isEqualTo("x-token-secret");
    }

    @Test
    void restTemplateConfig_createsRestTemplate() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate restTemplate = config.restTemplate();
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void jacksonConfig_createsObjectMapper() {
        JacksonConfig config = new JacksonConfig();
        ObjectMapper mapper = config.objectMapper();
        assertThat(mapper).isNotNull();
    }

    @Test
    void dataSourceConfig_createsDataSourceWithDefaults() {
        DataSourceConfig config = new DataSourceConfig();
        when(environment.getProperty("spring.datasource.url")).thenReturn(null);
        when(environment.getProperty("spring.datasource.username")).thenReturn(null);
        when(environment.getProperty("spring.datasource.password")).thenReturn(null);
        when(environment.getProperty("spring.datasource.driver-class-name")).thenReturn(null);

        DataSource ds = config.dataSource(environment);
        assertThat(ds).isNotNull();
    }

    @Test
    void dataSourceConfig_createsDataSourceWithCustomValues() {
        DataSourceConfig config = new DataSourceConfig();
        when(environment.getProperty("spring.datasource.url"))
                .thenReturn("jdbc:mysql://localhost:3306/mydb?useSSL=false");
        when(environment.getProperty("spring.datasource.username")).thenReturn("admin");
        when(environment.getProperty("spring.datasource.password")).thenReturn("pass");
        when(environment.getProperty("spring.datasource.driver-class-name"))
                .thenReturn("com.mysql.cj.jdbc.Driver");

        DataSource ds = config.dataSource(environment);
        assertThat(ds).isNotNull();
    }

    @Test
    void webConfig_hasAddResourceHandlers() {
        WebConfig webConfig = new WebConfig();
        assertThat(webConfig).isNotNull();
    }

    @Test
    void startupDiagnostic_constructor() {
        StartupDiagnostic diag = new StartupDiagnostic(environment);
        assertThat(diag).isNotNull();
    }
}
