package com.example.metatry.Controllers;

import com.example.metatry.DTOs.MarketingStrategyDTO;
import com.example.metatry.DTOs.MarketingStrategyRequest;
import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.Repositories.MarketingStrategyRepository;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Services.strategy.MarketingStrategyService;
import com.example.metatry.Services.scheduler.WeeklyCampaignService;
import com.example.metatry.Services.scheduler.WeeklyImageDecisionService;
import com.example.metatry.Services.scheduler.WeeklyPostPlanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarketingStrategyController.class)
class MarketingStrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketingStrategyService strategyService;

    @MockitoBean
    private MarketingStrategyRepository strategyRepository;

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private WeeklyPostPlanner weeklyPostPlanner;

    @MockitoBean
    private WeeklyImageDecisionService weeklyImageDecisionService;

    @MockitoBean
    private WeeklyCampaignService weeklyCampaignService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void getAll_returnsList() throws Exception {
        when(strategyService.getAllStrategies()).thenReturn(List.of(
                MarketingStrategyDTO.builder().id(1L).title("S1").build()));

        mockMvc.perform(get("/marketing-strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void getActive_returnsDTO() throws Exception {
        when(strategyService.getActiveStrategy()).thenReturn(
                MarketingStrategyDTO.builder().id(1L).status("ACTIVE").build());

        mockMvc.perform(get("/marketing-strategies/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void getActive_whenNull_returns204() throws Exception {
        when(strategyService.getActiveStrategy()).thenReturn(null);

        mockMvc.perform(get("/marketing-strategies/active"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getById_returnsDTO() throws Exception {
        when(strategyService.getStrategy(1L)).thenReturn(
                MarketingStrategyDTO.builder().id(1L).title("Test").build());

        mockMvc.perform(get("/marketing-strategies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void generate_returnsDTO() throws Exception {
        String body = objectMapper.writeValueAsString(
                new com.example.metatry.DTOs.GenerateStrategyRequest("Test", 8, false));
        when(strategyService.generateStrategy(any())).thenReturn(
                MarketingStrategyDTO.builder().id(1L).title("Test").build());

        mockMvc.perform(post("/marketing-strategies/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void generateAuto_returnsDTO() throws Exception {
        when(strategyService.generateAutoStrategy()).thenReturn(
                MarketingStrategyDTO.builder().id(1L).title("Auto").build());

        mockMvc.perform(post("/marketing-strategies/generate-auto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Auto"));
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void update_returnsDTO() throws Exception {
        MarketingStrategyRequest req = new MarketingStrategyRequest();
        req.setTitle("Updated");
        String body = objectMapper.writeValueAsString(req);
        when(strategyService.updateStrategy(eq(1L), any())).thenReturn(
                MarketingStrategyDTO.builder().id(1L).title("Updated").build());

        mockMvc.perform(put("/marketing-strategies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void approve_returnsDTO() throws Exception {
        when(strategyService.approveStrategy(1L)).thenReturn(
                MarketingStrategyDTO.builder().id(1L).status("ACTIVE").build());

        mockMvc.perform(post("/marketing-strategies/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void deactivate_returnsDTO() throws Exception {
        when(strategyService.deactivateStrategy(1L)).thenReturn(
                MarketingStrategyDTO.builder().id(1L).status("INACTIVE").build());

        mockMvc.perform(post("/marketing-strategies/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser
    void getCampaigns_returnsList() throws Exception {
        when(campaignService.getCampaignsByStrategy(1L)).thenReturn(List.of(
                CampaignDTO.builder().id(10L).name("Camp1").build()));

        mockMvc.perform(get("/marketing-strategies/1/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @WithMockUser(roles = "MARKETING")
    void setAutoGenerate_returnsDTO() throws Exception {
        when(strategyService.setAutoGenerate(1L, true)).thenReturn(
                MarketingStrategyDTO.builder().id(1L).autoGenerate(true).build());

        mockMvc.perform(put("/marketing-strategies/1/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"autoGenerate\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoGenerate").value(true));
    }
}
