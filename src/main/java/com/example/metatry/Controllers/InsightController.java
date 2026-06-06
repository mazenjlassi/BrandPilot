    package com.example.metatry.Controllers;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Services.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/insights")
    @RequiredArgsConstructor
    public class InsightController {

        private final InsightService insightService;


        @GetMapping("/campaign/{campaignId}")
        @PreAuthorize("isAuthenticated()")
        public PostInsightDTO getCampaignInsights(@PathVariable Long campaignId) {
            return insightService.generateCampaignInsights(campaignId);
        }
    }