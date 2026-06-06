package com.example.metatry.Services;

import com.example.metatry.Config.LinkedInConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LinkedInTokenService {

    private final RestTemplate restTemplate;
    private final LinkedInConfig.LinkedInAuthProperties authProps;

    private String accessToken;
    private String personUrn;
    private long tokenExpiryTime;

    public LinkedInTokenService(RestTemplate restTemplate, LinkedInConfig.LinkedInAuthProperties authProps) {
        this.restTemplate = restTemplate;
        this.authProps = authProps;
    }

    /**
     * Échange un code d'autorisation contre un token d'accès
     */
    public void exchangeAuthorizationCode(String code) {
        String url = "https://www.linkedin.com/oauth/v2/accessToken";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", authProps.redirectUri());
        body.add("client_id", authProps.clientId());
        body.add("client_secret", authProps.clientSecret());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        this.accessToken = (String) response.getBody().get("access_token");
        this.tokenExpiryTime = System.currentTimeMillis() + ((Number) response.getBody().get("expires_in")).longValue() * 1000;

        // Récupérer le Person URN
        fetchPersonUrn();
    }

    /**
     * Récupère le Person URN du membre connecté
     */
    private void fetchPersonUrn() {
        String url = "https://api.linkedin.com/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            // ✅ Construire l'URN correctement
            String sub = (String) response.getBody().get("sub");
            this.personUrn = "urn:li:person:" + sub;

            System.out.println("✅ Person URN récupéré: " + this.personUrn);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du Person URN: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération du profil", e);
        }
    }

    /**
     * Retourne le token actuel
     */
    public String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() > tokenExpiryTime - 300000) {
            throw new RuntimeException("Token LinkedIn expiré ou non disponible. Veuillez réautoriser.");
        }
        return accessToken;
    }

    /**
     * Retourne le Person URN
     */
    public String getPersonUrn() {
        return personUrn;
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    public boolean isAuthenticated() {
        return accessToken != null && System.currentTimeMillis() < tokenExpiryTime;
    }

    /**
     * Génère l'URL d'autorisation pour le frontend
     */
    public String getAuthorizationUrl() {
        return "https://www.linkedin.com/oauth/v2/authorization?" +
                "response_type=code&" +
                "client_id=" + authProps.clientId() + "&" +
                "redirect_uri=" + authProps.redirectUri() + "&" +
                "scope=" + "openid%20profile%20w_member_social";
    }
}