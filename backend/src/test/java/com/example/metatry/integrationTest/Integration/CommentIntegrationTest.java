package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.*;
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
class CommentIntegrationTest {

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

    private String token;
    private final String testName = "comint_" + UUID.randomUUID().toString().substring(0, 8);
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
                Campaign.builder().name("CommentCamp").topic("Test").build());
        campaignId = c.getId();
        Post p = postRepository.save(Post.builder()
                .title("Comment Post")
                .content("Test")
                .platform(PlatformType.FACEBOOK)
                .status(PostStatus.DRAFT)
                .campaign(c)
                .build());
        postId = p.getId();
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
    void getCommentsByPost_returnsList() {
        postCommentRepository.save(PostComment.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .commentText("Great post!")
                .sentiment("POSITIVE")
                .authorName("User1")
                .build());

        ResponseEntity<List> r = restTemplate.exchange(
                "/comments/post/" + postId, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).hasSize(1);
    }

    @Test
    void getCommentsByPost_empty_returnsEmptyList() {
        ResponseEntity<List> r = restTemplate.exchange(
                "/comments/post/" + postId, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isEmpty();
    }

    @Test
    void getCommentsByCampaign_returnsList() {
        Post p2 = postRepository.save(Post.builder()
                .title("Campaign Post 2")
                .content("Second")
                .platform(PlatformType.INSTAGRAM)
                .status(PostStatus.DRAFT)
                .campaign(campaignRepository.findById(campaignId).orElseThrow())
                .build());

        postCommentRepository.save(PostComment.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .commentText("First comment")
                .sentiment("POSITIVE")
                .authorName("User1")
                .build());
        postCommentRepository.save(PostComment.builder()
                .post(p2)
                .commentText("Second comment")
                .sentiment("NEGATIVE")
                .authorName("User2")
                .build());

        ResponseEntity<List> r = restTemplate.exchange(
                "/comments/campaign/" + campaignId, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).hasSize(2);

        postCommentRepository.deleteById(
                postCommentRepository.findByPostId(p2.getId()).get(0).getId());
        postRepository.delete(p2);
    }

    @Test
    void getCommentsByPostAndSentiment_returnsFiltered() {
        postCommentRepository.save(PostComment.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .commentText("Love it!")
                .sentiment("POSITIVE")
                .authorName("User1")
                .build());
        postCommentRepository.save(PostComment.builder()
                .post(postRepository.findById(postId).orElseThrow())
                .commentText("Terrible!")
                .sentiment("NEGATIVE")
                .authorName("User2")
                .build());

        ResponseEntity<List> r = restTemplate.exchange(
                "/comments/post/" + postId + "/sentiment/POSITIVE", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).hasSize(1);
        Map first = (Map) ((List) r.getBody()).get(0);
        assertThat(first.get("sentiment")).isEqualTo("POSITIVE");
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/comments/post/" + postId, HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
