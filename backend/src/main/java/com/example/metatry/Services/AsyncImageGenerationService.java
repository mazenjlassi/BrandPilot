package com.example.metatry.Services;

import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.scheduler.WeeklyImageDecisionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsyncImageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncImageGenerationService.class);

    private final PostRepository postRepository;
    private final WeeklyImageDecisionService weeklyImageDecisionService;

    @Async("imageGenerationExecutor")
    public void generateImagesForStrategy(String strategyContext, List<Long> postIds) {
        log.info("Starting async image generation for {} posts", postIds.size());
        List<Post> posts = postRepository.findAllById(postIds);
        try {
            List<String> errors = weeklyImageDecisionService.decideAndGenerateImages(posts, strategyContext);
            if (!errors.isEmpty()) {
                log.warn("Image generation completed with {} errors: {}", errors.size(), errors);
            } else {
                log.info("Image generation completed successfully for {} posts", posts.size());
            }
        } catch (Exception e) {
            log.error("Async image generation failed: {}", e.getMessage(), e);
        }
    }
}