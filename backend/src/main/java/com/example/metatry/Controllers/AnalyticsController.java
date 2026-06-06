package com.example.metatry.Controllers;

import com.example.metatry.Services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/collect")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<String> collectMetrics(){

        analyticsService.collectMetricsForPublishedPosts();

        return ResponseEntity.ok("Metrics collected !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
}