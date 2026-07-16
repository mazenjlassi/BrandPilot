package com.example.metatry.Services.scheduler;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.prompts.WeeklyPostPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyPostPlanner {

    private final PostRepository postRepository;
    private final GeminiService geminiService;
    private final WeeklyPostPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<Post> generateWeeklyPosts(MarketingStrategy strategy, List<Campaign> campaigns) {
        String campaignsContext = campaigns.stream()
                .map(c -> "- Campaign: " + c.getName() + " | Topic: " + c.getTopic())
                .collect(Collectors.joining("\n"));

        List<Post> recentPosts = postRepository.findTop3ByOrderByCreatedAtDesc();
        String previousPostsContext = recentPosts.stream()
                .map(p -> "[" + p.getPlatform() + "] " + p.getTitle() + " - " +
                        (p.getContent() != null && p.getContent().length() > 80
                                ? p.getContent().substring(0, 80) + "..."
                                : p.getContent()))
                .collect(Collectors.joining("\n"));

        String prompt = promptBuilder.build(strategy, campaignsContext, previousPostsContext);
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
                int hour = 9 + (i % 3) * 3;
                post.setScheduledAt(LocalDateTime.of(date, LocalTime.of(hour, 0)));
                postIdx++;
            }
        }
    }

    private LocalDate deriveWeekStart(List<Post> posts) {
        for (Post post : posts) {
            if (post.getScheduledAt() != null) {
                LocalDate d = post.getScheduledAt().toLocalDate();
                return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }
        }
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}