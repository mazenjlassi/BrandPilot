package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialPublisherServiceUnitTest {

    @Mock private InstagramService instagramService;
    @Mock private FacebookService facebookService;
    @Mock private LinkedInService linkedInService;
    @Mock private EmailService emailService;
    @Mock private PostRepository postRepository;

    @InjectMocks
    private SocialPublisherService socialPublisherService;

    @Test
    void publishPost_throwsWhenNotApproved() {
        Post post = Post.builder()
                .approved(false)
                .platform(PlatformType.INSTAGRAM)
                .content("Test content")
                .build();

        assertThatThrownBy(() -> socialPublisherService.publishPost(post))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("approved");
    }

    @Test
    void publishPost_publishesToInstagram() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.INSTAGRAM)
                .content("Test post")
                .images(List.of(image))
                .build();
        when(instagramService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("success", true, "mediaId", "ig-123"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(instagramService).postPhotoFromUrl("https://img.com/img.png", "Test post");
    }

    @Test
    void publishPost_publishesToFacebook() {
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.FACEBOOK)
                .content("Test post")
                .build();
        when(facebookService.postText(anyString()))
                .thenReturn(Map.of("id", "fb-123"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(facebookService).postText("Test post");
    }

    @Test
    void publishPost_publishesToLinkedIn() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.LINKEDIN)
                .content("Test post")
                .images(List.of(image))
                .build();
        when(linkedInService.postArticleWithImage(anyString(), anyString(), anyString()))
                .thenReturn(Map.of("success", true, "postId", "li-123"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(linkedInService).postArticleWithImage("Test post", "https://img.com/img.png", "AI Generated Post");
    }

    @Test
    void publishPost_sendsEmail_whenNewlyPublished() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.INSTAGRAM)
                .content("Test post")
                .images(List.of(image))
                .build();
        when(instagramService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("success", true, "mediaId", "ig-123"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(emailService).sendPostPublishedEmail(any(Post.class));
    }

    // ================= BUSINESS BEHAVIOR — platform guards =================

    @Test
    void publishInstagram_skipsWhenNoImage() {
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.INSTAGRAM)
                .content("Test")
                .images(null)
                .build();
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Post result = socialPublisherService.publishPost(post);

        assertThat(result.getStatus()).isEqualTo(com.example.metatry.Enums.PostStatus.PUBLISHED);
        assertThat(result.getPlatformPostId()).isNull();
    }

    @Test
    void publishFacebook_withImage_callsPostPhoto() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.FACEBOOK)
                .content("Test")
                .images(List.of(image))
                .build();
        when(facebookService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("id", "fb-123"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(facebookService).postPhotoFromUrl("https://img.com/img.png", "Test");
    }

    @Test
    void publishLinkedIn_withoutImage_callsPostText() {
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.LINKEDIN)
                .content("Text only post")
                .images(null)
                .build();
        when(linkedInService.postText(anyString()))
                .thenReturn(Map.of("success", true, "postId", "li-456"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(linkedInService).postText("Text only post");
    }

    // ================= BUSINESS BEHAVIOR — publication state =================

    @Test
    void publishPost_setsPublishedAtAndStatus() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.INSTAGRAM)
                .content("Test")
                .images(List.of(image))
                .build();
        when(instagramService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("success", true, "mediaId", "ig-1"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Post result = socialPublisherService.publishPost(post);

        assertThat(result.getStatus()).isEqualTo(com.example.metatry.Enums.PostStatus.PUBLISHED);
        assertThat(result.getPublishedAt()).isNotNull();
    }

    @Test
    void publishPost_skipsEmailWhenNotificationSent() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.FACEBOOK)
                .content("Test")
                .images(List.of(image))
                .notificationSent(true)
                .build();
        when(facebookService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("id", "fb-1"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(emailService, never()).sendPostPublishedEmail(any());
    }

    @Test
    void publishPost_doesNotCrashWhenEmailFails() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.INSTAGRAM)
                .content("Test")
                .images(List.of(image))
                .build();
        when(instagramService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("success", true, "mediaId", "ig-1"));
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendPostPublishedEmail(any());
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Post result = socialPublisherService.publishPost(post);

        assertThat(result).isNotNull();
    }

    @Test
    void publishPost_throwsWhenApprovedIsNull() {
        Post post = Post.builder()
                .approved(null)
                .platform(PlatformType.FACEBOOK)
                .content("Test")
                .build();

        assertThatThrownBy(() -> socialPublisherService.publishPost(post))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("approved");
    }

    // ================= BUSINESS BEHAVIOR — caption building =================

    @Test
    void publishPost_captionIncludesLinkAndHashtags() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").selected(true).build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.LINKEDIN)
                .content("Main content")
                .link("https://link.com/article")
                .hashtags("#AI #Tech")
                .images(List.of(image))
                .build();
        when(linkedInService.postArticleWithImage(anyString(), anyString(), anyString()))
                .thenReturn(Map.of("success", true, "postId", "li-1"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(linkedInService).postArticleWithImage(captor.capture(), anyString(), anyString());
        String caption = captor.getValue();
        assertThat(caption).contains("Main content");
        assertThat(caption).contains("https://link.com/article");
        assertThat(caption).contains("#AI #Tech");
    }

    @Test
    void publishPost_captionExcludesNullLink() {
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.FACEBOOK)
                .content("Main content")
                .link(null)
                .hashtags(null)
                .build();
        when(facebookService.postText(anyString()))
                .thenReturn(Map.of("id", "fb-1"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(facebookService).postText(captor.capture());
        assertThat(captor.getValue()).isEqualTo("Main content");
    }
}
