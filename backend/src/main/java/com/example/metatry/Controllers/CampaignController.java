package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.CreatePostRequest;
import com.example.metatry.DTOs.PostSummaryDTO;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final PostService postService;

    // 🔥 AI GENERATION - NEW CAMPAIGN
    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public List<Post> generateCampaign(@RequestBody CreateCampaignRequest request) {
        return campaignService.createCampaignAndGeneratePosts(request);
    }

    // 🔥 AI GENERATION - EXISTING CAMPAIGN
    @PostMapping("/{campaignId}/generate")
    @PreAuthorize("isAuthenticated()")
    public List<Post> generateForExistingCampaign(@PathVariable Long campaignId) {
        return campaignService.generatePostsForExistingCampaign(campaignId);
    }

    // 🔥 NEW: MANUAL CAMPAIGN
    @PostMapping("/{campaignId}/posts/with-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPostWithImage(
            @PathVariable Long campaignId,
            @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        try {
            Post post = campaignService.createPostForCampaign(campaignId, request, image, video);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create post: " + e.getMessage()));
        }
    }

    // 📊 Get all campaigns
    @GetMapping
    public List<CampaignDTO> getAllCampaigns() {
        return campaignService.getAllCampaigns();
    }
    // 📊 Get one campaign
    @GetMapping("/{id}")
    public Campaign getCampaign(@PathVariable Long id) {
        return campaignService.getCampaign(id);
    }

    // 📊 Get posts by campaign
    @GetMapping("/{campaignId}/posts")
    public List<Post> getPostsByCampaign(@PathVariable Long campaignId) {
        return postService.getPostsByCampaign(campaignId);
    }

    // ❌ Delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public String deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return "Campaign deleted";
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public Campaign createCampaign(@RequestBody CreateCampaignRequest request) {
        return campaignService.createManualCampaign(request);
    }

    // 📊 Get recent campaigns (last N)
    @GetMapping("/recent")
    public List<CampaignDTO> getRecentCampaigns(@RequestParam(defaultValue = "5") int limit) {
        return campaignService.getRecentCampaigns(limit);
    }
}