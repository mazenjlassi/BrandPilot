package com.example.metatry.Services;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.DTOs.PatternResponse;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatternAnalysisService {

    private final ContentPatternRepository contentPatternRepository;
    private final ScrapedPostRepository scrapedPostRepository;
    private final GeminiService geminiService;
    private final MemoryContextService memoryContextService;
    private final ObjectMapper objectMapper;

    public PatternResponse analyzePattern(PatternAnalysisRequest request) {
        try {
            String topic = request.getTopic();
            String platform = request.getPlatform() != null ? request.getPlatform() : "linkedin";
            int minPosts = request.getMinPostsRequired() != null ? request.getMinPostsRequired() : 3;

            List<ScrapedPost> posts;
            if (request.getCompanyName() != null && !request.getCompanyName().isEmpty()) {
                posts = scrapedPostRepository.findByCompanyNameAndPlatform(request.getCompanyName(), platform);
            } else {
                posts = scrapedPostRepository.findByPlatform(platform);
            }

            if (posts.size() < minPosts) {
                return PatternResponse.builder()
                    .status("error")
                    .message("Not enough posts. Found " + posts.size() + ", need at least " + minPosts)
                    .build();
            }

            String postsText = posts.stream()
                .map(p -> "- " + p.getPostText())
                .collect(Collectors.joining("\n"));

            String prompt = buildAnalysisPrompt(topic, platform, postsText);

            String memory = memoryContextService.getRecentContext();
            String fullPrompt = prompt + "\n\n" + memory;

            String aiResponse = geminiService.generate(fullPrompt);

            ContentPattern pattern = parseAndSavePattern(topic, platform, aiResponse, posts.size());

            return PatternResponse.builder()
                .id(pattern.getId())
                .topic(pattern.getTopic())
                .campaignName(pattern.getCampaignName())
                .platformBreakdown(pattern.getPlatformBreakdown())
                .postFrequency(pattern.getPostFrequency())
                .contentLength(pattern.getContentLength())
                .mediaType(pattern.getMediaType())
                .hashtagCount(pattern.getHashtagCount())
                .timingPattern(pattern.getTimingPattern())
                .tone(pattern.getTone())
                .ctaStyle(pattern.getCtaStyle())
                .totalPostsAnalyzed(pattern.getTotalPostsAnalyzed())
                .extractedAt(pattern.getExtractedAt())
                .status("success")
                .message("Pattern extracted from " + posts.size() + " posts")
                .build();

        } catch (Exception e) {
            return PatternResponse.builder()
                .status("error")
                .message("Analysis failed: " + e.getMessage())
                .build();
        }
    }

    public int analyzeUnanalyzedBatch(String companyName) {
        List<ScrapedPost> posts = scrapedPostRepository.findTop30ByCompanyNameAndUsedForPatternFalse(companyName);
        if (posts == null || posts.size() < 3) {
            return 0;
        }

        String postsText = buildPostsTextWithIndices(posts);

        String prompt = buildCampaignAnalysisPrompt(postsText);

        String memory = memoryContextService.getRecentContext();
        String fullPrompt = prompt + "\n\n" + memory;

        String aiResponse = geminiService.generate(fullPrompt);

        return parseAndSaveCampaigns(aiResponse, posts, companyName);
    }

    private String buildPostsTextWithIndices(List<ScrapedPost> posts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < posts.size(); i++) {
            ScrapedPost p = posts.get(i);
            sb.append("[").append(i).append("] ");
            sb.append("Platform: ").append(p.getPlatform()).append(" | ");
            sb.append("Text: ").append(p.getPostText()).append("\n\n");
        }
        return sb.toString();
    }

    private String buildCampaignAnalysisPrompt(String postsText) {
        return """
            Analyze these social media posts and identify cohesive campaigns.

            A "campaign" = 3+ posts across platforms (LinkedIn/Instagram/Facebook)
            that share the same topic/theme.

            For each campaign found, extract:
            - campaignName: descriptive name for the campaign
            - topic: main theme/topic
            - postIndices: array of post indices that belong to this campaign
            - platformBreakdown: count of posts per platform (e.g., {"linkedin": 2, "instagram": 3, "facebook": 2})
            - tone: e.g., Technical/educational, casual/friendly
            - contentLength: e.g., 150-300 chars, under 100
            - mediaType: e.g., 80%% images, 20%% videos
            - hashtagCount: e.g., 3-5 per post, none, 10+
            - timingPattern: e.g., Tuesday/Thursday 9-11am
            - ctaStyle: e.g., Links to articles, questions to engage

            Posts that don't belong to any campaign = noise/reused, ignore them.

            Return ONLY valid JSON array. No markdown. No explanation.

            Example format:
            [
              {
                "campaignName": "AI Awareness Series",
                "topic": "AI",
                "postIndices": [0, 2, 5, 8, 12],
                "platformBreakdown": {"linkedin": 2, "instagram": 2, "facebook": 1},
                "tone": "Educational/technical",
                "contentLength": "200-400 chars",
                "mediaType": "Mostly images with infographics",
                "hashtagCount": "3-5 per post",
                "timingPattern": "Tuesday/Thursday 9-11am",
                "ctaStyle": "Links to blog articles"
              }
            ]

            Posts:
            %s
            """.formatted(postsText);
    }

    private int parseAndSaveCampaigns(String aiResponse, List<ScrapedPost> allPosts, String companyName) {
        try {
            String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();

            JsonNode root = objectMapper.readTree(cleanJson);

            JsonNode campaigns;
            if (root.isArray()) {
                campaigns = root;
            } else if (root.isObject()) {
                campaigns = root.path("campaigns");
                if (campaigns.isMissingNode() || !campaigns.isArray()) {
                    campaigns = root.path("campaignIndices");
                }
                if (campaigns.isMissingNode() || !campaigns.isArray()) {
                    return 0;
                }
            } else {
                return 0;
            }

            int savedCount = 0;

            for (JsonNode campaign : campaigns) {
                String topic = truncate(campaign.path("topic").asText(""), 255);
                if (topic.isEmpty()) continue;

                String campaignName = truncate(campaign.path("campaignName").asText(""), 255);
                String tone = truncate(campaign.path("tone").asText(""), 255);
                String contentLength = truncate(campaign.path("contentLength").asText(""), 100);
                String mediaType = truncate(campaign.path("mediaType").asText(""), 100);
                String hashtagCount = truncate(campaign.path("hashtagCount").asText(""), 100);
                String timingPattern = truncate(campaign.path("timingPattern").asText(""), 200);
                String ctaStyle = truncate(campaign.path("ctaStyle").asText(""), 500);
                String postFrequency = truncate(campaign.path("postFrequency").asText(""), 100);

                JsonNode platformBreakdownNode = campaign.path("platformBreakdown");
                String platformBreakdown = platformBreakdownNode.isObject()
                        ? objectMapper.writeValueAsString(platformBreakdownNode)
                        : "";

                List<Long> postIds = new ArrayList<>();
                JsonNode postIndicesNode = campaign.path("postIndices");
                if (postIndicesNode.isArray()) {
                    for (JsonNode idxNode : postIndicesNode) {
                        int idx = idxNode.asInt(-1);
                        if (idx >= 0 && idx < allPosts.size()) {
                            ScrapedPost post = allPosts.get(idx);
                            postIds.add(post.getId());
                            post.setUsedForPattern(true);
                        }
                    }
                }

                if (postIds.isEmpty()) continue;

                String usedPostIdsJson = objectMapper.writeValueAsString(postIds);

                ContentPattern pattern = ContentPattern.builder()
                    .companyName(companyName)
                    .topic(topic)
                    .campaignName(campaignName)
                    .platformBreakdown(platformBreakdown)
                    .usedPostIds(usedPostIdsJson)
                    .tone(tone)
                    .contentLength(contentLength)
                    .mediaType(mediaType)
                    .hashtagCount(hashtagCount)
                    .timingPattern(timingPattern)
                    .ctaStyle(ctaStyle)
                    .postFrequency(postFrequency)
                    .totalPostsAnalyzed(postIds.size())
                    .aiAnalysisRaw(truncate(aiResponse, 5000))
                    .build();

                contentPatternRepository.save(pattern);
                savedCount++;
            }

            scrapedPostRepository.saveAll(allPosts);
            return savedCount;

        } catch (Exception e) {
            System.out.println("Failed to parse campaigns: " + e.getMessage());
            return 0;
        }
    }

    public List<ContentPattern> getAllPatterns() {
        return contentPatternRepository.findAll();
    }

    public List<ContentPattern> getPatternsByCompany(String companyName) {
        return contentPatternRepository.findByCompanyName(companyName);
    }

    public ContentPattern getPatternByTopic(String topic) {
        return contentPatternRepository.findByTopic(topic).orElse(null);
    }

    public List<ContentPattern> findMatchingPatterns(String topic) {
        List<ContentPattern> result = new ArrayList<>();

        contentPatternRepository.findByTopic(topic).ifPresent(result::add);

        if (result.isEmpty()) {
            String[] keywords = topic.split("\\s+");
            for (String keyword : keywords) {
                if (keyword.length() < 3) continue;
                List<ContentPattern> matches = contentPatternRepository.findByTopicContainingIgnoreCase(keyword);
                for (ContentPattern p : matches) {
                    if (!result.contains(p)) {
                        result.add(p);
                    }
                }
            }
        }

        return result;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() > maxLength ? value.substring(0, maxLength - 3) + "..." : value;
    }

    private String extractJsonValue(String json, String key) {
        try {
            String[] keys = {key, key.replace("Case", "_case"), key.replace("_", "")};
            for (String k : keys) {
                String pattern = "\"" + k + "\"\\s*:\\s*\"([^\"]+)\"";
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher m = p.matcher(json);
                if (m.find()) {
                    return m.group(1);
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String buildAnalysisPrompt(String topic, String platform, String postsText) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following social media posts from a ").append(topic).append(" company on ").append(platform).append(" and extract content patterns.\n\n");
        prompt.append("Posts:\n").append(postsText).append("\n\n");
        prompt.append("Return a JSON with these exact fields (no markdown, just plain JSON):\n");
        prompt.append("{\n");
        prompt.append("  \"postFrequency\": \"e.g., 3x/week, daily, weekly\",\n");
        prompt.append("  \"contentLength\": \"e.g., 150-300 chars, under 100, 500+\",\n");
        prompt.append("  \"mediaType\": \"e.g., 80% images, 20% videos, mostly text\",\n");
        prompt.append("  \"hashtagCount\": \"e.g., 3-5 per post, none, 10+\",\n");
        prompt.append("  \"timingPattern\": \"e.g., Tuesday/Thursday 9-11am, morning posts, evening posts\",\n");
        prompt.append("  \"tone\": \"e.g., Technical/educational, casual/friendly, formal/professional\",\n");
        prompt.append("  \"ctaStyle\": \"e.g., Links to articles, questions to engage, calls to action\"\n");
        prompt.append("}");
        return prompt.toString();
    }

    private ContentPattern parseAndSavePattern(String topic, String platform, String aiResponse, int postsCount) {
        ContentPattern pattern = ContentPattern.builder()
            .topic(topic)
            .totalPostsAnalyzed(postsCount)
            .build();

        try {
            if (aiResponse.contains("postFrequency")) {
                pattern.setPostFrequency(truncate(extractJsonValue(aiResponse, "postFrequency"), 100));
                pattern.setContentLength(truncate(extractJsonValue(aiResponse, "contentLength"), 100));
                pattern.setMediaType(truncate(extractJsonValue(aiResponse, "mediaType"), 100));
                pattern.setHashtagCount(truncate(extractJsonValue(aiResponse, "hashtagCount"), 100));
                pattern.setTimingPattern(truncate(extractJsonValue(aiResponse, "timingPattern"), 200));
                pattern.setTone(truncate(extractJsonValue(aiResponse, "tone"), 255));
                pattern.setCtaStyle(truncate(extractJsonValue(aiResponse, "ctaStyle"), 500));
                pattern.setAiAnalysisRaw(truncate(aiResponse, 5000));
            }
        } catch (Exception ignored) {}

        return contentPatternRepository.save(pattern);
    }
}
