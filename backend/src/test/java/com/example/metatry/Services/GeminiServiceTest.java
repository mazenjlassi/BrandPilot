package com.example.metatry.Services;

import com.example.metatry.Config.GroqConfig;
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

    @Mock private GroqConfig groqConfig;
    @Mock private RestTemplate restTemplate;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(groqConfig, restTemplate);
    }

    @Test
    void generate_success_returnsCleanedJson() {
        when(groqConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of(
                                "content", "```json\n{\"result\": \"ok\"}\n```"
                        ))
                )
        );
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        String result = geminiService.generate("test prompt");

        assertThat(result).isEqualTo("{\"result\": \"ok\"}");
        verify(restTemplate).exchange(
                contains("api.groq.com"),
                eq(HttpMethod.POST),
                any(),
                eq(Map.class)
        );
    }

    @Test
    void generate_apiError_throwsRuntimeException() {
        when(groqConfig.getApiKey()).thenReturn("test-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> geminiService.generate("test"));
        assertThat(ex.getMessage()).contains("AI API failed");
    }

    @Test
    void generate_emptyResponse_throwsRuntimeException() {
        when(groqConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of("choices", List.of());
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> geminiService.generate("test"));
        assertThat(ex.getMessage()).contains("AI API failed");
    }

    @Test
    void generate_emptyContent_returnsEmpty() {
        when(groqConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", ""))
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
        when(groqConfig.getApiKey()).thenReturn("test-key");
        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content",
                                "```json\n[{\"id\": 1}, {\"id\": 2}]\n```"
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
