package com.example.metatry.Services.scheduler;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.prompts.WeeklyPostPromptBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyPostPlanner {

    private static final Map<PlatformType, Integer> PEAK_HOUR_BASE = Map.of(
        PlatformType.LINKEDIN, 8,
        PlatformType.FACEBOOK, 9,
        PlatformType.INSTAGRAM, 11
    );

    private final PostRepository postRepository;
    private final GeminiService geminiService;
    private final WeeklyPostPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<Post> generateWeeklyPosts(MarketingStrategy strategy, List<Campaign> campaigns) {
        String campaignsContext = campaigns.stream()
                .map(c -> "- Campaign: " + c.getName() + " | Topic: " + c.getTopic())
                .collect(Collectors.joining("\n"));

        String campaignPlanContext = buildCampaignPlanContext(strategy);

        List<Post> recentPosts = postRepository.findTop3ByOrderByCreatedAtDesc();
        String previousPostsContext = recentPosts.stream()
                .map(p -> "[" + p.getPlatform() + "] " + p.getTitle() + " - " +
                        (p.getContent() != null && p.getContent().length() > 80
                                ? p.getContent().substring(0, 80) + "..."
                                : p.getContent()))
                .collect(Collectors.joining("\n"));

        String prompt = promptBuilder.build(strategy, campaignsContext, previousPostsContext, campaignPlanContext);
        String aiText = geminiService.generate(prompt);

        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(aiText, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse weekly posts response", e);
        }

        List<Map<String, Object>> postsData = (List<Map<String, Object>>) parsed.get("posts");
        if (postsData == null || postsData.isEmpty()) {
            return List.of();
        }

        List<Post> postsToSave = new ArrayList<>();
        for (Map<String, Object> data : postsData) {
            String platformStr = (String) data.get("platform");
            PlatformType platform;
            try {
                platform = PlatformType.valueOf(platformStr);
            } catch (Exception e) {
                continue;
            }

            boolean needsImage = true;

            Post post = Post.builder()
                    .title((String) data.getOrDefault("title", ""))
                    .content((String) data.getOrDefault("content", ""))
                    .hashtags(data.get("hashtags") != null
                            ? String.join(",", (List<String>) data.get("hashtags"))
                            : "")
                    .platform(platform)
                    .generatedByAI(true)
                    .approved(false)
                    .status(PostStatus.DRAFT)
                    .permanent(data.get("permanent") != null && (Boolean) data.get("permanent"))
                    .link((String) data.getOrDefault("link", "https://3lm-solutions2.odoo.com/contactus"))
                    .needsImage(needsImage)
                    .build();

            if (data.get("scheduledDay") != null && data.get("scheduledHour") != null) {
                try {
                    LocalDate day = LocalDate.parse((String) data.get("scheduledDay"));
                    int hour = ((Number) data.get("scheduledHour")).intValue();
                    post.setScheduledAt(LocalDateTime.of(day, LocalTime.of(hour, 0)));
                } catch (Exception ignored) {
                }
            }

            postsToSave.add(post);
        }

        if (!campaigns.isEmpty()) {
            assignCampaignsSequentially(postsToSave, campaigns);
        }

        return postRepository.saveAll(postsToSave);
    }

    private void assignCampaignsSequentially(List<Post> posts, List<Campaign> campaigns) {
        if (posts.isEmpty() || campaigns.isEmpty()) return;

        int totalPosts = posts.size();
        int numCampaigns = campaigns.size();
        int base = totalPosts / numCampaigns;
        int remainder = totalPosts % numCampaigns;

        LocalDate weekStart = deriveWeekStart(posts);

        int postIdx = 0;
        for (int c = 0; c < numCampaigns; c++) {
            int count = base + (c < remainder ? 1 : 0);
            for (int i = 0; i < count && postIdx < totalPosts; i++) {
                Post post = posts.get(postIdx);
                post.setCampaign(campaigns.get(c));
                LocalDate date = weekStart.plusDays(Math.min(c * 2 + (i * 2 / Math.max(count, 1)), 6));
                int baseHour = PEAK_HOUR_BASE.getOrDefault(post.getPlatform(), 9);
                int hour = baseHour + (i % 3);
                post.setScheduledAt(LocalDateTime.of(date, LocalTime.of(hour, 0)));
                postIdx++;
            }
        }
    }

    private String buildCampaignPlanContext(MarketingStrategy strategy) {
        if (strategy.getCampaignPlans() == null || strategy.getCampaignPlans().isBlank()) {
            return "";
        }
        try {
            List<Map<String, Object>> plans = objectMapper.readValue(
                    strategy.getCampaignPlans(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> plan : plans) {
                String name = (String) plan.getOrDefault("name", "Campaign");
                Object dist = plan.get("weeklyPostDistribution");
                if (dist instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> distMap = (Map<String, Object>) dist;
                    int total = distMap.values().stream()
                            .filter(Number.class::isInstance)
                            .mapToInt(v -> ((Number) v).intValue())
                            .sum();
                    sb.append("- ").append(name).append(": ").append(total).append(" posts (");
                    List<String> parts = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : distMap.entrySet()) {
                        parts.add(entry.getValue() + " " + entry.getKey());
                    }
                    sb.append(String.join(", ", parts)).append(")\n");
                }
            }
            return sb.length() > 0 ? sb.toString() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private LocalDate deriveWeekStart(List<Post> posts) {
        LocalDate today = LocalDate.now();
        for (Post post : posts) {
            if (post.getScheduledAt() != null) {
                LocalDate d = post.getScheduledAt().toLocalDate();
                LocalDate monday = d.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
                if (!monday.isBefore(today)) return monday;
            }
        }
        return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }
}