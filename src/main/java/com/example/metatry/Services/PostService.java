package com.example.metatry.Services;

import com.example.metatry.DTOs.*;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final CampaignRepository campaignRepository;
    private  final CloudinaryService cloudinaryService;

    public List<Post> getAllPosts(){
        return postRepository.findAll();
    }

    public PostDto mapToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .hashtags(post.getHashtags())
                .platform(post.getPlatform() != null ? post.getPlatform().name() : null)
                .scheduledAt(post.getScheduledAt())
                .publishedAt(post.getPublishedAt())
                .permanent(post.isPermanent())
                .link(post.getLink())
                .likes(post.getLikes())
                .commentsCount(post.getCommentsCount())
                .shares(post.getShares())
                .campaignId(post.getCampaign() != null ? post.getCampaign().getId() : null)
                .campaignName(post.getCampaign() != null ? post.getCampaign().getName() : null)
                .imageUrl(post.getImage() != null ? post.getImage().getImageUrl() : null)
                .status(post.getStatus().name())

                .build();
    }

    public PostDto getPostById(Long id) {
        Post post = postRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return mapToDto(post);
    }

    // ================= UPDATE POST =================
    public Post updatePost(Long id, UpdatePostRequest request){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        //  BLOCK update if published (unless permanent)
        if (post.getStatus() == PostStatus.PUBLISHED && !post.isPermanent()) {
            throw new RuntimeException("Cannot update a published non-permanent post");
        }

        if(request.getTitle() != null)
            post.setTitle(request.getTitle());

        if(request.getContent() != null)
            post.setContent(request.getContent());

        if(request.getHashtags() != null)
            post.setHashtags(request.getHashtags().replace(" ", ""));

        if(request.getVideoUrl() != null)
            post.setVideoUrl(request.getVideoUrl());

        if(request.getApproved() != null)
            post.setApproved(request.getApproved());

        if(request.getScheduledAt() != null)
            post.setScheduledAt(request.getScheduledAt());

        if(request.getPermanent() != null)
            post.setPermanent(request.getPermanent());

        if(request.getLink() != null)
            post.setLink(request.getLink());

        //  restrict platform change
        if(request.getPlatform() != null && post.getStatus() == PostStatus.DRAFT)
            post.setPlatform(request.getPlatform());

        //  update image if exists
        if(request.getImageUrl() != null && post.getImage() != null)
            post.getImage().setImageUrl(request.getImageUrl());

        // update status logic
        if (post.getScheduledAt() != null && post.getStatus() != PostStatus.PUBLISHED) {
            post.setStatus(PostStatus.SCHEDULED);
        }

        return postRepository.save(post);
    }


    // ================= DELETE =================

    public void deletePost(Long id){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postRepository.delete(post);
    }

    // =================  CREATE MANUALLY =================
    public Post createPostForCampaign(
            Long campaignId,
            CreatePostRequest request,
            MultipartFile file
    ) {

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        Post post = new Post();

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setHashtags(request.getHashtags());
        post.setPlatform(request.getPlatform());

        post.setGeneratedByAI(false);
        post.setApproved(true);

        if (request.getScheduledAt() != null) {
            post.setStatus(PostStatus.SCHEDULED);
            post.setScheduledAt(request.getScheduledAt());
        } else {
            post.setStatus(PostStatus.DRAFT);
            post.setScheduledAt(null);
        }

        post.setPermanent(request.isPermanent());
        post.setCampaign(campaign);

        String link = request.getLink();
        if (link == null || link.isBlank()) {
            link = "https://3lm-solutions2.odoo.com/contactus";
        }
        post.setLink(link);

        //  USE  CLOUDINARY SERVICE
        if (file != null && !file.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(file);

                PostImage image = new PostImage();
                image.setImageUrl(imageUrl);
                image.setPost(post);

                post.setImage(image);

            } catch (Exception e) {
                throw new RuntimeException("Image upload failed: " + e.getMessage());
            }
        }

        return postRepository.save(post);
    }

    // ================= BASIC GET =================

    public List<Post> getPublishedPosts(){
        return postRepository.findByStatus(PostStatus.PUBLISHED);
    }

    public List<Post> getDraftPosts(){
        return postRepository.findByStatus(PostStatus.DRAFT);
    }

    public List<Post> getApprovedPosts(){
        return postRepository.findByApprovedTrue();
    }

    public List<Post> getPostsByPlatform(PlatformType platform){
        return postRepository.findByPlatform(platform);
    }

    // 🔥 NEW: Permanent posts
    public List<Post> getPermanentPosts(){
        return postRepository.findByPermanentTrue();
    }

    // ================= STATS =================

    public PostStatsResponse getStats(){

        long total = postRepository.count();
        long published = postRepository.countByStatus(PostStatus.PUBLISHED);
        long draft = postRepository.countByStatus(PostStatus.DRAFT);
        long approved = postRepository.countByApprovedTrue();

        long facebook = postRepository.countByPlatform(PlatformType.FACEBOOK);
        long instagram = postRepository.countByPlatform(PlatformType.INSTAGRAM);
        long linkedin = postRepository.countByPlatform(PlatformType.LINKEDIN);

        return new PostStatsResponse(
                total,
                published,
                draft,
                approved,
                facebook,
                instagram,
                linkedin
        );
    }

    // ================= SCHEDULER =================

    // Frontend: /posts/scheduled — returns future scheduled posts only
    public List<Post> getAllScheduledPosts() {
        return postRepository.findByStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                PostStatus.SCHEDULED, LocalDateTime.now().plusHours(1)
        );
    }

    // Scheduler: only posts ready to publish (approved=true + time has arrived)
    public List<Post> getScheduledPostsToPublish() {

        return postRepository.findByApprovedTrueAndStatusAndScheduledAtBefore(
                PostStatus.SCHEDULED,
                LocalDateTime.now().plusHours(1)
        );
    }

    // ================= CLEAN IMAGES =================

    public void cleanDuplicateImages() {

        List<PostImage> allImages = postImageRepository.findAll();

        Map<Long, PostImage> uniqueImages = new HashMap<>();

        for (PostImage image : allImages) {

            Long postId = image.getPost().getId();

            if (!uniqueImages.containsKey(postId)) {
                uniqueImages.put(postId, image);
            } else {
                postImageRepository.delete(image);
            }
        }
    }

    // ================= DASHBOARD =================

    public List<Post> getLatestPublishedPosts(int limit) {

        return postRepository.findByStatus(
                PostStatus.PUBLISHED,
                org.springframework.data.domain.PageRequest.of(
                        0,
                        limit,
                        org.springframework.data.domain.Sort.by("publishedAt").descending()
                )
        ).getContent();
    }

    public List<Post> getTopPosts(int limit) {

        return postRepository.findByStatus(
                PostStatus.PUBLISHED,
                org.springframework.data.domain.PageRequest.of(
                        0,
                        limit,
                        org.springframework.data.domain.Sort.by("likes").descending()
                )
        ).getContent();
    }

    // ================= POSTS DISPLAY =================

    //  By campaign
    public List<Post> getPostsByCampaign(Long campaignId) {
        return postRepository.findByCampaignId(campaignId);
    }

    //  Campaign + Status
    public List<Post> getCampaignPostsByStatus(Long campaignId, PostStatus status) {
        return postRepository.findByCampaignIdAndStatus(campaignId, status);
    }

    // ================= POSTS SUMMARY (NEW - DO NOT TOUCH EXISTING METHODS) =================

    public List<PostSummaryDTO> getPostSummariesByCampaign(Long campaignId) {
        return postRepository.findByCampaignId(campaignId)
                .stream()
                .map(this::mapToSummaryDTO)
                .toList();
    }

    private PostSummaryDTO mapToSummaryDTO(Post post) {
        return PostSummaryDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .platform(post.getPlatform() != null ? post.getPlatform().name() : null)
                .status(post.getStatus().name())
                .likes(post.getLikes())
                .commentsCount(post.getCommentsCount())
                .shares(post.getShares())
                .build();
    }

    public List<Post> getLastPublishedPosts(int limit) {
        return postRepository.findTop20ByStatusOrderByPublishedAtDesc(PostStatus.PUBLISHED);
    }

    public List<CalendarEventDTO> getCalendarEvents(LocalDateTime start, LocalDateTime end) {
        List<CalendarEventDTO> events = new java.util.ArrayList<>();

        List<Post> scheduledPosts = postRepository.findByStatusAndScheduledAtBetween(PostStatus.SCHEDULED, start, end);
        for (Post post : scheduledPosts) {
            events.add(mapToCalendarEvent(post, post.getScheduledAt()));
        }

        return events;
    }

    private CalendarEventDTO mapToCalendarEvent(Post post, LocalDateTime eventTime) {
        String imageUrl = null;
        if (post.getImage() != null) {
            imageUrl = post.getImage().getImageUrl();
        }

        String campaignName = null;
        Long campaignId = null;
        if (post.getCampaign() != null) {
            campaignName = post.getCampaign().getName();
            campaignId = post.getCampaign().getId();
        }

        return CalendarEventDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent() != null && post.getContent().length() > 100
                    ? post.getContent().substring(0, 100) + "..." : post.getContent())
                .scheduledAt(post.getScheduledAt())
                .publishedAt(post.getPublishedAt())
                .status(post.getStatus())
                .platform(post.getPlatform())
                .imageUrl(imageUrl)
                .campaignId(campaignId)
                .campaignName(campaignName)
                .build();
    }

    public WeeklyComparisonDTO getWeeklyComparison() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisWeekStart = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thisWeekEnd = thisWeekStart.plusDays(7);
        LocalDateTime lastWeekStart = thisWeekStart.minusDays(7);
        LocalDateTime lastWeekEnd = thisWeekStart;

        long thisWeekCount = postRepository.countByStatusAndPublishedAtBetween(
            PostStatus.PUBLISHED, thisWeekStart, thisWeekEnd
        );

        long lastWeekCount = postRepository.countByStatusAndPublishedAtBetween(
            PostStatus.PUBLISHED, lastWeekStart, lastWeekEnd
        );

        double percentage = 0;
        boolean increased = false;

        if (lastWeekCount > 0) {
            percentage = ((double)(thisWeekCount - lastWeekCount) / lastWeekCount) * 100;
            increased = thisWeekCount >= lastWeekCount;
        } else if (thisWeekCount > 0) {
            percentage = 100;
            increased = true;
        }

        return WeeklyComparisonDTO.builder()
            .thisWeek((int) thisWeekCount)
            .lastWeek((int) lastWeekCount)
            .percentage(Math.abs(percentage))
            .increased(increased)
            .build();
    }

    public List<Post> getUpcomingScheduledPosts(int limit) {
        return postRepository.findByStatusAndScheduledAtAfterOrderByScheduledAtAsc(
            PostStatus.SCHEDULED,
            LocalDateTime.now()
        ).stream().limit(limit).toList();
    }


}