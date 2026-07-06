package com.example.metatry.unitTest.DTOs;
import com.example.metatry.DTOs.*;

import com.example.metatry.Enums.PlatformType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleDtoUnitTest {

    @Test
    void createPostRequest() {
        CreatePostRequest dto = new CreatePostRequest();
        dto.setTitle("Test");
        dto.setContent("Content");
        dto.setHashtags("#tag");
        dto.setPlatform(PlatformType.LINKEDIN);
        dto.setScheduledAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        dto.setPermanent(true);
        dto.setLink("https://link.com");
        dto.setImageUrl("https://img.com/img.jpg");

        assertThat(dto.getTitle()).isEqualTo("Test");
        assertThat(dto.getContent()).isEqualTo("Content");
        assertThat(dto.getHashtags()).isEqualTo("#tag");
        assertThat(dto.getPlatform()).isEqualTo(PlatformType.LINKEDIN);
        assertThat(dto.getScheduledAt()).isNotNull();
        assertThat(dto.isPermanent()).isTrue();
        assertThat(dto.getLink()).isEqualTo("https://link.com");
        assertThat(dto.getImageUrl()).isEqualTo("https://img.com/img.jpg");
    }

    @Test
    void updatePostRequest() {
        UpdatePostRequest dto = new UpdatePostRequest();
        dto.setTitle("Updated");
        dto.setContent("Updated content");
        dto.setHashtags("#new");
        dto.setPlatform(PlatformType.FACEBOOK);
        dto.setApproved(true);
        dto.setScheduledAt(LocalDateTime.now());
        dto.setPermanent(false);
        dto.setLink("https://new.com");
        dto.setImageUrl("https://img.com/new.jpg");
        dto.setVideoUrl("https://vid.com/vid.mp4");

        assertThat(dto.getTitle()).isEqualTo("Updated");
        assertThat(dto.getApproved()).isTrue();
        assertThat(dto.getPlatform()).isEqualTo(PlatformType.FACEBOOK);
        assertThat(dto.getPermanent()).isFalse();
        assertThat(dto.getVideoUrl()).isEqualTo("https://vid.com/vid.mp4");
    }

    @Test
    void createCampaignRequest() {
        CreateCampaignRequest dto = new CreateCampaignRequest();
        dto.setConversationId(1L);
        dto.setName("Campaign 1");
        dto.setTopic("AI Marketing");

        assertThat(dto.getConversationId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Campaign 1");
        assertThat(dto.getTopic()).isEqualTo("AI Marketing");
    }

    @Test
    void aiPostRequest() {
        AiPostRequest dto = new AiPostRequest();
        dto.setTopic("Tech Trends");
        dto.setPlatforms(List.of("LINKEDIN", "FACEBOOK"));
        dto.setGenerateImage(true);
        dto.setGenerateVideo(false);

        assertThat(dto.getTopic()).isEqualTo("Tech Trends");
        assertThat(dto.getPlatforms()).containsExactly("LINKEDIN", "FACEBOOK");
        assertThat(dto.getGenerateImage()).isTrue();
        assertThat(dto.getGenerateVideo()).isFalse();
    }

    @Test
    void aiGeneratedContent() {
        AiGeneratedContent dto = new AiGeneratedContent();
        dto.setLinkedinTitle("LI Title");
        dto.setFacebookTitle("FB Title");
        dto.setLinkedinPost("LI Post");
        dto.setInstagramPost("IG Post");
        dto.setLinkedinHashtags(List.of("#ai", "#tech"));
        dto.setImagePrompt("A futuristic city");

        assertThat(dto.getLinkedinTitle()).isEqualTo("LI Title");
        assertThat(dto.getFacebookTitle()).isEqualTo("FB Title");
        assertThat(dto.getLinkedinPost()).isEqualTo("LI Post");
        assertThat(dto.getInstagramPost()).isEqualTo("IG Post");
        assertThat(dto.getLinkedinHashtags()).containsExactly("#ai", "#tech");
        assertThat(dto.getImagePrompt()).isEqualTo("A futuristic city");
    }

    @Test
    void aiContentPostItem() {
        AiContentPostItem dto = new AiContentPostItem();
        dto.setTitle("Post Title");
        dto.setContent("Post Content");
        dto.setHashtags(List.of("#ai", "#ml"));
        dto.setImagePrompt("Generate image");

        assertThat(dto.getTitle()).isEqualTo("Post Title");
        assertThat(dto.getContent()).isEqualTo("Post Content");
        assertThat(dto.getHashtags()).containsExactly("#ai", "#ml");
        assertThat(dto.getImagePrompt()).isEqualTo("Generate image");
    }

    @Test
    void aiContentPostList() {
        AiContentPostList dto = new AiContentPostList();
        AiContentPostItem item = new AiContentPostItem();
        item.setTitle("Item 1");
        dto.setPosts(List.of(item));

        assertThat(dto.getPosts()).hasSize(1);
        assertThat(dto.getPosts().get(0).getTitle()).isEqualTo("Item 1");
    }

    @Test
    void analyticsRequest() {
        AnalyticsRequest dto = new AnalyticsRequest();
        dto.setPostId(1L);
        dto.setLikes(100);
        dto.setComments(20);
        dto.setShares(5);
        dto.setImpressions(5000);

        assertThat(dto.getPostId()).isEqualTo(1L);
        assertThat(dto.getLikes()).isEqualTo(100);
        assertThat(dto.getComments()).isEqualTo(20);
        assertThat(dto.getShares()).isEqualTo(5);
        assertThat(dto.getImpressions()).isEqualTo(5000);
    }

    @Test
    void analyticsReport() {
        AnalyticsReport dto = new AnalyticsReport();
        dto.setTotalPosts(50);
        dto.setAverageEngagement(0.75);
        dto.setPositiveComments(30);
        dto.setNegativeComments(5);
        dto.setBestPostingHour("10:00 AM");
        dto.setAiRecommendation("Post more videos");

        assertThat(dto.getTotalPosts()).isEqualTo(50);
        assertThat(dto.getAverageEngagement()).isEqualTo(0.75);
        assertThat(dto.getPositiveComments()).isEqualTo(30);
        assertThat(dto.getNegativeComments()).isEqualTo(5);
        assertThat(dto.getBestPostingHour()).isEqualTo("10:00 AM");
        assertThat(dto.getAiRecommendation()).isEqualTo("Post more videos");
    }

    @Test
    void createConversationRequest() {
        CreateConversationRequest dto = new CreateConversationRequest();
        dto.setTitle("Chat Title");
        assertThat(dto.getTitle()).isEqualTo("Chat Title");
    }

    @Test
    void createMessageRequest() {
        CreateMessageRequest dto = new CreateMessageRequest();
        dto.setContent("Hello");
        assertThat(dto.getContent()).isEqualTo("Hello");
    }

    @Test
    void nullValues_areHandled() {
        CreatePostRequest dto = new CreatePostRequest();
        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getContent()).isNull();
        assertThat(dto.getPlatform()).isNull();
        assertThat(dto.isPermanent()).isFalse();
    }
}
