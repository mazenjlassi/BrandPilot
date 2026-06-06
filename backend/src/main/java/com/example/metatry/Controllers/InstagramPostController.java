package com.example.metatry.Controllers;

import com.example.metatry.Services.InstagramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/instagram")
@RequiredArgsConstructor
public class InstagramPostController {

    private final InstagramService instagramService;

    @PostMapping("/post/url")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> postUrl(@RequestBody Map<String, String> request) {

        String imageUrl = request.get("imageUrl");
        String caption = request.get("caption");

        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Image URL required"));
        }

        return ResponseEntity.ok(
                instagramService.postPhotoFromUrl(imageUrl, caption)
        );
    }

    @PostMapping(value = "/post/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> postLocal(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
        }

        if (!file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be image"));
        }

        return ResponseEntity.ok(
                instagramService.postLocalPhoto(file, caption)
        );
    }

    @GetMapping("/test")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "Instagram"
        ));
    }
}