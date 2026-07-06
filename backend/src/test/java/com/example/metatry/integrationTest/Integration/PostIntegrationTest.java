package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.DTOs.UpdatePostRequest;
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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class PostIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PostRepository postRepository;

    private String token;
    private final String testName = "pstint_" + UUID.randomUUID().toString().substring(0, 8);
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

        var campaignReq = new CreateCampaignRequest();
        campaignReq.setName("PostInt Campaign");
        campaignReq.setTopic("Integration");

        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Campaign> campResp = restTemplate.exchange(
                "/campaigns", HttpMethod.POST,
                new HttpEntity<>(campaignReq, h),
                Campaign.class);
        campaignId = campResp.getBody().getId();
    }

    @AfterEach
    void tearDown() {
        if (campaignId != null) {
            HttpHeaders h = new HttpHeaders();
            h.set("Authorization", token);
            restTemplate.exchange("/campaigns/" + campaignId, HttpMethod.DELETE,
                    new HttpEntity<>(h), String.class);
        }
        userRepository.findByName(testName).ifPresent(u -> userRepository.delete(u));
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return h;
    }

    private Post createPost(String title, PlatformType platform, PostStatus status) {
        Campaign c = campaignRepository.findById(campaignId).orElseThrow();
        Post p = Post.builder()
                .title(title)
                .content("Content: " + title)
                .hashtags("#test")
                .platform(platform)
                .status(status)
                .campaign(c)
                .build();
        return postRepository.save(p);
    }

    @Test
    void getAllPosts_returnsOk() {
        createPost("GP1", PlatformType.FACEBOOK, PostStatus.DRAFT);

        ResponseEntity<List> r = restTemplate.exchange(
                "/posts", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getPostById_returnsPostDto() {
        Post saved = createPost("BPID", PlatformType.LINKEDIN, PostStatus.DRAFT);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/posts/" + saved.getId(), HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "title", "platform", "status");
        assertThat(r.getBody().get("title")).isEqualTo("BPID");
    }

    @Test
    void getPostById_notFound_returns400() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/posts/99999", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getDrafts_returnsDraftPosts() {
        createPost("Draf1", PlatformType.FACEBOOK, PostStatus.DRAFT);

        ResponseEntity<List> r = restTemplate.exchange(
                "/posts/drafts", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getPublished_returnsPublishedPosts() {
        createPost("Pub1", PlatformType.INSTAGRAM, PostStatus.PUBLISHED);

        ResponseEntity<List> r = restTemplate.exchange(
                "/posts/published", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getScheduled_returnsScheduledPosts() {
        Campaign c = campaignRepository.findById(campaignId).orElseThrow();
        Post p = Post.builder()
                .title("Sch1")
                .content("Scheduled content")
                .hashtags("#test")
                .platform(PlatformType.LINKEDIN)
                .status(PostStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now().plusDays(7))
                .campaign(c)
                .build();
        postRepository.save(p);

        ResponseEntity<List> r = restTemplate.exchange(
                "/posts/scheduled", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getPostsByPlatform_returnsFiltered() {
        createPost("FB1", PlatformType.FACEBOOK, PostStatus.DRAFT);
        createPost("IG1", PlatformType.INSTAGRAM, PostStatus.DRAFT);

        ResponseEntity<List> r = restTemplate.exchange(
                "/posts/platform/FACEBOOK", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).hasSize(1);
    }

    @Test
    void getStats_returnsStats() {
        createPost("Stat1", PlatformType.FACEBOOK, PostStatus.DRAFT);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/posts/stats", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("totalPosts", "draftPosts", "publishedPosts");
    }

    @Test
    void getPostsByCampaign_returnsPosts() {
        createPost("Camp1", PlatformType.FACEBOOK, PostStatus.DRAFT);

        ResponseEntity<List> r = restTemplate.exchange(
                "/posts/campaign/" + campaignId, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void updatePost_updatesFields() {
        Post saved = createPost("Upd1", PlatformType.FACEBOOK, PostStatus.DRAFT);

        UpdatePostRequest update = new UpdatePostRequest();
        update.setTitle("Updated Title");
        update.setContent("Updated content");
        update.setPlatform(PlatformType.INSTAGRAM);

        HttpHeaders h = authHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/posts/" + saved.getId(), HttpMethod.PUT,
                new HttpEntity<>(update, h), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsEntry("message", "Post updated");

        ResponseEntity<Map> getR = restTemplate.exchange(
                "/posts/" + saved.getId(), HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);
        assertThat(getR.getBody().get("title")).isEqualTo("Updated Title");
    }

    @Test
    void deletePost_removesPost() {
        Post saved = createPost("Del1", PlatformType.FACEBOOK, PostStatus.DRAFT);

        ResponseEntity<Map> delR = restTemplate.exchange(
                "/posts/" + saved.getId(), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(delR.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(delR.getBody()).containsEntry("message", "Post deleted successfully");

        ResponseEntity<String> getR = restTemplate.exchange(
                "/posts/" + saved.getId(), HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);
        assertThat(getR.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unauthorizedAccess_returns401() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/posts", HttpMethod.GET, null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getCalendar_returnsEvents() {
        createPost("Cal1", PlatformType.FACEBOOK, PostStatus.SCHEDULED);

        ZonedDateTime now = ZonedDateTime.now();
        String start = now.minusDays(1).toString();
        String end = now.plusDays(30).toString();

        ResponseEntity<List> r = restTemplate.exchange(
                "/posts/calendar?start={start}&end={end}",
                HttpMethod.GET, new HttpEntity<>(authHeaders()),
                List.class, start, end);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
