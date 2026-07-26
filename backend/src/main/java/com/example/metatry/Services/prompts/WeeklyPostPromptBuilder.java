package com.example.metatry.Services.prompts;

import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Services.MemoryContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyPostPromptBuilder {

    private final MemoryContextService memoryContextService;

    public String build(MarketingStrategy strategy, String campaignsContext, String previousPostsContext, String campaignPlanContext) {
        String brandContext = memoryContextService.getRecentContext();
        return """
You are an expert social media content creator. Generate weekly posts for the active marketing strategy.

COMPANY / BRAND CONTEXT:
%s

STRATEGY TITLE: %s
STRATEGY DESCRIPTION: %s

CAMPAIGNS FOR THIS WEEK:
%s

CAMPAIGN POST QUOTAS (THIS WEEK — generate exactly this many posts per campaign):
%s

PREVIOUS POSTS (for context, DO NOT duplicate):
%s

CRITICAL INSTRUCTIONS:
- Generate posts that align with both the strategy and the weekly campaigns
- Campaigns run SEQUENTIALLY — posts must be ordered by campaign priority
- Each post must have a specific platform
- Assign a scheduled day (YYYY-MM-DD) and hour (0-23) for each post
- Schedule posts sequentially by campaign: all Campaign 1 posts first (Mon-Wed), then Campaign 2 (Thu-Fri), etc.
- Spread posts across the week (Monday to Friday)
- YOU MUST GENERATE THE EXACT NUMBER OF POSTS SPECIFIED IN THE CAMPAIGN POST QUOTAS above, distributed across the specified platforms
- Indicate if the post needs an AI-generated image
- Return ONLY valid JSON with no markdown formatting
- CRITICAL: Generate COMPLETE, READY-TO-PUBLISH posts. NEVER use placeholders, brackets, or generic text like "[Product Name]", "[Company]", "[Feature]", "[Industry]", etc.
- CRITICAL: Use the actual company name "3LM Solutions" and specific details from the COMPANY / BRAND CONTEXT above
- CRITICAL: Write specific, concrete content that can be posted immediately without any human editing

Respond with this exact JSON structure:
{
  "posts": [
    {
      "title": "Post title",
      "content": "Post body content",
      "hashtags": ["tag1", "tag2"],
      "platform": "LINKEDIN",
      "cta": "Call to action text",
      "link": "https://3lm-solutions2.odoo.com/contactus",
      "permanent": false,
      "scheduledDay": "2026-07-13",
      "scheduledHour": 9
    }
  ]
}
""".formatted(brandContext, strategy.getTitle(), strategy.getDescription(), campaignsContext, campaignPlanContext, previousPostsContext);
    }
}
