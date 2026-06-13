package com.example.metatry.Controllers;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.DTOs.PatternResponse;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.PatternAnalysisService;
import com.example.metatry.Services.PerformanceFeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatternController.class)
class PatternControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PatternAnalysisService patternAnalysisService;

    @MockitoBean
    private PerformanceFeedbackService performanceFeedbackService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void analyzePattern_returnsResponse() throws Exception {
        PatternResponse response = PatternResponse.builder()
                .status("success").topic("AI").build();
        when(patternAnalysisService.analyzePattern(any())).thenReturn(response);

        mockMvc.perform(post("/api/patterns/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("topic", "AI", "companyName", "NVIDIA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void analyzePattern_missingTopic_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/patterns/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("topic", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void analyzeBatch_returnsOk() throws Exception {
        when(patternAnalysisService.analyzeUnanalyzedBatch("NVIDIA")).thenReturn(3);

        mockMvc.perform(post("/api/patterns/analyze-batch")
                        .param("companyName", "NVIDIA"))
                .andExpect(status().isOk())
                .andExpect(content().string("Saved 3 patterns from batch for NVIDIA"));
    }

    @Test
    void getAllPatterns_returnsList() throws Exception {
        when(patternAnalysisService.getAllPatterns()).thenReturn(List.of(new ContentPattern()));

        mockMvc.perform(get("/api/patterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllPatterns_withCompany_returnsFiltered() throws Exception {
        when(patternAnalysisService.getPatternsByCompany("NVIDIA"))
                .thenReturn(List.of(ContentPattern.builder().topic("AI").build()));

        mockMvc.perform(get("/api/patterns")
                        .param("companyName", "NVIDIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].topic").value("AI"));
    }

    @Test
    void getPatternByTopic_returnsPattern() throws Exception {
        ContentPattern pattern = ContentPattern.builder().topic("AI").build();
        when(patternAnalysisService.getPatternByTopic("AI")).thenReturn(pattern);

        mockMvc.perform(get("/api/patterns/AI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("AI"));
    }

    @Test
    void getPatternByTopic_notFound_returns404() throws Exception {
        when(patternAnalysisService.getPatternByTopic("Unknown")).thenReturn(null);

        mockMvc.perform(get("/api/patterns/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void matchPattern_returnsMatches() throws Exception {
        when(patternAnalysisService.findMatchingPatterns("Machine Learning"))
                .thenReturn(List.of(ContentPattern.builder().topic("ML").build()));

        mockMvc.perform(get("/api/patterns/match")
                        .param("topic", "Machine Learning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].topic").value("ML"));
    }

    @Test
    void matchPattern_noMatches_returns404() throws Exception {
        when(patternAnalysisService.findMatchingPatterns("Unknown")).thenReturn(List.of());

        mockMvc.perform(get("/api/patterns/match")
                        .param("topic", "Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPatternsByPerformance_returnsList() throws Exception {
        when(performanceFeedbackService.getPatternsByPerformance())
                .thenReturn(List.of(new ContentPattern()));

        mockMvc.perform(get("/api/patterns/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void runFeedback_returnsOk() throws Exception {
        mockMvc.perform(post("/api/patterns/feedback/run"))
                .andExpect(status().isOk())
                .andExpect(content().string("Performance feedback executed"));
    }
}
