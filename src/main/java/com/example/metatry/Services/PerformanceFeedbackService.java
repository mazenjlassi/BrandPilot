package com.example.metatry.Services;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceFeedbackService {

    private final ContentPatternRepository contentPatternRepository;
    private final PostRepository postRepository;
    private final GeminiService geminiService;

    private LocalDateTime lastFeedbackRun = LocalDateTime.now().minusDays(1);

    public void updatePatternsFromPerformance() {
        List<Post> newEngagedPosts = postRepository.findByStatus(com.example.metatry.Enums.PostStatus.PUBLISHED)
                .stream()
                .filter(p -> p.getEngagementScore() != null && p.getEngagementScore() > 0)
                .filter(p -> p.getPublishedAt() != null && p.getPublishedAt().isAfter(lastFeedbackRun))
                .collect(Collectors.toList());

        if (newEngagedPosts.isEmpty()) {
            System.out.println("PerformanceFeedback: No new engagement data to process");
            return;
        }

        System.out.println("PerformanceFeedback: Processing " + newEngagedPosts.size() + " posts with new engagement data");

        List<ContentPattern> patterns = contentPatternRepository.findAll();
        if (patterns.isEmpty()) {
            lastFeedbackRun = LocalDateTime.now();
            return;
        }

        List<PatternPerformance> performances = new ArrayList<>();

        for (ContentPattern pattern : patterns) {
            List<Post> matchingPosts = findMatchingPosts(pattern, newEngagedPosts);

            double avgEngagement = matchingPosts.stream()
                    .mapToDouble(p -> p.getEngagementScore() != null ? p.getEngagementScore() : 0)
                    .average()
                    .orElse(0.0);

            int totalPosts = matchingPosts.size();

            PatternPerformance perf = new PatternPerformance();
            perf.pattern = pattern;
            perf.avgEngagement = avgEngagement;
            perf.totalPosts = totalPosts;
            perf.matchingPosts = matchingPosts;
            performances.add(perf);
        }

        String globalAdvice = generateGlobalAdvice(performances);

        for (PatternPerformance perf : performances) {
            ContentPattern pattern = perf.pattern;
            pattern.setAvgEngagementScore(perf.avgEngagement);
            pattern.setTotalPostsGenerated(perf.totalPosts);
            pattern.setPerformanceAdvice(globalAdvice);
            pattern.setLastPerformanceUpdate(LocalDateTime.now());
            contentPatternRepository.save(pattern);
        }

        lastFeedbackRun = LocalDateTime.now();
        System.out.println("PerformanceFeedback: Updated " + performances.size() + " patterns");
    }

    private List<Post> findMatchingPosts(ContentPattern pattern, List<Post> allPosts) {
        String topic = pattern.getTopic();
        if (topic == null || topic.isBlank()) return List.of();

        return allPosts.stream()
                .filter(p -> p.getCampaign() != null && p.getCampaign().getTopic() != null)
                .filter(p -> {
                    String campaignTopic = p.getCampaign().getTopic().toLowerCase();
                    String patternTopic = topic.toLowerCase();
                    return campaignTopic.equals(patternTopic)
                            || campaignTopic.contains(patternTopic)
                            || patternTopic.contains(campaignTopic);
                })
                .collect(Collectors.toList());
    }

    private String generateGlobalAdvice(List<PatternPerformance> performances) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the performance of these content patterns and provide actionable advice.\n\n");

        for (PatternPerformance perf : performances) {
            ContentPattern p = perf.pattern;
            sb.append("Pattern: ").append(p.getCampaignName() != null ? p.getCampaignName() : p.getTopic()).append("\n");
            sb.append("  Topic: ").append(p.getTopic()).append("\n");
            sb.append("  Avg Engagement: ").append(String.format("%.2f", perf.avgEngagement)).append("\n");
            sb.append("  Total Posts: ").append(perf.totalPosts).append("\n");
            if (p.getTone() != null) sb.append("  Tone: ").append(p.getTone()).append("\n");
            if (p.getContentLength() != null) sb.append("  Length: ").append(p.getContentLength()).append("\n");
            if (p.getTimingPattern() != null) sb.append("  Timing: ").append(p.getTimingPattern()).append("\n");
            if (p.getCtaStyle() != null) sb.append("  CTA: ").append(p.getCtaStyle()).append("\n");
            if (p.getPlatformBreakdown() != null) sb.append("  Platforms: ").append(p.getPlatformBreakdown()).append("\n");

            if (!perf.matchingPosts.isEmpty()) {
                Post best = perf.matchingPosts.stream()
                        .filter(p2 -> p2.getEngagementScore() != null)
                        .max((a, b) -> Double.compare(a.getEngagementScore(), b.getEngagementScore()))
                        .orElse(null);
                if (best != null) {
                    sb.append("  Best Post: \"").append(best.getTitle() != null ? best.getTitle() : best.getContent().substring(0, Math.min(50, best.getContent().length()))).append("\" (").append(String.format("%.2f", best.getEngagementScore())).append(")\n");
                }
            }
            sb.append("\n");
        }

        sb.append("Provide advice on:\n");
        sb.append("1. What's working across all patterns\n");
        sb.append("2. What should be stopped (e.g., bad timing, wrong tone)\n");
        sb.append("3. What should be doubled down on\n");
        sb.append("4. Specific recommendations for future content\n\n");
        sb.append("Return as a single actionable paragraph (3-5 sentences).");

        try {
            return geminiService.generate(sb.toString());
        } catch (Exception e) {
            System.out.println("PerformanceFeedback: Failed to generate advice: " + e.getMessage());
            return "Performance data collected but advice generation failed.";
        }
    }

    public List<ContentPattern> getPatternsByPerformance() {
        return contentPatternRepository.findAll().stream()
                .filter(p -> p.getAvgEngagementScore() != null)
                .sorted((a, b) -> Double.compare(b.getAvgEngagementScore(), a.getAvgEngagementScore()))
                .collect(Collectors.toList());
    }

    private static class PatternPerformance {
        ContentPattern pattern;
        double avgEngagement;
        int totalPosts;
        List<Post> matchingPosts;
    }
}
