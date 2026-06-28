package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Repositories.ConversationRepository;
import com.example.metatry.Repositories.UserRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.MemoryContextService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, ChatIntegrationTest.MockConfig.class})
class ChatIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        GeminiService geminiService() {
            return Mockito.mock(GeminiService.class);
        }

        @Bean
        @Primary
        MemoryContextService memoryContextService() {
            return Mockito.mock(MemoryContextService.class);
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private MemoryContextService memoryContextService;

    private String token;
    private final String testName = "chatint_" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setUp() {
        when(geminiService.generate(anyString())).thenReturn("Mock AI response");
        when(memoryContextService.getRecentContext()).thenReturn("Mock memory context");

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
        conversationRepository.findAll().forEach(c -> conversationRepository.delete(c));
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

    private Long createConversation(String title) {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/chat/conversations", HttpMethod.POST,
                new HttpEntity<>(Map.of("title", title), jsonAuthHeaders()),
                Map.class);
        return ((Number) r.getBody().get("id")).longValue();
    }

    @Test
    void createConversation_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/chat/conversations", HttpMethod.POST,
                new HttpEntity<>(Map.of("title", "Test Chat"), jsonAuthHeaders()),
                Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "title", "createdAt");
        assertThat(r.getBody().get("title")).isEqualTo("Test Chat");

        Long id = ((Number) r.getBody().get("id")).longValue();
        conversationRepository.deleteById(id);
    }

    @Test
    void getAllConversations_returnsList() {
        Long id = createConversation("List Chat");

        ResponseEntity<List> r = restTemplate.exchange(
                "/chat/conversations", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();

        conversationRepository.deleteById(id);
    }

    @Test
    void getConversationById_success() {
        Long id = createConversation("Get Chat");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/chat/conversations/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("title")).isEqualTo("Get Chat");

        conversationRepository.deleteById(id);
    }

    @Test
    void getConversationById_notFound_returns400() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/chat/conversations/99999", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteConversation_success() {
        Long id = createConversation("Del Chat");

        ResponseEntity<Void> r = restTemplate.exchange(
                "/chat/conversations/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), Void.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(conversationRepository.findById(id)).isEmpty();
    }

    @Test
    void getMessages_emptyList() {
        Long id = createConversation("Msg Chat");

        ResponseEntity<List> r = restTemplate.exchange(
                "/chat/conversations/" + id + "/messages", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isEmpty();

        conversationRepository.deleteById(id);
    }

    @Test
    void sendMessage_success() {
        Long id = createConversation("Send Chat");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/chat/conversations/" + id + "/messages", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", "Hello"), jsonAuthHeaders()),
                Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "role", "content", "timestamp");
        assertThat(r.getBody().get("role")).isEqualTo("AI");

        conversationRepository.deleteById(id);
    }

    @Test
    void generateConclusion_success() {
        Long id = createConversation("Conclude Chat");

        ResponseEntity<String> r = restTemplate.exchange(
                "/chat/conversations/" + id + "/conclusion", HttpMethod.POST,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isEqualTo("Mock AI response");

        conversationRepository.deleteById(id);
    }

    @Test
    void createConversation_defaultTitle() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/chat/conversations", HttpMethod.POST,
                new HttpEntity<>(Map.of(), jsonAuthHeaders()),
                Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("title")).isEqualTo("New Chat");

        Long id = ((Number) r.getBody().get("id")).longValue();
        conversationRepository.deleteById(id);
    }

    @Test
    void unauthorizedAccess_returns403() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/chat/conversations", HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
