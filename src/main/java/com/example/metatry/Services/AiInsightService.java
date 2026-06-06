package com.example.metatry.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInsightService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final MemoryContextService memoryContextService;

    public String analyzeComments(List<String> comments){

        String joinedComments = String.join("\n- ", comments);

        String prompt = """
You are a senior social media marketing strategist.

Analyze the following user comments and extract meaningful insights.

Return ONLY valid JSON.

STRICT RULES:
- Do NOT return text outside JSON
- Do NOT explain anything outside fields
- "topPositives" MUST be an array of short keywords (max 3 words each)
- "topComplaints" MUST be an array
- NEVER return sentences for these fields
- If no data, return empty array []

IMPORTANT:
- "summary" must be a clear, human-friendly paragraph (2–3 sentences)
- "advice" must be a detailed paragraph (3–5 sentences) with actionable recommendations
- "ideas" must be short actionable suggestions (max 6 words each)

Example format:

{
  "overallSentiment": "POSITIVE",
  "topPositives": ["clean UI", "fast loading"],
  "topComplaints": [],
  "summary": "Users are generally satisfied with the product. They appreciate the speed and simplicity, and there are no major complaints.",
  "advice": "Your content is performing well with your audience. To build on this momentum, continue using the same tone and structure while experimenting with stronger hooks and clearer calls-to-action. Consider adding more engaging visuals and interactive elements to increase user participation. Maintaining consistency will help strengthen your brand identity.",
  "ideas": ["Add CTA", "Use storytelling", "Test visuals"]
}

Comments:
- """ + joinedComments;

        String memory = memoryContextService.getRecentContext();
        String fullPrompt = prompt + "\n\n" + memory;

        return geminiService.generate(fullPrompt);
    }
}