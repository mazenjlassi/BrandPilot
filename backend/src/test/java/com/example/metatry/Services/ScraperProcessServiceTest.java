package com.example.metatry.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScraperProcessServiceTest {

    private ScraperProcessService scraperProcessService;

    @BeforeEach
    void setUp() {
        scraperProcessService = new ScraperProcessService();
    }

    @Test
    void isAlive_returnsFalse_whenNoServerRunning() {
        boolean alive = scraperProcessService.isAlive();
        assertThat(alive).isFalse();
    }

    @Test
    void cleanup_doesNotThrow() {
        scraperProcessService.cleanup();
    }

}
