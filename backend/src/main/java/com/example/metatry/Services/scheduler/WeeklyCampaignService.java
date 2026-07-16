package com.example.metatry.Services.scheduler;

import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.prompts.WeeklyCampaignPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeeklyCampaignService {

    private final CampaignRepository campaignRepository;
    private final GeminiService geminiService;
    private final WeeklyCampaignPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<Campaign> generateWeeklyCampaigns(MarketingStrategy strategy, int weekNumber) {
        String prompt = promptBuilder.build(strategy, weekNumber);
        String aiText = geminiService.generate(prompt);

        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(aiText, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse weekly campaigns response", e);
        }

        List<Map<String, Object>> campaignData = (List<Map<String, Object>>) parsed.get("campaigns");
        if (campaignData == null || campaignData.isEmpty()) {
            return List.of();
        }

        List<Campaign> campaigns = new ArrayList<>();
        for (Map<String, Object> data : campaignData) {
            Campaign campaign = Campaign.builder()
                    .name((String) data.getOrDefault("name", "Weekly Campaign"))
                    .topic((String) data.getOrDefault("topic", ""))
                    .createdAt(LocalDateTime.now())
                    .marketingStrategy(strategy)
                    .build();
            campaigns.add(campaign);
        }

        return campaignRepository.saveAll(campaigns);
    }
}
