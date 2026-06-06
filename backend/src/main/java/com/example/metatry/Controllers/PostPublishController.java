package com.example.metatry.Controllers;

import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.SocialPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/publish")
@RequiredArgsConstructor
public class PostPublishController {

    private final PostRepository postRepository;
    private final SocialPublisherService socialPublisherService;

    @PostMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public Post publishPost(@PathVariable Long postId){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return socialPublisherService.publishPost(post);
    }
}