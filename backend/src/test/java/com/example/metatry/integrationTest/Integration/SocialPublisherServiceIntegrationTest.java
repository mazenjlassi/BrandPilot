package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.*;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class SocialPublisherServiceIntegrationTest {

    @MockitoBean
    private InstagramService instagramService;

    @MockitoBean
    private FacebookService facebookService;

    @MockitoBean
    private LinkedInService linkedInService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private SocialPublisherService socialPublisherService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    private Post approvedPost;

    @BeforeEach
    void setUp() {
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        approvedPost = postRepository.save(Post.builder()
                .title("Pub Post").content("Content")
                .platform(PlatformType.FACEBOOK).status(PostStatus.DRAFT)
                .approved(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        postImageRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    void publishPost_notApproved_throws() {
        Post unapproved = postRepository.save(Post.builder()
                .title("Not Approved").content("Nope")
                .platform(PlatformType.FACEBOOK).approved(false)
                .build());

        assertThatThrownBy(() -> socialPublisherService.publishPost(unapproved))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Post must be approved");
    }

    @Test
    void publishPost_facebook_publishesAndSaves() {
        when(facebookService.postText(anyString())).thenReturn(Map.of("id", "fb-123"));

        Post result = socialPublisherService.publishPost(approvedPost);

        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(result.getPublishedAt()).isNotNull();
        assertThat(result.getPlatformPostId()).isEqualTo("fb-123");
        verify(facebookService).postText(anyString());
    }

    @Test
    void publishPost_instagram_publishes() {
        approvedPost.setPlatform(PlatformType.INSTAGRAM);
        postRepository.save(approvedPost);

        PostImage image = postImageRepository.save(PostImage.builder()
                .imageUrl("http://img.com/a.jpg").sortOrder(0)
                .selected(true).post(approvedPost).build());
        approvedPost.getImages().add(image);

        when(instagramService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("success", true, "mediaId", "ig-456"));

        Post result = socialPublisherService.publishPost(approvedPost);

        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(result.getPlatformPostId()).isEqualTo("ig-456");
    }

    @Test
    void publishPost_linkedIn_publishes() {
        approvedPost.setPlatform(PlatformType.LINKEDIN);
        postRepository.save(approvedPost);

        PostImage image = postImageRepository.save(PostImage.builder()
                .imageUrl("http://img.com/b.jpg").sortOrder(0)
                .selected(true).post(approvedPost).build());
        approvedPost.getImages().add(image);

        when(linkedInService.postArticleWithImage(anyString(), anyString(), anyString()))
                .thenReturn(Map.of("success", true, "postId", "li-789"));

        Post result = socialPublisherService.publishPost(approvedPost);

        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(result.getPlatformPostId()).isEqualTo("li-789");
    }

    @Test
    void publishPost_sendsEmailWhenPlatformPostIdExists() {
        when(facebookService.postText(anyString())).thenReturn(Map.of("id", "fb-email-1"));
        doNothing().when(emailService).sendPostPublishedEmail(any(Post.class));

        socialPublisherService.publishPost(approvedPost);

        verify(emailService).sendPostPublishedEmail(any(Post.class));
    }

    @Test
    void publishPost_emailFailure_doesNotThrow() {
        when(facebookService.postText(anyString())).thenReturn(Map.of("id", "fb-fail"));
        doThrow(new RuntimeException("SMTP down")).when(emailService).sendPostPublishedEmail(any(Post.class));

        Post result = socialPublisherService.publishPost(approvedPost);

        assertThat(result.isNotificationSent()).isFalse();
        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }
}
