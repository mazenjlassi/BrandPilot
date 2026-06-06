package com.example.metatry.Services;

import com.example.metatry.Models.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostScheduler {

    private final PostService postService;
    private final SocialPublisherService publisher;

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void publishScheduledPosts() {

        List<Post> posts = postService.getScheduledPostsToPublish();

        for (Post post : posts) {

            try {
                publisher.publishPost(post);
                System.out.println("✅ Published scheduled post: " + post.getId());

            } catch (Exception e) {
                System.err.println("❌ Failed to publish post: " + post.getId());
                e.printStackTrace();
            }
        }
    }
}