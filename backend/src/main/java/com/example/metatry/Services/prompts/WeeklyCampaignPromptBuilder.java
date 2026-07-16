package com.example.metatry.Services.prompts;

import com.example.metatry.Models.MarketingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyCampaignPromptBuilder {

    public String build(MarketingStrategy strategy, int weekNumber) {
        return """
You are an expert marketing campaign planner. Generate weekly campaigns for an active marketing strategy.

STRATEGY TITLE: %s
STRATEGY DESCRIPTION: %s
WEEK NUMBER: %d of %d weeks

INSTRUCTIONS:
- Generate 1-3 campaigns for this week that align with the overall strategy
- Each campaign should have a clear objective
- Return ONLY valid JSON with no markdown formatting

Respond with this exact JSON structure:
{
  "campaigns": [
    {
      "name": "Campaign name",
      "topic": "Specific topic for this campaign",
      "objective": "Clear objective for this campaign"
    }
  ]
}
""".formatted(strategy.getTitle(), strategy.getDescription(), weekNumber, strategy.getDurationWeeks());
    }
}
