package com.example.metatry.Controllers;

import com.example.metatry.DTOs.ScrapeRequest;
import com.example.metatry.DTOs.ScrapeResponse;
import com.example.metatry.Services.ScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final ScraperService scraperService;

    @PostMapping("/scrape")
    public ResponseEntity<ScrapeResponse> scrape(@RequestBody ScrapeRequest request) {
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            return ResponseEntity.badRequest().body(
                ScrapeResponse.builder()
                    .status("error")
                    .message("companyName is required")
                    .build()
            );
        }

        ScrapeResponse response = scraperService.scrape(
            request.getCompanyName(),
            request.getLinkedin(),
            request.getInstagram(),
            request.getFacebook()
        );

        if ("error".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger")
    public ResponseEntity<ScrapeResponse> trigger(@RequestParam String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return ResponseEntity.badRequest().body(
                ScrapeResponse.builder()
                    .status("error")
                    .message("companyName is required")
                    .build()
            );
        }

        ScrapeResponse response = scraperService.scrapeCompany(companyName);

        if ("error".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}