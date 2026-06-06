package com.example.metatry.Controllers;

import com.example.metatry.Services.CloudinaryService;
import com.example.metatry.Services.LinkedInService;
import com.example.metatry.Services.LinkedInTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/linkedin")
public class LinkedInController {

    private final LinkedInService linkedInService;
    private final LinkedInTokenService tokenService;
    private final CloudinaryService cloudinaryService;

    public LinkedInController(LinkedInService linkedInService, LinkedInTokenService tokenService, CloudinaryService cloudinaryService) {
        this.linkedInService = linkedInService;
        this.tokenService = tokenService;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * ✅ Obtenir l'URL d'authentification
     */
    @GetMapping("/auth-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAuthUrl() {
        return ResponseEntity.ok(Map.of(
                "authUrl", tokenService.getAuthorizationUrl()
        ));
    }

    /**
     * ✅ Callback OAuth (public)
     */
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        System.out.println("=== CALLBACK LINKEDIN REÇU ===");
        System.out.println("Code: " + code);

        try {
            tokenService.exchangeAuthorizationCode(code);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Authentification LinkedIn réussie"
            ));
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Post texte simple
     */
    @PostMapping("/post/text")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postText(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le texte est requis"));
        }

        Map<String, Object> result = linkedInService.postText(text);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * ✅ Poster une image depuis un fichier local (via Cloudinary)
     */
    @PostMapping(value = "/post/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @RequestParam(value = "title", required = false) String title) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le fichier est vide"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une image"));
            }

            // Upload vers Cloudinary
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "metatry/linkedin/images");
            options.put("resource_type", "image");

            Map<String, Object> uploadResult = cloudinaryService.uploadWithOptions(file, options);
            String imageUrl = (String) uploadResult.get("secure_url");

            System.out.println("✅ Image uploadée vers Cloudinary: " + imageUrl);

            // Poster sur LinkedIn
            String imageTitle = title != null ? title : file.getOriginalFilename();
            Map<String, Object> result = linkedInService.postArticleWithImage(text, imageUrl, imageTitle);

            if ((boolean) result.get("success")) {
                result.put("cloudinaryUrl", imageUrl);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Poster une image via URL (déjà sur Cloudinary)
     */
    @PostMapping("/post/image/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postImageFromUrl(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("imageUrl");
        String text = request.get("text");
        String title = request.getOrDefault("title", "Image");

        if (imageUrl == null || text == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "imageUrl et text sont requis"));
        }

        Map<String, Object> result = linkedInService.postArticleWithImage(text, imageUrl, title);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * ✅ Poster une vidéo depuis un fichier local (via Cloudinary)
     */
    @PostMapping(value = "/post/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @RequestParam(value = "title", required = false) String title) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le fichier est vide"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une vidéo"));
            }

            if (file.getSize() > 100 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "La vidéo ne doit pas dépasser 100MB"));
            }

            // Upload vers Cloudinary
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "metatry/linkedin/videos");
            options.put("resource_type", "video");

            Map<String, Object> uploadResult = cloudinaryService.uploadWithOptions(file, options);
            String videoUrl = (String) uploadResult.get("secure_url");

            System.out.println("✅ Vidéo uploadée vers Cloudinary: " + videoUrl);

            // Poster sur LinkedIn
            String videoTitle = title != null ? title : file.getOriginalFilename();
            Map<String, Object> result = linkedInService.postArticleWithVideo(text, videoUrl, videoTitle);

            if ((boolean) result.get("success")) {
                result.put("cloudinaryUrl", videoUrl);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Poster une vidéo via URL (déjà sur Cloudinary)
     */
    @PostMapping("/post/video/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postVideoFromUrl(@RequestBody Map<String, String> request) {
        String videoUrl = request.get("videoUrl");
        String text = request.get("text");
        String title = request.getOrDefault("title", "Vidéo");

        if (videoUrl == null || text == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "videoUrl et text sont requis"));
        }

        Map<String, Object> result = linkedInService.postArticleWithVideo(text, videoUrl, title);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * ✅ Récupérer le profil
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(linkedInService.getUserProfile());
    }

    /**
     * ✅ Vérifier le statut d'authentification
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "authenticated", tokenService.isAuthenticated(),
                "personUrn", tokenService.getPersonUrn()
        ));
    }
}