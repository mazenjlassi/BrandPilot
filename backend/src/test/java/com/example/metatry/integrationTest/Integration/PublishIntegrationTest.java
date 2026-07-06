package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.SocialPublisherService;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, PublishIntegrationTest.MockConfig.class})
class PublishIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        SocialPublisherService socialPublisherService() {
            return Mockito.mock(SocialPublisherService.class);
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
    private SocialPublisherService socialPublisherService;

    private String token;
    private final String testName = "pubint_" + UUID.randomUUID().toString().substring(0, 8);
    private Long postId;
    private Long campaignId;

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
                Campaign.builder().name("PublishCamp").topic("Test").build());
        campaignId = c.getId();
        Post p = postRepository.save(Post.builder()
                .title("Publish Test Post")
                .content("About to be published")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.DRAFT)
                .approved(true)
                .campaign(c)
                .build());
        postId = p.getId();
    }

    @AfterEach
    void tearDown() {
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
    void publishPost_success() {
        Post original = postRepository.findById(postId).orElseThrow();
        Post published = Post.builder()
                .id(postId)
                .title(original.getTitle())
                .content(original.getContent())
                .platform(original.getPlatform())
                .status(PostStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .approved(true)
                .campaign(original.getCampaign())
                .build();

        when(socialPublisherService.publishPost(any())).thenReturn(published);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/publish/" + postId, HttpMethod.POST,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("status")).isEqualTo("PUBLISHED");
    }

    @Test
    void publishPost_notFound_returns400() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/publish/99999", HttpMethod.POST,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/publish/" + postId, HttpMethod.POST,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
