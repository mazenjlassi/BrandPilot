package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CalendarEventDTO;
import com.example.metatry.DTOs.CreatePostRequest;
import com.example.metatry.DTOs.TimingAnalysisDTO;
import com.example.metatry.DTOs.WeeklyComparisonDTO;
import com.example.metatry.DTOs.PostDto;
import com.example.metatry.DTOs.PostStatsResponse;
import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.AiImageService;
import com.example.metatry.Services.CloudinaryService;
import com.example.metatry.Services.AiContentService;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.PostService;
import com.example.metatry.Services.PostTimingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;
    private final AiImageService aiImageService;
    private final PostImageRepository postImageRepository;
    private final PostTimingService postTimingService;
    private final CloudinaryService cloudinaryService;
    private final GeminiService geminiService;
    private final AiContentService aiContentService;

    // ================= BASIC =================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Post> getAllPosts(){
        return postService.getAllPosts();
    }


    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/drafts")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getDrafts() {
        return postService.getDraftPosts();
    }

    @GetMapping("/scheduled")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getScheduled() {
        return postService.getAllScheduledPosts();
    }

    @GetMapping("/published")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getPublished() {
        return postService.getPublishedPosts();
    }

    @GetMapping("/approved")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getApprovedPosts(){
        return postService.getApprovedPosts();
    }

    @GetMapping("/platform/{platform}")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getPostsByPlatform(@PathVariable PlatformType platform){
        return postService.getPostsByPlatform(platform);
    }

    // ================= CAMPAIGN =================

    @GetMapping("/campaign/{id}")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getByCampaign(@PathVariable Long id) {
        return postService.getPostsByCampaign(id);
    }

    @GetMapping("/campaign/{id}/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getCampaignPostsByStatus(
            @PathVariable Long id,
            @PathVariable PostStatus status) {

        return postService.getCampaignPostsByStatus(id, status);
    }

    // ================= UPDATE =================

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request){

        postService.updatePost(id, request);

        return ResponseEntity.ok(Map.of("message", "Post updated"));
    }

    // ================= DELETE =================

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable Long id){

        postService.deletePost(id);

        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    @DeleteMapping("/draft/images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteDraftImages() {
        int count = postService.deleteImagesForDraftPosts();
        return ResponseEntity.ok(Map.of(
                "message", count + " image(s) deleted for draft posts",
                "deletedCount", count
        ));
    }

    // ================= CREATE MANUALLY =================
    @PostMapping(value = "/campaigns/{campaignId}/posts", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public Post createPost(
            @PathVariable Long campaignId,
            @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        return postService.createPostForCampaign(campaignId, request, image, video);
    }
    // ================= STATS =================



    @GetMapping("/stats")
    public PostStatsResponse getStats(){
        return postService.getStats();
    }

    // ================= AI IMAGE =================

    @PostMapping("/{id}/generate-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> generateImage(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ){
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (body != null) {
            String customPrompt = body.get("prompt");
            if (customPrompt != null && !customPrompt.isBlank()) {
                PostImage existing = post.getImage();
                if (existing != null) {
                    existing.setImagePrompt(customPrompt);
                    postImageRepository.save(existing);
                }
            }
        }

        try {
            PostImage image = aiImageService.generateImageForPost(post);
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image generation failed: " + e.getMessage()));
        }
    }

    // ================= GENERIC UPLOAD =================

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file
    ) throws java.io.IOException {
        String contentType = file.getContentType();
        String url;
        if (contentType != null && contentType.startsWith("video/")) {
            url = cloudinaryService.uploadVideo(file);
        } else {
            url = cloudinaryService.uploadImage(file);
        }
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ================= CLEANUP =================

    @DeleteMapping("/cleanup-images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cleanDuplicateImages(){

        postService.cleanDuplicateImages();

        return ResponseEntity.ok(Map.of("message", "Duplicate images removed"));
    }

    // ================= DASHBOARD =================

    @GetMapping("/latestPublished")
    public List<Post> getLatestPublished(
            @RequestParam(defaultValue = "15") int limit
    ){
        return postService.getLatestPublishedPosts(limit);
    }

    @GetMapping("/top")
    public List<Post> getTopPosts(
            @RequestParam(defaultValue = "5") int limit
    ){
        return postService.getTopPosts(limit);
    }

    @GetMapping("/permanent")
    public ResponseEntity<List<Post>> getPermanentPosts() {
        List<Post> posts = postService.getPermanentPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/calendar")
    public List<CalendarEventDTO> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end
    ) {
        LocalDateTime startLocal = start.toLocalDateTime();
        LocalDateTime endLocal = end.toLocalDateTime();
        return postService.getCalendarEvents(startLocal, endLocal);
    }

    @GetMapping("/timing-analysis")
    public TimingAnalysisDTO getTimingAnalysis() {
        return postTimingService.analyzeBestPostingTimes();
    }

    @GetMapping("/weekly-comparison")
    public WeeklyComparisonDTO getWeeklyComparison() {
        return postService.getWeeklyComparison();
    }

    @GetMapping("/upcoming-scheduled")
    public List<Post> getUpcomingScheduled(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return postService.getUpcomingScheduledPosts(limit);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> approvePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setApproved(true);
        if (post.getScheduledAt() != null) {
            post.setStatus(PostStatus.SCHEDULED);
        }
        postRepository.save(post);

        if (post.getNeedsImage() != null && post.getNeedsImage()) {
            try {
                aiImageService.generateImageForPost(post);
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("message", "Post approved but image generation failed: " + e.getMessage()));
            }
        }

        return ResponseEntity.ok(Map.of("message", "Post approved"));
    }

    @PostMapping("/approve-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> approveAllPosts() {
        List<Post> unapproved = postRepository.findByApprovedTrue();
        List<Post> allPosts = postRepository.findAll();
        int count = 0;
        for (Post post : allPosts) {
            if (!Boolean.TRUE.equals(post.getApproved())) {
                post.setApproved(true);
                if (post.getScheduledAt() != null) {
                    post.setStatus(PostStatus.SCHEDULED);
                }
                postRepository.save(post);
                count++;
            }
        }
        return ResponseEntity.ok(Map.of("message", count + " posts approved"));
    }

    @PostMapping("/{id}/regenerate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> regeneratePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String prompt = "Rewrite this social media post for " + post.getPlatform()
                + " keep the same topic but improve the content:\n\n"
                + "Title: " + post.getTitle() + "\n"
                + "Content: " + post.getContent();

        String newContent = geminiService.generate(prompt);

        post.setContent(newContent);
        postRepository.save(post);

        return ResponseEntity.ok(Map.of("message", "Post regenerated", "content", newContent));
    }
}