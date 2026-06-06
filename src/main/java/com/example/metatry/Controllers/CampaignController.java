package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.CreatePostRequest;
import com.example.metatry.DTOs.PostSummaryDTO;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.PostService;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final PostService postService;

    // 🔥 AI GENERATION - NEW CAMPAIGN
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<Post> generateCampaign(@RequestBody CreateCampaignRequest request) {
        return campaignService.createCampaignAndGeneratePosts(request);
    }

    // 🔥 AI GENERATION - EXISTING CAMPAIGN
    @PostMapping("/{campaignId}/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public List<Post> generateForExistingCampaign(@PathVariable Long campaignId) {
        return campaignService.generatePostsForExistingCampaign(campaignId);
    }

    // 🔥 NEW: MANUAL CAMPAIGN
    @PostMapping("/{campaignId}/posts/with-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    public Post createPostWithImage(
            @PathVariable Long campaignId,
            @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException, java.io.IOException {

        return campaignService.createPostForCampaign(campaignId, request, image);
    }

    // 📊 Get all campaigns
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CampaignDTO> getAllCampaigns() {
        return campaignService.getAllCampaigns();
    }
    // 📊 Get one campaign
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Campaign getCampaign(@PathVariable Long id) {
        return campaignService.getCampaign(id);
    }

    // 📊 Get posts by campaign
    @GetMapping("/{campaignId}/posts")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    public List<CampaignDTO> getRecentCampaigns(@RequestParam(defaultValue = "5") int limit) {
        return campaignService.getRecentCampaigns(limit);
    }
}