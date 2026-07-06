package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.PerformanceFeedbackService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PerformanceFeedbackServiceIntegrationTest {

    @MockitoBean
    private GeminiService geminiService;

    @Autowired
    private PerformanceFeedbackService performanceFeedbackService;

    @Autowired
    private ContentPatternRepository contentPatternRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    private Campaign campaign;
    private ContentPattern pattern;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(performanceFeedbackService, "lastFeedbackRun", LocalDateTime.now().minusDays(1));

        campaign = campaignRepository.save(
                Campaign.builder().name("PerfCamp").topic("technology")
                        .createdAt(LocalDateTime.now()).build());
        pattern = contentPatternRepository.save(
                ContentPattern.builder().topic("technology").campaignName("Tech Campaign").build());
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        contentPatternRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    @Test
    void updatePatterns_noNewEngagedPosts_returnsEarly() {
        Post p = Post.builder()
                .title("NoEng").content("Content").platform(com.example.metatry.Enums.PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).engagementScore(0.0).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        postRepository.save(p);

        performanceFeedbackService.updatePatternsFromPerformance();

        ContentPattern unchanged = contentPatternRepository.findById(pattern.getId()).orElseThrow();
        assertThat(unchanged.getAvgEngagementScore()).isNull();
    }

    @Test
    void updatePatterns_skipsPostsBeforeLastFeedbackRun() {
        Post oldPost = Post.builder()
                .title("Old").content("Old content").platform(com.example.metatry.Enums.PlatformType.LINKEDIN)
                .status(PostStatus.PUBLISHED).engagementScore(0.8).campaign(campaign)
                .publishedAt(LocalDateTime.now().minusDays(2)).build();
        postRepository.save(oldPost);

        performanceFeedbackService.updatePatternsFromPerformance();

        ContentPattern unchanged = contentPatternRepository.findById(pattern.getId()).orElseThrow();
        assertThat(unchanged.getAvgEngagementScore()).isNull();
    }

    @Test
    void updatePatterns_matchesPostsAndUpdatesPattern() {
        Post post = Post.builder()
                .title("Tech Post").content("About technology").platform(com.example.metatry.Enums.PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).engagementScore(0.75).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        postRepository.save(post);

        when(geminiService.generate(anyString())).thenReturn("Great performance advice");

        performanceFeedbackService.updatePatternsFromPerformance();

        ContentPattern updated = contentPatternRepository.findById(pattern.getId()).orElseThrow();
        assertThat(updated.getAvgEngagementScore()).isEqualTo(0.75);
        assertThat(updated.getTotalPostsGenerated()).isEqualTo(1);
        assertThat(updated.getPerformanceAdvice()).isEqualTo("Great performance advice");
        assertThat(updated.getLastPerformanceUpdate()).isNotNull();
    }

    @Test
    void updatePatterns_averagesEngagementAcrossMultiplePosts() {
        Post p1 = Post.builder().title("P1").content("C1").platform(com.example.metatry.Enums.PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).engagementScore(0.5).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        Post p2 = Post.builder().title("P2").content("C2").platform(com.example.metatry.Enums.PlatformType.LINKEDIN)
                .status(PostStatus.PUBLISHED).engagementScore(0.9).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        Post p3 = Post.builder().title("P3").content("C3").platform(com.example.metatry.Enums.PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).engagementScore(null).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        postRepository.saveAll(List.of(p1, p2, p3));

        when(geminiService.generate(anyString())).thenReturn("Average advice");

        performanceFeedbackService.updatePatternsFromPerformance();

        ContentPattern updated = contentPatternRepository.findById(pattern.getId()).orElseThrow();
        assertThat(updated.getAvgEngagementScore()).isEqualTo((0.5 + 0.9) / 2);
        assertThat(updated.getTotalPostsGenerated()).isEqualTo(2);
    }

    @Test
    void updatePatterns_handlesGeminiFailureGracefully() {
        Post post = Post.builder().title("GemFail").content("Content").platform(com.example.metatry.Enums.PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).engagementScore(0.6).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        postRepository.save(post);

        when(geminiService.generate(anyString())).thenThrow(new RuntimeException("Gemini down"));

        performanceFeedbackService.updatePatternsFromPerformance();

        ContentPattern updated = contentPatternRepository.findById(pattern.getId()).orElseThrow();
        assertThat(updated.getAvgEngagementScore()).isEqualTo(0.6);
        assertThat(updated.getPerformanceAdvice()).isEqualTo("Performance data collected but advice generation failed.");
    }

    @Test
    void updatePatterns_handlesNullTopicPatterns() {
        ContentPattern nullTopic = contentPatternRepository.save(
                ContentPattern.builder().topic(null).campaignName("Null").build());
        ContentPattern blankTopic = contentPatternRepository.save(
                ContentPattern.builder().topic("").campaignName("Blank").build());

        Post post = Post.builder().title("T").content("C").platform(com.example.metatry.Enums.PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).engagementScore(0.5).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        postRepository.save(post);

        when(geminiService.generate(anyString())).thenReturn("OK");

        performanceFeedbackService.updatePatternsFromPerformance();

        ContentPattern nullUpdated = contentPatternRepository.findById(nullTopic.getId()).orElseThrow();
        assertThat(nullUpdated.getAvgEngagementScore()).isEqualTo(0.0);
        assertThat(nullUpdated.getTotalPostsGenerated()).isZero();
    }

    @Test
    void updatePatterns_noPatterns_returnsEarly() {
        contentPatternRepository.deleteAll();

        Post post = Post.builder().title("T").content("C").platform(com.example.metatry.Enums.PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).engagementScore(0.5).campaign(campaign)
                .publishedAt(LocalDateTime.now()).build();
        postRepository.save(post);

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(geminiService, never()).generate(any());
    }

    @Test
    void getPatternsByPerformance_returnsSortedByEngagement() {
        ContentPattern low = contentPatternRepository.save(
                ContentPattern.builder().topic("low").avgEngagementScore(0.3).build());
        ContentPattern high = contentPatternRepository.save(
                ContentPattern.builder().topic("high").avgEngagementScore(0.9).build());
        ContentPattern none = contentPatternRepository.save(
                ContentPattern.builder().topic("none").avgEngagementScore(null).build());
        ContentPattern mid = contentPatternRepository.save(
                ContentPattern.builder().topic("mid").avgEngagementScore(0.6).build());

        List<ContentPattern> result = performanceFeedbackService.getPatternsByPerformance();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAvgEngagementScore()).isEqualTo(0.9);
        assertThat(result.get(1).getAvgEngagementScore()).isEqualTo(0.6);
        assertThat(result.get(2).getAvgEngagementScore()).isEqualTo(0.3);
    }

    @Test
    void getPatternsByPerformance_returnsEmptyWhenNoneHaveScore() {
        contentPatternRepository.deleteAll();
        contentPatternRepository.save(ContentPattern.builder().topic("noscore").avgEngagementScore(null).build());

        List<ContentPattern> result = performanceFeedbackService.getPatternsByPerformance();

        assertThat(result).isEmpty();
    }
}
