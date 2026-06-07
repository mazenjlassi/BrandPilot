package com.example.metatry.Controllers;

import com.example.metatry.DTOs.*;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.AiImageService;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.PostService;
import com.example.metatry.Services.PostTimingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private AiImageService aiImageService;

    @MockitoBean
    private PostImageRepository postImageRepository;

    @MockitoBean
    private PostTimingService postTimingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // ================= BASIC =================

    @Test
    @WithMockUser
    void getAllPosts_returnsPosts() throws Exception {
        when(postService.getAllPosts()).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPostById_returnsPostDto() throws Exception {
        PostDto dto = PostDto.builder().id(1L).title("Test").build();
        when(postService.getPostById(1L)).thenReturn(dto);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    @WithMockUser
    void getDrafts_returnsPosts() throws Exception {
        when(postService.getDraftPosts()).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/drafts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getScheduled_returnsPosts() throws Exception {
        when(postService.getAllScheduledPosts()).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/scheduled"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPublished_returnsPosts() throws Exception {
        when(postService.getPublishedPosts()).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/published"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getApprovedPosts_returnsPosts() throws Exception {
        when(postService.getApprovedPosts()).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/approved"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPostsByPlatform_returnsPosts() throws Exception {
        when(postService.getPostsByPlatform(PlatformType.FACEBOOK))
                .thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/platform/FACEBOOK"))
                .andExpect(status().isOk());
    }

    // ================= CAMPAIGN =================

    @Test
    @WithMockUser
    void getByCampaign_returnsPosts() throws Exception {
        when(postService.getPostsByCampaign(1L)).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/campaign/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getCampaignPostsByStatus_returnsPosts() throws Exception {
        when(postService.getCampaignPostsByStatus(1L, PostStatus.DRAFT))
                .thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/campaign/1/status/DRAFT"))
                .andExpect(status().isOk());
    }

    // ================= UPDATE =================

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void updatePost_returnsOk() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated");

        when(postService.updatePost(eq(1L), any(UpdatePostRequest.class))).thenReturn(new Post());

        mockMvc.perform(put("/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post updated"));
    }

    // ================= DELETE =================

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void deletePost_returnsOk() throws Exception {
        doNothing().when(postService).deletePost(1L);

        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post deleted successfully"));
    }

    // ================= CREATE =================

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void createPost_returnsPost() throws Exception {
        CreatePostRequest data = new CreatePostRequest();
        data.setTitle("New Post");

        MockMultipartFile jsonPart = new MockMultipartFile("data", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(data));

        when(postService.createPostForCampaign(eq(1L), any(CreatePostRequest.class), any()))
                .thenReturn(new Post());

        mockMvc.perform(multipart("/posts/campaigns/1/posts")
                        .file(jsonPart))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void createPost_withImage_returnsPost() throws Exception {
        CreatePostRequest data = new CreatePostRequest();
        data.setTitle("New Post");

        MockMultipartFile image = new MockMultipartFile("image", "test.png",
                MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes());
        MockMultipartFile jsonPart = new MockMultipartFile("data", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(data));

        when(postService.createPostForCampaign(eq(1L), any(CreatePostRequest.class), any()))
                .thenReturn(new Post());

        mockMvc.perform(multipart("/posts/campaigns/1/posts")
                        .file(image)
                        .file(jsonPart))
                .andExpect(status().isOk());
    }

    // ================= STATS =================

    @Test
    @WithMockUser
    void getStats_returnsStats() throws Exception {
        PostStatsResponse stats = new PostStatsResponse(10L, 5L, 3L, 2L, 4L, 3L, 3L);
        when(postService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/posts/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPosts").value(10))
                .andExpect(jsonPath("$.publishedPosts").value(5))
                .andExpect(jsonPath("$.draftPosts").value(3));
    }

    // ================= AI IMAGE =================

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void generateImage_returnsImage() throws Exception {
        Post post = new Post();
        post.setId(1L);
        PostImage image = PostImage.builder().imageUrl("https://img.url").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(aiImageService.generateImageForPost(post)).thenReturn(image);

        mockMvc.perform(post("/posts/1/generate-image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://img.url"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void generateImage_whenPostNotFound_returnsError() throws Exception {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/posts/99/generate-image"))
                .andExpect(status().isBadRequest());
    }

    // ================= CLEANUP =================

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void cleanDuplicateImages_returnsOk() throws Exception {
        doNothing().when(postService).cleanDuplicateImages();

        mockMvc.perform(delete("/posts/cleanup-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Duplicate images removed"));
    }

    // ================= DASHBOARD =================

    @Test
    @WithMockUser
    void getLatestPublished_returnsPosts() throws Exception {
        when(postService.getLatestPublishedPosts(15)).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/latestPublished"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getLatestPublished_withCustomLimit() throws Exception {
        when(postService.getLatestPublishedPosts(5)).thenReturn(List.of());

        mockMvc.perform(get("/posts/latestPublished").param("limit", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getTopPosts_returnsPosts() throws Exception {
        when(postService.getTopPosts(5)).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/top"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPermanentPosts_returnsPosts() throws Exception {
        when(postService.getPermanentPosts()).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/permanent"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void getCalendarEvents_returnsEvents() throws Exception {
        when(postService.getCalendarEvents(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new CalendarEventDTO()));

        mockMvc.perform(get("/posts/calendar")
                        .param("start", "2026-01-01T00:00:00Z")
                        .param("end", "2026-01-31T00:00:00Z"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void getTimingAnalysis_returnsAnalysis() throws Exception {
        when(postTimingService.analyzeBestPostingTimes())
                .thenReturn(new TimingAnalysisDTO());

        mockMvc.perform(get("/posts/timing-analysis"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void getWeeklyComparison_returnsComparison() throws Exception {
        WeeklyComparisonDTO dto = WeeklyComparisonDTO.builder()
                .thisWeek(10).lastWeek(5).percentage(100).increased(true).build();
        when(postService.getWeeklyComparison()).thenReturn(dto);

        mockMvc.perform(get("/posts/weekly-comparison"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thisWeek").value(10))
                .andExpect(jsonPath("$.lastWeek").value(5))
                .andExpect(jsonPath("$.increased").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void getUpcomingScheduled_returnsPosts() throws Exception {
        when(postService.getUpcomingScheduledPosts(3)).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/posts/upcoming-scheduled"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void getUpcomingScheduled_withCustomLimit() throws Exception {
        when(postService.getUpcomingScheduledPosts(5)).thenReturn(List.of());

        mockMvc.perform(get("/posts/upcoming-scheduled").param("limit", "5"))
                .andExpect(status().isOk());
    }

}
