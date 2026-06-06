package com.example.metatry.Services;

import com.example.metatry.DTOs.AiContentPostItem;
import com.example.metatry.DTOs.AiContentPostList;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiContentService {

    private final PromptBuilderService promptBuilderService;
    private final GeminiService geminiService;
    private final MemoryContextService memoryContextService;
    private final PatternAnalysisService patternAnalysisService;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final ContentPatternRepository contentPatternRepository;

    private final ObjectMapper objectMapper;

    public List<Post> generatePostsWithCampaign(
            String topic,
            Campaign campaign,
            String insights,
            String conclusion
    ) {
        String safeInsights = (insights == null || insights.isBlank())
                ? "No insights available"
                : insights;

        String safeConclusion = (conclusion == null || conclusion.isBlank())
                ? "Focus on engagement, clarity, and value"
                : conclusion;

        ContentPattern pattern = findBestMatchingPattern(topic);

        if (pattern != null && pattern.getPlatformBreakdown() != null && !pattern.getPlatformBreakdown().isEmpty()) {
            return generateFromPattern(topic, campaign, safeInsights, safeConclusion, pattern);
        }

        return generateEstimatedPosts(topic, campaign, safeInsights, safeConclusion, pattern);
    }

    private ContentPattern findBestMatchingPattern(String topic) {
        ContentPattern pattern = contentPatternRepository.findByTopic(topic).orElse(null);
        if (pattern != null) return pattern;

        List<ContentPattern> matches = patternAnalysisService.findMatchingPatterns(topic);
        if (!matches.isEmpty()) return matches.get(0);

        return null;
    }

    private List<Post> generateFromPattern(
            String topic,
            Campaign campaign,
            String insights,
            String conclusion,
            ContentPattern pattern
    ) {
        Map<String, Integer> breakdown = parsePlatformBreakdown(pattern.getPlatformBreakdown());
        if (breakdown == null || breakdown.isEmpty()) return List.of();

        List<Post> allPosts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            PlatformType platform = mapToPlatformType(entry.getKey());
            int count = entry.getValue();
            if (platform == null || count <= 0) continue;

            allPosts.addAll(generatePlatformPosts(topic, campaign, insights, conclusion, pattern, platform, count));
        }
        return allPosts;
    }

    private List<Post> generatePlatformPosts(
            String topic,
            Campaign campaign,
            String insights,
            String conclusion,
            ContentPattern pattern,
            PlatformType platform,
            int count
    ) {
        try {
            String prompt = promptBuilderService.buildPlatformPrompt(topic, insights, conclusion, pattern, platform, count);
            String memory = memoryContextService.getMatchingContext(topic);
            String fullPrompt = prompt + "\n\n" + memory;
            String aiText = geminiService.generate(fullPrompt);

            AiContentPostList postList = objectMapper.readValue(aiText, AiContentPostList.class);

            List<Post> postsToSave = new ArrayList<>();
            for (AiContentPostItem item : postList.getPosts()) {
                postsToSave.add(createPost(item.getTitle(), item.getContent(), item.getHashtags(), platform, campaign));
            }

            List<Post> savedPosts = postRepository.saveAll(postsToSave);

            List<PostImage> imagesToSave = new ArrayList<>();
            for (int i = 0; i < savedPosts.size(); i++) {
                Post post = savedPosts.get(i);
                AiContentPostItem item = postList.getPosts().get(i);

                ImageSize size = switch (platform) {
                    case INSTAGRAM -> ImageSize.SQUARE;
                    case LINKEDIN, FACEBOOK -> ImageSize.LANDSCAPE;
                };

                PostImage image = PostImage.builder()
                        .imagePrompt(item.getImagePrompt())
                        .size(size)
                        .post(post)
                        .selected(false)
                        .build();
                imagesToSave.add(image);
            }
            postImageRepository.saveAll(imagesToSave);

            return savedPosts;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating " + platform + " posts: " + e.getMessage());
        }
    }

    private Map<String, Integer> parsePlatformBreakdown(String breakdownJson) {
        if (breakdownJson == null || breakdownJson.isBlank()) return null;
        try {
            return objectMapper.readValue(breakdownJson, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private PlatformType mapToPlatformType(String key) {
        if (key == null) return null;
        return switch (key.toLowerCase()) {
            case "linkedin" -> PlatformType.LINKEDIN;
            case "instagram" -> PlatformType.INSTAGRAM;
            case "facebook" -> PlatformType.FACEBOOK;
            default -> null;
        };
    }

    private List<Post> generateEstimatedPosts(
            String topic,
            Campaign campaign,
            String insights,
            String conclusion,
            ContentPattern pattern
    ) {
        try {
            String prompt = promptBuilderService.buildEstimatedPrompt(topic, insights, conclusion, pattern);

            String memory = memoryContextService.getMatchingContext(topic);
            String fullPrompt = prompt + "\n\n" + memory;

            String aiText = geminiService.generate(fullPrompt);

            AiContentPostList postList = objectMapper.readValue(aiText, AiContentPostList.class);

            if (postList.getPosts() == null || postList.getPosts().isEmpty()) {
                return List.of();
            }

            List<Post> postsToSave = new ArrayList<>();
            for (AiContentPostItem item : postList.getPosts()) {
                PlatformType platform = mapToPlatformType(item.getPlatform());
                if (platform == null) continue;

                postsToSave.add(createPost(
                        item.getTitle(),
                        item.getContent(),
                        item.getHashtags(),
                        platform,
                        campaign
                ));
            }

            List<Post> savedPosts = postRepository.saveAll(postsToSave);

            List<PostImage> imagesToSave = new ArrayList<>();
            for (int i = 0; i < savedPosts.size(); i++) {
                Post post = savedPosts.get(i);
                AiContentPostItem item = postList.getPosts().get(i);

                ImageSize size = switch (post.getPlatform()) {
                    case INSTAGRAM -> ImageSize.SQUARE;
                    case LINKEDIN, FACEBOOK -> ImageSize.LANDSCAPE;
                };

                PostImage image = PostImage.builder()
                        .imagePrompt(item.getImagePrompt())
                        .size(size)
                        .post(post)
                        .selected(false)
                        .build();
                imagesToSave.add(image);
            }
            postImageRepository.saveAll(imagesToSave);

            return savedPosts;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating AI posts: " + e.getMessage());
        }
    }

    private Post createPost(
            String title,
            String content,
            List<String> hashtags,
            PlatformType platform,
            Campaign campaign
    ) {

        return Post.builder()
                .title(title)
                .content(content)
                .hashtags(hashtags != null ? String.join(",", hashtags) : "")
                .platform(platform)
                .generatedByAI(true)
                .approved(false)
                .status(PostStatus.DRAFT)
                .campaign(campaign)
                .permanent(false)
                .link("https://3lm-solutions2.odoo.com/contactus")
                .build();
    }
}