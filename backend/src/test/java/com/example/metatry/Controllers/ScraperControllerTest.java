package com.example.metatry.Controllers;

import com.example.metatry.DTOs.ScrapeRequest;
import com.example.metatry.DTOs.ScrapeResponse;
import com.example.metatry.Services.JwtService;
import com.example.metatry.Repositories.CompanyProfileRepository;
import com.example.metatry.Services.ScraperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScraperController.class)
class ScraperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ScraperService scraperService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private CompanyProfileRepository companyProfileRepository;

    @Test
    void scrape_returnsOk() throws Exception {
        ScrapeResponse response = ScrapeResponse.builder()
                .status("success").companyName("NVIDIA").build();
        when(scraperService.scrape(eq("NVIDIA"), any(), any(), any()))
                .thenReturn(response);

        ScrapeRequest request = ScrapeRequest.builder()
                .companyName("NVIDIA").build();

        mockMvc.perform(post("/api/scraper/scrape")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void scrape_missingCompanyName_returnsBadRequest() throws Exception {
        ScrapeRequest request = ScrapeRequest.builder()
                .companyName("").build();

        mockMvc.perform(post("/api/scraper/scrape")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void scrape_serviceError_returnsBadRequest() throws Exception {
        ScrapeResponse response = ScrapeResponse.builder()
                .status("error").message("Scraping failed").build();
        when(scraperService.scrape(eq("NVIDIA"), any(), any(), any()))
                .thenReturn(response);

        ScrapeRequest request = ScrapeRequest.builder()
                .companyName("NVIDIA").build();

        mockMvc.perform(post("/api/scraper/scrape")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void trigger_returnsOk() throws Exception {
        doNothing().when(scraperService).dispatchWorkflow("NVIDIA");

        mockMvc.perform(post("/api/scraper/trigger")
                        .param("companyName", "NVIDIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void trigger_missingCompanyName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/scraper/trigger")
                        .param("companyName", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void trigger_serviceError_returnsBadGateway() throws Exception {
        doThrow(new RuntimeException("GitHub API error"))
                .when(scraperService).dispatchWorkflow("NVIDIA");

        mockMvc.perform(post("/api/scraper/trigger")
                        .param("companyName", "NVIDIA"))
                .andExpect(status().isBadGateway());
    }
}
