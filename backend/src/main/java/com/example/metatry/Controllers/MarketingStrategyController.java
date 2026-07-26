package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.GenerateStrategyRequest;
import com.example.metatry.DTOs.MarketingStrategyDTO;
import com.example.metatry.DTOs.MarketingStrategyRequest;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.MarketingStrategyRepository;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.scheduler.WeeklyCampaignService;
import com.example.metatry.Services.scheduler.WeeklyImageDecisionService;
import com.example.metatry.Services.scheduler.WeeklyPostPlanner;
import com.example.metatry.Services.strategy.MarketingStrategyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marketing-strategies")
@RequiredArgsConstructor
public class MarketingStrategyController {

    private static final Logger log = LoggerFactory.getLogger(MarketingStrategyController.class);

    private final MarketingStrategyService strategyService;
    private final MarketingStrategyRepository marketingStrategyRepository;
    private final CampaignService campaignService;
    private final WeeklyCampaignService weeklyCampaignService;
    private final WeeklyPostPlanner weeklyPostPlanner;
    private final WeeklyImageDecisionService weeklyImageDecisionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MarketingStrategyDTO> getAllStrategies() {
        return strategyService.getAllStrategies();
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> getActiveStrategy() {
        MarketingStrategyDTO active = strategyService.getActiveStrategy();
        if (active == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(active);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> getStrategy(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(strategyService.getStrategy(id));
        } catch (Exception e) {
            log.error("Error fetching strategy id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> generateStrategy(@RequestBody GenerateStrategyRequest request) {
        MarketingStrategyDTO strategy = strategyService.generateStrategy(request);
        return ResponseEntity.ok(strategy);
    }

    @PostMapping("/generate-auto")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> generateAutoStrategy() {
        MarketingStrategyDTO strategy = strategyService.generateAutoStrategy();
        return ResponseEntity.ok(strategy);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> updateStrategy(
            @PathVariable Long id,
            @RequestBody MarketingStrategyRequest request) {
        return ResponseEntity.ok(strategyService.updateStrategy(id, request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> approveStrategy(@PathVariable Long id) {
        return ResponseEntity.ok(strategyService.approveStrategy(id));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> deactivateStrategy(@PathVariable Long id) {
        return ResponseEntity.ok(strategyService.deactivateStrategy(id));
    }

    @GetMapping("/{id}/campaigns")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CampaignDTO>> getStrategyCampaigns(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignsByStrategy(id));
    }

    @PostMapping("/{id}/generate-week")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> generateWeek(@PathVariable Long id) {
        MarketingStrategy strategy = marketingStrategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + id));

        List<Campaign> campaigns = weeklyCampaignService.generateWeeklyCampaigns(strategy, 1);
        List<Post> posts = weeklyPostPlanner.generateWeeklyPosts(strategy, campaigns);
        weeklyImageDecisionService.decideAndGenerateImages(posts,
                strategy.getTitle() + ": " + strategy.getSummary());

        List<CampaignDTO> campaignDTOs = campaignService.getCampaignsByStrategy(id);

        return ResponseEntity.ok(Map.of(
                "campaigns", campaignDTOs,
                "posts", posts.size()
        ));
    }

    @PostMapping("/generate-current-week")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> generateCurrentWeek() {
        MarketingStrategyDTO active = strategyService.getActiveStrategy();
        if (active == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active strategy found"));
        }
        return generateWeek(active.getId());
    }

    @DeleteMapping("/inactive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteInactiveStrategies() {
        strategyService.deleteInactiveStrategies();
        return ResponseEntity.ok(Map.of("message", "Inactive strategies deleted successfully"));
    }

    @PutMapping("/{id}/auto-generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarketingStrategyDTO> setAutoGenerate(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean autoGenerate = body.getOrDefault("autoGenerate", false);
        return ResponseEntity.ok(strategyService.setAutoGenerate(id, autoGenerate));
    }
}
