package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialPublisherServiceTest {

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
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.INSTAGRAM)
                .content("Test post")
                .image(image)
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
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.LINKEDIN)
                .content("Test post")
                .image(image)
                .build();
        when(linkedInService.postArticleWithImage(anyString(), anyString(), anyString()))
                .thenReturn(Map.of("success", true, "postId", "li-123"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(linkedInService).postArticleWithImage("Test post", "https://img.com/img.png", "AI Generated Post");
    }

    @Test
    void publishPost_sendsEmail_whenNewlyPublished() {
        PostImage image = PostImage.builder().imageUrl("https://img.com/img.png").build();
        Post post = Post.builder()
                .approved(true)
                .platform(PlatformType.INSTAGRAM)
                .content("Test post")
                .platformPostId("ig-123")
                .image(image)
                .build();
        when(instagramService.postPhotoFromUrl(anyString(), anyString()))
                .thenReturn(Map.of("success", true, "mediaId", "ig-123"));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        socialPublisherService.publishPost(post);

        verify(emailService).sendPostPublishedEmail(any(Post.class));
    }
}
