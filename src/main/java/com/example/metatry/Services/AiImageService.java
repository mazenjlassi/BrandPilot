package com.example.metatry.Services;

import com.example.metatry.Config.CloudflareConfig;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class AiImageService {

    private final CloudinaryService cloudinaryService;
    private final CloudflareConfig cloudflareConfig;
    private final PostImageRepository postImageRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final double GUIDANCE = 8.5;
    private static final int NUM_STEPS = 20;
    private static final int MAX_PROMPT_LENGTH = 500;

    private static final String NEGATIVE_PROMPT =
            "blurry, low quality, distorted, ugly, bad anatomy, watermark, text, " +
            "extra fingers, cropped, worst quality, low resolution, jpeg artifacts, " +
            "signature, username, logo, deformed, bad proportions, unnatural, " +
            "disfigured face, bad face, ugly face, missing fingers, extra digit, " +
            "bad hands, mutated hands, cloned face, morbid";

    // ================= CLOUDFARE + CLOUDINARY =================

    public String generateAndUploadImage(String prompt, ImageSize size) {

        try {

            if (prompt.length() > MAX_PROMPT_LENGTH) {
                prompt = prompt.substring(0, MAX_PROMPT_LENGTH - 3) + "...";
            }

            String url =
                    "https://api.cloudflare.com/client/v4/accounts/"
                            + cloudflareConfig.getAccountId()
                            + "/ai/run/@cf/stabilityai/stable-diffusion-xl-base-1.0";

            int[] dimensions = getDimensions(size);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cloudflareConfig.getApiToken());
            headers.setAccept(List.of(MediaType.IMAGE_PNG, MediaType.APPLICATION_OCTET_STREAM));

            Map<String, Object> body = new HashMap<>();
            body.put("prompt", prompt);
            body.put("negative_prompt", NEGATIVE_PROMPT);
            body.put("width", dimensions[0]);
            body.put("height", dimensions[1]);
            body.put("guidance", GUIDANCE);
            body.put("num_steps", NUM_STEPS);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    byte[].class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Cloudflare AI returned status: " + response.getStatusCode()
                );
            }

            byte[] imageBytes = response.getBody();

            if (imageBytes == null || imageBytes.length == 0) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "AI returned empty image"
                );
            }

            String imageUrl = cloudinaryService.uploadImageBytes(imageBytes);

            if (imageUrl == null || imageUrl.isBlank()) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Cloudinary upload failed"
                );
            }

            return imageUrl;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Error generating AI image: " + e.getMessage()
            );
        }
    }

    private int[] getDimensions(ImageSize size) {
        return switch (size) {
            case SQUARE -> new int[]{1024, 1024};
            case LANDSCAPE -> new int[]{1216, 832};
            case PORTRAIT -> new int[]{832, 1216};
        };
    }

    // ================= MAIN LOGIC =================

    public PostImage generateImageForPost(Post post){

        if (post == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Post not found");
        }

        PostImage image = post.getImage();
        ImageSize size = imageSizeForPlatform(post.getPlatform());

        // ✅ CASE 1: No image exists → create one
        if (image == null) {
            image = createImage(post, size);
            image.setSelected(true);
            return postImageRepository.save(image);
        }

        String prompt = image.getImagePrompt();
        ImageSize existingSize = image.getSize() != null ? image.getSize() : size;

        // ✅ CASE 2: Prompt missing → build one
        if (prompt == null || prompt.isBlank()) {
            prompt = buildPrompt(post, existingSize);
            image.setImagePrompt(prompt);
        }

        // ✅ Generate AI image
        String imageUrl = generateAndUploadImage(prompt, existingSize);

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "AI image generation failed"
            );
        }

        image.setImageUrl(imageUrl);
        image.setSelected(true);

        return postImageRepository.save(image);
    }

    private ImageSize imageSizeForPlatform(PlatformType platform) {
        if (platform == null) return ImageSize.SQUARE;
        return switch (platform) {
            case INSTAGRAM -> ImageSize.SQUARE;
            case LINKEDIN, FACEBOOK -> ImageSize.LANDSCAPE;
        };
    }

    // ================= CREATE IMAGE =================

    private PostImage createImage(Post post, ImageSize size){

        String prompt = buildPrompt(post, size);

        String imageUrl = generateAndUploadImage(prompt, size);

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Failed to generate image"
            );
        }

        return PostImage.builder()
                .imageUrl(imageUrl)
                .imagePrompt(prompt)
                .size(size)
                .post(post)
                .selected(false)
                .build();
    }

    // ================= PROMPT BUILDER =================

    private String buildPrompt(Post post, ImageSize size){

        if (post.getImage() != null &&
                post.getImage().getImagePrompt() != null &&
                !post.getImage().getImagePrompt().isBlank()) {

            return post.getImage().getImagePrompt();
        }

        String styleTag = switch (size){
            case SQUARE -> "square 1:1 instagram";
            case LANDSCAPE -> "landscape 16:9 linkedin facebook";
            case PORTRAIT -> "portrait 9:16 vertical";
        };

        String title = post.getTitle() != null
                ? post.getTitle().replaceAll("[^a-zA-Z0-9 ]", " ").trim()
                : "";

        String[] titleWords = title.split("\\s+");
        StringBuilder keywords = new StringBuilder();
        for (int i = 0; i < Math.min(titleWords.length, 8); i++) {
            String w = titleWords[i].toLowerCase();
            if (w.length() > 2) {
                if (keywords.length() > 0) keywords.append(" ");
                keywords.append(w);
            }
        }

        String result = keywords.length() > 0
                ? keywords.toString().trim() + " " + styleTag
                : styleTag;

        return result + " professional business technology cinematic lighting photorealistic 4k clean minimalist";
    }
}