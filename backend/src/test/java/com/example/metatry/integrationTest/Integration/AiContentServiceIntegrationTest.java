package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.AiContentPostItem;
import com.example.metatry.DTOs.AiContentPostList;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.*;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class AiContentServiceIntegrationTest {

    @MockitoBean
    private PromptBuilderService promptBuilderService;

    @MockitoBean
    private GeminiService geminiService;

    @MockitoBean
    private MemoryContextService memoryContextService;

    @MockitoBean
    private PatternAnalysisService patternAnalysisService;

    @Autowired
    private AiContentService aiContentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private ContentPatternRepository contentPatternRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        campaign = campaignRepository.save(
                Campaign.builder().name("AICamp").topic("AI").build());
    }

    @AfterEach
    void tearDown() {
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        campaignRepository.deleteAll();
        contentPatternRepository.deleteAll();
    }

    // ================= generatePostsWithCampaign with pattern breakdown =================

    @Test
    void generatePostsWithCampaign_withPatternBreakdown_generatesPosts() throws Exception {
        contentPatternRepository.save(ContentPattern.builder()
                .topic("AI")
                .platformBreakdown("{\"linkedin\": 1, \"facebook\": 1}")
                .build());

        AiContentPostList postList = new AiContentPostList();
        AiContentPostItem item = new AiContentPostItem();
        item.setTitle("AI Post");
        item.setContent("AI content");
        item.setHashtags(List.of("#ai"));
        item.setImagePrompt("tech image");
        postList.setPosts(List.of(item));

        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(), any(), any(), anyInt()))
                .thenReturn("platform prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("memory");
        when(geminiService.generate(anyString())).thenReturn(objectMapper.writeValueAsString(postList));

        List<Post> posts = aiContentService.generatePostsWithCampaign("AI", campaign, "Insights", "Conclusion");

        assertThat(posts).isNotEmpty();
        assertThat(posts.get(0).getTitle()).isEqualTo("AI Post");
        assertThat(posts.get(0).getGeneratedByAI()).isTrue();
    }

    // ================= generatePostsWithCampaign without pattern (estimated) =================

    @Test
    void generatePostsWithCampaign_withoutPattern_usesEstimatedFlow() throws Exception {
        AiContentPostList postList = new AiContentPostList();
        AiContentPostItem item = new AiContentPostItem();
        item.setPlatform("LINKEDIN");
        item.setTitle("Estimated Post");
        item.setContent("Estimated content");
        item.setHashtags(List.of("#est"));
        item.setImagePrompt("est image");
        postList.setPosts(List.of(item));

        when(promptBuilderService.buildEstimatedPrompt(anyString(), anyString(), anyString(), any()))
                .thenReturn("estimated prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("memory");
        when(geminiService.generate(anyString())).thenReturn(objectMapper.writeValueAsString(postList));

        List<Post> posts = aiContentService.generatePostsWithCampaign("Security", campaign, "Insights", "Conclusion");

        assertThat(posts).isNotEmpty();
        assertThat(posts.get(0).getTitle()).isEqualTo("Estimated Post");
    }

    // ================= with null insights/conclusion =================

    @Test
    void generatePostsWithCampaign_nullInsights_usesDefaults() throws Exception {
        contentPatternRepository.save(ContentPattern.builder()
                .topic("AI")
                .platformBreakdown("{\"linkedin\": 1}")
                .build());

        AiContentPostList postList = new AiContentPostList();
        AiContentPostItem item = new AiContentPostItem();
        item.setTitle("Default Post");
        item.setContent("Content");
        item.setHashtags(List.of("#default"));
        item.setImagePrompt("default img");
        postList.setPosts(List.of(item));

        when(promptBuilderService.buildPlatformPrompt(anyString(), anyString(), anyString(), any(), any(), anyInt()))
                .thenReturn("prompt");
        when(memoryContextService.getMatchingContext(anyString())).thenReturn("memory");
        when(geminiService.generate(anyString())).thenReturn(objectMapper.writeValueAsString(postList));

        List<Post> posts = aiContentService.generatePostsWithCampaign("AI", campaign, null, null);

        assertThat(posts).isNotEmpty();
        assertThat(posts.get(0).getTitle()).isEqualTo("Default Post");
    }

    // ================= gemini failure =================

    @Test
    void generatePostsWithCampaign_geminiError_throws() {
        contentPatternRepository.save(ContentPattern.builder()
                .topic("AI")
                .platformBreakdown("{\"linkedin\": 1}")
                .build());

        when(geminiService.generate(anyString())).thenThrow(new RuntimeException("Gemini failed"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> aiContentService.generatePostsWithCampaign("AI", campaign, "Insights", "Conclusion"));
    }
}
