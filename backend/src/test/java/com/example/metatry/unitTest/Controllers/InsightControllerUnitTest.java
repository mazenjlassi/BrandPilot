package com.example.metatry.unitTest.Controllers;
import com.example.metatry.Controllers.*;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Services.InsightService;
import com.example.metatry.Services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsightController.class)
class InsightControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsightService insightService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getCampaignInsights_returnsInsights() throws Exception {
        PostInsightDTO dto = PostInsightDTO.builder().build();
        when(insightService.generateCampaignInsights(1L)).thenReturn(dto);

        mockMvc.perform(get("/insights/campaign/1"))
                .andExpect(status().isOk());
    }


}
