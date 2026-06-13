package com.example.metatry.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsSchedulerTest {

    @Mock private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsScheduler analyticsScheduler;

    @Test
    void collectAnalytics_delegatesToService() {
        analyticsScheduler.collectAnalytics();
        verify(analyticsService).collectMetricsForPublishedPosts();
    }
}
