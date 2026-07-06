package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Models.Post;
import com.example.metatry.Services.PostScheduler;
import com.example.metatry.Services.PostService;
import com.example.metatry.Services.SocialPublisherService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PostSchedulerIntegrationTest {

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private SocialPublisherService publisher;

    @Autowired
    private PostScheduler postScheduler;

    @Test
    void publishScheduledPosts_noPosts_doesNothing() {
        when(postService.getScheduledPostsToPublish()).thenReturn(List.of());

        postScheduler.publishScheduledPosts();

        verify(publisher, never()).publishPost(any());
    }

    @Test
    void publishScheduledPosts_withPosts_publishesEach() {
        Post post1 = Post.builder().id(1L).build();
        Post post2 = Post.builder().id(2L).build();

        when(postService.getScheduledPostsToPublish()).thenReturn(List.of(post1, post2));

        postScheduler.publishScheduledPosts();

        verify(publisher).publishPost(post1);
        verify(publisher).publishPost(post2);
    }

    @Test
    void publishScheduledPosts_publishFails_continuesToNext() {
        Post post1 = Post.builder().id(1L).build();
        Post post2 = Post.builder().id(2L).build();

        when(postService.getScheduledPostsToPublish()).thenReturn(List.of(post1, post2));
        doThrow(new RuntimeException("FB error")).when(publisher).publishPost(post1);

        postScheduler.publishScheduledPosts();

        verify(publisher).publishPost(post1);
        verify(publisher).publishPost(post2);
    }
}
