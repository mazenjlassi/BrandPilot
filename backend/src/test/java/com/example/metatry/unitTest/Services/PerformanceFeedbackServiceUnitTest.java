package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceFeedbackServiceUnitTest {

    @Mock
    private ContentPatternRepository contentPatternRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private GeminiService geminiService;

    private PerformanceFeedbackService performanceFeedbackService;

    @Captor
    private ArgumentCaptor<ContentPattern> patternCaptor;

    @BeforeEach
    void setUp() {
        performanceFeedbackService = new PerformanceFeedbackService(contentPatternRepository, postRepository, geminiService);
    }

    private Campaign campaign(String topic) {
        return Campaign.builder().topic(topic).build();
    }

    private Post publishedPost(Long id, Double engagement, Campaign campaign, LocalDateTime publishedAt) {
        return Post.builder()
                .id(id)
                .engagementScore(engagement)
                .campaign(campaign)
                .publishedAt(publishedAt)
                .status(PostStatus.PUBLISHED)
                .content("Test content for post " + id)
                .build();
    }

    private ContentPattern pattern(String topic, String campaignName) {
        return ContentPattern.builder()
                .topic(topic)
                .campaignName(campaignName)
                .build();
    }

    @Test
    void updatePatterns_noNewEngagedPosts_returnsEarly() {
        Post p = Post.builder().status(PostStatus.PUBLISHED).engagementScore(0.0).build();
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(p));

        performanceFeedbackService.updatePatternsFromPerformance();

        verifyNoInteractions(contentPatternRepository);
        verifyNoInteractions(geminiService);
    }

    @Test
    void updatePatterns_noMatchingPatterns_returnsEarly() {
        Campaign camp = campaign("tech");
        Post p = publishedPost(1L, 0.5, camp, LocalDateTime.now());
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(p));
        when(contentPatternRepository.findAll()).thenReturn(List.of());

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(geminiService, never()).generate(any());
        verify(contentPatternRepository, never()).save(any());
    }

    @Test
    void updatePatterns_skipsPostsBeforeLastFeedbackRun() {
        Campaign camp = campaign("tech");
        Post oldPost = publishedPost(1L, 0.5, camp, LocalDateTime.now().minusDays(2));
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(oldPost));

        performanceFeedbackService.updatePatternsFromPerformance();

        verifyNoInteractions(contentPatternRepository);
    }

    @Test
    void updatePatterns_matchesPostsByCampaignTopic() {
        Campaign camp = campaign("technology");
        Post p = publishedPost(1L, 0.8, camp, LocalDateTime.now());
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(p));

        ContentPattern pattern = pattern("technology", "Tech Campaign");
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(any())).thenReturn("Keep up the good work");

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository, times(1)).save(patternCaptor.capture());
        ContentPattern saved = patternCaptor.getValue();
        assertThat(saved.getAvgEngagementScore()).isEqualTo(0.8);
        assertThat(saved.getTotalPostsGenerated()).isEqualTo(1);
        assertThat(saved.getPerformanceAdvice()).isEqualTo("Keep up the good work");
        assertThat(saved.getLastPerformanceUpdate()).isNotNull();
    }

    @Test
    void updatePatterns_matchesPostsBySubstring() {
        Campaign camp = campaign("The future of AI technology");
        Post p = publishedPost(1L, 0.9, camp, LocalDateTime.now());
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(p));

        ContentPattern pattern = pattern("technology", "AI Campaign");
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(any())).thenReturn("Advice");

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository).save(patternCaptor.capture());
        assertThat(patternCaptor.getValue().getAvgEngagementScore()).isEqualTo(0.9);
    }

    @Test
    void updatePatterns_skipsNullTopicPatterns() {
        Campaign camp = campaign("tech");
        Post p = publishedPost(1L, 0.7, camp, LocalDateTime.now());
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(p));

        ContentPattern nullTopic = pattern(null, "Null Topic");
        ContentPattern blankTopic = ContentPattern.builder().topic("").build();
        ContentPattern validPattern = pattern("tech", "Tech Pattern");
        when(contentPatternRepository.findAll()).thenReturn(List.of(nullTopic, blankTopic, validPattern));
        when(geminiService.generate(any())).thenReturn("Good");

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository, times(3)).save(patternCaptor.capture());
        assertThat(patternCaptor.getValue().getTopic()).isEqualTo("tech");
    }

    @Test
    void updatePatterns_averagesEngagementAcrossMultipleMatchingPosts() {
        Campaign camp = campaign("tech");
        Post p1 = publishedPost(1L, 0.5, camp, LocalDateTime.now());
        Post p2 = publishedPost(2L, 0.7, camp, LocalDateTime.now());
        Post p3 = publishedPost(3L, null, camp, LocalDateTime.now());
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(p1, p2, p3));

        ContentPattern pattern = pattern("tech", "Tech Posts");
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(any())).thenReturn("Average advice");

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository).save(patternCaptor.capture());
        assertThat(patternCaptor.getValue().getAvgEngagementScore()).isEqualTo((0.5 + 0.7) / 2);
        assertThat(patternCaptor.getValue().getTotalPostsGenerated()).isEqualTo(2);
    }

    @Test
    void updatePatterns_handlesGeminiFailureGracefully() {
        Campaign camp = campaign("tech");
        Post p = publishedPost(1L, 0.6, camp, LocalDateTime.now());
        when(postRepository.findByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(p));

        ContentPattern pattern = pattern("tech", "Tech");
        when(contentPatternRepository.findAll()).thenReturn(List.of(pattern));
        when(geminiService.generate(any())).thenThrow(new RuntimeException("Gemini down"));

        performanceFeedbackService.updatePatternsFromPerformance();

        verify(contentPatternRepository).save(patternCaptor.capture());
        assertThat(patternCaptor.getValue().getPerformanceAdvice())
                .isEqualTo("Performance data collected but advice generation failed.");
        assertThat(patternCaptor.getValue().getAvgEngagementScore()).isEqualTo(0.6);
    }

    @Test
    void getPatternsByPerformance_returnsSortedByEngagement() {
        ContentPattern low = ContentPattern.builder().avgEngagementScore(0.3).build();
        ContentPattern high = ContentPattern.builder().avgEngagementScore(0.9).build();
        ContentPattern none = ContentPattern.builder().avgEngagementScore(null).build();
        ContentPattern mid = ContentPattern.builder().avgEngagementScore(0.6).build();

        when(contentPatternRepository.findAll()).thenReturn(List.of(low, high, none, mid));

        List<ContentPattern> result = performanceFeedbackService.getPatternsByPerformance();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAvgEngagementScore()).isEqualTo(0.9);
        assertThat(result.get(1).getAvgEngagementScore()).isEqualTo(0.6);
        assertThat(result.get(2).getAvgEngagementScore()).isEqualTo(0.3);
    }

    @Test
    void getPatternsByPerformance_returnsEmptyWhenNoneHaveScore() {
        ContentPattern p1 = ContentPattern.builder().avgEngagementScore(null).build();
        when(contentPatternRepository.findAll()).thenReturn(List.of(p1));

        List<ContentPattern> result = performanceFeedbackService.getPatternsByPerformance();

        assertThat(result).isEmpty();
    }
}
