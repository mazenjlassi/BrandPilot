package com.example.metatry.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScraperScheduler {

    private final ScraperService scraperService;

    @Scheduled(fixedRate = 86400000)
    public void scheduledScrapeAll() {
        System.out.println("=== Scheduled daily scrape started ===");
        try {
            scraperService.scrapeAllCompanies();
            System.out.println("=== Scheduled daily scrape completed ===");
        } catch (Exception e) {
            System.err.println("Scheduled scrape failed: " + e.getMessage());
        }
    }
}