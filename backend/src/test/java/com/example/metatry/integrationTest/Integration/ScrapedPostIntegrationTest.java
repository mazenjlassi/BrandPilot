package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ScrapedPostRepository;
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
class ScrapedPostIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScrapedPostRepository scrapedPostRepository;

    private String token;
    private final String testName = "spint_" + UUID.randomUUID().toString().substring(0, 8);
    private final String testCompany = "SC_" + UUID.randomUUID().toString().substring(0, 8);
    private Long createdPostId;

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
        if (createdPostId != null) {
            scrapedPostRepository.findById(createdPostId).ifPresent(p ->
                    scrapedPostRepository.delete(p));
        }
        scrapedPostRepository.findByCompanyName(testCompany).forEach(p ->
                scrapedPostRepository.delete(p));
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
    void createScrapedPost_success() {
        var post = new ScrapedPost();
        post.setCompanyName(testCompany);
        post.setPlatform("FACEBOOK");
        post.setPostText("Integration test post");
        post.setPostUrl("https://facebook.com/test");
        post.setTopic("AI");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(post, jsonAuthHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "companyName", "platform");
        assertThat(r.getBody().get("companyName")).isEqualTo(testCompany);

        createdPostId = ((Number) r.getBody().get("id")).longValue();
    }

    @Test
    void getAllScrapedPosts_returnsList() {
        var post = new ScrapedPost();
        post.setCompanyName(testCompany);
        post.setPlatform("INSTAGRAM");
        post.setPostText("List test");
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(post, jsonAuthHeaders()), Map.class);
        createdPostId = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<List> r = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getScrapedPostById_success() {
        var post = new ScrapedPost();
        post.setCompanyName(testCompany);
        post.setPlatform("LINKEDIN");
        post.setPostText("Get by ID");
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(post, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();
        createdPostId = id;

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/scraped-posts/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("postText")).isEqualTo("Get by ID");
    }

    @Test
    void getScrapedPostById_notFound_returns404() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraped-posts/99999", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateScrapedPost_success() {
        var post = new ScrapedPost();
        post.setCompanyName(testCompany);
        post.setPlatform("FACEBOOK");
        post.setPostText("Original text");
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(post, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();
        createdPostId = id;

        var updated = new ScrapedPost();
        updated.setCompanyName(testCompany);
        updated.setPlatform("FACEBOOK");
        updated.setPostText("Updated text");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/scraped-posts/" + id, HttpMethod.PUT,
                new HttpEntity<>(updated, jsonAuthHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("postText")).isEqualTo("Updated text");
    }

    @Test
    void deleteScrapedPost_success() {
        var post = new ScrapedPost();
        post.setCompanyName(testCompany);
        post.setPlatform("INSTAGRAM");
        post.setPostText("To be deleted");
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(post, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<Void> delR = restTemplate.exchange(
                "/api/scraped-posts/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), Void.class);

        assertThat(delR.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(scrapedPostRepository.findById(id)).isEmpty();
    }

    @Test
    void getScrapedPosts_withFilters_returnsFiltered() {
        var p1 = new ScrapedPost();
        p1.setCompanyName(testCompany);
        p1.setPlatform("FACEBOOK");
        p1.setPostText("FB post");
        restTemplate.exchange("/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(p1, jsonAuthHeaders()), Map.class);

        var p2 = new ScrapedPost();
        p2.setCompanyName(testCompany);
        p2.setPlatform("INSTAGRAM");
        p2.setPostText("IG post");
        ResponseEntity<Map> createR2 = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(p2, jsonAuthHeaders()), Map.class);
        createdPostId = ((Number) createR2.getBody().get("id")).longValue();

        ResponseEntity<List> r = restTemplate.exchange(
                "/api/scraped-posts?platform=INSTAGRAM", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        for (Object item : r.getBody()) {
            Map m = (Map) item;
            assertThat(m.get("platform")).isEqualTo("INSTAGRAM");
        }
    }

    @Test
    void getCount_returnsCount() {
        var post = new ScrapedPost();
        post.setCompanyName(testCompany);
        post.setPlatform("FACEBOOK");
        post.setPostText("Count test");
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.POST,
                new HttpEntity<>(post, jsonAuthHeaders()), Map.class);
        createdPostId = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/scraped-posts/count", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKey("count");
    }

    @Test
    void unauthorizedAccess_returns401() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/scraped-posts", HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
