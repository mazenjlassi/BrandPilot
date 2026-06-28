package com.example.metatry.integrationTest.Integration;

import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.CampaignProgressDTO;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.CreatePostRequest;
import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.AiContentService;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.ChatService;
import com.example.metatry.Services.CloudinaryService;
import com.example.metatry.Services.InsightService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class CampaignServiceIntegrationTest {

    @MockitoBean
    private AiContentService aiContentService;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @MockitoBean
    private InsightService insightService;

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PostRepository postRepository;

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        campaign = campaignRepository.save(
                Campaign.builder().name("TestCamp").topic("AI Marketing")
                        .createdAt(LocalDateTime.now()).build());
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    @Test
    void createCampaignAndGeneratePosts_createsCampaignAndReturnsPosts() {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("New Campaign");
        request.setTopic("Social Media");

        when(insightService.generateCampaignInsights(anyLong()))
                .thenThrow(new RuntimeException("No insights"));
        when(aiContentService.generatePostsWithCampaign(eq("Social Media"), any(), anyString(), anyString()))
                .thenReturn(List.of(
                        Post.builder().title("Post 1").content("Content 1").build(),
                        Post.builder().title("Post 2").content("Content 2").build()
                ));

        List<Post> result = campaignService.createCampaignAndGeneratePosts(request);

        assertThat(result).hasSize(2);
        List<Campaign> campaigns = campaignRepository.findAllByOrderByCreatedAtDesc();
        assertThat(campaigns).hasSize(2);
        Campaign newCamp = campaigns.stream()
                .filter(c -> c.getName().equals("New Campaign"))
                .findFirst().orElseThrow();
        assertThat(newCamp.getTopic()).isEqualTo("Social Media");
        verify(aiContentService).generatePostsWithCampaign(
                eq("Social Media"), any(), eq("No insights yet"), eq("Focus on engagement and clarity"));
    }

    @Test
    void createCampaignAndGeneratePosts_usesConversationConclusion() {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("Chat Campaign");
        request.setTopic("AI");
        request.setConversationId(5L);

        when(insightService.generateCampaignInsights(anyLong()))
                .thenThrow(new RuntimeException("No insights"));
        when(chatService.generateConclusion(5L)).thenReturn("Custom strategy");
        when(aiContentService.generatePostsWithCampaign(eq("AI"), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().title("P1").build()));

        List<Post> posts = campaignService.createCampaignAndGeneratePosts(request);

        assertThat(posts).hasSize(1);
        verify(chatService).generateConclusion(5L);
        verify(aiContentService).generatePostsWithCampaign(anyString(), any(), anyString(), eq("Custom strategy"));
    }

    @Test
    void createCampaignAndGeneratePosts_fallsBackWhenChatFails() {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("Fallback");
        request.setTopic("Topic");
        request.setConversationId(5L);

        when(insightService.generateCampaignInsights(anyLong()))
                .thenThrow(new RuntimeException("No insights"));
        when(chatService.generateConclusion(5L)).thenThrow(new RuntimeException("Chat error"));
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().title("P1").build()));

        campaignService.createCampaignAndGeneratePosts(request);

        verify(aiContentService).generatePostsWithCampaign(
                anyString(), any(), anyString(), eq("Focus on engagement and clarity"));
    }

    @Test
    void createCampaignAndGeneratePosts_doesNotCallChatWhenNoConversationId() {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("No Chat");
        request.setTopic("Topic");

        when(insightService.generateCampaignInsights(anyLong()))
                .thenThrow(new RuntimeException("No insights"));
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of());

        campaignService.createCampaignAndGeneratePosts(request);

        verify(chatService, never()).generateConclusion(any());
    }

    @Test
    void createCampaignAndGeneratePosts_usesInsightSummaryWhenAvailable() {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("Insight Camp");
        request.setTopic("Analytics");

        PostInsightDTO insight = PostInsightDTO.builder().summary("Data-driven insights").build();
        when(insightService.generateCampaignInsights(anyLong())).thenReturn(insight);
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().title("P1").build()));

        campaignService.createCampaignAndGeneratePosts(request);

        verify(aiContentService).generatePostsWithCampaign(anyString(), any(), eq("Data-driven insights"), anyString());
    }

    @Test
    void generatePostsForExistingCampaign_returnsPosts() {
        when(insightService.generateCampaignInsights(campaign.getId()))
                .thenThrow(new RuntimeException("No insights"));
        when(chatService.generateConclusion(null)).thenThrow(new RuntimeException("No chat"));
        when(aiContentService.generatePostsWithCampaign(
                anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().title("Gen 1").build()));

        List<Post> posts = campaignService.generatePostsForExistingCampaign(campaign.getId());

        assertThat(posts).hasSize(1);
        verify(aiContentService).generatePostsWithCampaign(
                eq(campaign.getTopic()), any(), anyString(), anyString());
    }

    @Test
    void generatePostsForExistingCampaign_throwsWhenNotFound() {
        assertThatThrownBy(() -> campaignService.generatePostsForExistingCampaign(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void getAllCampaigns_returnsDtosWithPostCount() {
        postRepository.save(Post.builder()
                .title("P1").content("C1").platform(PlatformType.FACEBOOK)
                .status(PostStatus.DRAFT).campaign(campaign).build());
        postRepository.save(Post.builder()
                .title("P2").content("C2").platform(PlatformType.LINKEDIN)
                .status(PostStatus.DRAFT).campaign(campaign).build());

        List<CampaignDTO> dtos = campaignService.getAllCampaigns();

        assertThat(dtos).hasSize(1);
        CampaignDTO dto = dtos.get(0);
        assertThat(dto.getName()).isEqualTo("TestCamp");
        assertThat(dto.getPostCount()).isEqualTo(2);
    }

    @Test
    @Transactional
    void getCampaignDTO_returnsDto() {
        CampaignDTO dto = campaignService.getCampaignDTO(campaign.getId());

        assertThat(dto.getName()).isEqualTo("TestCamp");
        assertThat(dto.getTopic()).isEqualTo("AI Marketing");
        assertThat(dto.getPostCount()).isZero();
    }

    @Test
    void getCampaignDTO_throwsWhenNotFound() {
        assertThatThrownBy(() -> campaignService.getCampaignDTO(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void deleteCampaign_removesFromDb() {
        campaignService.deleteCampaign(campaign.getId());

        assertThat(campaignRepository.findById(campaign.getId())).isEmpty();
    }

    @Test
    void getCampaign_returnsEntity() {
        Campaign result = campaignService.getCampaign(campaign.getId());

        assertThat(result.getId()).isEqualTo(campaign.getId());
        assertThat(result.getName()).isEqualTo("TestCamp");
    }

    @Test
    void getCampaign_throwsWhenNotFound() {
        assertThatThrownBy(() -> campaignService.getCampaign(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void getAllCampaignsRaw_returnsAll() {
        campaignRepository.save(Campaign.builder().name("Second").topic("T2").build());

        List<Campaign> all = campaignService.getAllCampaignsRaw();

        assertThat(all).hasSize(2);
    }

    @Test
    void createManualCampaign_savesCampaign() {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setName("Manual");
        request.setTopic("Manual Topic");

        Campaign result = campaignService.createManualCampaign(request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Manual");
        assertThat(result.getTopic()).isEqualTo("Manual Topic");
        assertThat(campaignRepository.findById(result.getId())).isPresent();
    }

    @Test
    void getCampaignsWithProgress_returnsProgress() {
        postRepository.save(Post.builder()
                .title("Pub").content("C").platform(PlatformType.FACEBOOK)
                .status(PostStatus.PUBLISHED).campaign(campaign).build());
        postRepository.save(Post.builder()
                .title("Draft").content("C").platform(PlatformType.LINKEDIN)
                .status(PostStatus.DRAFT).campaign(campaign).build());

        List<CampaignProgressDTO> progress = campaignService.getCampaignsWithProgress(5);

        assertThat(progress).hasSize(1);
        assertThat(progress.get(0).getTotalPosts()).isEqualTo(2);
        assertThat(progress.get(0).getPublishedPosts()).isEqualTo(1);
        assertThat(progress.get(0).getStatus()).isEqualTo("Active");
    }

    @Test
    void getCampaignsWithProgress_emptyCampaign_returnsZero() {
        List<CampaignProgressDTO> progress = campaignService.getCampaignsWithProgress(5);

        assertThat(progress).hasSize(1);
        assertThat(progress.get(0).getTotalPosts()).isZero();
        assertThat(progress.get(0).getPublishedPosts()).isZero();
    }

    @Test
    void getCampaignsWithProgress_respectsLimit() {
        campaignRepository.save(Campaign.builder().name("C2").topic("T2").build());
        campaignRepository.save(Campaign.builder().name("C3").topic("T3").build());

        List<CampaignProgressDTO> progress = campaignService.getCampaignsWithProgress(2);

        assertThat(progress).hasSize(2);
    }

    @Test
    void getCampaignsWithProgress_noCreatedAt_returnsDraft() {
        Campaign draftCamp = campaignRepository.save(
                Campaign.builder().name("Draft").topic("D").createdAt(null).build());

        List<CampaignProgressDTO> progress = campaignService.getCampaignsWithProgress(5);

        CampaignProgressDTO d = progress.stream()
                .filter(p -> p.getId().equals(draftCamp.getId()))
                .findFirst().orElseThrow();
        assertThat(d.getStatus()).isEqualTo("Draft");
    }

    @Test
    void createPostForCampaign_withImages_uploadsAndSaves() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Image Post");
        request.setContent("With image");
        request.setPlatform(PlatformType.FACEBOOK);
        request.setScheduledAt(LocalDateTime.now().plusDays(1));
        request.setPermanent(false);

        when(cloudinaryService.uploadImage(any())).thenReturn("https://cloudinary.com/img.jpg");

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "img.jpg",
                        "image/jpeg", "content".getBytes());

        Post result = campaignService.createPostForCampaign(campaign.getId(), request, file, null);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Image Post");
        assertThat(result.getVideoUrl()).isNull();
        verify(cloudinaryService).uploadImage(any());
    }

    @Test
    void createPostForCampaign_setsDefaultLinkWhenEmpty() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("No Link");
        request.setContent("Content");
        request.setPlatform(PlatformType.LINKEDIN);
        request.setScheduledAt(LocalDateTime.now().plusDays(1));
        request.setPermanent(false);
        request.setLink("");

        Post result = campaignService.createPostForCampaign(campaign.getId(), request, null, null);

        assertThat(result.getLink()).isEqualTo("https://3lm-solutions2.odoo.com/contactus");
    }

    @Test
    void createPostForCampaign_campaignNotFound_throws() {
        CreatePostRequest request = new CreatePostRequest();

        assertThatThrownBy(() -> campaignService.createPostForCampaign(99999L, request, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    @Transactional
    void getRecentCampaigns_returnsSortedByDate() {
        Campaign old = campaignRepository.save(
                Campaign.builder().name("Old").topic("O")
                        .createdAt(LocalDateTime.now().minusDays(5)).build());
        Campaign recent = campaignRepository.save(
                Campaign.builder().name("Newest").topic("N")
                        .createdAt(LocalDateTime.now()).build());

        List<CampaignDTO> recentCamps = campaignService.getRecentCampaigns(10);

        assertThat(recentCamps.get(0).getName()).isEqualTo("Newest");
        assertThat(recentCamps.get(recentCamps.size() - 1).getName()).isIn("Old", "TestCamp");
    }
}
