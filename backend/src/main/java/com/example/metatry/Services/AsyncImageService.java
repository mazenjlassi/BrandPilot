package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsyncImageService {

    private static final Logger log = LoggerFactory.getLogger(AsyncImageService.class);

    private final AiImageService aiImageService;
    private final PostRepository postRepository;

    @Async
    @Transactional
    public void generateImagesForPosts(List<Post> posts) {
        for (Post p : posts) {
            if (p.getPlatform() != null && p.getPlatform() != PlatformType.LINKEDIN) {
                try {
                    Post managedPost = postRepository.findById(p.getId()).orElse(null);
                    if (managedPost != null) {
                        aiImageService.generateImageForPost(managedPost);
                        log.info("Image generated for post {} ({})", managedPost.getId(), managedPost.getPlatform());
                    }
                } catch (Exception e) {
                    log.error("Failed to generate image for post {}: {}", p.getId(), e.getMessage());
                }
            }
        }
    }
}
