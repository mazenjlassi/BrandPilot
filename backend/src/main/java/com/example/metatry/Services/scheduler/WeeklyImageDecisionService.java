package com.example.metatry.Services.scheduler;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.AiImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyImageDecisionService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyImageDecisionService.class);

    private final AiImageService aiImageService;

    @Transactional
    public List<String> decideAndGenerateImages(List<Post> posts, String strategyContext) {
        List<String> errors = new java.util.ArrayList<>();
        for (Post post : posts) {
            if (post.getPlatform() == null) continue;
            if (post.getPlatform() == null || post.getPlatform() == PlatformType.LINKEDIN) continue;

            try {
                aiImageService.generateImageForPost(post);
            } catch (Exception e) {
                log.warn("Failed to generate image for post #{} ({}: {}): {}",
                        post.getId(), post.getPlatform(), post.getTitle(), e.getMessage());
                errors.add("Post #" + post.getId() + " (" + post.getPlatform() + "): " + e.getMessage());
            }
        }
        return errors;
    }
}