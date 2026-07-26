package com.example.metatry.unitTest.Models;
import com.example.metatry.Models.*;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostUnitTest {

    @Test
    void builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Campaign campaign = Campaign.builder().id(1L).build();
        PostImage image = PostImage.builder().imageUrl("https://example.com/img.png").build();

        Post post = Post.builder()
                .id(1L)
                .title("Test Title")
                .content("Test content body")
                .hashtags("#AI #Tech")
                .videoUrl("https://example.com/vid.mp4")
                .platform(PlatformType.LINKEDIN)
                .generatedByAI(true)
                .status(PostStatus.DRAFT)
                .scheduledAt(now)
                .publishedAt(now)
                .permanent(true)
                .link("https://example.com")
                .likes(100)
                .commentsCount(10)
                .shares(5)
                .impressions(1000)
                .engagementScore(0.85)
                .platformPostId("fp_123")
                .approved(true)
                .campaign(campaign)
                .images(List.of(image))
                .notificationSent(true)
                .build();

        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getTitle()).isEqualTo("Test Title");
        assertThat(post.getContent()).isEqualTo("Test content body");
        assertThat(post.getHashtags()).isEqualTo("#AI #Tech");
        assertThat(post.getVideoUrl()).isEqualTo("https://example.com/vid.mp4");
        assertThat(post.getPlatform()).isEqualTo(PlatformType.LINKEDIN);
        assertThat(post.getGeneratedByAI()).isTrue();
        assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(post.getScheduledAt()).isEqualTo(now);
        assertThat(post.getPublishedAt()).isEqualTo(now);
        assertThat(post.isPermanent()).isTrue();
        assertThat(post.getLink()).isEqualTo("https://example.com");
        assertThat(post.getLikes()).isEqualTo(100);
        assertThat(post.getCommentsCount()).isEqualTo(10);
        assertThat(post.getShares()).isEqualTo(5);
        assertThat(post.getImpressions()).isEqualTo(1000);
        assertThat(post.getEngagementScore()).isEqualTo(0.85);
        assertThat(post.getPlatformPostId()).isEqualTo("fp_123");
        assertThat(post.getApproved()).isTrue();
        assertThat(post.getCampaign()).isSameAs(campaign);
        assertThat(post.getImage()).isSameAs(image);
        assertThat(post.isNotificationSent()).isTrue();
    }

    @Test
    void noArgsConstructor_setsDefaults() {
        Post post = new Post();

        assertThat(post.isPermanent()).isFalse();
        assertThat(post.getApproved()).isFalse();
        assertThat(post.isNotificationSent()).isFalse();
        assertThat(post.getLink()).isEqualTo("https://3lm-solutions2.odoo.com/contactus");
        assertThat(post.getId()).isNull();
        assertThat(post.getTitle()).isNull();
        assertThat(post.getStatus()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Campaign campaign = Campaign.builder().id(2L).build();
        PostImage image = PostImage.builder().imageUrl("https://example.com/img.png").build();

        Post post = new Post(
                1L, "Title", "Content", "#tag", "vid.mp4",
                PlatformType.FACEBOOK, true, PostStatus.SCHEDULED,
                now, now, now, true, "https://link.com",
                50, 5, 2, 500, 0.75, "fp_456", true,
                campaign, new ArrayList<>(), new ArrayList<>(), List.of(image), false
        );

        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getTitle()).isEqualTo("Title");
        assertThat(post.getPlatform()).isEqualTo(PlatformType.FACEBOOK);
        assertThat(post.getStatus()).isEqualTo(PostStatus.SCHEDULED);
        assertThat(post.getApproved()).isTrue();
        assertThat(post.getCampaign()).isSameAs(campaign);
        assertThat(post.getImage()).isSameAs(image);
    }

    @Test
    void setters_updateFields() {
        Post post = new Post();

        post.setTitle("Updated");
        post.setContent("Updated content");
        post.setPlatform(PlatformType.INSTAGRAM);
        post.setStatus(PostStatus.PUBLISHED);
        post.setApproved(true);
        post.setPermanent(true);

        assertThat(post.getTitle()).isEqualTo("Updated");
        assertThat(post.getContent()).isEqualTo("Updated content");
        assertThat(post.getPlatform()).isEqualTo(PlatformType.INSTAGRAM);
        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getApproved()).isTrue();
        assertThat(post.isPermanent()).isTrue();
    }

    @Test
    void prePersist_setsCreatedAt() {
        Post post = new Post();
        post.onCreate();

        assertThat(post.getCreatedAt()).isNotNull();
        assertThat(post.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void nullFields_areHandled() {
        Post post = Post.builder().build();

        assertThat(post.getId()).isNull();
        assertThat(post.getTitle()).isNull();
        assertThat(post.getContent()).isNull();
        assertThat(post.getPlatform()).isNull();
        assertThat(post.getStatus()).isNull();
        assertThat(post.getCampaign()).isNull();
        assertThat(post.getImage()).isNull();
        assertThat(post.getCreatedAt()).isNull();
    }

    @Test
    void relationship_campaign() {
        Campaign campaign = Campaign.builder().id(10L).name("Spring Campaign").build();
        Post post = Post.builder().campaign(campaign).build();

        assertThat(post.getCampaign()).isSameAs(campaign);
        assertThat(post.getCampaign().getId()).isEqualTo(10L);
        assertThat(post.getCampaign().getName()).isEqualTo("Spring Campaign");
    }

    @Test
    void relationship_metrics() {
        Post post = Post.builder().build();
        PostMetric metric1 = PostMetric.builder().likes(10).build();
        PostMetric metric2 = PostMetric.builder().likes(20).build();
        List<PostMetric> metrics = List.of(metric1, metric2);

        post.setMetrics(metrics);

        assertThat(post.getMetrics()).hasSize(2);
        assertThat(post.getMetrics().get(0).getLikes()).isEqualTo(10);
        assertThat(post.getMetrics().get(1).getLikes()).isEqualTo(20);
    }

    @Test
    void relationship_comments() {
        Post post = Post.builder().build();
        PostComment comment = PostComment.builder().commentText("Nice post!").build();
        List<PostComment> comments = List.of(comment);

        post.setComments(comments);

        assertThat(post.getComments()).hasSize(1);
        assertThat(post.getComments().get(0).getCommentText()).isEqualTo("Nice post!");
    }

    @Test
    void platform_defaultLink() {
        Post post = new Post();
        assertThat(post.getLink()).isEqualTo("https://3lm-solutions2.odoo.com/contactus");
    }


}