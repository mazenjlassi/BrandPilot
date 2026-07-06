package com.example.metatry.unitTest.Controllers;
import com.example.metatry.Controllers.*;

import com.example.metatry.Models.PostMetric;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.PostMetricService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostMetricController.class)
class PostMetricControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostMetricService postMetricService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getHistory_returnsMetrics() throws Exception {
        when(postMetricService.getMetricsHistory(1L)).thenReturn(List.of(new PostMetric()));

        mockMvc.perform(get("/metrics/post/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getLatest_returnsMetric() throws Exception {
        when(postMetricService.getLatestMetric(1L)).thenReturn(new PostMetric());

        mockMvc.perform(get("/metrics/post/1/latest"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMax_returnsMaxValues() throws Exception {
        when(postMetricService.getMaxLikes(1L)).thenReturn(100);
        when(postMetricService.getMaxComments(1L)).thenReturn(50);
        when(postMetricService.getMaxShares(1L)).thenReturn(25);

        mockMvc.perform(get("/metrics/post/1/max"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(100))
                .andExpect(jsonPath("$.comments").value(50))
                .andExpect(jsonPath("$.shares").value(25));
    }


}
