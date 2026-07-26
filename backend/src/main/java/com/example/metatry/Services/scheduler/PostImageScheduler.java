package com.example.metatry.Services.scheduler;

import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.AiImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostImageScheduler {

    private static final Logger log = LoggerFactory.getLogger(PostImageScheduler.class);

    private final PostRepository postRepository;
    private final AiImageService aiImageService;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void generatePendingImages() {
        List<Post> pending = postRepository.findPlatformsNeedingImagesWithEmptyImages();
        if (pending.isEmpty()) return;

        log.info("Image scheduler: {} posts pending image generation", pending.size());
        for (Post post : pending) {
            try {
                aiImageService.generateImageForPost(post);
                log.info("Image generated for post #{} ({}: {})", post.getId(), post.getPlatform(), post.getTitle());
            } catch (Exception e) {
                log.warn("Image generation failed for post #{} ({}): {}", post.getId(), post.getTitle(), e.getMessage());
            }
        }
    }
}