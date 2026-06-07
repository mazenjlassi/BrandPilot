package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.CreatePostRequest;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void generateCampaign_returnsPosts() throws Exception {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("Test Campaign");

        when(campaignService.createCampaignAndGeneratePosts(any(CreateCampaignRequest.class)))
                .thenReturn(List.of(new Post()));

        mockMvc.perform(post("/campaigns/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void generateForExistingCampaign_returnsPosts() throws Exception {
        when(campaignService.generatePostsForExistingCampaign(1L))
                .thenReturn(List.of(new Post()));

        mockMvc.perform(post("/campaigns/1/generate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void createPostWithImage_returnsPost() throws Exception {
        CreatePostRequest data = new CreatePostRequest();
        data.setTitle("Test");

        MockMultipartFile image = new MockMultipartFile("image", "test.png",
                MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes());
        MockMultipartFile jsonPart = new MockMultipartFile("data", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(data));

        when(campaignService.createPostForCampaign(eq(1L), any(CreatePostRequest.class), any()))
                .thenReturn(new Post());

        mockMvc.perform(multipart("/campaigns/1/posts/with-image")
                        .file(image)
                        .file(jsonPart))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getAllCampaigns_returnsCampaigns() throws Exception {
        when(campaignService.getAllCampaigns()).thenReturn(List.of(new CampaignDTO()));

        mockMvc.perform(get("/campaigns"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getCampaign_returnsCampaign() throws Exception {
        when(campaignService.getCampaign(1L)).thenReturn(new Campaign());

        mockMvc.perform(get("/campaigns/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPostsByCampaign_returnsPosts() throws Exception {
        when(postService.getPostsByCampaign(1L)).thenReturn(List.of(new Post()));

        mockMvc.perform(get("/campaigns/1/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void deleteCampaign_returnsMessage() throws Exception {
        doNothing().when(campaignService).deleteCampaign(1L);

        mockMvc.perform(delete("/campaigns/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Campaign deleted"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MARKETING"})
    void createCampaign_returnsCampaign() throws Exception {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("Manual Campaign");

        when(campaignService.createManualCampaign(any(CreateCampaignRequest.class)))
                .thenReturn(new Campaign());

        mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getRecentCampaigns_returnsCampaigns() throws Exception {
        when(campaignService.getRecentCampaigns(5)).thenReturn(List.of(new CampaignDTO()));

        mockMvc.perform(get("/campaigns/recent"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getRecentCampaigns_withCustomLimit() throws Exception {
        when(campaignService.getRecentCampaigns(10)).thenReturn(List.of());

        mockMvc.perform(get("/campaigns/recent").param("limit", "10"))
                .andExpect(status().isOk());
    }

}
