package com.example.metatry.Services;

import com.example.metatry.Models.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostSchedulerTest {

    @Mock private PostService postService;
    @Mock private SocialPublisherService publisher;

    @InjectMocks
    private PostScheduler postScheduler;

    @Test
    void publishScheduledPosts_withNoPosts_doesNothing() {
        when(postService.getScheduledPostsToPublish()).thenReturn(List.of());

        postScheduler.publishScheduledPosts();

        verify(publisher, never()).publishPost(any());
    }

    @Test
    void publishScheduledPosts_publishesAllScheduledPosts() {
        Post post1 = Post.builder().id(1L).build();
        Post post2 = Post.builder().id(2L).build();
        when(postService.getScheduledPostsToPublish()).thenReturn(List.of(post1, post2));

        postScheduler.publishScheduledPosts();

        verify(publisher).publishPost(post1);
        verify(publisher).publishPost(post2);
    }

    @Test
    void publishScheduledPosts_whenPublishFails_continuesToNext() {
        Post post1 = Post.builder().id(1L).build();
        Post post2 = Post.builder().id(2L).build();
        when(postService.getScheduledPostsToPublish()).thenReturn(List.of(post1, post2));
        doThrow(new RuntimeException("Publish error")).when(publisher).publishPost(post1);

        postScheduler.publishScheduledPosts();

        verify(publisher).publishPost(post1);
        verify(publisher).publishPost(post2);
    }
}
