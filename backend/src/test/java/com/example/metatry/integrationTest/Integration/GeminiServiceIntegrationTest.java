package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Config.GeminiConfig;
import com.example.metatry.Exceptions.GeminiUnavailableException;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class GeminiServiceIntegrationTest {

    @Autowired
    private GeminiService geminiService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private GeminiConfig geminiConfig;

    @Test
    void generate_validResponse_returnsText() {
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(
                                        Map.of("text", "```json\n{\"key\": \"value\"}\n```")
                                )
                        ))
                )
        );

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        String result = geminiService.generate("Test prompt");

        assertThat(result).contains("{\"key\": \"value\"}");
    }

    @Test
    void generate_withoutJsonMarkdown_returnsCleanText() {
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(
                                        Map.of("text", "{\"result\": \"ok\"}")
                                )
                        ))
                )
        );

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        String result = geminiService.generate("Test");

        assertThat(result).isEqualTo("{\"result\": \"ok\"}");
    }

    @Test
    void generate_withArrayResponse_cleansProperly() {
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(
                                        Map.of("text", "```json\n[{\"a\": 1}, {\"a\": 2}]\n```")
                                )
                        ))
                )
        );

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        String result = geminiService.generate("Test");

        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]");
    }

    @Test
    void generate_noCandidates_throws() {
        Map<String, Object> responseBody = Map.of();

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        assertThatThrownBy(() -> geminiService.generate("Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gemini API failed");
    }

    @Test
    void generate_httpServerError_throwsGeminiUnavailable() {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(
                        HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                        "{\"error\": \"overloaded\"}".getBytes(),
                        StandardCharsets.UTF_8));

        assertThatThrownBy(() -> geminiService.generate("Test"))
                .isInstanceOf(GeminiUnavailableException.class)
                .hasMessageContaining("AI generation service is temporarily unavailable");
    }

    @Test
    void generate_otherException_throwsRuntime() {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> geminiService.generate("Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");
    }
}
