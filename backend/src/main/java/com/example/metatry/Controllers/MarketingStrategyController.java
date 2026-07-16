package com.example.metatry.Controllers;

import com.example.metatry.DTOs.GenerateStrategyRequest;
import com.example.metatry.DTOs.MarketingStrategyDTO;
import com.example.metatry.DTOs.MarketingStrategyRequest;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.scheduler.WeeklyCampaignService;
import com.example.metatry.Services.scheduler.WeeklyImageDecisionService;
import com.example.metatry.Services.scheduler.WeeklyPostPlanner;
import com.example.metatry.Services.strategy.MarketingStrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marketing-strategies")
@RequiredArgsConstructor
public class MarketingStrategyController {

    private final MarketingStrategyService strategyService;
    private final WeeklyCampaignService weeklyCampaignService;
    private final WeeklyPostPlanner weeklyPostPlanner;
    private final WeeklyImageDecisionService weeklyImageDecisionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<MarketingStrategyDTO> getAllStrategies() {
        return strategyService.getAllStrategies();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> getActiveStrategy() {
        MarketingStrategyDTO active = strategyService.getActiveStrategy();
        if (active == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(active);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> getStrategy(@PathVariable Long id) {
        return ResponseEntity.ok(strategyService.getStrategy(id));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> generateStrategy(@RequestBody GenerateStrategyRequest request) {
        MarketingStrategyDTO strategy = strategyService.generateStrategy(request);
        return ResponseEntity.ok(strategy);
    }

    @PostMapping("/generate-auto")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> generateAutoStrategy() {
        MarketingStrategyDTO strategy = strategyService.generateAutoStrategy();
        return ResponseEntity.ok(strategy);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> updateStrategy(
            @PathVariable Long id,
            @RequestBody MarketingStrategyRequest request) {
        return ResponseEntity.ok(strategyService.updateStrategy(id, request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> approveStrategy(@PathVariable Long id) {
        return ResponseEntity.ok(strategyService.approveStrategy(id));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> deactivateStrategy(@PathVariable Long id) {
        return ResponseEntity.ok(strategyService.deactivateStrategy(id));
    }

    @PostMapping("/{id}/generate-week")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<Map<String, Object>> generateWeek(@PathVariable Long id) {
        MarketingStrategyDTO strategyDTO = strategyService.getStrategy(id);
        com.example.metatry.Models.MarketingStrategy strategy =
                new com.example.metatry.Models.MarketingStrategy();
        strategy.setTitle(strategyDTO.getTitle());
        strategy.setDescription(strategyDTO.getDescription());
        strategy.setDurationWeeks(strategyDTO.getDurationWeeks());

        List<Campaign> campaigns = weeklyCampaignService.generateWeeklyCampaigns(strategy, 1);
        List<Post> posts = weeklyPostPlanner.generateWeeklyPosts(strategy, campaigns);
        weeklyImageDecisionService.decideAndGenerateImages(posts,
                strategyDTO.getTitle() + ": " + strategyDTO.getSummary());

        return ResponseEntity.ok(Map.of(
                "campaigns", campaigns.size(),
                "posts", posts.size()
        ));
    }

    @PostMapping("/generate-current-week")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<Map<String, Object>> generateCurrentWeek() {
        MarketingStrategyDTO active = strategyService.getActiveStrategy();
        if (active == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active strategy found"));
        }
        return generateWeek(active.getId());
    }

    @PutMapping("/{id}/auto-generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<MarketingStrategyDTO> setAutoGenerate(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean autoGenerate = body.getOrDefault("autoGenerate", false);
        return ResponseEntity.ok(strategyService.setAutoGenerate(id, autoGenerate));
    }
}
