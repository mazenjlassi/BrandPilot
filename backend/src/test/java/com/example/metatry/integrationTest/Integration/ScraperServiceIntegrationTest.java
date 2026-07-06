package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.ScrapeResponse;
import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Repositories.CompanyProfileRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import com.example.metatry.Services.*;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ScraperServiceIntegrationTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private ScrapedPostService scrapedPostService;

    @MockitoBean
    private PatternAnalysisService patternAnalysisService;

    @MockitoBean
    private ScraperProcessService scraperProcessService;

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private CompanyProfileRepository companyProfileRepository;

    @MockitoBean
    private ScrapedPostRepository scrapedPostRepository;

    @BeforeEach
    void setUp() {
        companyProfileRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        companyProfileRepository.deleteAll();
    }

    // ================= scrapeCompany =================

    @Test
    void scrapeCompany_success() {
        companyProfileRepository.save(CompanyProfile.builder()
                .companyName("TestCorp")
                .linkedinUrl("http://linkedin.com/test")
                .instagramUrl("http://instagram.com/test")
                .facebookUrl("http://facebook.com/test")
                .build());

        Map<String, Object> apiResponse = Map.of(
                "results", List.of(
                        Map.of("platform", "linkedin", "posts", List.of(
                                Map.of("postText", "LinkedIn post", "url", "http://li.com/p/1", "postedAt", "2024-01-01")
                        ))
                )
        );

        when(scraperProcessService.isAlive()).thenReturn(true);
        doNothing().when(scraperProcessService).ensureRunning();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
        when(scrapedPostService.save(any())).thenReturn(null);
        when(scrapedPostRepository.countByCompanyNameAndUsedForPatternFalse(anyString())).thenReturn(0L);
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        ScrapeResponse response = scraperService.scrapeCompany("TestCorp");

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getTotalPosts()).isEqualTo(1);
        assertThat(response.getCompanyName()).isEqualTo("TestCorp");
    }

    @Test
    void scrapeCompany_profileNotFound_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> scraperService.scrapeCompany("NonExistent"));
    }

    // ================= scrape =================

    @Test
    void scrape_callsScrapeAndSave() {
        Map<String, Object> apiResponse = Map.of(
                "results", List.of(
                        Map.of("platform", "instagram", "posts", List.of(
                                Map.of("postText", "IG post", "url", "http://ig.com/p/1", "postedAt", "2024-02-01")
                        ))
                )
        );

        doNothing().when(scraperProcessService).ensureRunning();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
        when(scrapedPostService.save(any())).thenReturn(null);
        when(scrapedPostRepository.countByCompanyNameAndUsedForPatternFalse(anyString())).thenReturn(0L);
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        ScrapeResponse response = scraperService.scrape("DirectCorp", "http://li.com", "http://ig.com", "http://fb.com");

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getTotalPosts()).isEqualTo(1);
    }

    // ================= scrapeAndSave - API error =================

    @Test
    void scrapeAndSave_apiReturnsErrorStatus() {
        doNothing().when(scraperProcessService).ensureRunning();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(), HttpStatus.INTERNAL_SERVER_ERROR));

        ScrapeResponse response = scraperService.scrapeAndSave("ErrCorp", "http://li.com", "", "", null);

        assertThat(response.getStatus()).isEqualTo("error");
    }

    @Test
    void scrapeAndSave_apiThrows_returnsError() {
        doNothing().when(scraperProcessService).ensureRunning();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        ScrapeResponse response = scraperService.scrapeAndSave("FailCorp", "http://li.com", "", "", null);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).contains("Connection failed");
    }

    // ================= scrapeAllCompanies =================

    @Test
    void scrapeAllCompanies_scrapesAll() {
        companyProfileRepository.save(CompanyProfile.builder()
                .companyName("C1")
                .linkedinUrl("http://li.com/1").instagramUrl("").facebookUrl("")
                .build());
        companyProfileRepository.save(CompanyProfile.builder()
                .companyName("C2")
                .linkedinUrl("http://li.com/2").instagramUrl("").facebookUrl("")
                .build());

        when(scraperProcessService.isAlive()).thenReturn(true);
        doNothing().when(scraperProcessService).ensureRunning();

        Map<String, Object> apiResponse = Map.of("results", List.of());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        scraperService.scrapeAllCompanies();

        verify(restTemplate, atLeast(2)).exchange(anyString(), any(), any(), eq(Map.class));
    }

    // ================= auto-analyze when unanalyzed >= 3 =================

    @Test
    void scrapeAndSave_triggersAutoAnalysisWhenEnoughUnanalyzed() {
        Map<String, Object> apiResponse = Map.of(
                "results", List.of(
                        Map.of("platform", "facebook", "posts", List.of(
                                Map.of("postText", "FB post", "url", "http://fb.com/p/1", "postedAt", "2024-03-01")
                        ))
                )
        );

        doNothing().when(scraperProcessService).ensureRunning();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
        when(scrapedPostService.save(any())).thenReturn(null);
        when(scrapedPostRepository.countByCompanyNameAndUsedForPatternFalse(anyString())).thenReturn(3L);
        when(patternAnalysisService.analyzeUnanalyzedBatch(anyString())).thenReturn(1);
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        ScrapeResponse response = scraperService.scrapeAndSave("AutoCorp", "http://li.com", "", "", null);

        assertThat(response.getStatus()).isEqualTo("success");
        verify(patternAnalysisService).analyzeUnanalyzedBatch("AutoCorp");
    }

    @Test
    void scrapeAndSave_autoAnalysisFails_doesNotThrow() {
        Map<String, Object> apiResponse = Map.of(
                "results", List.of(
                        Map.of("platform", "facebook", "posts", List.of(
                                Map.of("postText", "FB post", "url", "http://fb.com/p/1", "postedAt", "2024-03-01")
                        ))
                )
        );

        doNothing().when(scraperProcessService).ensureRunning();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
        when(scrapedPostService.save(any())).thenReturn(null);
        when(scrapedPostRepository.countByCompanyNameAndUsedForPatternFalse(anyString())).thenReturn(5L);
        when(patternAnalysisService.analyzeUnanalyzedBatch(anyString())).thenThrow(new RuntimeException("Parse error"));
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        ScrapeResponse response = scraperService.scrapeAndSave("FailAutoCorp", "http://li.com", "", "", null);

        assertThat(response.getStatus()).isEqualTo("success");
    }
}
