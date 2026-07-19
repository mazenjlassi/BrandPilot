package com.example.metatry.Services.prompts;

import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Services.MemoryContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyCampaignPromptBuilder {

    private final MemoryContextService memoryContextService;

    public String build(MarketingStrategy strategy, int weekNumber) {
        String brandContext = memoryContextService.getRecentContext();
        return """
You are an expert marketing campaign planner. Generate weekly campaigns for an active marketing strategy.

COMPANY / BRAND CONTEXT:
%s

STRATEGY TITLE: %s
STRATEGY DESCRIPTION: %s
WEEK NUMBER: %d of %d weeks

INSTRUCTIONS:
- Generate 1-3 campaigns for this week that align with the overall strategy
- Each campaign should have a clear objective
- Return ONLY valid JSON with no markdown formatting
- CRITICAL: Generate COMPLETE, READY-TO-USE campaigns. NEVER use placeholders, brackets, or generic text like "[Product Name]", "[Company]", "[Industry]", etc.
- CRITICAL: Use the actual company name "3LM Solutions" and specific details from the COMPANY / BRAND CONTEXT above
- CRITICAL: All campaign names, topics, and objectives must be specific and concrete — ready to execute without any human editing

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
""".formatted(brandContext, strategy.getTitle(), strategy.getDescription(), weekNumber, strategy.getDurationWeeks());
    }
}
