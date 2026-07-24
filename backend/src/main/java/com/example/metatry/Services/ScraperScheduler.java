package com.example.metatry.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ScraperScheduler {

    private final ScraperService scraperService;

    // Scheduling handled by GitHub Actions workflow (.github/workflows/scraper.yml)
    // @Scheduled(fixedRate = 86400000)
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
