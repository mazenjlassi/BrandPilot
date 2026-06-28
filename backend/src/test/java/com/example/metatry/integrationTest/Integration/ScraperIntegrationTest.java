package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.DTOs.ScrapeResponse;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.ScraperService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, ScraperIntegrationTest.MockConfig.class})
class ScraperIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        ScraperService scraperService() {
            return Mockito.mock(ScraperService.class);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScraperService scraperService;

    private String token;
    private final String testName = "scr_" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setUp() {
        var register = new RegisterRequest();
        register.setName(testName);
        register.setEmail(testName + "@test.com");
        register.setPassword("password123");
        restTemplate.postForEntity("/auth/register", register, String.class);

        var login = new AuthRequest();
        login.setUsername(testName);
        login.setPassword("password123");
        ResponseEntity<Map> loginResp = restTemplate.postForEntity("/auth/login", login, Map.class);
        token = "Bearer " + loginResp.getBody().get("token");
    }

    @AfterEach
    void tearDown() {
        userRepository.findByName(testName).ifPresent(u -> userRepository.delete(u));
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return h;
    }

    @Test
    void scrape_success() {
        ScrapeResponse resp = ScrapeResponse.builder()
                .companyName("TestCo")
                .totalPosts(3)
                .status("success")
                .build();

        when(scraperService.scrape(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class)))
                .thenReturn(resp);

        ResponseEntity<String> r = restTemplate.postForEntity(
                "/api/scraper/scrape",
                Map.of("companyName", "TestCo", "linkedin", "https://linkedin.com/company/testco"),
                String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).contains("success");
    }

    @Test
    void scrape_missingCompanyName_returns400() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>("{\"linkedin\":\"https://linkedin.com/company/testco\"}", h);

        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraper/scrape", HttpMethod.POST, req, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody()).contains("\"error\"");
    }

    @Test
    void scrape_serviceError_returns400() {
        ScrapeResponse errorResp = ScrapeResponse.builder()
                .status("error")
                .message("Scraping failed")
                .build();

        when(scraperService.scrape(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class)))
                .thenReturn(errorResp);

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>("{\"companyName\":\"TestCo\"}", h);

        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraper/scrape", HttpMethod.POST, req, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void trigger_success() {
        ScrapeResponse resp = ScrapeResponse.builder()
                .companyName("TestCo")
                .totalPosts(5)
                .status("success")
                .build();

        when(scraperService.scrapeCompany(nullable(String.class))).thenReturn(resp);

        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraper/trigger?companyName=TestCo", HttpMethod.POST,
                new HttpEntity<>(null, authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void trigger_missingCompanyName_returns400() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraper/trigger?companyName=", HttpMethod.POST,
                new HttpEntity<>(null, authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void trigger_serviceError_returns400() {
        ScrapeResponse errorResp = ScrapeResponse.builder()
                .status("error")
                .message("Trigger failed")
                .build();

        when(scraperService.scrapeCompany(nullable(String.class))).thenReturn(errorResp);

        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraper/trigger?companyName=BadCo", HttpMethod.POST,
                new HttpEntity<>(null, authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void trigger_unauthorized_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraper/trigger?companyName=TestCo", HttpMethod.POST,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
