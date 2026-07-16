package com.example.metatry.Services.strategy;

import com.example.metatry.Enums.MarketingStrategyStatus;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.MarketingStrategyRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StrategyContextService {

    private final MarketingStrategyRepository strategyRepository;
    private final PostRepository postRepository;
    private final ContentPatternRepository contentPatternRepository;

    public String buildContext() {
        StringBuilder context = new StringBuilder();

        context.append("PREVIOUS STRATEGY PERFORMANCE:\n");
        strategyRepository.findFirstByStatusOrderByCreatedAtDesc(MarketingStrategyStatus.COMPLETED)
                .ifPresent(last -> {
                    context.append("- Previous strategy: ").append(last.getTitle()).append("\n");
                    context.append("- Duration: ").append(last.getDurationWeeks()).append(" weeks\n");
                    context.append("- AI Reasoning: ").append(last.getAiReasoning()).append("\n");
                    if (last.getManagerNotes() != null && !last.getManagerNotes().isBlank()) {
                        context.append("- Manager notes: ").append(last.getManagerNotes()).append("\n");
                    }
                });

        context.append("\nCURRENT POST METRICS:\n");
        context.append("- Total posts: ").append(postRepository.count()).append("\n");
        context.append("- Approved posts: ").append(postRepository.countByApprovedTrue()).append("\n");

        context.append("\nTOP PERFORMING PATTERNS:\n");
        contentPatternRepository.findTop3ByOrderByExtractedAtDesc().forEach(p -> {
            context.append("- Topic: ").append(p.getTopic());
            if (p.getAvgEngagementScore() != null) {
                context.append(" | Engagement: ").append(String.format("%.2f", p.getAvgEngagementScore()));
            }
            context.append("\n");
        });

        return context.toString();
    }
}
