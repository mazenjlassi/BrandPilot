package com.example.metatry.Services.scheduler;

import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Services.AiImageService;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.prompts.WeeklyImageDecisionPromptBuilder;
import com.example.metatry.Services.prompts.WeeklyImageGenPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeeklyImageDecisionService {

    private final GeminiService geminiService;
    private final WeeklyImageDecisionPromptBuilder decisionPromptBuilder;
    private final WeeklyImageGenPromptBuilder genPromptBuilder;
    private final AiImageService aiImageService;
    private final PostImageRepository postImageRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void decideAndGenerateImages(List<Post> posts, String strategyContext) {
        for (Post post : posts) {
            if (post.getPlatform() == null) continue;

            String decisionPrompt = decisionPromptBuilder.build(
                    post.getTitle() + "\n" + post.getContent(),
                    post.getPlatform().name(),
                    strategyContext
            );

            String aiText;
            try {
                aiText = geminiService.generate(decisionPrompt);
            } catch (Exception e) {
                continue;
            }

            Map<String, Object> parsed;
            try {
                parsed = objectMapper.readValue(aiText, Map.class);
            } catch (Exception e) {
                continue;
            }

            boolean needsImage = parsed.get("needsImage") != null && (Boolean) parsed.get("needsImage");
            if (!needsImage) continue;

            String imagePrompt = (String) parsed.getOrDefault("imagePrompt", "");
            String optimizedPrompt = genPromptBuilder.build(post.getContent(), post.getPlatform(), imagePrompt);

            try {
                ImageSize size = switch (post.getPlatform()) {
                    case INSTAGRAM -> ImageSize.SQUARE;
                    case LINKEDIN, FACEBOOK -> ImageSize.LANDSCAPE;
                };

                String imageUrl = aiImageService.generateAndUploadImage(optimizedPrompt, size);

                PostImage image = PostImage.builder()
                        .imageUrl(imageUrl)
                        .imagePrompt(optimizedPrompt)
                        .size(size)
                        .post(post)
                        .selected(true)
                        .build();

                postImageRepository.save(image);
            } catch (Exception ignored) {
            }
        }
    }
}
