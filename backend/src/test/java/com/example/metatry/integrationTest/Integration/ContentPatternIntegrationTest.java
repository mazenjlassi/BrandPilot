package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class ContentPatternIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentPatternRepository contentPatternRepository;

    private String token;
    private final String testName = "cptint_" + UUID.randomUUID().toString().substring(0, 8);
    private final String testTopic = "topic_" + UUID.randomUUID().toString().substring(0, 8);
    private Long createdPatternId;

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
        if (createdPatternId != null) {
            contentPatternRepository.findById(createdPatternId)
                    .ifPresent(p -> contentPatternRepository.delete(p));
        }
        contentPatternRepository.findByCompanyName(testName)
                .forEach(p -> contentPatternRepository.delete(p));
        userRepository.findByName(testName).ifPresent(u -> userRepository.delete(u));
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return h;
    }

    private HttpHeaders jsonAuthHeaders() {
        HttpHeaders h = authHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void createContentPattern_success() {
        var pattern = ContentPattern.builder()
                .companyName(testName)
                .topic(testTopic)
                .campaignName("Test Campaign")
                .platformBreakdown("FACEBOOK: 5, INSTAGRAM: 3")
                .postFrequency("3 per week")
                .contentLength("medium")
                .mediaType("image")
                .hashtagCount("5-10")
                .timingPattern("mornings")
                .tone("professional")
                .ctaStyle("link in bio")
                .totalPostsAnalyzed(8)
                .build();

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.POST,
                new HttpEntity<>(pattern, jsonAuthHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "topic", "companyName");
        assertThat(r.getBody().get("topic")).isEqualTo(testTopic);

        createdPatternId = ((Number) r.getBody().get("id")).longValue();
    }

    @Test
    void getAllPatterns_returnsList() {
        var pattern = ContentPattern.builder()
                .companyName(testName).topic(testTopic)
                .build();
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.POST,
                new HttpEntity<>(pattern, jsonAuthHeaders()), Map.class);
        createdPatternId = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<List> r = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getPatternById_success() {
        var pattern = ContentPattern.builder()
                .companyName(testName).topic(testTopic)
                .build();
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.POST,
                new HttpEntity<>(pattern, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();
        createdPatternId = id;

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/patterns/crud/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("topic")).isEqualTo(testTopic);
    }

    @Test
    void getPatternById_notFound_returns404() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/patterns/crud/99999", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateContentPattern_success() {
        var pattern = ContentPattern.builder()
                .companyName(testName).topic(testTopic)
                .build();
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.POST,
                new HttpEntity<>(pattern, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();
        createdPatternId = id;

        var updated = ContentPattern.builder()
                .companyName(testName).topic(testTopic)
                .postFrequency("5 per week")
                .tone("updated tone")
                .build();

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/patterns/crud/" + id, HttpMethod.PUT,
                new HttpEntity<>(updated, jsonAuthHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("postFrequency")).isEqualTo("5 per week");
        assertThat(r.getBody().get("tone")).isEqualTo("updated tone");
    }

    @Test
    void deleteContentPattern_success() {
        var pattern = ContentPattern.builder()
                .companyName(testName).topic(testTopic)
                .build();
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.POST,
                new HttpEntity<>(pattern, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<Void> delR = restTemplate.exchange(
                "/api/patterns/crud/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), Void.class);

        assertThat(delR.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(contentPatternRepository.findById(id)).isEmpty();
    }

    @Test
    void existsEndpoint_returnsTrue() {
        var pattern = ContentPattern.builder()
                .companyName(testName).topic(testTopic)
                .build();
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.POST,
                new HttpEntity<>(pattern, jsonAuthHeaders()), Map.class);
        createdPatternId = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<Boolean> r = restTemplate.exchange(
                "/api/patterns/crud/exists?topic=" + testTopic, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Boolean.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isTrue();
    }

    @Test
    void getPatternByTopic_success() {
        var pattern = ContentPattern.builder()
                .companyName(testName).topic(testTopic)
                .build();
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.POST,
                new HttpEntity<>(pattern, jsonAuthHeaders()), Map.class);
        createdPatternId = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/patterns/" + testTopic, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPatternByTopic_notFound_returns404() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/patterns/nonexistent_topic", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/patterns/crud", HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
