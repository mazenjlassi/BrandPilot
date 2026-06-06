package com.example.metatry.Services;

import com.example.metatry.DTOs.ScrapeRequest;
import com.example.metatry.DTOs.ScrapeResponse;
import com.example.metatry.DTOs.ScrapedPostDTO;
import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.CompanyProfileRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScraperService {

    private static final String SCRAPER_BASE_URL = "http://host.docker.internal:3001";
    
    private final RestTemplate restTemplate;
    private final ScrapedPostRepository scrapedPostRepository;
    private final ScrapedPostService scrapedPostService;
    private final PatternAnalysisService patternAnalysisService;
    private final ScraperProcessService scraperProcessService;
    private final CompanyProfileRepository companyProfileRepository;

    public ScrapeResponse scrapeCompany(String companyName) {
        CompanyProfile profile = companyProfileRepository.findByCompanyName(companyName)
            .orElseThrow(() -> new RuntimeException("Company profile not found: " + companyName));
        return scrapeAndSave(
            profile.getCompanyName(),
            profile.getLinkedinUrl(),
            profile.getInstagramUrl(),
            profile.getFacebookUrl(),
            null
        );
    }

    public void scrapeAllCompanies() {
        List<CompanyProfile> profiles = companyProfileRepository.findAll();
        for (CompanyProfile profile : profiles) {
            try {
                scrapeCompany(profile.getCompanyName());
                Thread.sleep(2000);
            } catch (Exception e) {
                System.err.println("Failed to scrape " + profile.getCompanyName() + ": " + e.getMessage());
            }
        }
    }

    public ScrapeResponse scrapeAndSave(String companyName, String linkedin, String instagram, String facebook, String topic) {
        try {
            scraperProcessService.ensureRunning();

            String url = SCRAPER_BASE_URL + "/scrape";
            
            Map<String, String> accounts = new HashMap<>();
            accounts.put("linkedin", linkedin != null ? linkedin : "");
            accounts.put("instagram", instagram != null ? instagram : "");
            accounts.put("facebook", facebook != null ? facebook : "");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("companyName", companyName);
            requestBody.put("accounts", accounts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                savePostsToDatabase(companyName, body, topic);
                
                return buildResponse(companyName, body, "success", null);
            }

            return buildResponse(companyName, null, "error", "Unexpected response");

        } catch (Exception e) {
            return buildResponse(companyName, null, "error", e.getMessage());
        }
    }

    public ScrapeResponse scrape(String companyName, String linkedin, String instagram, String facebook) {
        return scrapeAndSave(companyName, linkedin, instagram, facebook, null);
    }

    private void savePostsToDatabase(String companyName, Map<String, Object> body, String topic) {
        Object rawResults = body.get("results");
        if (rawResults instanceof List) {
            List<?> resultsList = (List<?>) rawResults;
            for (Object item : resultsList) {
                if (item instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) item;
                    String platform = String.valueOf(itemMap.get("platform"));
                    Object postsObj = itemMap.get("posts");
                    
                    if (postsObj instanceof List) {
                        List<?> postsList = (List<?>) postsObj;
                        for (Object postObj : postsList) {
                            if (postObj instanceof Map) {
                                Map<?, ?> postMap = (Map<?, ?>) postObj;
                                
                                ScrapedPost post = ScrapedPost.builder()
                                    .companyName(companyName)
                                    .platform(platform)
                                    .postText(postMap.get("postText") != null ? postMap.get("postText").toString() : "")
                                    .postUrl(postMap.get("url") != null ? postMap.get("url").toString() : "")
                                    .postedAt(postMap.get("postedAt") != null ? postMap.get("postedAt").toString() : "")
                                    .scrapedAt(LocalDateTime.now())
                                    .topic(topic != null ? topic : companyName)
                                    .usedForPattern(false)
                                    .build();
                                
                                scrapedPostService.save(post);
                            }
                        }
                    }
                }
            }
        }

        long unanalyzed = scrapedPostRepository.countByCompanyNameAndUsedForPatternFalse(companyName);
        if (unanalyzed >= 3) {
            try {
                int patternsSaved = patternAnalysisService.analyzeUnanalyzedBatch(companyName);
                System.out.println("Auto-analyzed batch for " + companyName + ": " + patternsSaved + " patterns saved from " + unanalyzed + " unanalyzed posts");
            } catch (Exception e) {
                System.out.println("Pattern analysis failed for " + companyName + " (non-fatal): " + e.getMessage());
            }
        }

        int removed = scrapedPostService.removeDuplicates();
        if (removed > 0) {
            System.out.println("Cleaned up " + removed + " duplicate posts for " + companyName);
        }
    }

    private ScrapeResponse buildResponse(String companyName, Map<String, Object> body, String status, String message) {
        if (body == null) {
            return ScrapeResponse.builder()
                .companyName(companyName)
                .totalPosts(0)
                .results(new HashMap<>())
                .status(status)
                .message(message)
                .build();
        }

        Map<String, List<ScrapedPostDTO>> results = new HashMap<>();
        int totalPosts = 0;

        Object rawResults = body.get("results");
        if (rawResults instanceof List) {
            List<?> resultsList = (List<?>) rawResults;
            for (Object item : resultsList) {
                if (item instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) item;
                    String platform = String.valueOf(itemMap.get("platform"));
                    Object postsObj = itemMap.get("posts");
                    
                    List<ScrapedPostDTO> posts = new ArrayList<>();
                    if (postsObj instanceof List) {
                        List<?> postsList = (List<?>) postsObj;
                        for (Object postObj : postsList) {
                            if (postObj instanceof Map) {
                                Map<?, ?> postMap = (Map<?, ?>) postObj;
                                String postText = postMap.get("postText") != null ? postMap.get("postText").toString() : "";
                            String postedAt = postMap.get("postedAt") != null ? postMap.get("postedAt").toString() : "";
                            String url = postMap.get("url") != null ? postMap.get("url").toString() : "";
                            
                            ScrapedPostDTO post = ScrapedPostDTO.builder()
                                .platform(platform)
                                .postText(postText)
                                .postedAt(postedAt)
                                .url(url)
                                .build();
                            posts.add(post);
                            }
                        }
                    }
                    results.put(platform, posts);
                    totalPosts += posts.size();
                }
            }
        }

        return ScrapeResponse.builder()
            .companyName(companyName)
            .totalPosts(totalPosts)
            .results(results)
            .status(status)
            .message(message)
            .build();
    }
}