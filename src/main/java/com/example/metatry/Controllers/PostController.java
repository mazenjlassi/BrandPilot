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
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request){

        postService.updatePost(id, request);

        return ResponseEntity.ok(Map.of("message", "Post updated"));
    }

    // ================= DELETE =================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> deletePost(@PathVariable Long id){

        postService.deletePost(id);

        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    // ================= CREATE MANUALLY =================
    @PostMapping(value = "/campaigns/{campaignId}/posts", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public Post createPost(
            @PathVariable Long campaignId,
            @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return postService.createPostForCampaign(campaignId, request, image);
    }
    // ================= STATS =================



    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public PostStatsResponse getStats(){
        return postService.getStats();
    }

    // ================= AI IMAGE =================

    @PostMapping("/{id}/generate-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<PostImage> generateImage(
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

        PostImage image = aiImageService.generateImageForPost(post);

        return ResponseEntity.ok(image);
    }

    // ================= CLEANUP =================

    @DeleteMapping("/cleanup-images")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public ResponseEntity<?> cleanDuplicateImages(){

        postService.cleanDuplicateImages();

        return ResponseEntity.ok(Map.of("message", "Duplicate images removed"));
    }

    // ================= DASHBOARD =================

    @GetMapping("/latestPublished")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getLatestPublished(
            @RequestParam(defaultValue = "15") int limit
    ){
        return postService.getLatestPublishedPosts(limit);
    }

    @GetMapping("/top")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getTopPosts(
            @RequestParam(defaultValue = "5") int limit
    ){
        return postService.getTopPosts(limit);
    }

    @GetMapping("/permanent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Post>> getPermanentPosts() {
        List<Post> posts = postService.getPermanentPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<CalendarEventDTO> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end
    ) {
        LocalDateTime startLocal = start.toLocalDateTime();
        LocalDateTime endLocal = end.toLocalDateTime();
        return postService.getCalendarEvents(startLocal, endLocal);
    }

    @GetMapping("/timing-analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public TimingAnalysisDTO getTimingAnalysis() {
        return postTimingService.analyzeBestPostingTimes();
    }

    @GetMapping("/weekly-comparison")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public WeeklyComparisonDTO getWeeklyComparison() {
        return postService.getWeeklyComparison();
    }

    @GetMapping("/upcoming-scheduled")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<Post> getUpcomingScheduled(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return postService.getUpcomingScheduledPosts(limit);
    }
}