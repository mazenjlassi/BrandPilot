package com.example.metatry.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsScheduler {

    private final AnalyticsService analyticsService;

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public synchronized void collectAnalytics() {

        System.out.println("Running scheduled analytics...");

        analyticsService.collectMetricsForPublishedPosts();

        System.out.println("Analytics finished.");
    }
}