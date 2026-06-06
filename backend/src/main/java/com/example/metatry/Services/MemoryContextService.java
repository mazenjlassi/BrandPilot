package com.example.metatry.Services;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Conversation;
import com.example.metatry.Models.MarketingInsight;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.ConversationRepository;
import com.example.metatry.Repositories.MarketingInsightRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoryContextService {

    private final ContentPatternRepository contentPatternRepository;
    private final PostRepository postRepository;
    private final ConversationRepository conversationRepository;
    private final MarketingInsightRepository marketingInsightRepository;

    public String getRecentContext() {
        StringBuilder context = new StringBuilder();

        context.append("============================================\n");
        context.append("RECENT CONTEXT (From your recent activity)\n");
        context.append("============================================\n\n");

        // 1. Recent patterns
        List<ContentPattern> patterns = contentPatternRepository.findTop3ByOrderByExtractedAtDesc();
        if (!patterns.isEmpty()) {
            context.append("CONTENT PATTERNS (From competitor analysis):\n");
            for (ContentPattern p : patterns) {
                context.append("- Campaign: ").append(p.getCampaignName() != null ? p.getCampaignName() : p.getTopic());
                if (p.getTone() != null) context.append(" | Tone: ").append(p.getTone());
                if (p.getContentLength() != null) context.append(" | Length: ").append(p.getContentLength());
                if (p.getCtaStyle() != null) context.append(" | CTA: ").append(p.getCtaStyle());
                if (p.getPlatformBreakdown() != null) context.append(" | Platforms: ").append(p.getPlatformBreakdown());
                context.append("\n");
            }
            context.append("\n");
        }

        // 2. Recent posts
        List<Post> recentPosts = postRepository.findTop3ByOrderByCreatedAtDesc();
        if (!recentPosts.isEmpty()) {
            context.append("RECENTLY GENERATED POSTS:\n");
            for (Post post : recentPosts) {
                context.append("- [").append(post.getPlatform()).append("] ");
                if (post.getTitle() != null && !post.getTitle().isBlank()) {
                    context.append("\"").append(post.getTitle()).append("\"");
                } else {
                    context.append(post.getContent() != null && post.getContent().length() > 50
                            ? post.getContent().substring(0, 50) + "..."
                            : post.getContent());
                }
                context.append("\n");
            }
            context.append("\n");
        }

        // 3. Chat conclusions
        List<Conversation> conversations = conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc();
        if (!conversations.isEmpty()) {
            context.append("CHAT CONCLUSIONS (Strategy notes):\n");
            for (Conversation c : conversations) {
                if (c.getConclusion() != null && !c.getConclusion().isBlank()) {
                    context.append("- ").append(c.getConclusion()).append("\n");
                }
            }
            context.append("\n");
        }

        // 4. Marketing insights
        List<MarketingInsight> insights = marketingInsightRepository.findTop2ByOrderByCreatedAtDesc();
        if (!insights.isEmpty()) {
            context.append("MARKETING INSIGHTS (From comment analysis):\n");
            for (MarketingInsight i : insights) {
                context.append("- [").append(i.getPlatform()).append("] ");
                if (i.getDescription() != null) {
                    context.append(i.getDescription().length() > 100
                            ? i.getDescription().substring(0, 100) + "..."
                            : i.getDescription());
                }
                context.append("\n");
            }
            context.append("\n");
        }

        return context.toString();
    }

    public String getMatchingContext(String topic) {
        StringBuilder context = new StringBuilder();

        context.append("============================================\n");
        context.append("MATCHING PATTERNS FOR TOPIC: ").append(topic).append("\n");
        context.append("============================================\n\n");

        List<ContentPattern> matches = new ArrayList<>();
        contentPatternRepository.findByTopic(topic).ifPresent(matches::add);

        if (matches.isEmpty()) {
            String[] keywords = topic.split("\\s+");
            for (String keyword : keywords) {
                if (keyword.length() < 3) continue;
                List<ContentPattern> kwMatches = contentPatternRepository.findByTopicContainingIgnoreCase(keyword);
                for (ContentPattern p : kwMatches) {
                    if (!matches.contains(p)) {
                        matches.add(p);
                    }
                }
            }
        }

        if (!matches.isEmpty()) {
            for (ContentPattern p : matches) {
                context.append("- Campaign: ").append(p.getCampaignName() != null ? p.getCampaignName() : p.getTopic()).append("\n");
                if (p.getTone() != null) context.append("  Tone: ").append(p.getTone()).append("\n");
                if (p.getContentLength() != null) context.append("  Length: ").append(p.getContentLength()).append("\n");
                if (p.getMediaType() != null) context.append("  Media: ").append(p.getMediaType()).append("\n");
                if (p.getHashtagCount() != null) context.append("  Hashtags: ").append(p.getHashtagCount()).append("\n");
                if (p.getTimingPattern() != null) context.append("  Timing: ").append(p.getTimingPattern()).append("\n");
                if (p.getCtaStyle() != null) context.append("  CTA: ").append(p.getCtaStyle()).append("\n");
                if (p.getPlatformBreakdown() != null) context.append("  Platform split: ").append(p.getPlatformBreakdown()).append("\n");
                if (p.getAvgEngagementScore() != null) {
                    String level = p.getAvgEngagementScore() > 0.5 ? "HIGH" : p.getAvgEngagementScore() > 0.2 ? "MEDIUM" : "LOW";
                    context.append("  Avg Engagement: ").append(String.format("%.2f", p.getAvgEngagementScore())).append(" (").append(level).append(")\n");
                }
                if (p.getTotalPostsGenerated() != null) context.append("  Total Posts: ").append(p.getTotalPostsGenerated()).append("\n");
                if (p.getPerformanceAdvice() != null && !p.getPerformanceAdvice().isBlank()) {
                    context.append("  Advice: ").append(p.getPerformanceAdvice().length() > 150 ? p.getPerformanceAdvice().substring(0, 150) + "..." : p.getPerformanceAdvice()).append("\n");
                }
                context.append("\n");
            }
        }

        return context.toString();
    }
}
