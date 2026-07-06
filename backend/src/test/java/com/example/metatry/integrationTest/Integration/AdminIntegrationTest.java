package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class AdminIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String adminName = "admint_" + UUID.randomUUID().toString().substring(0, 8);
    private final String mktName = "mktint_" + UUID.randomUUID().toString().substring(0, 8);
    private String adminToken;
    private String marketingToken;
    private Long extraUserId;

    @BeforeEach
    void setUp() {
        User admin = new User(adminName, adminName + "@test.com",
                passwordEncoder.encode("admin123"), Role.ADMIN);
        userRepository.save(admin);

        User mkt = new User(mktName, mktName + "@test.com",
                passwordEncoder.encode("mkt123"), Role.MARKETING);
        userRepository.save(mkt);

        var login = new AuthRequest();
        login.setUsername(adminName);
        login.setPassword("admin123");
        ResponseEntity<Map> r = restTemplate.postForEntity("/auth/login", login, Map.class);
        adminToken = "Bearer " + r.getBody().get("token");

        login.setUsername(mktName);
        login.setPassword("mkt123");
        r = restTemplate.postForEntity("/auth/login", login, Map.class);
        marketingToken = "Bearer " + r.getBody().get("token");
    }

    @AfterEach
    void tearDown() {
        if (extraUserId != null) {
            userRepository.findById(extraUserId).ifPresent(u -> userRepository.delete(u));
        }
        userRepository.findByName(mktName).ifPresent(u -> userRepository.delete(u));
        userRepository.findByName(adminName).ifPresent(u -> userRepository.delete(u));
    }

    private HttpHeaders headers(String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return h;
    }

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders h = headers(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void adminGetUsers_success() {
        ResponseEntity<List> r = restTemplate.exchange(
                "/admin/users", HttpMethod.GET,
                new HttpEntity<>(headers(adminToken)), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void adminGetUserById_success() {
        ResponseEntity<List> listR = restTemplate.exchange(
                "/admin/users", HttpMethod.GET,
                new HttpEntity<>(headers(adminToken)), List.class);
        Number firstId = (Number) ((Map) ((List) listR.getBody()).get(0)).get("id");

        ResponseEntity<Map> r = restTemplate.exchange(
                "/admin/users/" + firstId, HttpMethod.GET,
                new HttpEntity<>(headers(adminToken)), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "name", "role");
    }

    @Test
    void adminCreateUser_success() {
        var newUser = Map.of(
                "name", "new_" + adminName,
                "email", "new_" + adminName + "@test.com",
                "password", "pass123",
                "role", "MARKETING"
        );

        ResponseEntity<Map> r = restTemplate.exchange(
                "/admin/users", HttpMethod.POST,
                new HttpEntity<>(newUser, jsonHeaders(adminToken)), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("name")).isEqualTo("new_" + adminName);
        extraUserId = ((Number) r.getBody().get("id")).longValue();
    }

    @Test
    void adminDeleteUser_success() {
        User temp = new User("todelete_" + adminName, "todelete_" + adminName + "@test.com",
                passwordEncoder.encode("pass"), Role.MARKETING);
        userRepository.save(temp);
        extraUserId = temp.getId();

        ResponseEntity<Map> r = restTemplate.exchange(
                "/admin/users/" + extraUserId, HttpMethod.DELETE,
                new HttpEntity<>(headers(adminToken)), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsEntry("message", "User deleted successfully");
        assertThat(userRepository.findById(extraUserId)).isEmpty();
        extraUserId = null;
    }

    @Test
    void adminGetStats_success() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/admin/stats", HttpMethod.GET,
                new HttpEntity<>(headers(adminToken)), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKey("totalUsers");
    }

    @Test
    void marketingUserGetsForbidden() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/admin/users", HttpMethod.GET,
                new HttpEntity<>(headers(marketingToken)), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unauthorizedAccess_returns401() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/admin/users", HttpMethod.GET, null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
