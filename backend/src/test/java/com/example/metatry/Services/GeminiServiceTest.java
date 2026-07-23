package com.example.metatry.Services;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock private GeminiConfig geminiConfig;
    @Mock private RestTemplate restTemplate;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(geminiConfig, restTemplate);
    }

    @Test
    void generate_success_returnsCleanedJson() {
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(Map.of("text", "```json\n{\"result\": \"ok\"}\n```"))
                        ))
                )
        );
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        String result = geminiService.generate("test prompt");

        assertThat(result).isEqualTo("{\"result\": \"ok\"}");
        verify(restTemplate).exchange(
                contains("gemini-2.0-flash:generateContent"),
                eq(HttpMethod.POST),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void generate_apiError_throwsRuntimeException() {
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> geminiService.generate("test"));
        assertThat(ex.getMessage()).contains("Gemini API failed");
    }

    @Test
    void generate_emptyCandidates_throwsRuntimeException() {
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of("candidates", List.of());
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> geminiService.generate("test"));
        assertThat(ex.getMessage()).contains("Failed to extract Gemini response");
    }

    @Test
    void generate_nullCandidates_throwsRuntimeException() {
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of();
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> geminiService.generate("test"));
        assertThat(ex.getMessage()).contains("Failed to extract Gemini response");
    }

    @Test
    void generate_malformedResponse_throwsRuntimeException() {
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(Map.of("content", Map.of()))
        );
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> geminiService.generate("test"));
        assertThat(ex.getMessage()).contains("Failed to extract");
    }

    @Test
    void generate_withEmptyText_returnsEmpty() {
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(Map.of("text", ""))
                        ))
                )
        );
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        String result = geminiService.generate("test");
        assertThat(result).isEqualTo("");
    }

    @Test
    void generate_withArrayResult_cleansCorrectly() {
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(Map.of("text",
                                        "```json\n[{\"id\": 1}, {\"id\": 2}]\n```"))
                        ))
                )
        );
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        String result = geminiService.generate("test");
        assertThat(result).isEqualTo("[{\"id\": 1}, {\"id\": 2}]");
    }
}
