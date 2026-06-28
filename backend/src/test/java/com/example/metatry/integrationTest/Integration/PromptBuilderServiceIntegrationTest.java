package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Services.PromptBuilderService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PromptBuilderServiceIntegrationTest {

    @Autowired
    private PromptBuilderService promptBuilderService;

    // ================= buildPrompt (3-arg overload) =================

    @Test
    void buildPrompt_withoutPattern_containsTopics() {
        String prompt = promptBuilderService.buildPrompt("AI", "Insights text", "Conclusion text");

        assertThat(prompt).contains("AI");
        assertThat(prompt).contains("Insights text");
        assertThat(prompt).contains("Conclusion text");
        assertThat(prompt).contains("LINKEDIN");
        assertThat(prompt).contains("INSTAGRAM");
        assertThat(prompt).contains("FACEBOOK");
        assertThat(prompt).contains("OUTPUT FORMAT");
    }

    @Test
    void buildPrompt_withPattern_includesPatternData() {
        ContentPattern pattern = ContentPattern.builder()
                .tone("Technical").postFrequency("3x/week")
                .contentLength("200-400").mediaType("images")
                .hashtagCount("5-8").timingPattern("9am")
                .ctaStyle("Links").platformBreakdown("{\"linkedin\":2}")
                .avgEngagementScore(0.6)
                .totalPostsGenerated(10)
                .performanceAdvice("Use more questions")
                .build();

        String prompt = promptBuilderService.buildPrompt("AI", "Insights", "Conclusion", pattern);

        assertThat(prompt).contains("Technical");
        assertThat(prompt).contains("3x/week");
        assertThat(prompt).contains("200-400");
        assertThat(prompt).contains("images");
        assertThat(prompt).contains("5-8");
        assertThat(prompt).contains("9am");
        assertThat(prompt).contains("Links");
        assertThat(prompt).contains("HIGH");
        assertThat(prompt).contains("10");
        assertThat(prompt).contains("Use more questions");
    }

    @Test
    void buildPrompt_withNullPattern_omitsPatternSection() {
        String prompt = promptBuilderService.buildPrompt("AI", "Insights", "Conclusion", null);

        assertThat(prompt).doesNotContain("CONTENT PATTERN");
        assertThat(prompt).doesNotContain("PAST PERFORMANCE DATA");
    }

    @Test
    void buildPrompt_withPatternButNoPerformance_omitsPerformance() {
        ContentPattern pattern = ContentPattern.builder()
                .tone("Casual").build();

        String prompt = promptBuilderService.buildPrompt("AI", "Insights", "Conclusion", pattern);

        assertThat(prompt).contains("Casual");
        assertThat(prompt).doesNotContain("PAST PERFORMANCE DATA");
    }

    // ================= buildEstimatedPrompt =================

    @Test
    void buildEstimatedPrompt_containsEstimatedFormat() {
        String prompt = promptBuilderService.buildEstimatedPrompt("Security", "Insights", "Conclusion", null);

        assertThat(prompt).contains("You must decide how many posts");
        assertThat(prompt).contains("Security");
        assertThat(prompt).contains("Insights");
        assertThat(prompt).contains("\"platform\"");
    }

    @Test
    void buildEstimatedPrompt_withPattern_includesPatternData() {
        ContentPattern pattern = ContentPattern.builder()
                .tone("Formal").postFrequency("daily").build();

        String prompt = promptBuilderService.buildEstimatedPrompt("Security", "Insights", "Conclusion", pattern);

        assertThat(prompt).contains("Formal");
        assertThat(prompt).contains("daily");
    }

    // ================= buildPlatformPrompt =================

    @Test
    void buildPlatformPrompt_forLinkedIn_containsLinkedInOptimization() {
        ContentPattern pattern = ContentPattern.builder().tone("Technical").build();

        String prompt = promptBuilderService.buildPlatformPrompt(
                "AI", "Insights", "Conclusion", pattern, PlatformType.LINKEDIN, 3);

        assertThat(prompt).contains("PLATFORM OPTIMIZATION: LINKEDIN");
        assertThat(prompt).contains("Generate exactly 3 posts");
        assertThat(prompt).contains("Technical");
    }

    @Test
    void buildPlatformPrompt_forInstagram_containsInstagramOptimization() {
        String prompt = promptBuilderService.buildPlatformPrompt(
                "AI", "Insights", "Conclusion", null, PlatformType.INSTAGRAM, 1);

        assertThat(prompt).contains("PLATFORM OPTIMIZATION: INSTAGRAM");
        assertThat(prompt).contains("MAX: 220 characters");
    }

    @Test
    void buildPlatformPrompt_forFacebook_containsFacebookOptimization() {
        String prompt = promptBuilderService.buildPlatformPrompt(
                "AI", "Insights", "Conclusion", null, PlatformType.FACEBOOK, 2);

        assertThat(prompt).contains("PLATFORM OPTIMIZATION: FACEBOOK");
        assertThat(prompt).contains("MAX: 500 characters");
    }
}
