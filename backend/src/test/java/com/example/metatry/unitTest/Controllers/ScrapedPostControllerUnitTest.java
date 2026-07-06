package com.example.metatry.unitTest.Controllers;
import com.example.metatry.Controllers.*;

import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.ScrapedPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScrapedPostController.class)
class ScrapedPostControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ScrapedPostService scrapedPostService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getAll_returnsAll() throws Exception {
        when(scrapedPostService.getAll()).thenReturn(List.of(new ScrapedPost()));

        mockMvc.perform(get("/api/scraped-posts"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_withCompanyName_returnsFiltered() throws Exception {
        when(scrapedPostService.getByCompanyName("Acme")).thenReturn(List.of(new ScrapedPost()));

        mockMvc.perform(get("/api/scraped-posts").param("companyName", "Acme"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_withPlatform_returnsFiltered() throws Exception {
        when(scrapedPostService.getByPlatform("twitter")).thenReturn(List.of(new ScrapedPost()));

        mockMvc.perform(get("/api/scraped-posts").param("platform", "twitter"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_withTopic_returnsFiltered() throws Exception {
        when(scrapedPostService.getByTopic("tech")).thenReturn(List.of(new ScrapedPost()));

        mockMvc.perform(get("/api/scraped-posts").param("topic", "tech"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_whenFound_returnsPost() throws Exception {
        ScrapedPost post = new ScrapedPost();
        post.setId(1L);
        when(scrapedPostService.getById(1L)).thenReturn(post);

        mockMvc.perform(get("/api/scraped-posts/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        when(scrapedPostService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/scraped-posts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsSavedPost() throws Exception {
        ScrapedPost post = new ScrapedPost();
        post.setPostText("Test content");
        when(scrapedPostService.save(any(ScrapedPost.class))).thenReturn(post);

        mockMvc.perform(post("/api/scraped-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postText").value("Test content"));
    }

    @Test
    void update_whenFound_returnsUpdated() throws Exception {
        ScrapedPost existing = new ScrapedPost();
        existing.setId(1L);
        ScrapedPost updated = new ScrapedPost();
        updated.setPostText("Updated");

        when(scrapedPostService.getById(1L)).thenReturn(existing);
        when(scrapedPostService.save(any(ScrapedPost.class))).thenReturn(updated);

        mockMvc.perform(put("/api/scraped-posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postText").value("Updated"));
    }

    @Test
    void update_whenNotFound_returns404() throws Exception {
        when(scrapedPostService.getById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/scraped-posts/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsOk() throws Exception {
        doNothing().when(scrapedPostService).delete(1L);

        mockMvc.perform(delete("/api/scraped-posts/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCompanies_returnsList() throws Exception {
        when(scrapedPostService.getDistinctCompanies()).thenReturn(List.of("Acme", "Beta"));

        mockMvc.perform(get("/api/scraped-posts/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Acme"))
                .andExpect(jsonPath("$[1]").value("Beta"));
    }

    @Test
    void getCount_withCompanyName_returnsCount() throws Exception {
        when(scrapedPostService.countByCompany("Acme")).thenReturn(5L);

        mockMvc.perform(get("/api/scraped-posts/count").param("companyName", "Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void getCount_withPlatform_returnsCount() throws Exception {
        when(scrapedPostService.countByPlatform("twitter")).thenReturn(3L);

        mockMvc.perform(get("/api/scraped-posts/count").param("platform", "twitter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void getCount_withoutFilter_returnsTotal() throws Exception {
        when(scrapedPostService.getAll()).thenReturn(List.of(new ScrapedPost(), new ScrapedPost()));

        mockMvc.perform(get("/api/scraped-posts/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }
}
