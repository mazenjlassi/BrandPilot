package com.example.metatry.Services.prompts;

import com.example.metatry.Enums.PlatformType;
import org.springframework.stereotype.Component;

@Component
public class WeeklyImageGenPromptBuilder {

    public String build(String postContent, PlatformType platform, String imagePrompt) {
        String styleTag = switch (platform) {
            case INSTAGRAM -> "square 1:1 instagram";
            case LINKEDIN, FACEBOOK -> "landscape 16:9 linkedin facebook";
        };

        return (imagePrompt != null && !imagePrompt.isBlank() ? imagePrompt : postContent)
                + " " + styleTag + " professional business technology cinematic lighting photorealistic 4k clean minimalist";
    }
}
