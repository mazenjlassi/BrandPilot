package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AuthRequest;
import com.example.metatry.DTOs.RegisterRequest;
import com.example.metatry.Models.CompanyProfile;
import com.example.metatry.Repositories.CompanyProfileRepository;
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
class CompanyProfileIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyProfileRepository companyProfileRepository;

    private String token;
    private final String testName = "cpi_" + UUID.randomUUID().toString().substring(0, 8);
    private final String testCompany = "IntTest_" + UUID.randomUUID().toString().substring(0, 8);
    private Long createdProfileId;

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
        if (createdProfileId != null) {
            companyProfileRepository.deleteById(createdProfileId);
        }
        companyProfileRepository.findByCompanyName(testCompany).ifPresent(
                p -> companyProfileRepository.delete(p));
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
    void createCompanyProfile_success() {
        var profile = new CompanyProfile();
        profile.setCompanyName(testCompany);
        profile.setInstagramUrl("https://instagram.com/" + testCompany);
        profile.setFacebookUrl("https://facebook.com/" + testCompany);
        profile.setLinkedinUrl("https://linkedin.com/" + testCompany);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/company-profiles", HttpMethod.POST,
                new HttpEntity<>(profile, jsonAuthHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).containsKeys("id", "companyName", "instagramUrl");
        assertThat(r.getBody().get("companyName")).isEqualTo(testCompany);

        createdProfileId = ((Number) r.getBody().get("id")).longValue();
    }

    @Test
    void getAllCompanyProfiles_returnsList() {
        var profile = new CompanyProfile();
        profile.setCompanyName(testCompany);
        profile.setInstagramUrl("https://instagram.com/" + testCompany);
        profile.setFacebookUrl("https://facebook.com/" + testCompany);
        profile.setLinkedinUrl("https://linkedin.com/" + testCompany);
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/company-profiles", HttpMethod.POST,
                new HttpEntity<>(profile, jsonAuthHeaders()), Map.class);
        createdProfileId = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<List> r = restTemplate.exchange(
                "/api/company-profiles", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), List.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).isNotEmpty();
    }

    @Test
    void getCompanyProfileByName_success() {
        var profile = new CompanyProfile();
        profile.setCompanyName(testCompany);
        profile.setInstagramUrl("https://instagram.com/" + testCompany);
        profile.setFacebookUrl("https://facebook.com/" + testCompany);
        profile.setLinkedinUrl("https://linkedin.com/" + testCompany);
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/company-profiles", HttpMethod.POST,
                new HttpEntity<>(profile, jsonAuthHeaders()), Map.class);
        createdProfileId = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/company-profiles/by-name/" + testCompany, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("companyName")).isEqualTo(testCompany);
    }

    @Test
    void getCompanyProfileByName_notFound_returns404() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/company-profiles/by-name/nonexistent_" + UUID.randomUUID(),
                HttpMethod.GET, new HttpEntity<>(authHeaders()), String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateCompanyProfile_success() {
        var profile = new CompanyProfile();
        profile.setCompanyName(testCompany);
        profile.setInstagramUrl("https://instagram.com/" + testCompany);
        profile.setFacebookUrl("https://facebook.com/" + testCompany);
        profile.setLinkedinUrl("https://linkedin.com/" + testCompany);
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/company-profiles", HttpMethod.POST,
                new HttpEntity<>(profile, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();
        createdProfileId = id;

        var updated = new CompanyProfile();
        updated.setCompanyName(testCompany);
        updated.setInstagramUrl("https://instagram.com/updated_" + testCompany);
        updated.setFacebookUrl("https://facebook.com/" + testCompany);
        updated.setLinkedinUrl("https://linkedin.com/" + testCompany);

        ResponseEntity<Map> r = restTemplate.exchange(
                "/api/company-profiles/" + id, HttpMethod.PUT,
                new HttpEntity<>(updated, jsonAuthHeaders()), Map.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) ((Map) r.getBody()).get("instagramUrl"))
                .contains("updated_");
    }

    @Test
    void deleteCompanyProfile_success() {
        var profile = new CompanyProfile();
        profile.setCompanyName(testCompany);
        profile.setInstagramUrl("https://instagram.com/" + testCompany);
        profile.setFacebookUrl("https://facebook.com/" + testCompany);
        profile.setLinkedinUrl("https://linkedin.com/" + testCompany);
        ResponseEntity<Map> createR = restTemplate.exchange(
                "/api/company-profiles", HttpMethod.POST,
                new HttpEntity<>(profile, jsonAuthHeaders()), Map.class);
        Long id = ((Number) createR.getBody().get("id")).longValue();

        ResponseEntity<Void> delR = restTemplate.exchange(
                "/api/company-profiles/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), Void.class);

        assertThat(delR.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(companyProfileRepository.findById(id)).isEmpty();
    }

    @Test
    void unauthorizedAccess_returns401() {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/company-profiles", HttpMethod.GET,
                null, String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
