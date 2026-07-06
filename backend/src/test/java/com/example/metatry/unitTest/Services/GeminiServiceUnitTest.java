package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Config.GeminiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceUnitTest {

    @Mock
    private GeminiConfig geminiConfig;
    @Mock
    private RestTemplate restTemplate;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        when(geminiConfig.getApiKey()).thenReturn("fake-api-key");
        geminiService = new GeminiService(geminiConfig, restTemplate);
    }

    @Test
    void generate_returnsCleanedJson() {
        Map<String, Object> candidateContent = Map.of(
                "content", Map.of(
                        "parts", List.of(Map.of("text", "```json\n{\"key\": \"value\"}\n```"))
                )
        );
        Map<String, Object> responseBody = Map.of("candidates", List.of(candidateContent));
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        String result = geminiService.generate("Tell me something");

        assertThat(result).isEqualTo("{\"key\": \"value\"}");
    }

    @Test
    void generate_throwsException_onApiError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API unavailable"));

        assertThatThrownBy(() -> geminiService.generate("prompt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gemini API failed");
    }

    @Test
    void generate_throwsException_whenNoCandidates() {
        Map<String, Object> responseBody = Map.of("candidates", List.of());
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        assertThatThrownBy(() -> geminiService.generate("prompt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gemini API failed");
    }

    @Test
    void generate_handlesResponseWithoutCodeFences() {
        Map<String, Object> candidateContent = Map.of(
                "content", Map.of(
                        "parts", List.of(Map.of("text", "{\"result\": \"ok\"}"))
                )
        );
        Map<String, Object> responseBody = Map.of("candidates", List.of(candidateContent));
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        String result = geminiService.generate("Give JSON");

        assertThat(result).isEqualTo("{\"result\": \"ok\"}");
    }

    @Test
    void generate_handlesArrayResponse() {
        Map<String, Object> candidateContent = Map.of(
                "content", Map.of(
                        "parts", List.of(Map.of("text", "```\n[{\"a\": 1}]\n```"))
                )
        );
        Map<String, Object> responseBody = Map.of("candidates", List.of(candidateContent));
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        String result = geminiService.generate("Array JSON");

        assertThat(result).isEqualTo("[{\"a\": 1}]");
    }
}
