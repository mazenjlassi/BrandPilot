package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.AnalyticsService;
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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class AnalyticsServiceIntegrationTest {

    @MockitoBean
    private PerformanceFeedbackService performanceFeedbackService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMetricRepository postMetricRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    private RestTemplate mockRestTemplate;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(analyticsService, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(analyticsService, "token", "fake-token");
        ReflectionTestUtils.setField(analyticsService, "pageId", "123");
        ReflectionTestUtils.setField(analyticsService, "instagramBusinessId", "ig-biz-1");

        campaign = campaignRepository.save(
                Campaign.builder().name("AnalyticsCamp").topic("Testing").build());
    }

    @AfterEach
    void tearDown() {
        postCommentRepository.deleteAll();
        postMetricRepository.deleteAll();
        postRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    private Post savePublishedPost(Long id, String platformPostId, PlatformType platform) {
        return postRepository.save(Post.builder()
                .title("Post " + id)
                .content("Content " + id)
                .platform(platform)
                .status(PostStatus.PUBLISHED)
                .platformPostId(platformPostId)
                .publishedAt(LocalDateTime.now())
                .campaign(campaign)
                .build());
    }

    // ================= Facebook =================

    @Test
    void collectMetrics_withFacebookPost_savesMetricAndUpdatesPost() {
        Post fbPost = savePublishedPost(1L, "fb-post-1", PlatformType.FACEBOOK);

        Map<String, Object> fbMetrics = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 10)),
                "comments", Map.of("summary", Map.of("total_count", 3)),
                "shares", Map.of("count", 2)
        );
        Map<String, Object> fbComments = Map.of("data", List.of());

        doReturn(fbMetrics).when(mockRestTemplate).getForObject(contains("reactions.summary"), any(Class.class));
        doReturn(fbComments).when(mockRestTemplate).getForObject(contains("/comments?fields=id,message"), any(Class.class));

        analyticsService.collectMetricsForPublishedPosts();

        List<PostMetric> metrics = postMetricRepository.findByPostIdOrderByCollectedAtAsc(fbPost.getId());
        assertThat(metrics).hasSize(1);
        PostMetric saved = metrics.get(0);
        assertThat(saved.getLikes()).isEqualTo(10);
        assertThat(saved.getComments()).isEqualTo(3);
        assertThat(saved.getShares()).isEqualTo(2);

        Post refreshed = postRepository.findById(fbPost.getId()).get();
        assertThat(refreshed.getLikes()).isEqualTo(10);
        assertThat(refreshed.getShares()).isEqualTo(2);

        verify(performanceFeedbackService, atLeastOnce()).updatePatternsFromPerformance();
    }

    @Test
    void collectMetrics_withFacebookPost_ignoresDuplicateComment() {
        Post fbPost = savePublishedPost(2L, "fb-post-2", PlatformType.FACEBOOK);

        postCommentRepository.save(PostComment.builder()
                .externalCommentId("existing-1")
                .commentText("Existing")
                .authorName("User")
                .post(fbPost)
                .build());

        Map<String, Object> fbMetrics = Map.of(
                "reactions", Map.of("summary", Map.of("total_count", 1)),
                "comments", Map.of("summary", Map.of("total_count", 1)),
                "shares", Map.of("count", 0)
        );
        Map<String, Object> commentData = Map.of(
                "id", "existing-1",
                "message", "Existing",
                "created_time", "2024-01-15T10:30:00+0000",
                "from", Map.of("name", "User")
        );
        Map<String, Object> fbComments = Map.of("data", List.of(commentData));

        doReturn(fbMetrics).when(mockRestTemplate).getForObject(contains("reactions.summary"), any(Class.class));
        doReturn(fbComments).when(mockRestTemplate).getForObject(contains("/comments?fields=id,message"), any(Class.class));

        analyticsService.collectMetricsForPublishedPosts();

        long commentCount = postCommentRepository.countByPostId(fbPost.getId());
        assertThat(commentCount).isEqualTo(1);
    }

    @Test
    void collectMetrics_withFacebookApiError_doesNotSaveMetric() {
        Post fbPost = savePublishedPost(3L, "fb-post-3", PlatformType.FACEBOOK);

        doThrow(new RuntimeException("API error")).when(mockRestTemplate)
                .getForObject(contains("reactions.summary"), any(Class.class));

        analyticsService.collectMetricsForPublishedPosts();

        List<PostMetric> metrics = postMetricRepository.findByPostIdOrderByCollectedAtAsc(fbPost.getId());
        assertThat(metrics).isEmpty();
    }

    // ================= Instagram =================

    @Test
    void collectMetrics_withInstagramPost_usesCachedMetrics() {
        Post igPost = savePublishedPost(4L, "ig-1", PlatformType.INSTAGRAM);

        Map<String, Object> igBatch = Map.of(
                "data", List.of(Map.of(
                        "id", "ig-1",
                        "like_count", 20,
                        "comments_count", 5
                ))
        );

        when(mockRestTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(igBatch);

        analyticsService.collectMetricsForPublishedPosts();

        List<PostMetric> metrics = postMetricRepository.findByPostIdOrderByCollectedAtAsc(igPost.getId());
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).getLikes()).isEqualTo(20);
        assertThat(metrics.get(0).getComments()).isEqualTo(5);
    }

    @Test
    void collectMetrics_withInstagramCacheMiss_fetchesIndividualMetrics() {
        Post igPost = savePublishedPost(5L, "ig-miss-1", PlatformType.INSTAGRAM);

        Map<String, Object> igBatch = Map.of(
                "data", List.of(Map.of(
                        "id", "other-post",
                        "like_count", 1,
                        "comments_count", 0
                ))
        );
        Map<String, Object> igIndividual = Map.of(
                "like_count", 7,
                "comments_count", 2
        );

        when(mockRestTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(igBatch)
                .thenReturn(igIndividual);

        analyticsService.collectMetricsForPublishedPosts();

        List<PostMetric> metrics = postMetricRepository.findByPostIdOrderByCollectedAtAsc(igPost.getId());
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).getLikes()).isEqualTo(7);
        assertThat(metrics.get(0).getComments()).isEqualTo(2);
    }

    // ================= Skip scenarios =================

    @Test
    void collectMetrics_skipsPostWhenPlatformPostIdIsNull() {
        savePublishedPost(6L, null, PlatformType.FACEBOOK);

        analyticsService.collectMetricsForPublishedPosts();

        List<PostMetric> all = postMetricRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void collectMetrics_skipsLinkedInPosts() {
        savePublishedPost(7L, "li-1", PlatformType.LINKEDIN);

        analyticsService.collectMetricsForPublishedPosts();

        List<PostMetric> all = postMetricRepository.findAll();
        assertThat(all).isEmpty();
    }
}
