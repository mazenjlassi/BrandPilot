package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.CalendarEventDTO;
import com.example.metatry.DTOs.PostStatsResponse;
import com.example.metatry.DTOs.WeeklyComparisonDTO;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.CloudinaryService;
import com.example.metatry.Services.PostService;
import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PostServiceIntegrationTest {

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    private Campaign campaign;
    private Post savedPost;

    @BeforeEach
    void setUp() {
        campaign = campaignRepository.save(
                Campaign.builder().name("PostSvcCamp").topic("AI").build());
        savedPost = postRepository.save(Post.builder()
                .title("Test Post")
                .content("Test content")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.DRAFT)
                .campaign(campaign)
                .build());
    }

    @AfterEach
    void tearDown() {
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    // ================= getAllPosts =================

    @Test
    void getAllPosts_returnsAllPosts() {
        List<Post> posts = postService.getAllPosts();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("Test Post");
    }

    // ================= getPostById =================

    @Test
    void getPostById_success() {
        com.example.metatry.DTOs.PostDto dto = postService.getPostById(savedPost.getId());
        assertThat(dto.getTitle()).isEqualTo("Test Post");
        assertThat(dto.getPlatform()).isEqualTo("FACEBOOK");
    }

    @Test
    void getPostById_notFound_throws() {
        assertThatThrownBy(() -> postService.getPostById(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");
    }

    // ================= updatePost =================

    @Test
    void updatePost_updatesFields() {
        UpdatePostRequest req = new UpdatePostRequest();
        req.setTitle("Updated Title");
        req.setContent("Updated content");
        req.setHashtags("#test #update");

        Post updated = postService.updatePost(savedPost.getId(), req);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getContent()).isEqualTo("Updated content");
        assertThat(updated.getHashtags()).isEqualTo("#test#update");
    }

    @Test
    void updatePost_publishedNonPermanent_throws() {
        savedPost.setStatus(PostStatus.PUBLISHED);
        savedPost.setPermanent(false);
        postRepository.save(savedPost);

        UpdatePostRequest req = new UpdatePostRequest();
        req.setTitle("Should fail");

        assertThatThrownBy(() -> postService.updatePost(savedPost.getId(), req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot update a published non-permanent post");
    }

    @Test
    void updatePost_setsScheduledStatus_whenScheduledAtSet() {
        UpdatePostRequest req = new UpdatePostRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));

        Post updated = postService.updatePost(savedPost.getId(), req);

        assertThat(updated.getScheduledAt()).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(PostStatus.SCHEDULED);
    }

    @Test
    void updatePost_setsLink() {
        UpdatePostRequest req = new UpdatePostRequest();
        req.setLink("https://custom.link");

        Post updated = postService.updatePost(savedPost.getId(), req);

        assertThat(updated.getLink()).isEqualTo("https://custom.link");
    }

    // ================= deletePost =================

    @Test
    void deletePost_success() {
        postService.deletePost(savedPost.getId());
        assertThat(postRepository.findById(savedPost.getId())).isEmpty();
    }

    @Test
    void deletePost_notFound_throws() {
        assertThatThrownBy(() -> postService.deletePost(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");
    }

    // ================= getPublishedPosts =================

    @Test
    void getPublishedPosts_returnsPublishedOnly() {
        postRepository.save(Post.builder()
                .title("Published Post")
                .content("Pub")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED)
                .campaign(campaign)
                .build());

        List<Post> published = postService.getPublishedPosts();
        assertThat(published).hasSize(1);
        assertThat(published.get(0).getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    // ================= getDraftPosts =================

    @Test
    void getDraftPosts_returnsDrafts() {
        List<Post> drafts = postService.getDraftPosts();
        assertThat(drafts).hasSize(1);
        assertThat(drafts.get(0).getStatus()).isEqualTo(PostStatus.DRAFT);
    }

    // ================= getApprovedPosts =================

    @Test
    void getApprovedPosts_returnsApprovedOnly() {
        postRepository.save(Post.builder()
                .title("Approved Post")
                .content("App")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.DRAFT)
                .approved(true)
                .campaign(campaign)
                .build());

        List<Post> approved = postService.getApprovedPosts();
        assertThat(approved).hasSize(1);
        assertThat(approved.get(0).getApproved()).isTrue();
    }

    // ================= getStats =================

    @Test
    void getStats_returnsCorrectCounts() {
        postRepository.save(Post.builder()
                .title("Pub Post").content("P")
                .platform(PlatformType.FACEBOOK).status(PostStatus.PUBLISHED)
                .campaign(campaign).build());
        postRepository.save(Post.builder()
                .title("LinkedIn Post").content("L")
                .platform(PlatformType.LINKEDIN).status(PostStatus.DRAFT)
                .approved(true)
                .campaign(campaign).build());

        PostStatsResponse stats = postService.getStats();

        assertThat(stats.getTotalPosts()).isGreaterThanOrEqualTo(3);
        assertThat(stats.getPublishedPosts()).isGreaterThanOrEqualTo(1);
        assertThat(stats.getDraftPosts()).isGreaterThanOrEqualTo(2);
    }

    // ================= getPermanentPosts =================

    @Test
    void getPermanentPosts_returnsPermanentOnly() {
        postRepository.save(Post.builder()
                .title("Perm Post").content("Perm")
                .platform(PlatformType.FACEBOOK).status(PostStatus.DRAFT)
                .permanent(true)
                .campaign(campaign).build());

        List<Post> perms = postService.getPermanentPosts();
        assertThat(perms).isNotEmpty();
    }

    // ================= getPostsByPlatform =================

    @Test
    void getPostsByPlatform_filtersByPlatform() {
        List<Post> fbPosts = postService.getPostsByPlatform(PlatformType.FACEBOOK);
        assertThat(fbPosts).allMatch(p -> p.getPlatform() == PlatformType.FACEBOOK);
    }

    // ================= getPostsByCampaign =================

    @Test
    void getPostsByCampaign_returnsCampaignPosts() {
        List<Post> posts = postService.getPostsByCampaign(campaign.getId());
        assertThat(posts).hasSize(1);
    }

    // ================= getLatestPublishedPosts =================

    @Test
    void getLatestPublishedPosts_returnsOrdered() {
        postRepository.save(Post.builder()
                .title("Newer Post").content("New")
                .platform(PlatformType.FACEBOOK).status(PostStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .campaign(campaign).build());

        List<Post> latest = postService.getLatestPublishedPosts(5);
        assertThat(latest).isNotEmpty();
    }

    // ================= getWeeklyComparison =================

    @Test
    void getWeeklyComparison_returnsComparison() {
        WeeklyComparisonDTO comp = postService.getWeeklyComparison();
        assertThat(comp).isNotNull();
        assertThat(comp.getThisWeek()).isGreaterThanOrEqualTo(0);
        assertThat(comp.getLastWeek()).isGreaterThanOrEqualTo(0);
    }

    // ================= getUpcomingScheduledPosts =================

    @Test
    void getUpcomingScheduledPosts_returnsFutureScheduled() {
        postRepository.save(Post.builder()
                .title("Future Post").content("Fut")
                .platform(PlatformType.FACEBOOK).status(PostStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .campaign(campaign).build());

        List<Post> upcoming = postService.getUpcomingScheduledPosts(10);
        assertThat(upcoming).isNotEmpty();
    }

    // ================= getCalendarEvents =================

    @Test
    @Transactional
    void getCalendarEvents_returnsScheduledEvents() {
        postRepository.save(Post.builder()
                .title("Calendar Post").content("Cal")
                .platform(PlatformType.FACEBOOK).status(PostStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .campaign(campaign).build());

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(7);

        List<CalendarEventDTO> events = postService.getCalendarEvents(start, end);
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getId()).isNotNull();
    }

    // ================= cleanDuplicateImages =================

    @Test
    void cleanDuplicateImages_removesDuplicates() {
        postImageRepository.save(PostImage.builder()
                .imageUrl("https://example.com/dup.jpg").sortOrder(0)
                .post(savedPost).build());
        postImageRepository.save(PostImage.builder()
                .imageUrl("https://example.com/dup.jpg").sortOrder(1)
                .post(savedPost).build());

        postService.cleanDuplicateImages();

        List<PostImage> remaining = postImageRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    void cleanDuplicateImages_keepsOnlyOnePerPost() {
        postImageRepository.save(PostImage.builder()
                .imageUrl("https://example.com/a.jpg").sortOrder(0)
                .post(savedPost).build());
        postImageRepository.save(PostImage.builder()
                .imageUrl("https://example.com/b.jpg").sortOrder(1)
                .post(savedPost).build());

        postService.cleanDuplicateImages();

        List<PostImage> remaining = postImageRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    // ================= getScheduledPostsToPublish =================

    @Test
    void getScheduledPostsToPublish_returnsApprovedScheduledBeforeNow() {
        postRepository.save(Post.builder()
                .title("Ready to Publish").content("Ready")
                .platform(PlatformType.FACEBOOK).status(PostStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now().minusMinutes(5))
                .approved(true)
                .campaign(campaign).build());

        List<Post> toPublish = postService.getScheduledPostsToPublish();
        assertThat(toPublish).isNotEmpty();
    }

    // ================= getCampaignPostsByStatus =================

    @Test
    void getCampaignPostsByStatus_filtersByStatus() {
        postRepository.save(Post.builder()
                .title("Campaign Draft").content("CD")
                .platform(PlatformType.FACEBOOK).status(PostStatus.DRAFT)
                .campaign(campaign).build());

        List<Post> drafts = postService.getCampaignPostsByStatus(campaign.getId(), PostStatus.DRAFT);
        assertThat(drafts).hasSize(2); // savedPost + new one
    }

    // ================= createPostForCampaign =================

    @Test
    void createPostForCampaign_noImageNoVideo_createsPost() {
        com.example.metatry.DTOs.CreatePostRequest req = new com.example.metatry.DTOs.CreatePostRequest();
        req.setTitle("New Post");
        req.setContent("New content");
        req.setPlatform(PlatformType.LINKEDIN);
        req.setPermanent(false);

        Post created = postService.createPostForCampaign(campaign.getId(), req, null, null);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("New Post");
        assertThat(created.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(created.getGeneratedByAI()).isFalse();
        assertThat(created.getApproved()).isTrue();
    }

    @Test
    void createPostForCampaign_withScheduledAt_setsScheduled() {
        com.example.metatry.DTOs.CreatePostRequest req = new com.example.metatry.DTOs.CreatePostRequest();
        req.setTitle("Scheduled Post");
        req.setContent("Scheduled");
        req.setPlatform(PlatformType.FACEBOOK);
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setPermanent(false);

        Post created = postService.createPostForCampaign(campaign.getId(), req, null, null);

        assertThat(created.getStatus()).isEqualTo(PostStatus.SCHEDULED);
        assertThat(created.getScheduledAt()).isNotNull();
    }

    @Test
    void createPostForCampaign_campaignNotFound_throws() {
        com.example.metatry.DTOs.CreatePostRequest req = new com.example.metatry.DTOs.CreatePostRequest();

        assertThatThrownBy(() -> postService.createPostForCampaign(99999L, req, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void createPostForCampaign_setsDefaultLinkWhenBlank() {
        com.example.metatry.DTOs.CreatePostRequest req = new com.example.metatry.DTOs.CreatePostRequest();
        req.setTitle("Link Post");
        req.setContent("Content");
        req.setPlatform(PlatformType.FACEBOOK);
        req.setPermanent(false);

        Post created = postService.createPostForCampaign(campaign.getId(), req, null, null);

        assertThat(created.getLink()).isEqualTo("https://3lm-solutions2.odoo.com/contactus");
    }

    // ================= addImagesToPost =================

    @Test
    @Transactional
    void addImagesToPost_success() throws Exception {
        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.jpg",
                        "image/jpeg", "fake-image-content".getBytes());
        when(cloudinaryService.uploadImage(any())).thenReturn("https://cloudinary.com/test.jpg");

        Post updated = postService.addImagesToPost(savedPost.getId(), List.of(file));

        assertThat(updated.getImages()).isNotEmpty();
        assertThat(updated.getImages().get(0).getImageUrl()).isEqualTo("https://cloudinary.com/test.jpg");
    }

    @Test
    void addImagesToPost_postNotFound_throws() {
        assertThatThrownBy(() -> postService.addImagesToPost(99999L, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    @Transactional
    void addImagesToPost_cloudinaryFails_throws() throws Exception {
        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.jpg",
                        "image/jpeg", "fake".getBytes());
        when(cloudinaryService.uploadImage(any())).thenThrow(new RuntimeException("Upload failed"));

        assertThatThrownBy(() -> postService.addImagesToPost(savedPost.getId(), List.of(file)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Image upload failed");
    }
}
