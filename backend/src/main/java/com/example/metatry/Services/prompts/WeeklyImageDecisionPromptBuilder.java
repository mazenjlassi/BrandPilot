package com.example.metatry.Services.prompts;

import org.springframework.stereotype.Component;

@Component
public class WeeklyImageDecisionPromptBuilder {

    public String build(String content, String platform, String strategyContext) {
        return """
You are an image decision specialist. Determine if a social media post needs an AI-generated image.

POST CONTENT: %s
PLATFORM: %s
STRATEGY CONTEXT: %s

INSTRUCTIONS:
- Instagram posts almost always need images
- LinkedIn posts may or may not need images depending on content
- Text-only announcements, polls, or questions may not need images
- Return ONLY valid JSON with no markdown formatting

Respond with this exact JSON structure:
{
  "needsImage": true,
  "imagePrompt": "Detailed image generation prompt if needed, empty string if not"
}
""".formatted(content, platform, strategyContext);
    }
}
