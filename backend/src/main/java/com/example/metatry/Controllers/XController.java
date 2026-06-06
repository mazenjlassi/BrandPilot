package com.example.metatry.Controllers;

import com.example.metatry.Services.XService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/x")
public class XController {

    private final XService xService;

    public XController(XService xService) {
        this.xService = xService;
    }

    /**
     * Poster un tweet texte simple
     * POST /api/x/post/text
     */
    @PostMapping("/post/text")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postText(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        // Validation
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Le texte est requis"
            ));
        }

        if (text.length() > 280) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Le tweet ne peut pas dépasser 280 caractères",
                    "length", text.length()
            ));
        }

        // Appel au service
        Map<String, Object> result = xService.postText(text);

        // Retour de la réponse
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Tester la configuration X
     * GET /api/x/test
     */
    @GetMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "X/Twitter Controller est opérationnel",
                "note", "Utilisez POST /api/x/post/text pour tweeter"
        ));
    }

    /**
     * Obtenir les limites de l'API
     * GET /api/x/limits
     */
    @GetMapping("/limits")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getLimits() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "maxTweetLength", 280,
                "rateLimits", "Free tier: ~1500 tweets/mois",
                "authentication", "OAuth 1.0a"
        ));
    }

    /**
     * Documentation des endpoints
     * GET /api/x
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDocs() {
        return ResponseEntity.ok(Map.of(
                "service", "X/Twitter API",
                "baseUrl", "/api/x",
                "endpoints", Map.of(
                        "POST /post/text", "Publier un tweet (JSON: {\"text\": \"...\"})",
                        "GET /test", "Tester le contrôleur",
                        "GET /limits", "Voir les limites",
                        "GET /", "Cette documentation"
                )
        ));
    }

    /**
     * Health check
     * GET /api/x/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "X/Twitter Controller",
                "timestamp", System.currentTimeMillis()
        ));
    }
}