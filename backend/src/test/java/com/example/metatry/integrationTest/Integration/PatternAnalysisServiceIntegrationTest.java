package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.DTOs.PatternResponse;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.MemoryContextService;
import com.example.metatry.Services.PatternAnalysisService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PatternAnalysisServiceIntegrationTest {

    @MockitoBean
    private GeminiService geminiService;

    @MockitoBean
    private MemoryContextService memoryContextService;

    @Autowired
    private PatternAnalysisService patternAnalysisService;

    @Autowired
    private ScrapedPostRepository scrapedPostRepository;

    @Autowired
    private ContentPatternRepository contentPatternRepository;

    @AfterEach
    void tearDown() {
        contentPatternRepository.deleteAll();
        scrapedPostRepository.deleteAll();
    }

    // ================= analyzePattern =================

    @Test
    void analyzePattern_withEnoughPosts_returnsSuccess() {
        for (int i = 0; i < 5; i++) {
            scrapedPostRepository.save(ScrapedPost.builder()
                    .companyName("TestCorp").platform("linkedin")
                    .postText("Post " + i).build());
        }
        when(geminiService.generate(anyString())).thenReturn("""
                {"postFrequency": "3x/week", "contentLength": "200-400 chars",
                 "mediaType": "images", "hashtagCount": "5", "timingPattern": "9am",
                 "tone": "Technical", "ctaStyle": "Links"}
                """);
        when(memoryContextService.getRecentContext()).thenReturn("Memory context");

        PatternAnalysisRequest request = PatternAnalysisRequest.builder()
                .topic("AI").platform("linkedin").companyName("TestCorp").build();
        PatternResponse response = patternAnalysisService.analyzePattern(request);

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getTopic()).isEqualTo("AI");
        assertThat(response.getTone()).isEqualTo("Technical");
    }

    @Test
    void analyzePattern_notEnoughPosts_returnsError() {
        scrapedPostRepository.save(ScrapedPost.builder()
                .companyName("TestCorp").platform("linkedin")
                .postText("Only one").build());

        PatternAnalysisRequest request = PatternAnalysisRequest.builder()
                .topic("AI").companyName("TestCorp").build();
        PatternResponse response = patternAnalysisService.analyzePattern(request);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).contains("Not enough posts");
    }

    @Test
    void analyzePattern_geminiFailure_returnsError() {
        for (int i = 0; i < 4; i++) {
            scrapedPostRepository.save(ScrapedPost.builder()
                    .companyName("TestCorp").platform("linkedin")
                    .postText("Post " + i).build());
        }
        when(geminiService.generate(anyString())).thenThrow(new RuntimeException("API down"));

        PatternAnalysisRequest request = PatternAnalysisRequest.builder()
                .topic("AI").companyName("TestCorp").build();
        PatternResponse response = patternAnalysisService.analyzePattern(request);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).contains("API down");
    }

    // ================= getAllPatterns =================

    @Test
    void getAllPatterns_returnsAll() {
        contentPatternRepository.save(ContentPattern.builder().topic("AI").build());
        contentPatternRepository.save(ContentPattern.builder().topic("Security").build());

        List<ContentPattern> patterns = patternAnalysisService.getAllPatterns();
        assertThat(patterns).hasSize(2);
    }

    // ================= getPatternsByCompany =================

    @Test
    void getPatternsByCompany_filtersByCompany() {
        contentPatternRepository.save(ContentPattern.builder().companyName("C1").topic("T1").build());
        contentPatternRepository.save(ContentPattern.builder().companyName("C2").topic("T2").build());

        List<ContentPattern> result = patternAnalysisService.getPatternsByCompany("C1");
        assertThat(result).hasSize(1);
    }

    // ================= getPatternByTopic =================

    @Test
    void getPatternByTopic_found() {
        contentPatternRepository.save(ContentPattern.builder().topic("AI").build());

        ContentPattern found = patternAnalysisService.getPatternByTopic("AI");
        assertThat(found).isNotNull();
    }

    @Test
    void getPatternByTopic_notFound() {
        ContentPattern found = patternAnalysisService.getPatternByTopic("nonexistent");
        assertThat(found).isNull();
    }

    // ================= findMatchingPatterns =================

    @Test
    void findMatchingPatterns_exactMatch() {
        contentPatternRepository.save(ContentPattern.builder().topic("cloud computing").build());

        List<ContentPattern> matches = patternAnalysisService.findMatchingPatterns("cloud computing");
        assertThat(matches).isNotEmpty();
    }

    @Test
    void findMatchingPatterns_keywordMatch() {
        contentPatternRepository.save(ContentPattern.builder().topic("Cloud Security").build());

        List<ContentPattern> matches = patternAnalysisService.findMatchingPatterns("security");
        assertThat(matches).isNotEmpty();
    }

    @Test
    void findMatchingPatterns_noMatch() {
        List<ContentPattern> matches = patternAnalysisService.findMatchingPatterns("nonexistent");
        assertThat(matches).isEmpty();
    }
}
