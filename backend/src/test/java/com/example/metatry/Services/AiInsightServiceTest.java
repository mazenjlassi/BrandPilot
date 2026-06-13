package com.example.metatry.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiInsightServiceTest {

    @Mock private GeminiService geminiService;
    @Mock private MemoryContextService memoryContextService;

    @InjectMocks
    private AiInsightService aiInsightService;

    @Test
    void analyzeComments_joinsCommentsAndCallsGemini() {
        when(memoryContextService.getRecentContext()).thenReturn("memory context");
        when(geminiService.generate(anyString())).thenReturn("{\"result\": \"ok\"}");

        String result = aiInsightService.analyzeComments(List.of("Great product", "Love it", "Needs improvement"));

        assertThat(result).isEqualTo("{\"result\": \"ok\"}");
        verify(geminiService).generate(argThat(prompt ->
                prompt.contains("Great product") &&
                prompt.contains("Love it") &&
                prompt.contains("Needs improvement") &&
                prompt.contains("memory context")
        ));
    }

    @Test
    void analyzeComments_withSingleComment_returnsInsight() {
        when(memoryContextService.getRecentContext()).thenReturn("");
        when(geminiService.generate(anyString())).thenReturn("{\"summary\": \"Good\"}");

        String result = aiInsightService.analyzeComments(List.of("Amazing"));

        assertThat(result).contains("Good");
        verify(geminiService).generate(argThat(prompt -> prompt.contains("Amazing")));
    }
}
