package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.*;
import com.example.metatry.Services.AiInsightService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, InsightIntegrationTest.MockConfig.class})
class InsightIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        AiInsightService aiInsightService() {
            return Mockito.mock(AiInsightService.class);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private AiInsightService aiInsightService;

    private String token;
    private final String testName = "insint_" + UUID.randomUUID().toString().substring(0, 8);
    private Long campaignId;
    private Long postId;

    @BeforeEach
    void setUp() {
        when(aiInsightService.analyzeComments(anyList()))
                .thenReturn("{\"overallSentiment\":\"POSITIVE\",\"topPositives\":[\"Great content\"],\"topComplaints\":[],\"summary\":\"Good engagement\",\"advice\":\"Keep posting\",\"ideas\":[\"More video\"]}");

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
                Campaign.builder().name("InsightCamp").topic("AI").build());
        campaignId = c.getId();
        Post p = postRepository.save(Post.builder()
                .title("Insight Post")
                .content("Test")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED)
                .campaign(c)
                .build());
        postId = p.getId();

        postCommentRepository.save(PostComment.builder()
                .post(p)
                .commentText("Excellent work!")
                .sentiment("POSITIVE")
                .authorName("User1")
                .build());
        postCommentRepository.save(PostComment.builder()
                .post(p)
                .commentText("Could be better")
                .sentiment("NEUTRAL")
                .authorName("User2")
                .build());
    }

    @AfterEach
    void tearDown() {
        postCommentRepository.findByPostId(postId)
                .forEach(pc -> postCommentRepository.delete(pc));
        if (postId != null) {
            postRepository.findById(postId).ifPresent(p -> postRepository.delete(p));
        }
        if (campaignId != null) {
            campaignRepository.deleteById(campaignId);
        }
        userRepository.findByName(testName).ifPresent(u -> userRepository.delete(u));
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return h;
    }

    @Test
    void getCampaignInsights_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/insights/campaign/" + campaignId, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("overallSentiment", "positiveRatio",
                "negativeRatio", "neutralRatio", "summary", "advice");
        assertThat(r.getBody().get("overallSentiment")).isEqualTo("POSITIVE");
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/insights/campaign/" + campaignId, HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
