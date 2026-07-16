package com.example.metatry.Services.strategy;

import com.example.metatry.DTOs.GenerateStrategyRequest;
import com.example.metatry.DTOs.MarketingStrategyDTO;
import com.example.metatry.DTOs.MarketingStrategyRequest;
import com.example.metatry.Enums.MarketingStrategyStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.MarketingStrategyRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.MemoryContextService;
import com.example.metatry.Services.prompts.StrategyPromptBuilder;
import com.example.metatry.Services.scheduler.WeeklyImageDecisionService;
import com.example.metatry.Services.scheduler.WeeklyPostPlanner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketingStrategyService {

    private final MarketingStrategyRepository strategyRepository;
    private final CampaignRepository campaignRepository;
    private final GeminiService geminiService;
    private final StrategyPromptBuilder strategyPromptBuilder;
    private final MemoryContextService memoryContextService;
    private final MarketingStrategyMapper marketingStrategyMapper;
    private final WeeklyPostPlanner weeklyPostPlanner;
    private final WeeklyImageDecisionService weeklyImageDecisionService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public MarketingStrategyDTO generateStrategy(GenerateStrategyRequest request) {
        if (strategyRepository.existsByStatus(MarketingStrategyStatus.PENDING)) {
            throw new RuntimeException("A PENDING strategy already exists. Approve or deactivate it first.");
        }

        String prompt = strategyPromptBuilder.build(request.getTopic(), request.getDurationWeeks());
        String aiText = geminiService.generate(prompt);

        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(aiText, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI strategy response", e);
        }

        String campaignsJson = null;
        Object campaignsRaw = parsed.get("campaigns");
        if (campaignsRaw != null) {
            try {
                campaignsJson = objectMapper.writeValueAsString(campaignsRaw);
            } catch (Exception ignored) {}
        }

        MarketingStrategy strategy = MarketingStrategy.builder()
                .title((String) parsed.getOrDefault("title", "AI Strategy: " + request.getTopic()))
                .summary((String) parsed.getOrDefault("summary", ""))
                .description((String) parsed.getOrDefault("description", ""))
                .aiReasoning((String) parsed.getOrDefault("aiReasoning", ""))
                .durationWeeks(parsed.get("durationWeeks") != null
                        ? ((Number) parsed.get("durationWeeks")).intValue()
                        : request.getDurationWeeks())
                .createdByAI(true)
                .approved(false)
                .status(MarketingStrategyStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .campaignPlans(campaignsJson)
                .autoGenerate(request.getAutoGenerate() != null && request.getAutoGenerate())
                .build();

        if (strategy.getDurationWeeks() != null) {
            strategy.setStartDate(LocalDate.now());
            strategy.setExpectedEndDate(LocalDate.now().plusWeeks(strategy.getDurationWeeks()));
        }

        strategy = strategyRepository.save(strategy);
        return marketingStrategyMapper.toDTO(strategy);
    }

    @Transactional
    public MarketingStrategyDTO generateAutoStrategy() {
        String context = memoryContextService.getRecentContext();
        String aiResponse = geminiService.generate(
                "Based on this context, suggest a short marketing strategy topic (max 10 words). " +
                "Return ONLY the topic, no quotes, no extra text.\n\nContext:\n" + context
        );
        String topic = aiResponse.replaceAll("[\"*#]","").trim();
        if (topic.length() > 80) topic = topic.substring(0, 80);

        GenerateStrategyRequest request = GenerateStrategyRequest.builder()
                .topic(topic)
                .durationWeeks(8)
                .autoGenerate(true)
                .build();
        return generateStrategy(request);
    }

    @Transactional(readOnly = true)
    public MarketingStrategyDTO getActiveStrategy() {
        return strategyRepository.findFirstByStatusOrderByCreatedAtDesc(MarketingStrategyStatus.ACTIVE)
                .map(marketingStrategyMapper::toDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MarketingStrategyDTO> getAllStrategies() {
        return strategyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(marketingStrategyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MarketingStrategyDTO getStrategy(Long id) {
        return strategyRepository.findById(id)
                .map(marketingStrategyMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));
    }

    @Transactional
    public MarketingStrategyDTO updateStrategy(Long id, MarketingStrategyRequest request) {
        MarketingStrategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));

        if (strategy.getStatus() == MarketingStrategyStatus.COMPLETED
                || strategy.getStatus() == MarketingStrategyStatus.INACTIVE) {
            throw new RuntimeException("Cannot edit a " + strategy.getStatus().name().toLowerCase() + " strategy");
        }

        if (request.getTitle() != null) strategy.setTitle(request.getTitle());
        if (request.getSummary() != null) strategy.setSummary(request.getSummary());
        if (request.getDescription() != null) strategy.setDescription(request.getDescription());
        if (request.getDurationWeeks() != null) {
            strategy.setDurationWeeks(request.getDurationWeeks());
            strategy.setExpectedEndDate(LocalDate.now().plusWeeks(request.getDurationWeeks()));
        }
        if (request.getManagerNotes() != null) strategy.setManagerNotes(request.getManagerNotes());
        if (request.getAutoGenerate() != null) strategy.setAutoGenerate(request.getAutoGenerate());

        strategy.setUpdatedAt(LocalDateTime.now());
        strategy = strategyRepository.save(strategy);
        return marketingStrategyMapper.toDTO(strategy);
    }

    @Transactional
    public MarketingStrategyDTO approveStrategy(Long id) {
        MarketingStrategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));

        if (strategy.getStatus() != MarketingStrategyStatus.PENDING) {
            throw new RuntimeException("Only PENDING strategies can be approved");
        }

        strategyRepository.findFirstByStatusOrderByCreatedAtDesc(MarketingStrategyStatus.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(MarketingStrategyStatus.INACTIVE);
                    strategyRepository.save(active);
                });

        strategy.setApproved(true);
        strategy.setApprovedAt(LocalDateTime.now());
        strategy.setStatus(MarketingStrategyStatus.ACTIVE);
        strategy.setStartDate(LocalDate.now());
        if (strategy.getDurationWeeks() != null) {
            strategy.setExpectedEndDate(LocalDate.now().plusWeeks(strategy.getDurationWeeks()));
        }
        strategy.setUpdatedAt(LocalDateTime.now());

        strategy = strategyRepository.save(strategy);

        List<Campaign> campaigns = createCampaignsFromPlans(strategy);

        try {
            List<Post> posts = weeklyPostPlanner.generateWeeklyPosts(strategy, campaigns);
            String strategyContext = strategy.getTitle() + ": " + strategy.getSummary();
            weeklyImageDecisionService.decideAndGenerateImages(posts, strategyContext);

            strategy.setLastWeeklyGeneration(LocalDate.now());
            strategyRepository.save(strategy);

            notificationService.createNotification(
                    "Strategy \"" + strategy.getTitle() + "\" approved. Week 1 content generated: "
                            + campaigns.size() + " campaigns, " + posts.size() + " posts ready for review.",
                    "INFO",
                    "/weekly-planner"
            );
        } catch (Exception e) {
            notificationService.createNotification(
                    "Strategy \"" + strategy.getTitle() + "\" approved but week 1 generation failed: " + e.getMessage(),
                    "WARNING",
                    "/marketing-strategies/" + strategy.getId()
            );
        }

        return marketingStrategyMapper.toDTO(strategy);
    }

    private List<Campaign> createCampaignsFromPlans(MarketingStrategy strategy) {
        if (strategy.getCampaignPlans() == null || strategy.getCampaignPlans().isBlank()) {
            return List.of();
        }

        try {
            List<Map<String, Object>> campaignData = objectMapper.readValue(
                    strategy.getCampaignPlans(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<Campaign> campaigns = new ArrayList<>();
            for (Map<String, Object> data : campaignData) {
                Campaign campaign = Campaign.builder()
                        .name((String) data.getOrDefault("name", "Campaign"))
                        .topic((String) data.getOrDefault("topic", ""))
                        .createdAt(LocalDateTime.now())
                        .marketingStrategy(strategy)
                        .build();
                campaigns.add(campaign);
            }

            return campaignRepository.saveAll(campaigns);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse campaign plans", e);
        }
    }

    @Transactional
    public MarketingStrategyDTO setAutoGenerate(Long id, boolean autoGenerate) {
        MarketingStrategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));

        strategy.setAutoGenerate(autoGenerate);
        strategy.setUpdatedAt(LocalDateTime.now());
        strategy = strategyRepository.save(strategy);

        String verb = autoGenerate ? "enabled" : "disabled";
        notificationService.createNotification(
                "Auto-generate " + verb + " for strategy \"" + strategy.getTitle() + "\".",
                "INFO",
                "/marketing-strategies"
        );

        return marketingStrategyMapper.toDTO(strategy);
    }

    @Transactional
    public MarketingStrategyDTO deactivateStrategy(Long id) {
        MarketingStrategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));

        strategy.setStatus(MarketingStrategyStatus.INACTIVE);
        strategy.setUpdatedAt(LocalDateTime.now());
        strategy = strategyRepository.save(strategy);
        return marketingStrategyMapper.toDTO(strategy);
    }

    @Transactional
    public void autoCompleteExpiredStrategies() {
        List<MarketingStrategy> activeStrategies = List.copyOf(
                strategyRepository.findAll().stream()
                        .filter(s -> s.getStatus() == MarketingStrategyStatus.ACTIVE)
                        .toList()
        );

        for (MarketingStrategy strategy : activeStrategies) {
            if (strategy.getExpectedEndDate() != null
                    && strategy.getExpectedEndDate().isBefore(LocalDate.now())) {
                strategy.setStatus(MarketingStrategyStatus.COMPLETED);
                strategy.setUpdatedAt(LocalDateTime.now());
                strategyRepository.save(strategy);
            }
        }
    }
}
