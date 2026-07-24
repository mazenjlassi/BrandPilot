package com.example.metatry.Controllers;

import com.example.metatry.DTOs.IngestRequest;
import com.example.metatry.DTOs.ScrapeRequest;
import com.example.metatry.DTOs.ScrapeResponse;
import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Repositories.CompanyProfileRepository;
import com.example.metatry.Services.ScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final ScraperService scraperService;
    private final CompanyProfileRepository companyProfileRepository;

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

    @GetMapping("/companies")
    public ResponseEntity<List<CompanyProfile>> getCompanies(
            @RequestHeader("Authorization") String auth) {
        scraperService.validateToken(auth);
        return ResponseEntity.ok(companyProfileRepository.findAll());
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(
            @RequestHeader("Authorization") String auth,
            @RequestBody IngestRequest request) {
        scraperService.validateToken(auth);
        int saved = scraperService.ingestPosts(request);
        return ResponseEntity.ok(Map.of("saved", saved));
    }
}
