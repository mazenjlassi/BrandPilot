package com.example.metatry.Services.scheduler;

import com.example.metatry.DTOs.GenerateStrategyRequest;
import com.example.metatry.Enums.MarketingStrategyStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.MarketingStrategyRepository;
import com.example.metatry.Services.EmailService;
import com.example.metatry.Services.strategy.MarketingStrategyService;
import com.example.metatry.Services.strategy.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyMarketingScheduler {

    private final MarketingStrategyRepository strategyRepository;
    private final CampaignRepository campaignRepository;
    private final MarketingStrategyService strategyService;
    private final WeeklyPostPlanner weeklyPostPlanner;
    private final WeeklyImageDecisionService weeklyImageDecisionService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * MON")
    @Transactional
    public void executeWeeklyGeneration() {
        MarketingStrategy activeStrategy = strategyRepository
                .findFirstByStatusOrderByCreatedAtDesc(MarketingStrategyStatus.ACTIVE)
                .orElse(null);

        if (activeStrategy == null) {
            notificationService.createNotification(
                    "No active marketing strategy found. Please generate a new strategy.",
                    "WARNING",
                    "/marketing-strategies"
            );
            return;
        }

        if (activeStrategy.getLastWeeklyGeneration() != null
                && activeStrategy.getLastWeeklyGeneration().isAfter(LocalDate.now().minusDays(6))) {
            return;
        }

        int weekNumber = activeStrategy.getDurationWeeks() != null
                ? (int) (LocalDate.now().toEpochDay() - activeStrategy.getStartDate().toEpochDay()) / 7 + 1
                : 1;

        if (weekNumber > activeStrategy.getDurationWeeks()) {
            handleStrategyCompletion(activeStrategy);
            return;
        }

        try {
            List<Campaign> campaigns = campaignRepository.findByMarketingStrategyId(activeStrategy.getId());

            if (campaigns.isEmpty()) {
                notificationService.createNotification(
                        "No campaigns found for strategy \"" + activeStrategy.getTitle() + "\". Cannot generate weekly posts.",
                        "WARNING",
                        "/marketing-strategies/" + activeStrategy.getId()
                );
                return;
            }

            List<Post> posts = weeklyPostPlanner.generateWeeklyPosts(activeStrategy, campaigns);

            String strategyContext = activeStrategy.getTitle() + ": " + activeStrategy.getSummary();
            weeklyImageDecisionService.decideAndGenerateImages(posts, strategyContext);

            activeStrategy.setLastWeeklyGeneration(LocalDate.now());
            strategyRepository.save(activeStrategy);

            notificationService.createNotification(
                    "Week " + weekNumber + " content generated: " + campaigns.size()
                            + " campaigns, " + posts.size() + " posts ready for review.",
                    "INFO",
                    "/weekly-planner"
            );

            emailService.sendStrategyNotification(
                    "Weekly Content Generated - Week " + weekNumber,
                    "Week " + weekNumber + " content has been generated.\n\n"
                            + "Campaigns: " + campaigns.size() + "\n"
                            + "Posts: " + posts.size() + "\n\n"
                            + "Please review and approve at your dashboard."
            );

        } catch (Exception e) {
            notificationService.createNotification(
                    "Weekly generation failed: " + e.getMessage(),
                    "ERROR",
                    null
            );

            emailService.sendStrategyNotification(
                    "Weekly Content Generation Failed",
                    "Error: " + e.getMessage()
            );
        }
    }

    private void handleStrategyCompletion(MarketingStrategy strategy) {
        strategy.setStatus(MarketingStrategyStatus.COMPLETED);
        strategy.setUpdatedAt(LocalDate.now().atStartOfDay());
        strategyRepository.save(strategy);

        if (Boolean.TRUE.equals(strategy.getAutoGenerate())) {
            notificationService.createNotification(
                    "Strategy \"" + strategy.getTitle() + "\" completed. Auto-generating a new strategy...",
                    "INFO",
                    "/marketing-strategies"
            );
            triggerNewStrategyGeneration(strategy);
        } else {
            notificationService.createNotification(
                    "Strategy \"" + strategy.getTitle() + "\" completed.",
                    "INFO",
                    "/marketing-strategies"
            );
        }

        emailService.sendStrategyNotification(
                "Marketing Strategy Completed",
                "Strategy \"" + strategy.getTitle() + "\" has completed."
                        + (Boolean.TRUE.equals(strategy.getAutoGenerate())
                            ? "\n\nAuto-generate is ON — a new strategy will be generated."
                            : "")
        );
    }

    private void triggerNewStrategyGeneration(MarketingStrategy completedStrategy) {
        try {
            String topic = completedStrategy.getCampaignPlans() != null
                    ? "Follow-up to: " + completedStrategy.getTitle()
                    : "New strategy after: " + completedStrategy.getTitle();
            GenerateStrategyRequest req = new GenerateStrategyRequest();
            req.setTopic(topic);
            req.setDurationWeeks(completedStrategy.getDurationWeeks());
            req.setAutoGenerate(true);
            strategyService.generateStrategy(req);
        } catch (Exception e) {
            notificationService.createNotification(
                    "Failed to auto-generate new strategy: " + e.getMessage(),
                    "ERROR",
                    "/marketing-strategies"
            );
        }
    }
}
