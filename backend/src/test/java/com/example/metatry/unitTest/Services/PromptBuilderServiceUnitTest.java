package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.ContentPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderServiceUnitTest {

    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        promptBuilderService = new PromptBuilderService();
    }

    @Test
    void buildPrompt_containsTopic() {
        String result = promptBuilderService.buildPrompt("AI Marketing", "Users love AI", "Focus on automation", null);
        assertThat(result).contains("AI Marketing");
        assertThat(result).contains("Users love AI");
        assertThat(result).contains("Focus on automation");
    }

    @Test
    void buildPrompt_includesContentPrinciples() {
        String result = promptBuilderService.buildPrompt("Topic", "Insights", "Conclusion", null);
        assertThat(result).contains("VALUE FIRST");
        assertThat(result).contains("HOOK IN 3 SECONDS");
        assertThat(result).contains("AUTHENTIC VOICE");
    }

    @Test
    void buildPrompt_includesPlatformSections() {
        String result = promptBuilderService.buildPrompt("Topic", "Insights", "Conclusion", null);
        assertThat(result).contains("LINKEDIN");
        assertThat(result).contains("INSTAGRAM");
        assertThat(result).contains("FACEBOOK");
    }

    @Test
    void buildPrompt_includesForbiddenContent() {
        String result = promptBuilderService.buildPrompt("Topic", "Insights", "Conclusion", null);
        assertThat(result).contains("FORBIDDEN CONTENT");
        assertThat(result).contains("NEVER include");
    }

    @Test
    void buildPrompt_includesJsonOutputFormat() {
        String result = promptBuilderService.buildPrompt("Topic", "Insights", "Conclusion", null);
        assertThat(result).contains("\"linkedinTitle\"");
        assertThat(result).contains("\"imagePrompt\"");
    }

    @Test
    void buildPrompt_withPattern_includesPatternData() {
        ContentPattern pattern = ContentPattern.builder()
                .tone("Professional")
                .postFrequency("Daily")
                .contentLength("Medium")
                .mediaType("Image")
                .hashtagCount("5-8")
                .timingPattern("Morning")
                .ctaStyle("Question")
                .platformBreakdown("60% LinkedIn, 40% Instagram")
                .build();
        String result = promptBuilderService.buildPrompt("Topic", "Insights", "Conclusion", pattern);
        assertThat(result).contains("Professional");
        assertThat(result).contains("Daily");
        assertThat(result).contains("Medium");
        assertThat(result).contains("Image");
        assertThat(result).contains("5-8");
        assertThat(result).contains("Question");
        assertThat(result).contains("60% LinkedIn, 40% Instagram");
    }

    @Test
    void buildPrompt_withPatternAndPerformance_includesPerformanceData() {
        ContentPattern pattern = ContentPattern.builder()
                .tone("Casual")
                .avgEngagementScore(0.75)
                .totalPostsGenerated(50)
                .performanceAdvice("Use more questions")
                .build();
        String result = promptBuilderService.buildPrompt("Topic", "Insights", "Conclusion", pattern);
        assertThat(result).containsPattern("0[.,]75");
        assertThat(result).contains("HIGH");
        assertThat(result).contains("50");
        assertThat(result).contains("Use more questions");
    }

    @Test
    void buildPrompt_overload_callsThreeArgMethod() {
        String result = promptBuilderService.buildPrompt("Topic", "Insights", "Conclusion");
        assertThat(result).contains("Topic");
        assertThat(result).contains("Insights");
        assertThat(result).contains("Conclusion");
    }

    @Test
    void buildPlatformPrompt_includesPlatformName() {
        ContentPattern pattern = ContentPattern.builder().tone("Professional").build();
        String result = promptBuilderService.buildPlatformPrompt(
                "AI", "Insights", "Conclusion", pattern, PlatformType.LINKEDIN, 3);
        assertThat(result).contains("LINKEDIN");
        assertThat(result).contains("Generate exactly 3 posts for LINKEDIN");
    }

    @Test
    void buildPlatformPrompt_instagram_includesInstagramSection() {
        ContentPattern pattern = ContentPattern.builder().tone("Casual").build();
        String result = promptBuilderService.buildPlatformPrompt(
                "AI", "Insights", "Conclusion", pattern, PlatformType.INSTAGRAM, 2);
        assertThat(result).contains("INSTAGRAM");
        assertThat(result).contains("MAX: 220 characters");
    }

    @Test
    void buildPlatformPrompt_facebook_includesFacebookSection() {
        ContentPattern pattern = ContentPattern.builder().tone("Friendly").build();
        String result = promptBuilderService.buildPlatformPrompt(
                "AI", "Insights", "Conclusion", pattern, PlatformType.FACEBOOK, 1);
        assertThat(result).contains("FACEBOOK");
        assertThat(result).contains("MAX: 500 characters");
    }

    @Test
    void buildPlatformPrompt_withPerformanceData_includesEngagementLevel() {
        ContentPattern pattern = ContentPattern.builder()
                .tone("Professional")
                .avgEngagementScore(0.15)
                .build();
        String result = promptBuilderService.buildPlatformPrompt(
                "AI", "Insights", "Conclusion", pattern, PlatformType.LINKEDIN, 2);
        assertThat(result).contains("LOW").containsPattern("0[.,]15");
    }
}
