package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class PostMetricIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PostMetricRepository postMetricRepository;

    private String token;
    private final String testName = "metint_" + UUID.randomUUID().toString().substring(0, 8);
    private Long postId;

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

        Campaign c = campaignRepository.save(
                Campaign.builder().name("MetricCamp").topic("Metrics").build());
        Post p = postRepository.save(Post.builder()
                .title("Metric Post")
                .content("Test")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.DRAFT)
                .campaign(c)
                .build());
        postId = p.getId();
    }

    @AfterEach
    void tearDown() {
        postMetricRepository.findByPostIdOrderByCollectedAtAsc(postId)
                .forEach(m -> postMetricRepository.delete(m));
        if (postId != null) {
            postRepository.findById(postId).ifPresent(p -> {
                Long cid = p.getCampaign().getId();
                postRepository.delete(p);
                campaignRepository.deleteById(cid);
            });
        }
        userRepository.findByName(testName).ifPresent(u -> userRepository.delete(u));
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return h;
    }

    @Test
    void getMetricsHistory_returnsList() {
        postMetricRepository.save(PostMetric.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .likes(10).comments(2).shares(1).impressions(100)
                .collectedAt(LocalDateTime.now())
                .build());
        postMetricRepository.save(PostMetric.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .likes(20).comments(5).shares(3).impressions(200)
                .collectedAt(LocalDateTime.now().plusHours(1))
                .build());

        ResponseEntity<List> r = restTemplate.exchange(
                "/metrics/post/" + postId, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).hasSize(2);
    }

    @Test
    void getMetricsHistory_empty_returnsEmptyList() {
        ResponseEntity<List> r = restTemplate.exchange(
                "/metrics/post/" + postId, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isEmpty();
    }

    @Test
    void getLatestMetric_returnsLatest() {
        postMetricRepository.save(PostMetric.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .likes(5).collectedAt(LocalDateTime.now())
                .build());
        postMetricRepository.save(PostMetric.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .likes(15).collectedAt(LocalDateTime.now().plusHours(2))
                .build());

        ResponseEntity<Map> r = restTemplate.exchange(
                "/metrics/post/" + postId + "/latest", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("likes")).isEqualTo(15);
    }

    @Test
    void getLatestMetric_noMetrics_returnsOkWithNullBody() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/metrics/post/" + postId + "/latest", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNull();
    }

    @Test
    void getMaxMetrics_returnsMaxValues() {
        postMetricRepository.save(PostMetric.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .likes(10).comments(2).shares(1)
                .collectedAt(LocalDateTime.now())
                .build());
        postMetricRepository.save(PostMetric.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .likes(30).comments(8).shares(5)
                .collectedAt(LocalDateTime.now().plusHours(1))
                .build());

        ResponseEntity<Map> r = restTemplate.exchange(
                "/metrics/post/" + postId + "/max", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("likes")).isEqualTo(30);
        assertThat(r.getBody().get("comments")).isEqualTo(8);
        assertThat(r.getBody().get("shares")).isEqualTo(5);
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/metrics/post/" + postId, HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
