package com.example.metatry.Controllers;

import com.example.metatry.Services.AnalyticsService;
import com.example.metatry.Services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void collectMetrics_returnsOk() throws Exception {
        mockMvc.perform(post("/analytics/collect"))
                .andExpect(status().isOk())
                .andExpect(content().string("Metrics collected !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"));
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void collectMetrics_withMarketingRole_returnsOk() throws Exception {
        mockMvc.perform(post("/analytics/collect"))
                .andExpect(status().isOk());
    }
}
