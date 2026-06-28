package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.DTOs.ScrapeResponse;
import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Repositories.CompanyProfileRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperServiceUnitTest {

    @Mock private RestTemplate restTemplate;
    @Mock private ScrapedPostRepository scrapedPostRepository;
    @Mock private ScrapedPostService scrapedPostService;
    @Mock private PatternAnalysisService patternAnalysisService;
    @Mock private ScraperProcessService scraperProcessService;
    @Mock private CompanyProfileRepository companyProfileRepository;

    @InjectMocks
    private ScraperService scraperService;

    @Test
    void scrapeCompany_throwsWhenProfileNotFound() {
        when(companyProfileRepository.findByCompanyName("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scraperService.scrapeCompany("Unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Company profile not found");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeAndSave_callsNodeScraper() {
        doNothing().when(scraperProcessService).ensureRunning();

        Map<String, Object> responseBody = Map.of(
                "companyName", "NVIDIA",
                "status", "success",
                "results", List.of(
                        Map.of("platform", "instagram", "posts", List.of(
                                Map.of("postText", "Hello world", "url", "https://ig.com/p/1", "postedAt", "2024-01-01")
                        ))
                )
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(responseEntity);

        var result = scraperService.scrapeAndSave("NVIDIA", null, "https://instagram.com/nvidia", null, null);

        assertThat(result.getStatus()).isEqualTo("success");
        verify(scraperProcessService).ensureRunning();
        verify(scrapedPostService).save(any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeAndSave_triggersPatternAnalysis_whenEnoughPosts() {
        doNothing().when(scraperProcessService).ensureRunning();

        Map<String, Object> responseBody = Map.of(
                "companyName", "NVIDIA",
                "status", "success",
                "results", List.of(
                        Map.of("platform", "instagram", "posts", List.of(
                                Map.of("postText", "Post 1", "url", "https://ig.com/p/1", "postedAt", "2024-01-01"),
                                Map.of("postText", "Post 2", "url", "https://ig.com/p/2", "postedAt", "2024-01-02"),
                                Map.of("postText", "Post 3", "url", "https://ig.com/p/3", "postedAt", "2024-01-03")
                        ))
                )
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);
        when(scrapedPostRepository.countByCompanyNameAndUsedForPatternFalse("NVIDIA")).thenReturn(3L);
        when(patternAnalysisService.analyzeUnanalyzedBatch("NVIDIA")).thenReturn(1);

        var result = scraperService.scrapeAndSave("NVIDIA", null, null, null, null);

        assertThat(result.getStatus()).isEqualTo("success");
        verify(patternAnalysisService).analyzeUnanalyzedBatch("NVIDIA");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeAndSave_returnsError_whenNodeUnreachable() {
        doNothing().when(scraperProcessService).ensureRunning();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        var result = scraperService.scrapeAndSave("NVIDIA", null, null, null, null);

        assertThat(result.getStatus()).isEqualTo("error");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeAndSave_removesDuplicates() {
        doNothing().when(scraperProcessService).ensureRunning();

        Map<String, Object> responseBody = Map.of(
                "companyName", "NVIDIA",
                "status", "success",
                "results", List.of()
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);
        when(scrapedPostService.removeDuplicates()).thenReturn(2);

        scraperService.scrapeAndSave("NVIDIA", null, null, null, null);

        verify(scrapedPostService).removeDuplicates();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeCompany_passesProfileUrls() {
        CompanyProfile profile = CompanyProfile.builder()
                .companyName("NVIDIA")
                .linkedinUrl("https://linkedin.com/nvidia")
                .instagramUrl("https://instagram.com/nvidia")
                .facebookUrl("https://facebook.com/nvidia")
                .build();
        when(companyProfileRepository.findByCompanyName("NVIDIA")).thenReturn(Optional.of(profile));
        doNothing().when(scraperProcessService).ensureRunning();

        ResponseEntity<Map> responseEntity = new ResponseEntity(
                Map.of("companyName", "NVIDIA", "status", "success", "results", List.of()),
                HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        ScrapeResponse result = scraperService.scrapeCompany("NVIDIA");

        assertThat(result.getStatus()).isEqualTo("success");
        verify(companyProfileRepository).findByCompanyName("NVIDIA");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeAndSave_savesPostsForMultiplePlatforms() {
        doNothing().when(scraperProcessService).ensureRunning();
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        Map<String, Object> responseBody = Map.of(
                "companyName", "Acme",
                "status", "success",
                "results", List.of(
                        Map.of("platform", "instagram", "posts", List.of(
                                Map.of("postText", "IG post", "url", "https://ig.com/p/1", "postedAt", "2024-01-01")
                        )),
                        Map.of("platform", "linkedin", "posts", List.of(
                                Map.of("postText", "LI post", "url", "https://li.com/p/1", "postedAt", "2024-01-02")
                        ))
                )
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        scraperService.scrapeAndSave("Acme", null, null, null, null);

        verify(scrapedPostService, times(2)).save(any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeAndSave_doesNotCrashWhenPatternAnalysisFails() {
        doNothing().when(scraperProcessService).ensureRunning();
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        Map<String, Object> responseBody = Map.of(
                "companyName", "Acme",
                "status", "success",
                "results", List.of(
                        Map.of("platform", "instagram", "posts", List.of(
                                Map.of("postText", "P1", "url", "https://ig.com/p/1", "postedAt", "2024-01-01"),
                                Map.of("postText", "P2", "url", "https://ig.com/p/2", "postedAt", "2024-01-02"),
                                Map.of("postText", "P3", "url", "https://ig.com/p/3", "postedAt", "2024-01-03")
                        ))
                )
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);
        when(scrapedPostRepository.countByCompanyNameAndUsedForPatternFalse("Acme")).thenReturn(3L);
        when(patternAnalysisService.analyzeUnanalyzedBatch("Acme")).thenThrow(new RuntimeException("Analysis error"));

        ScrapeResponse result = scraperService.scrapeAndSave("Acme", null, null, null, null);

        assertThat(result.getStatus()).isEqualTo("success");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void scrapeAndSave_handlesEmptyResults() {
        doNothing().when(scraperProcessService).ensureRunning();
        when(scrapedPostService.removeDuplicates()).thenReturn(0);

        ResponseEntity<Map> responseEntity = new ResponseEntity(
                Map.of("companyName", "Acme", "status", "success", "results", List.of()),
                HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        ScrapeResponse result = scraperService.scrapeAndSave("Acme", null, null, null, null);

        assertThat(result.getTotalPosts()).isZero();
    }
}
