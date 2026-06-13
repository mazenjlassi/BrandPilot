package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private PostService postService;
    @Mock private PostMetricRepository postMetricRepository;
    @Mock private PostRepository postRepository;
    @Mock private PostCommentRepository postCommentRepository;
    @Mock private PerformanceFeedbackService performanceFeedbackService;

    @Captor private ArgumentCaptor<PostMetric> metricCaptor;

    private RestTemplate restTemplate;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        analyticsService = new AnalyticsService(
                postService, postMetricRepository, postRepository,
                postCommentRepository, performanceFeedbackService
        );
        ReflectionTestUtils.setField(analyticsService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(analyticsService, "token", "test-token");
        ReflectionTestUtils.setField(analyticsService, "pageId", "test-page");
        ReflectionTestUtils.setField(analyticsService, "instagramBusinessId", "test-ig");
    }

    @Test
    void collectMetricsForPublishedPosts_withFacebookPost_fetchesAndSaves() {
        Post post = Post.builder().id(1L).platformPostId("123").platform(PlatformType.FACEBOOK).build();
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(post));

        Map<String, Object> fbResponse = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 10)),
                "comments", Map.of("summary", Map.of("total_count", 5)),
                "shares", Map.of("count", 2)
        );
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(fbResponse);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository).save(metricCaptor.capture());
        PostMetric saved = metricCaptor.getValue();
        assertThat(saved.getLikes()).isEqualTo(10);
        assertThat(saved.getComments()).isEqualTo(5);
        assertThat(saved.getShares()).isEqualTo(2);
        verify(postRepository, times(1)).save(any());
        verify(performanceFeedbackService).updatePatternsFromPerformance();
    }

    @Test
    void collectMetricsForPublishedPosts_withInstagramPost_usesCache() {
        Post post = Post.builder().id(2L).platformPostId("ig_456").platform(PlatformType.INSTAGRAM).build();
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(post));

        Map<String, Object> igBatchResponse = Map.of(
                "data", List.of(
                        Map.of("id", "ig_456", "like_count", 20, "comments_count", 8)
                )
        );
        when(restTemplate.getForObject(
                contains("/media?fields=id"), eq(Map.class)))
                .thenReturn(igBatchResponse);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository).save(metricCaptor.capture());
        PostMetric saved = metricCaptor.getValue();
        assertThat(saved.getLikes()).isEqualTo(20);
        assertThat(saved.getComments()).isEqualTo(8);
    }

    @Test
    void collectMetricsForPublishedPosts_withLinkedInPost_skips() {
        Post post = Post.builder().id(3L).platformPostId("li_789").platform(PlatformType.LINKEDIN).build();
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(post));

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository, never()).save(any());
        verify(postRepository, never()).save(any());
        verify(performanceFeedbackService).updatePatternsFromPerformance();
    }

    @Test
    void collectMetricsForPublishedPosts_withoutPostId_skips() {
        Post post = Post.builder().id(4L).platformPostId(null).platform(PlatformType.FACEBOOK).build();
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(post));

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void collectMetricsForPublishedPosts_whenApiCallFails_continues() {
        Post fb = Post.builder().id(5L).platformPostId("fb_001").platform(PlatformType.FACEBOOK).build();
        Post ig = Post.builder().id(6L).platformPostId("ig_002").platform(PlatformType.INSTAGRAM).build();
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(fb, ig));

        when(restTemplate.getForObject(
                contains("test-page_fb_001"), eq(Map.class)))
                .thenThrow(new RuntimeException("API error"));
        when(restTemplate.getForObject(
                contains("/media?fields=id"), eq(Map.class)))
                .thenThrow(new RuntimeException("IG batch error"));

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository, never()).save(any());
        verify(performanceFeedbackService).updatePatternsFromPerformance();
    }

    @Test
    void extractReactions_whenPresent_returnsCount() {
        Map<String, Object> response = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 25))
        );

        int result = ReflectionTestUtils.invokeMethod(analyticsService, "extractReactions", response);
        assertThat(result).isEqualTo(25);
    }

    @Test
    void extractReactions_whenMissing_returnsZero() {
        int result = ReflectionTestUtils.invokeMethod(analyticsService, "extractReactions", Map.of());
        assertThat(result).isEqualTo(0);
    }

    @Test
    void extractComments_whenPresent_returnsCount() {
        Map<String, Object> response = Map.of(
                "comments", Map.of("summary", Map.of("total_count", 12))
        );

        int result = ReflectionTestUtils.invokeMethod(analyticsService, "extractComments", response);
        assertThat(result).isEqualTo(12);
    }

    @Test
    void extractShares_whenPresent_returnsCount() {
        Map<String, Object> response = Map.of(
                "shares", Map.of("count", 7)
        );

        int result = ReflectionTestUtils.invokeMethod(analyticsService, "extractShares", response);
        assertThat(result).isEqualTo(7);
    }

    @Test
    void saveAndUpdate_updatesPostFields() {
        Post post = Post.builder().id(10L).likes(0).commentsCount(0).shares(0).impressions(0).build();

        ReflectionTestUtils.invokeMethod(analyticsService, "saveAndUpdate", post, 15, 3, 5, 100);

        verify(postMetricRepository).save(any(PostMetric.class));
        verify(postRepository).save(post);
        assertThat(post.getLikes()).isEqualTo(15);
        assertThat(post.getCommentsCount()).isEqualTo(3);
        assertThat(post.getShares()).isEqualTo(5);
        assertThat(post.getImpressions()).isEqualTo(100);
        assertThat(post.getEngagementScore()).isNotNull();
    }

    @Test
    void analyzeSentiment_positiveKeywords_returnsPositive() {
        String result = ReflectionTestUtils.invokeMethod(
                analyticsService, "analyzeSentiment", "This is great and I love it");
        assertThat(result).isEqualTo("POSITIVE");

        result = ReflectionTestUtils.invokeMethod(
                analyticsService, "analyzeSentiment", "good product");
        assertThat(result).isEqualTo("POSITIVE");
    }

    @Test
    void analyzeSentiment_negativeKeywords_returnsNegative() {
        String result = ReflectionTestUtils.invokeMethod(
                analyticsService, "analyzeSentiment", "This is bad");
        assertThat(result).isEqualTo("NEGATIVE");

        result = ReflectionTestUtils.invokeMethod(
                analyticsService, "analyzeSentiment", "I hate this");
        assertThat(result).isEqualTo("NEGATIVE");
    }

    @Test
    void analyzeSentiment_neutralKeywords_returnsNeutral() {
        String result = ReflectionTestUtils.invokeMethod(
                analyticsService, "analyzeSentiment", "This is a normal comment");
        assertThat(result).isEqualTo("NEUTRAL");
    }

    @Test
    void fetchFacebookComments_withExistingComment_skipsDuplicate() {
        Post post = Post.builder().id(11L).platformPostId("fb_post_1").build();
        when(postCommentRepository.existsByExternalCommentId("comment_1")).thenReturn(true);
        when(postRepository.save(any())).thenReturn(post);

        Map<String, Object> commentsResponse = Map.of(
                "data", List.of(
                        Map.of("id", "comment_1", "message", "Nice!", "created_time", "2026-01-01T12:00:00+0000",
                                "from", Map.of("name", "User1"))
                )
        );
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(commentsResponse);

        ReflectionTestUtils.invokeMethod(analyticsService, "fetchFacebookComments", post);

        verify(postCommentRepository, never()).save(any());
        verify(postRepository).save(post);
    }

    @Test
    void fetchFacebookComments_withNullMessage_skipsComment() {
        Post post = Post.builder().id(12L).platformPostId("fb_post_2").build();
        when(postCommentRepository.existsByExternalCommentId(anyString())).thenReturn(false);
        when(postRepository.save(any())).thenReturn(post);

        Map<String, Object> commentsResponse = Map.of(
                "data", List.of(
                        Map.of("id", "comment_2", "created_time", "2026-01-01T12:00:00+0000",
                                "from", Map.of("name", "User2"))
                )
        );
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(commentsResponse);

        ReflectionTestUtils.invokeMethod(analyticsService, "fetchFacebookComments", post);

        verify(postCommentRepository, never()).save(any());
    }

    @Test
    void parseFacebookDate_validDate_returnsParsed() {
        LocalDateTime result = ReflectionTestUtils.invokeMethod(
                analyticsService, "parseFacebookDate", "2026-01-15T10:30:00+0000");
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getMonthValue()).isEqualTo(1);
        assertThat(result.getDayOfMonth()).isEqualTo(15);
        assertThat(result.getHour()).isEqualTo(10);
        assertThat(result.getMinute()).isEqualTo(30);
    }

    @Test
    void parseFacebookDate_invalidDate_returnsNow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = ReflectionTestUtils.invokeMethod(
                analyticsService, "parseFacebookDate", "not-a-date");
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(now.getYear());
        assertThat(result.getMonthValue()).isEqualTo(now.getMonthValue());
    }
}
