package com.example.metatry.Services;

import com.example.metatry.DTOs.*;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, postImageRepository, campaignRepository, cloudinaryService);
    }

    @Test
    void getAllPosts_returnsAll() {
        when(postRepository.findAll()).thenReturn(List.of(Post.builder().id(1L).build(), Post.builder().id(2L).build()));
        assertThat(postService.getAllPosts()).hasSize(2);
    }

    @Test
    void mapToDto_mapsAllFields() {
        Campaign campaign = Campaign.builder().id(10L).name("Test Campaign").build();
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").build();
        Post post = Post.builder()
                .id(1L).title("Title").content("Content").hashtags("#AI")
                .platform(PlatformType.LINKEDIN).scheduledAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .publishedAt(LocalDateTime.of(2024, 1, 2, 10, 0))
                .permanent(false).link("https://link.com").likes(10).commentsCount(5).shares(2)
                .campaign(campaign).image(image)
                .status(PostStatus.PUBLISHED).build();

        PostDto dto = postService.mapToDto(post);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Title");
        assertThat(dto.getPlatform()).isEqualTo("LINKEDIN");
        assertThat(dto.getCampaignId()).isEqualTo(10L);
        assertThat(dto.getCampaignName()).isEqualTo("Test Campaign");
        assertThat(dto.getImageUrl()).isEqualTo("https://img.com/img.png");
        assertThat(dto.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    void getPostById_whenFound_returnsDto() {
        Post post = Post.builder().id(1L).title("Test").status(PostStatus.DRAFT).build();
        when(postRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(post));
        PostDto dto = postService.getPostById(1L);
        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void getPostById_whenNotFound_throwsException() {
        when(postRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.getPostById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void updatePost_updatesFields() {
        Post post = Post.builder().id(1L).status(PostStatus.DRAFT).permanent(false).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("New Title");
        request.setContent("New Content");
        request.setHashtags("#AI #Tech");
        request.setApproved(true);
        request.setScheduledAt(LocalDateTime.of(2024, 6, 1, 8, 0));
        request.setPermanent(true);
        request.setLink("https://newlink.com");
        request.setPlatform(PlatformType.INSTAGRAM);

        Post result = postService.updatePost(1L, request);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getContent()).isEqualTo("New Content");
        assertThat(result.getHashtags()).isEqualTo("#AI#Tech");
        assertThat(result.getApproved()).isTrue();
        assertThat(result.isPermanent()).isTrue();
        assertThat(result.getLink()).isEqualTo("https://newlink.com");
        assertThat(result.getPlatform()).isEqualTo(PlatformType.INSTAGRAM);
        assertThat(result.getStatus()).isEqualTo(PostStatus.SCHEDULED);
    }

    @Test
    void updatePost_whenPublishedAndNotPermanent_throwsException() {
        Post post = Post.builder().id(1L).status(PostStatus.PUBLISHED).permanent(false).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        assertThatThrownBy(() -> postService.updatePost(1L, new UpdatePostRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot update a published non-permanent post");
    }

    @Test
    void updatePost_whenPublishedAndPermanent_allowsUpdate() {
        Post post = Post.builder().id(1L).status(PostStatus.PUBLISHED).permanent(true).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdatePostRequest upr = new UpdatePostRequest();
        upr.setTitle("New");
        Post result = postService.updatePost(1L, upr);

        assertThat(result.getTitle()).isEqualTo("New");
    }

    @Test
    void deletePost_deletes() {
        Post post = Post.builder().id(1L).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        postService.deletePost(1L);
        verify(postRepository).delete(post);
    }

    @Test
    void createPostForCampaign_createsPostWithImage() throws Exception {
        Campaign campaign = Campaign.builder().id(1L).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(cloudinaryService.uploadImage(any())).thenReturn("https://cloud.com/img.png");
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Manual Post");
        request.setContent("Content");
        request.setHashtags("#AI");
        request.setPlatform(PlatformType.LINKEDIN);
        request.setLink("https://link.com");
        request.setScheduledAt(LocalDateTime.of(2024, 7, 1, 9, 0));
        request.setPermanent(false);

        Post result = postService.createPostForCampaign(1L, request, file);

        assertThat(result.getTitle()).isEqualTo("Manual Post");
        assertThat(result.getGeneratedByAI()).isFalse();
        assertThat(result.getApproved()).isTrue();
        assertThat(result.getStatus()).isEqualTo(PostStatus.SCHEDULED);
        assertThat(result.getImage()).isNotNull();
        assertThat(result.getImage().getImageUrl()).isEqualTo("https://cloud.com/img.png");
        assertThat(result.getCampaign()).isEqualTo(campaign);
    }

    @Test
    void createPostForCampaign_withoutLink_usesDefault() throws Exception {
        Campaign campaign = Campaign.builder().id(1L).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Manual Post");
        request.setContent("Content");
        request.setHashtags("#AI");
        request.setPlatform(PlatformType.LINKEDIN);
        request.setScheduledAt(LocalDateTime.of(2024, 7, 1, 9, 0));
        request.setPermanent(false);

        Post result = postService.createPostForCampaign(1L, request, null);

        assertThat(result.getLink()).isEqualTo("https://3lm-solutions2.odoo.com/contactus");
    }

    @Test
    void getPublishedPosts_delegates() {
        postService.getPublishedPosts();
        verify(postRepository).findByStatus(PostStatus.PUBLISHED);
    }

    @Test
    void getDraftPosts_delegates() {
        postService.getDraftPosts();
        verify(postRepository).findByStatus(PostStatus.DRAFT);
    }

    @Test
    void getApprovedPosts_delegates() {
        postService.getApprovedPosts();
        verify(postRepository).findByApprovedTrue();
    }

    @Test
    void getPostsByPlatform_delegates() {
        postService.getPostsByPlatform(PlatformType.LINKEDIN);
        verify(postRepository).findByPlatform(PlatformType.LINKEDIN);
    }

    @Test
    void getPermanentPosts_delegates() {
        postService.getPermanentPosts();
        verify(postRepository).findByPermanentTrue();
    }

    @Test
    void getStats_returnsCorrectCounts() {
        when(postRepository.count()).thenReturn(10L);
        when(postRepository.countByStatus(PostStatus.PUBLISHED)).thenReturn(5L);
        when(postRepository.countByStatus(PostStatus.DRAFT)).thenReturn(3L);
        when(postRepository.countByApprovedTrue()).thenReturn(4L);
        when(postRepository.countByPlatform(PlatformType.FACEBOOK)).thenReturn(2L);
        when(postRepository.countByPlatform(PlatformType.INSTAGRAM)).thenReturn(3L);
        when(postRepository.countByPlatform(PlatformType.LINKEDIN)).thenReturn(5L);

        PostStatsResponse stats = postService.getStats();

        assertThat(stats.getTotalPosts()).isEqualTo(10);
        assertThat(stats.getPublishedPosts()).isEqualTo(5);
        assertThat(stats.getDraftPosts()).isEqualTo(3);
        assertThat(stats.getApprovedPosts()).isEqualTo(4);
        assertThat(stats.getFacebookPosts()).isEqualTo(2);
        assertThat(stats.getInstagramPosts()).isEqualTo(3);
        assertThat(stats.getLinkedinPosts()).isEqualTo(5);
    }

    @Test
    void getAllScheduledPosts_delegates() {
        postService.getAllScheduledPosts();
        verify(postRepository).findByStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                eq(PostStatus.SCHEDULED), any());
    }

    @Test
    void getScheduledPostsToPublish_delegates() {
        postService.getScheduledPostsToPublish();
        verify(postRepository).findByApprovedTrueAndStatusAndScheduledAtBefore(
                eq(PostStatus.SCHEDULED), any());
    }

    @Test
    void cleanDuplicateImages_keepsFirst() {
        Post post1 = Post.builder().id(1L).build();
        Post post2 = Post.builder().id(1L).build();
        PostImage img1 = PostImage.builder().id(1L).post(post1).build();
        PostImage img2 = PostImage.builder().id(2L).post(post2).build();
        when(postImageRepository.findAll()).thenReturn(List.of(img1, img2));

        postService.cleanDuplicateImages();

        verify(postImageRepository).delete(img2);
        verify(postImageRepository, never()).delete(img1);
    }

    @Test
    void getLatestPublishedPosts_delegates() {
        Page<Post> page = new PageImpl<>(List.of(Post.builder().id(1L).build()));
        when(postRepository.findByStatus(eq(PostStatus.PUBLISHED), any(Pageable.class))).thenReturn(page);
        assertThat(postService.getLatestPublishedPosts(5)).hasSize(1);
    }

    @Test
    void getTopPosts_delegates() {
        Page<Post> page = new PageImpl<>(List.of(Post.builder().id(1L).likes(100).build()));
        when(postRepository.findByStatus(eq(PostStatus.PUBLISHED), any(Pageable.class))).thenReturn(page);
        assertThat(postService.getTopPosts(5)).hasSize(1);
    }

    @Test
    void getPostsByCampaign_delegates() {
        postService.getPostsByCampaign(1L);
        verify(postRepository).findByCampaignId(1L);
    }

    @Test
    void getCampaignPostsByStatus_delegates() {
        postService.getCampaignPostsByStatus(1L, PostStatus.DRAFT);
        verify(postRepository).findByCampaignIdAndStatus(1L, PostStatus.DRAFT);
    }

    @Test
    void getPostSummariesByCampaign_mapsToSummary() {
        Campaign campaign = Campaign.builder().id(1L).build();
        Post post = Post.builder()
                .id(1L).title("Post").platform(PlatformType.LINKEDIN)
                .status(PostStatus.PUBLISHED).likes(10).commentsCount(5).shares(2)
                .campaign(campaign).build();
        when(postRepository.findByCampaignId(1L)).thenReturn(List.of(post));

        List<PostSummaryDTO> summaries = postService.getPostSummariesByCampaign(1L);

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).getTitle()).isEqualTo("Post");
        assertThat(summaries.get(0).getPlatform()).isEqualTo("LINKEDIN");
        assertThat(summaries.get(0).getLikes()).isEqualTo(10);
    }

    @Test
    void getCalendarEvents_returnsEvents() {
        Campaign campaign = Campaign.builder().id(1L).name("Campaign").build();
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").build();
        Post post = Post.builder()
                .id(1L).title("Scheduled Post").content("A".repeat(150))
                .scheduledAt(LocalDateTime.of(2024, 8, 1, 10, 0))
                .publishedAt(LocalDateTime.of(2024, 8, 1, 10, 0))
                .status(PostStatus.SCHEDULED).platform(PlatformType.LINKEDIN)
                .campaign(campaign).image(image).build();
        when(postRepository.findByStatusAndScheduledAtBetween(
                eq(PostStatus.SCHEDULED), any(), any()))
                .thenReturn(List.of(post));

        List<CalendarEventDTO> events = postService.getCalendarEvents(
                LocalDateTime.now(), LocalDateTime.now().plusDays(30));

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Scheduled Post");
        assertThat(events.get(0).getImageUrl()).isEqualTo("https://img.com/img.png");
        assertThat(events.get(0).getCampaignName()).isEqualTo("Campaign");
    }

    @Test
    void getWeeklyComparison_calculatesCorrectly() {
        when(postRepository.countByStatusAndPublishedAtBetween(
                any(), any(), any())).thenReturn(10L, 5L);
        WeeklyComparisonDTO result = postService.getWeeklyComparison();
        assertThat(result.getThisWeek()).isEqualTo(10);
        assertThat(result.getLastWeek()).isEqualTo(5);
        assertThat(result.isIncreased()).isTrue();
    }

    @Test
    void getWeeklyComparison_whenThisWeekUp_increasedIsTrue() {
        when(postRepository.countByStatusAndPublishedAtBetween(
                any(), any(), any())).thenReturn(10L, 5L);

        WeeklyComparisonDTO result = postService.getWeeklyComparison();

        assertThat(result.getThisWeek()).isEqualTo(10);
        assertThat(result.getLastWeek()).isEqualTo(5);
        assertThat(result.isIncreased()).isTrue();
    }

    @Test
    void getWeeklyComparison_whenLastWeekZero_returns100Percent() {
        when(postRepository.countByStatusAndPublishedAtBetween(any(), any(), any()))
                .thenReturn(0L).thenReturn(0L);
        when(postRepository.countByStatusAndPublishedAtBetween(any(), any(), any()))
                .thenReturn(3L).thenReturn(0L);

        WeeklyComparisonDTO result = postService.getWeeklyComparison();

        assertThat(result.isIncreased()).isTrue();
    }

    @Test
    void getUpcomingScheduledPosts_returnsLimited() {
        List<Post> posts = List.of(
                Post.builder().id(1L).build(),
                Post.builder().id(2L).build(),
                Post.builder().id(3L).build()
        );
        when(postRepository.findByStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                any(), any())).thenReturn(posts);

        List<Post> result = postService.getUpcomingScheduledPosts(2);

        assertThat(result).hasSize(2);
    }
}
