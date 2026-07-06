package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class CampaignIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PostRepository postRepository;

    private String token;
    private final String testName = "cmpint_" + UUID.randomUUID().toString().substring(0, 8);
    private Long testCampaignId;

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
        if (testCampaignId != null) {
            HttpHeaders h = new HttpHeaders();
            h.set("Authorization", token);
            try {
                restTemplate.exchange("/campaigns/" + testCampaignId, HttpMethod.DELETE,
                        new HttpEntity<>(h), String.class);
            } catch (Exception ignored) {}
        }
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

    private Long createCampaign(String name, String topic) {
        var req = new CreateCampaignRequest();
        req.setName(name);
        req.setTopic(topic);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/campaigns", HttpMethod.POST,
                new HttpEntity<>(req, jsonAuthHeaders()), Map.class);

        testCampaignId = ((Number) r.getBody().get("id")).longValue();
        return testCampaignId;
    }

    @Test
    void createCampaign_success() {
        var req = new CreateCampaignRequest();
        req.setName("Test Campaign");
        req.setTopic("AI");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/campaigns", HttpMethod.POST,
                new HttpEntity<>(req, jsonAuthHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "name", "topic");
        assertThat(r.getBody().get("name")).isEqualTo("Test Campaign");

        Long createdId = ((Number) r.getBody().get("id")).longValue();
        HttpHeaders h = authHeaders();
        restTemplate.exchange("/campaigns/" + createdId, HttpMethod.DELETE,
                new HttpEntity<>(h), String.class);
    }

    @Test
    void getAllCampaigns_returnsList() {
        createCampaign("ListCamp", "Marketing");

        ResponseEntity<List> r = restTemplate.exchange(
                "/campaigns", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getCampaignById_success() {
        Long id = createCampaign("GetCamp", "Sales");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/campaigns/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("name")).isEqualTo("GetCamp");
    }

    @Test
    void getCampaignById_notFound_returns400() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/campaigns/99999", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getPostsByCampaign_returnsPosts() {
        Long id = createCampaign("PostCamp", "Content");

        Post p = Post.builder()
                .title("Campaign Post")
                .content("Test")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.DRAFT)
                .campaign(campaignRepository.findById(id).orElseThrow())
                .build();
        postRepository.save(p);

        ResponseEntity<List> r = restTemplate.exchange(
                "/campaigns/" + id + "/posts", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).hasSize(1);
    }

    @Test
    void deleteCampaign_success() {
        Long id = createCampaign("DelCamp", "Delete");

        HttpHeaders h = authHeaders();
        ResponseEntity<String> delR = restTemplate.exchange(
                "/campaigns/" + id, HttpMethod.DELETE,
                new HttpEntity<>(h), String.class);

        assertThat(delR.getStatusCode()).isEqualTo(HttpStatus.OK);
        testCampaignId = null;
    }

    @Test
    void getRecentCampaigns_success() {
        createCampaign("RecCamp", "Recent");

        ResponseEntity<List> r = restTemplate.exchange(
                "/campaigns/recent?limit=5", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void unauthorizedAccess_returns401() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/campaigns", HttpMethod.GET, null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
