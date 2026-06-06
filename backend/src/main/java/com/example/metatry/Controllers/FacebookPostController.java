package com.example.metatry.Controllers;

import com.example.metatry.Services.FacebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/facebook")
@RequiredArgsConstructor
public class FacebookPostController {

    private final FacebookService facebookService;

    @PostMapping("/post/text")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> postText(@RequestBody Map<String, String> request) {

        String message = request.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        return ResponseEntity.ok(facebookService.postText(message));
    }

    @PostMapping("/post/url")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> postPhotoUrl(@RequestBody Map<String, String> request) {

        String imageUrl = request.get("imageUrl");
        String caption = request.get("caption");

        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Image URL required"));
        }

        return ResponseEntity.ok(
                facebookService.postPhotoFromUrl(imageUrl, caption)
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

        return ResponseEntity.ok(
                facebookService.postLocalPhoto(file, caption)
        );
    }
}