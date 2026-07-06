package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.ContentPattern;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildPrompt(String topic, String insights, String conclusion, ContentPattern pattern) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a senior IT marketing strategist and viral content creator.\n\n");
        prompt.append("Your goal is to generate HIGH-PERFORMING, ENGAGING social media posts that drive real engagement.\n\n");

        // INPUT DATA
        prompt.append("============================================\n");
        prompt.append("INPUT DATA\n");
        prompt.append("============================================\n\n");
        prompt.append("TOPIC (What we're promoting):\n").append(topic).append("\n\n");
        prompt.append("INSIGHTS (What users love/hate):\n").append(insights).append("\n\n");
        prompt.append("CONVERSATION STRATEGY (How to position):\n").append(conclusion).append("\n\n");

        // PATTERN DATA (from scraped posts analysis)
        if (pattern != null) {
            prompt.append("============================================\n");
            prompt.append("CONTENT PATTERN (Based on scraped posts from competitors)\n");
            prompt.append("============================================\n\n");

            if (pattern.getTone() != null && !pattern.getTone().isEmpty()) {
                prompt.append("TONE: ").append(pattern.getTone()).append("\n");
            }
            if (pattern.getPostFrequency() != null && !pattern.getPostFrequency().isEmpty()) {
                prompt.append("POST FREQUENCY: ").append(pattern.getPostFrequency()).append("\n");
            }
            if (pattern.getContentLength() != null && !pattern.getContentLength().isEmpty()) {
                prompt.append("CONTENT LENGTH: ").append(pattern.getContentLength()).append("\n");
            }
            if (pattern.getMediaType() != null && !pattern.getMediaType().isEmpty()) {
                prompt.append("MEDIA TYPE: ").append(pattern.getMediaType()).append("\n");
            }
            if (pattern.getHashtagCount() != null && !pattern.getHashtagCount().isEmpty()) {
                prompt.append("HASHTAG STYLE: ").append(pattern.getHashtagCount()).append("\n");
            }
            if (pattern.getTimingPattern() != null && !pattern.getTimingPattern().isEmpty()) {
                prompt.append("BEST POSTING TIMES: ").append(pattern.getTimingPattern()).append("\n");
            }
            if (pattern.getCtaStyle() != null && !pattern.getCtaStyle().isEmpty()) {
                prompt.append("CTA STYLE: ").append(pattern.getCtaStyle()).append("\n");
            }
            if (pattern.getPlatformBreakdown() != null && !pattern.getPlatformBreakdown().isEmpty()) {
                prompt.append("PLATFORM SPLIT: ").append(pattern.getPlatformBreakdown()).append("\n");
            }
            prompt.append("\n");

            // PERFORMANCE DATA
            if (pattern.getAvgEngagementScore() != null) {
                prompt.append("============================================\n");
                prompt.append("PAST PERFORMANCE DATA\n");
                prompt.append("============================================\n\n");
                String level = pattern.getAvgEngagementScore() > 0.5 ? "HIGH" : pattern.getAvgEngagementScore() > 0.2 ? "MEDIUM" : "LOW";
                prompt.append("Avg Engagement Score: ").append(String.format("%.2f", pattern.getAvgEngagementScore())).append(" (").append(level).append(")\n");
                if (pattern.getTotalPostsGenerated() != null) {
                    prompt.append("Total Posts Generated: ").append(pattern.getTotalPostsGenerated()).append("\n");
                }
                if (pattern.getPerformanceAdvice() != null && !pattern.getPerformanceAdvice().isEmpty()) {
                    prompt.append("ADVICE: ").append(pattern.getPerformanceAdvice()).append("\n");
                }
                prompt.append("\n");
            }
        }

        // CONTENT PRINCIPLES
        prompt.append("============================================\n");
        prompt.append("CONTENT PRINCIPLES\n");
        prompt.append("============================================\n\n");
        prompt.append("1. VALUE FIRST - Every post must provide clear value to the reader\n");
        prompt.append("2. HOOK IN 3 SECONDS - First line must stop the scroll\n");
        prompt.append("3. EMOTION + LOGIC - Blend storytelling with practical tips\n");
        prompt.append("4. ONE MESSAGE PER POST - Don't clutter with multiple points\n");
        prompt.append("5. AUTHENTIC VOICE - Write like you speak, not like a robot\n\n");

        // PLATFORM-SPECIFIC OPTIMIZATION
        prompt.append("============================================\n");
        prompt.append("PLATFORM-SPECIFIC OPTIMIZATION\n");
        prompt.append("============================================\n\n");

        prompt.append("LINKEDIN (Professional Network)\n");
        prompt.append("-------------------------------\n");
        prompt.append("- Open with a bold statement or surprising insight\n");
        prompt.append("- Use short paragraphs (2-3 sentences max)\n");
        prompt.append("- Add line breaks for readability\n");
        prompt.append("- Include a clear CTA (comment, share, save)\n");
        prompt.append("- End with a thought-provoking question\n");
        prompt.append("- AUTHORITY TONE: \"Here's what I've learned...\" / \"The secret no one tells you...\"\n");
        prompt.append("- MAX: 1200 characters\n");
        prompt.append("- BEST: Monday-Thursday, 8am-10am\n\n");

        prompt.append("INSTAGRAM (Visual + Stories)\n");
        prompt.append("-----------------------------\n");
        prompt.append("- Start with emoji or attention-grabbing word\n");
        prompt.append("- Use minimal text, focus on visual storytelling\n");
        prompt.append("- Mix of: educational, inspirational, behind-the-scenes\n");
        prompt.append("- Add 5-8 relevant hashtags (1 brand, 4 niche, 3 trending)\n");
        prompt.append("- CATCHY TONE: Short, punchy, emoji-enhanced\n");
        prompt.append("- MAX: 220 characters\n");
        prompt.append("- BEST: Weekdays 11am-1pm, 7pm-9pm\n\n");

        prompt.append("FACEBOOK (Community + Conversation)\n");
        prompt.append("-----------------------------------\n");
        prompt.append("- Ask questions to spark comments\n");
        prompt.append("- Share stories and real experiences\n");
        prompt.append("- Be approachable and human\n");
        prompt.append("- Use casual language (but professional)\n");
        prompt.append("- COMMUNITY TONE: \"You guys...\" / \"Real talk...\"\n");
        prompt.append("- MAX: 500 characters\n");
        prompt.append("- BEST: Daily 9am-1pm\n\n");

        // ENGAGEMENT TRIGGERS
        prompt.append("============================================\n");
        prompt.append("ENGAGEMENT TRIGGERS\n");
        prompt.append("============================================\n\n");
        prompt.append("Use these techniques to boost engagement:\n");
        prompt.append("- \"Here's the uncomfortable truth about...\"\n");
        prompt.append("- \"Most people think X, but actually Y\"\n");
        prompt.append("- \"Stop doing X if you want Y\"\n");
        prompt.append("- \"The #1 mistake I see is...\"\n");
        prompt.append("- \"3 things that changed everything for me:\"\n");
        prompt.append("- \"What's your take on this? Drop a comment\"\n\n");

        // FORBIDDEN CONTENT
        prompt.append("============================================\n");
        prompt.append("FORBIDDEN CONTENT\n");
        prompt.append("============================================\n\n");
        prompt.append("NEVER include:\n");
        prompt.append("- Direct competitor names\n");
        prompt.append("- False claims or exaggerations\n");
        prompt.append("- Overly salesy language (\"BUY NOW!\", \"LIMITED!\")\n");
        prompt.append("- Controversial topics outside IT\n");
        prompt.append("- Long walls of text\n");
        prompt.append("- Generic advice that applies to any industry\n\n");

        // OUTPUT FORMAT
        prompt.append("============================================\n");
        prompt.append("OUTPUT FORMAT (JSON ONLY)\n");
        prompt.append("============================================\n\n");
        prompt.append("{\n");
        prompt.append("  \"linkedinTitle\": \"Bold statement or question\",\n");
        prompt.append("  \"linkedinPost\": \"Full post with hook, value, CTA\",\n");
        prompt.append("  \"linkedinHashtags\": [\"industry\", \"topic\", \"value\"],\n\n");
        prompt.append("  \"instagramTitle\": \"Catchy title\",\n");
        prompt.append("  \"instagramPost\": \"Short, punchy content\",\n");
        prompt.append("  \"instagramHashtags\": [\"brand\", \"niche1\", \"niche2\", \"niche3\", \"trending\"],\n\n");
        prompt.append("  \"facebookTitle\": \"Engaging title\",\n");
        prompt.append("  \"facebookPost\": \"Conversational, story-driven\",\n");
        prompt.append("  \"facebookHashtags\": [\"community\", \"topic\", \"engagement\"],\n\n");
        prompt.append("  \"imagePrompt\": \"Short keyword prompt (8-15 words): subject, style, lighting — NO sentences\"\n");
        prompt.append("}\n\n");

        // QUALITY CHECK
        prompt.append("============================================\n");
        prompt.append("QUALITY CHECK\n");
        prompt.append("============================================\n\n");
        prompt.append("Before returning, ensure:\n");
        prompt.append("- Each post has a strong hook\n");
        prompt.append("- Each post provides unique value\n");
        prompt.append("- Each platform sounds native to that platform\n");
        prompt.append("- CTAs are natural, not pushy\n");
        prompt.append("- No repetition across platforms\n");
        prompt.append("- JSON is valid and parseable\n");
        prompt.append("- Character limits are respected\n\n");

        prompt.append("Now generate the content.\n");

        return prompt.toString();
    }

    public String buildPrompt(String topic, String insights, String conclusion) {
        return buildPrompt(topic, insights, conclusion, null);
    }

    public String buildEstimatedPrompt(String topic, String insights, String conclusion, ContentPattern pattern) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a senior IT marketing strategist and viral content creator.\n\n");
        prompt.append("Your goal is to generate HIGH-PERFORMING, ENGAGING social media posts that drive real engagement.\n\n");
        prompt.append("IMPORTANT: You must decide how many posts to create for each platform based on the topic, context, and content strategy.\n\n");

        // INPUT DATA
        prompt.append("============================================\n");
        prompt.append("INPUT DATA\n");
        prompt.append("============================================\n\n");
        prompt.append("TOPIC (What we're promoting):\n").append(topic).append("\n\n");
        prompt.append("INSIGHTS (What users love/hate):\n").append(insights).append("\n\n");
        prompt.append("CONVERSATION STRATEGY (How to position):\n").append(conclusion).append("\n\n");

        // PATTERN DATA
        if (pattern != null) {
            prompt.append("============================================\n");
            prompt.append("CONTENT PATTERN (Based on scraped posts from competitors)\n");
            prompt.append("============================================\n\n");

            if (pattern.getTone() != null && !pattern.getTone().isEmpty()) {
                prompt.append("TONE: ").append(pattern.getTone()).append("\n");
            }
            if (pattern.getPostFrequency() != null && !pattern.getPostFrequency().isEmpty()) {
                prompt.append("POST FREQUENCY: ").append(pattern.getPostFrequency()).append("\n");
            }
            if (pattern.getContentLength() != null && !pattern.getContentLength().isEmpty()) {
                prompt.append("CONTENT LENGTH: ").append(pattern.getContentLength()).append("\n");
            }
            if (pattern.getMediaType() != null && !pattern.getMediaType().isEmpty()) {
                prompt.append("MEDIA TYPE: ").append(pattern.getMediaType()).append("\n");
            }
            if (pattern.getHashtagCount() != null && !pattern.getHashtagCount().isEmpty()) {
                prompt.append("HASHTAG STYLE: ").append(pattern.getHashtagCount()).append("\n");
            }
            if (pattern.getTimingPattern() != null && !pattern.getTimingPattern().isEmpty()) {
                prompt.append("BEST POSTING TIMES: ").append(pattern.getTimingPattern()).append("\n");
            }
            if (pattern.getCtaStyle() != null && !pattern.getCtaStyle().isEmpty()) {
                prompt.append("CTA STYLE: ").append(pattern.getCtaStyle()).append("\n");
            }
            prompt.append("\n");
        }

        // CONTENT PRINCIPLES
        prompt.append("============================================\n");
        prompt.append("CONTENT PRINCIPLES\n");
        prompt.append("============================================\n\n");
        prompt.append("1. VALUE FIRST - Every post must provide clear value to the reader\n");
        prompt.append("2. HOOK IN 3 SECONDS - First line must stop the scroll\n");
        prompt.append("3. EMOTION + LOGIC - Blend storytelling with practical tips\n");
        prompt.append("4. ONE MESSAGE PER POST - Don't clutter with multiple points\n");
        prompt.append("5. AUTHENTIC VOICE - Write like you speak, not like a robot\n\n");

        // PLATFORM-SPECIFIC OPTIMIZATION
        prompt.append("============================================\n");
        prompt.append("PLATFORM-SPECIFIC OPTIMIZATION\n");
        prompt.append("============================================\n\n");

        prompt.append("LINKEDIN (Professional Network)\n");
        prompt.append("-------------------------------\n");
        prompt.append("- Open with a bold statement or surprising insight\n");
        prompt.append("- Use short paragraphs (2-3 sentences max)\n");
        prompt.append("- Add line breaks for readability\n");
        prompt.append("- Include a clear CTA (comment, share, save)\n");
        prompt.append("- End with a thought-provoking question\n");
        prompt.append("- AUTHORITY TONE: \"Here's what I've learned...\" / \"The secret no one tells you...\"\n");
        prompt.append("- MAX: 1200 characters\n");
        prompt.append("- BEST: Monday-Thursday, 8am-10am\n\n");

        prompt.append("INSTAGRAM (Visual + Stories)\n");
        prompt.append("-----------------------------\n");
        prompt.append("- Start with emoji or attention-grabbing word\n");
        prompt.append("- Use minimal text, focus on visual storytelling\n");
        prompt.append("- Mix of: educational, inspirational, behind-the-scenes\n");
        prompt.append("- Add 5-8 relevant hashtags (1 brand, 4 niche, 3 trending)\n");
        prompt.append("- CATCHY TONE: Short, punchy, emoji-enhanced\n");
        prompt.append("- MAX: 220 characters\n");
        prompt.append("- BEST: Weekdays 11am-1pm, 7pm-9pm\n\n");

        prompt.append("FACEBOOK (Community + Conversation)\n");
        prompt.append("-----------------------------------\n");
        prompt.append("- Ask questions to spark comments\n");
        prompt.append("- Share stories and real experiences\n");
        prompt.append("- Be approachable and human\n");
        prompt.append("- Use casual language (but professional)\n");
        prompt.append("- COMMUNITY TONE: \"You guys...\" / \"Real talk...\"\n");
        prompt.append("- MAX: 500 characters\n");
        prompt.append("- BEST: Daily 9am-1pm\n\n");

        // ENGAGEMENT TRIGGERS
        prompt.append("============================================\n");
        prompt.append("ENGAGEMENT TRIGGERS\n");
        prompt.append("============================================\n\n");
        prompt.append("Use these techniques to boost engagement:\n");
        prompt.append("- \"Here's the uncomfortable truth about...\"\n");
        prompt.append("- \"Most people think X, but actually Y\"\n");
        prompt.append("- \"Stop doing X if you want Y\"\n");
        prompt.append("- \"The #1 mistake I see is...\"\n");
        prompt.append("- \"3 things that changed everything for me:\"\n");
        prompt.append("- \"What's your take on this? Drop a comment\"\n\n");

        // FORBIDDEN CONTENT
        prompt.append("============================================\n");
        prompt.append("FORBIDDEN CONTENT\n");
        prompt.append("============================================\n\n");
        prompt.append("NEVER include:\n");
        prompt.append("- Direct competitor names\n");
        prompt.append("- False claims or exaggerations\n");
        prompt.append("- Overly salesy language (\"BUY NOW!\", \"LIMITED!\")\n");
        prompt.append("- Controversial topics outside IT\n");
        prompt.append("- Long walls of text\n");
        prompt.append("- Generic advice that applies to any industry\n\n");

        // OUTPUT FORMAT
        prompt.append("============================================\n");
        prompt.append("OUTPUT FORMAT (JSON ONLY)\n");
        prompt.append("============================================\n\n");
        prompt.append("IMPORTANT: Decide how many posts to create for each platform (LINKEDIN, INSTAGRAM, FACEBOOK).\n");
        prompt.append("Each post in the array MUST include a \"platform\" field.\n\n");
        prompt.append("{\n");
        prompt.append("  \"posts\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"platform\": \"LINKEDIN\",\n");
        prompt.append("      \"title\": \"Bold statement or question\",\n");
        prompt.append("      \"content\": \"Full post with hook, value, CTA\",\n");
        prompt.append("      \"hashtags\": [\"industry\", \"topic\", \"value\"],\n");
        prompt.append("      \"imagePrompt\": \"Short keyword image prompt for AI (8-15 words max): subject, style, lighting, composition — NO full sentences\"\n");
        prompt.append("    },\n");
        prompt.append("    {\n");
        prompt.append("      \"platform\": \"INSTAGRAM\",\n");
        prompt.append("      \"title\": \"Catchy title\",\n");
        prompt.append("      \"content\": \"Short, punchy content\",\n");
        prompt.append("      \"hashtags\": [\"brand\", \"niche1\", \"niche2\", \"niche3\", \"trending\"],\n");
        prompt.append("      \"imagePrompt\": \"server room blue lights technology modern\"\n");
        prompt.append("    },\n");
        prompt.append("    {\n");
        prompt.append("      \"platform\": \"FACEBOOK\",\n");
        prompt.append("      \"title\": \"Engaging title\",\n");
        prompt.append("      \"content\": \"Conversational, story-driven\",\n");
        prompt.append("      \"hashtags\": [\"community\", \"topic\", \"engagement\"],\n");
        prompt.append("      \"imagePrompt\": \"server room blue lights technology modern\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        prompt.append("You can generate multiple posts per platform. For example: 2 LINKEDIN, 3 INSTAGRAM, 1 FACEBOOK — or any distribution you think works best.\n\n");

        // QUALITY CHECK
        prompt.append("============================================\n");
        prompt.append("QUALITY CHECK\n");
        prompt.append("============================================\n\n");
        prompt.append("Before returning, ensure:\n");
        prompt.append("- Each post has a strong hook\n");
        prompt.append("- Each post provides unique value\n");
        prompt.append("- Each platform sounds native to that platform\n");
        prompt.append("- CTAs are natural, not pushy\n");
        prompt.append("- No repetition across posts\n");
        prompt.append("- Each post has a valid platform field (LINKEDIN, INSTAGRAM, or FACEBOOK)\n");
        prompt.append("- JSON is valid and parseable\n");
        prompt.append("- Character limits are respected\n\n");

        prompt.append("Now generate the content.\n");

        return prompt.toString();
    }

    public String buildPlatformPrompt(
            String topic,
            String insights,
            String conclusion,
            ContentPattern pattern,
            PlatformType platform,
            int count
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a senior IT marketing strategist and viral content creator.\n\n");
        prompt.append("Your goal is to generate HIGH-PERFORMING, ENGAGING social media posts that drive real engagement.\n\n");

        // INPUT DATA
        prompt.append("============================================\n");
        prompt.append("INPUT DATA\n");
        prompt.append("============================================\n\n");
        prompt.append("TOPIC (What we're promoting):\n").append(topic).append("\n\n");
        prompt.append("INSIGHTS (What users love/hate):\n").append(insights).append("\n\n");
        prompt.append("CONVERSATION STRATEGY (How to position):\n").append(conclusion).append("\n\n");

        // PATTERN DATA
        if (pattern != null) {
            prompt.append("============================================\n");
            prompt.append("CONTENT PATTERN (Based on scraped posts from competitors)\n");
            prompt.append("============================================\n\n");

            if (pattern.getTone() != null && !pattern.getTone().isEmpty()) {
                prompt.append("TONE: ").append(pattern.getTone()).append("\n");
            }
            if (pattern.getPostFrequency() != null && !pattern.getPostFrequency().isEmpty()) {
                prompt.append("POST FREQUENCY: ").append(pattern.getPostFrequency()).append("\n");
            }
            if (pattern.getContentLength() != null && !pattern.getContentLength().isEmpty()) {
                prompt.append("CONTENT LENGTH: ").append(pattern.getContentLength()).append("\n");
            }
            if (pattern.getMediaType() != null && !pattern.getMediaType().isEmpty()) {
                prompt.append("MEDIA TYPE: ").append(pattern.getMediaType()).append("\n");
            }
            if (pattern.getHashtagCount() != null && !pattern.getHashtagCount().isEmpty()) {
                prompt.append("HASHTAG STYLE: ").append(pattern.getHashtagCount()).append("\n");
            }
            if (pattern.getTimingPattern() != null && !pattern.getTimingPattern().isEmpty()) {
                prompt.append("BEST POSTING TIMES: ").append(pattern.getTimingPattern()).append("\n");
            }
            if (pattern.getCtaStyle() != null && !pattern.getCtaStyle().isEmpty()) {
                prompt.append("CTA STYLE: ").append(pattern.getCtaStyle()).append("\n");
            }
            if (pattern.getPlatformBreakdown() != null && !pattern.getPlatformBreakdown().isEmpty()) {
                prompt.append("PLATFORM SPLIT: ").append(pattern.getPlatformBreakdown()).append("\n");
            }
            prompt.append("\n");

            if (pattern.getAvgEngagementScore() != null) {
                prompt.append("============================================\n");
                prompt.append("PAST PERFORMANCE DATA\n");
                prompt.append("============================================\n\n");
                String level = pattern.getAvgEngagementScore() > 0.5 ? "HIGH" : pattern.getAvgEngagementScore() > 0.2 ? "MEDIUM" : "LOW";
                prompt.append("Avg Engagement Score: ").append(String.format("%.2f", pattern.getAvgEngagementScore())).append(" (").append(level).append(")\n");
                if (pattern.getTotalPostsGenerated() != null) {
                    prompt.append("Total Posts Generated: ").append(pattern.getTotalPostsGenerated()).append("\n");
                }
                if (pattern.getPerformanceAdvice() != null && !pattern.getPerformanceAdvice().isEmpty()) {
                    prompt.append("ADVICE: ").append(pattern.getPerformanceAdvice()).append("\n");
                }
                prompt.append("\n");
            }
        }

        // CONTENT PRINCIPLES
        prompt.append("============================================\n");
        prompt.append("CONTENT PRINCIPLES\n");
        prompt.append("============================================\n\n");
        prompt.append("1. VALUE FIRST - Every post must provide clear value to the reader\n");
        prompt.append("2. HOOK IN 3 SECONDS - First line must stop the scroll\n");
        prompt.append("3. EMOTION + LOGIC - Blend storytelling with practical tips\n");
        prompt.append("4. ONE MESSAGE PER POST - Don't clutter with multiple points\n");
        prompt.append("5. AUTHENTIC VOICE - Write like you speak, not like a robot\n\n");

        // PLATFORM-SPECIFIC OPTIMIZATION
        prompt.append("============================================\n");
        prompt.append("PLATFORM OPTIMIZATION: ").append(platform.name()).append("\n");
        prompt.append("============================================\n\n");

        switch (platform) {
            case LINKEDIN -> {
                prompt.append("LINKEDIN (Professional Network)\n");
                prompt.append("-------------------------------\n");
                prompt.append("- Open with a bold statement or surprising insight\n");
                prompt.append("- Use short paragraphs (2-3 sentences max)\n");
                prompt.append("- Add line breaks for readability\n");
                prompt.append("- Include a clear CTA (comment, share, save)\n");
                prompt.append("- End with a thought-provoking question\n");
                prompt.append("- AUTHORITY TONE: \"Here's what I've learned...\" / \"The secret no one tells you...\"\n");
                prompt.append("- MAX: 1200 characters\n");
                prompt.append("- BEST: Monday-Thursday, 8am-10am\n\n");
            }
            case INSTAGRAM -> {
                prompt.append("INSTAGRAM (Visual + Stories)\n");
                prompt.append("-----------------------------\n");
                prompt.append("- Start with emoji or attention-grabbing word\n");
                prompt.append("- Use minimal text, focus on visual storytelling\n");
                prompt.append("- Mix of: educational, inspirational, behind-the-scenes\n");
                prompt.append("- Add 5-8 relevant hashtags (1 brand, 4 niche, 3 trending)\n");
                prompt.append("- CATCHY TONE: Short, punchy, emoji-enhanced\n");
                prompt.append("- MAX: 220 characters\n");
                prompt.append("- BEST: Weekdays 11am-1pm, 7pm-9pm\n\n");
            }
            case FACEBOOK -> {
                prompt.append("FACEBOOK (Community + Conversation)\n");
                prompt.append("-----------------------------------\n");
                prompt.append("- Ask questions to spark comments\n");
                prompt.append("- Share stories and real experiences\n");
                prompt.append("- Be approachable and human\n");
                prompt.append("- Use casual language (but professional)\n");
                prompt.append("- COMMUNITY TONE: \"You guys...\" / \"Real talk...\"\n");
                prompt.append("- MAX: 500 characters\n");
                prompt.append("- BEST: Daily 9am-1pm\n\n");
            }
        }

        // ENGAGEMENT TRIGGERS
        prompt.append("============================================\n");
        prompt.append("ENGAGEMENT TRIGGERS\n");
        prompt.append("============================================\n\n");
        prompt.append("Use these techniques to boost engagement:\n");
        prompt.append("- \"Here's the uncomfortable truth about...\"\n");
        prompt.append("- \"Most people think X, but actually Y\"\n");
        prompt.append("- \"Stop doing X if you want Y\"\n");
        prompt.append("- \"The #1 mistake I see is...\"\n");
        prompt.append("- \"3 things that changed everything for me:\"\n");
        prompt.append("- \"What's your take on this? Drop a comment\"\n\n");

        // FORBIDDEN CONTENT
        prompt.append("============================================\n");
        prompt.append("FORBIDDEN CONTENT\n");
        prompt.append("============================================\n\n");
        prompt.append("NEVER include:\n");
        prompt.append("- Direct competitor names\n");
        prompt.append("- False claims or exaggerations\n");
        prompt.append("- Overly salesy language (\"BUY NOW!\", \"LIMITED!\")\n");
        prompt.append("- Controversial topics outside IT\n");
        prompt.append("- Long walls of text\n");
        prompt.append("- Generic advice that applies to any industry\n\n");

        // OUTPUT FORMAT
        prompt.append("============================================\n");
        prompt.append("OUTPUT FORMAT (JSON ONLY)\n");
        prompt.append("============================================\n\n");
        prompt.append("Generate exactly ").append(count).append(" posts for ").append(platform.name()).append(".\n\n");
        prompt.append("{\n");
        prompt.append("  \"posts\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"Catchy/engaging title\",\n");
        prompt.append("      \"content\": \"Full post content respecting character limits\",\n");
        prompt.append("      \"hashtags\": [\"tag1\", \"tag2\", \"tag3\"],\n");
        prompt.append("      \"imagePrompt\": \"Short keyword image prompt for AI (8-15 words max): subject, style, lighting, composition — NO full sentences\"\n");
        prompt.append("    },\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"Another post title\",\n");
        prompt.append("      \"content\": \"Another post content\",\n");
        prompt.append("      \"hashtags\": [\"tag1\", \"tag2\"],\n");
        prompt.append("      \"imagePrompt\": \"server room blue lights technology modern\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        // QUALITY CHECK
        prompt.append("============================================\n");
        prompt.append("QUALITY CHECK\n");
        prompt.append("============================================\n\n");
        prompt.append("Before returning, ensure:\n");
        prompt.append("- Each post is unique and different from the others in this batch\n");
        prompt.append("- Each post has a strong hook\n");
        prompt.append("- Each post provides unique value\n");
        prompt.append("- The platform tone is native to ").append(platform.name()).append("\n");
        prompt.append("- CTAs are natural, not pushy\n");
        prompt.append("- No repetition across posts\n");
        prompt.append("- JSON is valid and parseable\n");
        prompt.append("- Character limits are respected\n\n");

        prompt.append("Now generate ").append(count).append(" ").append(platform.name()).append(" posts.\n");

        return prompt.toString();
    }
}