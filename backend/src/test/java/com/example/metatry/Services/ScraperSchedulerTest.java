package com.example.metatry.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperSchedulerTest {

    @Mock private ScraperService scraperService;

    @InjectMocks
    private ScraperScheduler scraperScheduler;

    @Test
    void scheduledScrapeAll_delegatesToScraperService() {
        scraperScheduler.scheduledScrapeAll();
        verify(scraperService).scrapeAllCompanies();
    }

    @Test
    void scheduledScrapeAll_whenScraperFails_doesNotThrow() {
        doThrow(new RuntimeException("Scrape error")).when(scraperService).scrapeAllCompanies();

        scraperScheduler.scheduledScrapeAll();

        verify(scraperService).scrapeAllCompanies();
    }
}
