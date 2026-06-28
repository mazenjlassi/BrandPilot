package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceUnitTest {

    @Mock
    private PostService postService;
    @Mock
    private PostMetricRepository postMetricRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private PerformanceFeedbackService performanceFeedbackService;
    @Mock
    private RestTemplate restTemplate;

    private AnalyticsService analyticsService;

    @Captor
    private ArgumentCaptor<Post> postCaptor;
    @Captor
    private ArgumentCaptor<PostMetric> metricCaptor;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(postService, postMetricRepository, postRepository, postCommentRepository, performanceFeedbackService);
        ReflectionTestUtils.setField(analyticsService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(analyticsService, "token", "fake-token");
        ReflectionTestUtils.setField(analyticsService, "pageId", "123");
        ReflectionTestUtils.setField(analyticsService, "instagramBusinessId", "ig-biz-1");
    }

    private Post postWithPlatformPostId(Long id, String platformPostId, PlatformType platform) {
        Post p = new Post();
        p.setId(id);
        p.setPlatformPostId(platformPostId);
        p.setPlatform(platform);
        p.setLikes(0);
        p.setCommentsCount(0);
        p.setShares(0);
        p.setImpressions(0);
        return p;
    }

    // ============ Null / LinkedIn skips ============

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_skipsPostWhenPlatformPostIdIsNull() {
        Post noIdPost = postWithPlatformPostId(1L, null, PlatformType.FACEBOOK);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(noIdPost));
        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(Map.of());

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository, never()).save(any());
        verify(performanceFeedbackService).updatePatternsFromPerformance();
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_skipsLinkedInPosts() {
        Post linkedInPost = postWithPlatformPostId(2L, "li-123", PlatformType.LINKEDIN);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(linkedInPost));
        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(Map.of());

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository, never()).save(any());
        verify(performanceFeedbackService).updatePatternsFromPerformance();
    }

    // ============ Facebook tests ============

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_fetchesFacebookMetricsAndSaves() {
        Post fbPost = postWithPlatformPostId(3L, "456", PlatformType.FACEBOOK);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(fbPost));

        Map<String, Object> emptyBatch = Map.of();
        Map<String, Object> fbMetrics = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 10)),
                "comments", Map.of("summary", Map.of("total_count", 3)),
                "shares", Map.of("count", 2)
        );
        Map<String, Object> fbComments = Map.of("data", List.of());

        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(emptyBatch);
        when(restTemplate.getForObject(contains("reactions.summary"), eq(Map.class))).thenReturn(fbMetrics);
        when(restTemplate.getForObject(contains("/comments"), eq(Map.class))).thenReturn(fbComments);
        when(postCommentRepository.countByPostId(3L)).thenReturn(3L);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository).save(metricCaptor.capture());
        PostMetric saved = metricCaptor.getValue();
        assertThat(saved.getLikes()).isEqualTo(10);
        assertThat(saved.getComments()).isEqualTo(3);
        assertThat(saved.getShares()).isEqualTo(2);

        verify(postRepository, times(2)).save(postCaptor.capture());
        Post updatedPost = postCaptor.getAllValues().get(1);
        assertThat(updatedPost.getLikes()).isEqualTo(10);
        assertThat(updatedPost.getCommentsCount()).isEqualTo(3);
        assertThat(updatedPost.getShares()).isEqualTo(2);
        double expectedEngagement = (double)(10 + 3 * 2 + 2 * 3) / (10 + 3 + 2);
        assertThat(updatedPost.getEngagementScore()).isEqualTo(expectedEngagement);
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_fetchesFacebookCommentsAndSaves() {
        Post fbPost = postWithPlatformPostId(4L, "456", PlatformType.FACEBOOK);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(fbPost));

        Map<String, Object> emptyBatch = Map.of();
        Map<String, Object> fbMetrics = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 5)),
                "comments", Map.of("summary", Map.of("total_count", 2)),
                "shares", Map.of("count", 1)
        );
        Map<String, Object> commentData = Map.of(
                "id", "comm-1",
                "message", "Great post!",
                "created_time", "2024-01-15T10:30:00+0000",
                "from", Map.of("name", "TestUser")
        );
        Map<String, Object> fbComments = Map.of("data", List.of(commentData));

        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(emptyBatch);
        when(restTemplate.getForObject(contains("reactions.summary"), eq(Map.class))).thenReturn(fbMetrics);
        when(restTemplate.getForObject(contains("/comments"), eq(Map.class))).thenReturn(fbComments);
        when(postCommentRepository.existsByExternalCommentId("comm-1")).thenReturn(false);
        when(postCommentRepository.countByPostId(4L)).thenReturn(1L);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postCommentRepository).save(any(PostComment.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_skipsExistingComments() {
        Post fbPost = postWithPlatformPostId(5L, "456", PlatformType.FACEBOOK);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(fbPost));

        Map<String, Object> emptyBatch = Map.of();
        Map<String, Object> fbMetrics = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 1)),
                "comments", Map.of("summary", Map.of("total_count", 1)),
                "shares", Map.of("count", 0)
        );
        Map<String, Object> commentData = Map.of(
                "id", "existing-comm",
                "message", "Already saved",
                "from", Map.of("name", "User")
        );
        Map<String, Object> fbComments = Map.of("data", List.of(commentData));

        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(emptyBatch);
        when(restTemplate.getForObject(contains("reactions.summary"), eq(Map.class))).thenReturn(fbMetrics);
        when(restTemplate.getForObject(contains("/comments"), eq(Map.class))).thenReturn(fbComments);
        when(postCommentRepository.existsByExternalCommentId("existing-comm")).thenReturn(true);
        when(postCommentRepository.countByPostId(5L)).thenReturn(0L);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postCommentRepository, never()).save(any(PostComment.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_handlesFacebookApiErrorGracefully() {
        Post fbPost = postWithPlatformPostId(7L, "456", PlatformType.FACEBOOK);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(fbPost));
        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(Map.of());

        when(restTemplate.getForObject(contains("reactions.summary"), eq(Map.class)))
                .thenThrow(new RuntimeException("API error"));

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository, never()).save(any());
        verify(performanceFeedbackService).updatePatternsFromPerformance();
    }

    // ============ Instagram tests ============

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_fetchesInstagramFromCache() {
        Post igPost = postWithPlatformPostId(8L, "ig-post-1", PlatformType.INSTAGRAM);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(igPost));

        Map<String, Object> igBatch = Map.of(
                "data", List.of(Map.of(
                        "id", "ig-post-1",
                        "like_count", 20,
                        "comments_count", 5
                ))
        );
        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(igBatch);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository).save(metricCaptor.capture());
        PostMetric saved = metricCaptor.getValue();
        assertThat(saved.getLikes()).isEqualTo(20);
        assertThat(saved.getComments()).isEqualTo(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_handlesInstagramCacheMissByIndividualCall() {
        Post igPost = postWithPlatformPostId(9L, "ig-post-2", PlatformType.INSTAGRAM);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(igPost));

        Map<String, Object> igBatch = Map.of("data", List.of(
                Map.of("id", "other-post", "like_count", 1, "comments_count", 0)
        ));
        Map<String, Object> igIndividual = Map.of(
                "like_count", 7,
                "comments_count", 2
        );

        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(igBatch);
        when(restTemplate.getForObject(contains("ig-post-2"), eq(Map.class))).thenReturn(igIndividual);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postMetricRepository).save(metricCaptor.capture());
        PostMetric saved = metricCaptor.getValue();
        assertThat(saved.getLikes()).isEqualTo(7);
        assertThat(saved.getComments()).isEqualTo(2);
    }

    // ============ Engagement score tests ============

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_usesNonZeroImpressionsForEngagement() {
        Post fbPost = postWithPlatformPostId(11L, "eng-1", PlatformType.FACEBOOK);
        fbPost.setImpressions(200);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(fbPost));

        Map<String, Object> emptyBatch = Map.of();
        Map<String, Object> fbMetrics = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 50)),
                "comments", Map.of("summary", Map.of("total_count", 10)),
                "shares", Map.of("count", 5)
        );
        Map<String, Object> emptyComments = Map.of("data", List.of());

        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(emptyBatch);
        when(restTemplate.getForObject(contains("reactions.summary"), eq(Map.class))).thenReturn(fbMetrics);
        when(restTemplate.getForObject(contains("/comments"), eq(Map.class))).thenReturn(emptyComments);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postRepository, times(2)).save(postCaptor.capture());
        Post updated = postCaptor.getAllValues().get(1);
        assertThat(updated.getImpressions()).isEqualTo(200);
        double expected = (double)(50 + 10 * 2 + 5 * 3) / 200;
        assertThat(updated.getEngagementScore()).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectMetrics_fallbackImpressionsWhenZero() {
        Post fbPost = postWithPlatformPostId(12L, "eng-2", PlatformType.FACEBOOK);
        fbPost.setImpressions(null);
        when(postService.getLastPublishedPosts(20)).thenReturn(List.of(fbPost));

        Map<String, Object> emptyBatch = Map.of();
        Map<String, Object> fbMetrics = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 8)),
                "comments", Map.of("summary", Map.of("total_count", 4)),
                "shares", Map.of("count", 2)
        );
        Map<String, Object> emptyComments = Map.of("data", List.of());

        when(restTemplate.getForObject(contains("/media"), eq(Map.class))).thenReturn(emptyBatch);
        when(restTemplate.getForObject(contains("reactions.summary"), eq(Map.class))).thenReturn(fbMetrics);
        when(restTemplate.getForObject(contains("/comments"), eq(Map.class))).thenReturn(emptyComments);

        analyticsService.collectMetricsForPublishedPosts();

        verify(postRepository, times(2)).save(postCaptor.capture());
        Post updated = postCaptor.getAllValues().get(1);
        int fallback = (8 + 4 + 2);
        double expected = (double)(8 + 4 * 2 + 2 * 3) / fallback;
        assertThat(updated.getEngagementScore()).isEqualTo(expected);
    }
}
