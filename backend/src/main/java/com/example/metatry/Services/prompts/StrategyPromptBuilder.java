package com.example.metatry.Services.prompts;

import com.example.metatry.Services.MemoryContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StrategyPromptBuilder {

    private final MemoryContextService memoryContextService;

    public String build(String topic, Integer durationWeeks) {
        String context = memoryContextService.getRecentContext();
        String today = LocalDate.now().toString();
        String duration = durationWeeks != null ? durationWeeks + " weeks" : "to be determined by AI based on the scope";

        return """
You are an expert marketing strategist. Generate a comprehensive marketing strategy.

TOPIC: %s
DURATION: %s
TODAY'S DATE: %s

CONTEXT (from recent activity):
%s

INSTRUCTIONS:
- Analyze the topic and generate a detailed marketing strategy
- Consider the platform mix (LinkedIn, Instagram, Facebook)
- Design 2-4 campaigns that will run sequentially (Campaign 1 finishes first, then Campaign 2, etc.)
- The campaign list is ordered by priority — earlier campaigns execute first
- For EACH campaign specify: totalPosts (all posts across its full duration), durationWeeks (how many weeks it runs), and weeklyPostDistribution (posts per platform per week)
- The sum of all campaign durationWeeks must equal the strategy's total durationWeeks
- Total weekly posts across all campaigns should be 5-10
- The weeklyPostDistribution must be explicit with exact post counts per platform per week
- Return ONLY valid JSON with no markdown formatting

Respond with this exact JSON structure:
{
  "title": "A concise, catchy title for this strategy",
  "summary": "2-3 sentence executive summary",
  "description": "Detailed strategy description covering objectives, themes, and approach (3-5 paragraphs)",
  "aiReasoning": "Explain why this strategy was designed this way",
  "durationWeeks": 8,
  "campaigns": [
    {
      "name": "Campaign display name",
      "topic": "Specific topic/focus of this campaign",
      "objective": "What this campaign aims to achieve",
      "totalPosts": 12,
      "durationWeeks": 3,
      "weeklyPostDistribution": {
        "facebook": 2,
        "instagram": 1,
        "linkedin": 1
      }
    }
  ]
}
""".formatted(topic, duration, today, context);
    }
}
