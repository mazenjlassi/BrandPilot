package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.DTOs.*;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceUnitTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private AiContentService aiContentService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private InsightService insightService;

    @Mock
    private ChatService chatService;

    private CampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignService(
                postRepository, campaignRepository, aiContentService,
                cloudinaryService, insightService, chatService);
    }

    private CreateCampaignRequest createCampaignReq(String name, String topic) {
        CreateCampaignRequest r = new CreateCampaignRequest();
        r.setName(name);
        r.setTopic(topic);
        return r;
    }

    private CreatePostRequest createPostReq(String title, String content, String hashtags,
                                             com.example.metatry.Enums.PlatformType platform,
                                             LocalDateTime scheduledAt, boolean permanent, String link) {
        CreatePostRequest r = new CreatePostRequest();
        r.setTitle(title);
        r.setContent(content);
        r.setHashtags(hashtags);
        r.setPlatform(platform);
        r.setScheduledAt(scheduledAt);
        r.setPermanent(permanent);
        r.setLink(link);
        return r;
    }

    @Test
    void createCampaignAndGeneratePosts_createsAndGenerates() {
        CreateCampaignRequest request = createCampaignReq("Test Campaign", "AI Marketing");
        Campaign savedCampaign = Campaign.builder().id(1L).name("Test Campaign").topic("AI Marketing").build();
        when(campaignRepository.save(any())).thenReturn(savedCampaign);
        when(insightService.generateCampaignInsights(1L)).thenThrow(new RuntimeException("No insights"));
        when(aiContentService.generatePostsWithCampaign(eq("AI Marketing"), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().id(1L).build()));

        List<Post> posts = campaignService.createCampaignAndGeneratePosts(request);

        assertThat(posts).hasSize(1);
        verify(campaignRepository).save(any());
    }

    @Test
    void createCampaignAndGeneratePosts_usesConversationConclusion() {
        CreateCampaignRequest request = createCampaignReq("Test Campaign", "AI Marketing");
        request.setConversationId(5L);
        Campaign savedCampaign = Campaign.builder().id(1L).name("Test Campaign").topic("AI Marketing").build();
        when(campaignRepository.save(any())).thenReturn(savedCampaign);
        when(insightService.generateCampaignInsights(1L)).thenThrow(new RuntimeException("No insights"));
        when(chatService.generateConclusion(5L)).thenReturn("Custom strategy conclusion");
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().id(1L).build()));

        campaignService.createCampaignAndGeneratePosts(request);

        verify(chatService).generateConclusion(5L);
        verify(aiContentService).generatePostsWithCampaign(anyString(), any(), anyString(), eq("Custom strategy conclusion"));
    }

    @Test
    void createCampaignAndGeneratePosts_fallsBackWhenConversationConclusionFails() {
        CreateCampaignRequest request = createCampaignReq("Test", "AI");
        request.setConversationId(5L);
        Campaign savedCampaign = Campaign.builder().id(1L).build();
        when(campaignRepository.save(any())).thenReturn(savedCampaign);
        when(insightService.generateCampaignInsights(1L)).thenThrow(new RuntimeException("No insights"));
        when(chatService.generateConclusion(5L)).thenThrow(new RuntimeException("Chat error"));
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().id(1L).build()));

        campaignService.createCampaignAndGeneratePosts(request);

        verify(aiContentService).generatePostsWithCampaign(anyString(), any(), anyString(), eq("Focus on engagement and clarity"));
    }

    @Test
    void createCampaignAndGeneratePosts_usesInsightSummaryWhenAvailable() {
        CreateCampaignRequest request = createCampaignReq("Test", "AI");
        Campaign savedCampaign = Campaign.builder().id(1L).build();
        when(campaignRepository.save(any())).thenReturn(savedCampaign);
        PostInsightDTO insight = PostInsightDTO.builder().summary("Users love AI content").build();
        when(insightService.generateCampaignInsights(any())).thenReturn(insight);
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().id(1L).build()));

        campaignService.createCampaignAndGeneratePosts(request);

        verify(aiContentService).generatePostsWithCampaign(anyString(), any(), eq("Users love AI content"), anyString());
    }

    @Test
    void generatePostsForExistingCampaign_generatesPosts() {
        Campaign campaign = Campaign.builder().id(1L).topic("AI").build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(insightService.generateCampaignInsights(1L)).thenThrow(new RuntimeException("No insights"));
        when(chatService.generateConclusion(null)).thenThrow(new RuntimeException("No chat"));
        when(aiContentService.generatePostsWithCampaign(eq("AI"), eq(campaign), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().id(1L).build(), Post.builder().id(2L).build()));

        List<Post> posts = campaignService.generatePostsForExistingCampaign(1L);

        assertThat(posts).hasSize(2);
    }

    @Test
    void createPostForCampaign_createsPost() throws Exception {
        Campaign campaign = Campaign.builder().id(1L).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreatePostRequest request = createPostReq("Post", "Content", "#AI",
                com.example.metatry.Enums.PlatformType.LINKEDIN,
                LocalDateTime.of(2024, 6, 1, 10, 0), false, "https://link.com");

        Post result = campaignService.createPostForCampaign(1L, request, null);

        assertThat(result.getTitle()).isEqualTo("Post");
        assertThat(result.getStatus()).isEqualTo(PostStatus.SCHEDULED);
        assertThat(result.getGeneratedByAI()).isFalse();
    }

    @Test
    void createPostForCampaign_whenCampaignNotFound_throwsException() {
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.createPostForCampaign(
                99L, new CreatePostRequest(), null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void getAllCampaigns_returnsDtos() {
        Campaign campaign = Campaign.builder()
                .id(1L).name("Campaign").topic("AI").posts(List.of(Post.builder().build(), Post.builder().build()))
                .build();
        when(campaignRepository.findAllWithPosts()).thenReturn(List.of(campaign));

        List<CampaignDTO> result = campaignService.getAllCampaigns();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPostCount()).isEqualTo(2);
    }

    @Test
    void getCampaignDTO_returnsDto() {
        Campaign campaign = Campaign.builder()
                .id(1L).name("Campaign").topic("AI").posts(List.of(Post.builder().build()))
                .build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        CampaignDTO result = campaignService.getCampaignDTO(1L);

        assertThat(result.getName()).isEqualTo("Campaign");
        assertThat(result.getPostCount()).isEqualTo(1);
    }

    @Test
    void getCampaignDTO_whenNotFound_throwsException() {
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> campaignService.getCampaignDTO(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteCampaign_deletes() {
        campaignService.deleteCampaign(1L);
        verify(campaignRepository).deleteById(1L);
    }

    @Test
    void getCampaign_returnsCampaign() {
        Campaign campaign = Campaign.builder().id(1L).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        assertThat(campaignService.getCampaign(1L)).isEqualTo(campaign);
    }

    @Test
    void getAllCampaignsRaw_returnsAll() {
        when(campaignRepository.findAll()).thenReturn(List.of(Campaign.builder().id(1L).build()));
        assertThat(campaignService.getAllCampaignsRaw()).hasSize(1);
    }

    @Test
    void createManualCampaign_savesAndReturns() {
        CreateCampaignRequest request = createCampaignReq("Manual", "Topic");
        when(campaignRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Campaign result = campaignService.createManualCampaign(request);

        assertThat(result.getName()).isEqualTo("Manual");
    }

    @Test
    void getCampaignsWithProgress_returnsProgress() {
        Campaign campaign = Campaign.builder().id(1L).name("Campaign").topic("AI")
                .createdAt(LocalDateTime.now()).build();
        when(campaignRepository.findAll()).thenReturn(List.of(campaign));
        when(postRepository.findByCampaignId(1L)).thenReturn(List.of(
                Post.builder().status(PostStatus.PUBLISHED).build(),
                Post.builder().status(PostStatus.DRAFT).build()
        ));

        List<CampaignProgressDTO> result = campaignService.getCampaignsWithProgress(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalPosts()).isEqualTo(2);
        assertThat(result.get(0).getPublishedPosts()).isEqualTo(1);
    }

    @Test
    void getRecentCampaigns_returnsSortedByDate() {
        Campaign c1 = Campaign.builder().id(1L).name("Old").createdAt(LocalDateTime.now().minusDays(5)).build();
        Campaign c2 = Campaign.builder().id(2L).name("New").createdAt(LocalDateTime.now()).build();
        when(campaignRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CampaignDTO> result = campaignService.getRecentCampaigns(3);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("New");
        assertThat(result.get(1).getName()).isEqualTo("Old");
    }

    @Test
    void generatePostsForExistingCampaign_passesInsightAndConclusion() {
        Campaign campaign = Campaign.builder().id(1L).topic("AI").build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        PostInsightDTO insight = PostInsightDTO.builder().summary("Great insights from data").build();
        when(insightService.generateCampaignInsights(1L)).thenReturn(insight);
        when(chatService.generateConclusion(null)).thenThrow(new RuntimeException("No chat"));
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of(Post.builder().id(1L).build()));

        campaignService.generatePostsForExistingCampaign(1L);

        verify(aiContentService).generatePostsWithCampaign(eq("AI"), eq(campaign), eq("Great insights from data"), eq("Focus on engagement and clarity"));
    }

    @Test
    void getCampaignsWithProgress_empty_returnsZeroPublished() {
        Campaign campaign = Campaign.builder().id(1L).name("Empty").topic("AI")
                .createdAt(LocalDateTime.now()).build();
        when(campaignRepository.findAll()).thenReturn(List.of(campaign));
        when(postRepository.findByCampaignId(1L)).thenReturn(List.of());

        List<CampaignProgressDTO> result = campaignService.getCampaignsWithProgress(5);

        assertThat(result.get(0).getTotalPosts()).isZero();
        assertThat(result.get(0).getPublishedPosts()).isZero();
    }

    @Test
    void getCampaignsWithProgress_noCreatedAt_returnsDraftStatus() {
        Campaign campaign = Campaign.builder().id(1L).name("Draft").topic("AI")
                .createdAt(null).build();
        when(campaignRepository.findAll()).thenReturn(List.of(campaign));
        when(postRepository.findByCampaignId(1L)).thenReturn(List.of());

        List<CampaignProgressDTO> result = campaignService.getCampaignsWithProgress(5);

        assertThat(result.get(0).getStatus()).isEqualTo("Draft");
    }

    @Test
    void getCampaignsWithProgress_respectsLimit() {
        Campaign c1 = Campaign.builder().id(1L).name("A").createdAt(LocalDateTime.now()).build();
        Campaign c2 = Campaign.builder().id(2L).name("B").createdAt(LocalDateTime.now()).build();
        Campaign c3 = Campaign.builder().id(3L).name("C").createdAt(LocalDateTime.now()).build();
        when(campaignRepository.findAll()).thenReturn(List.of(c1, c2, c3));

        List<CampaignProgressDTO> result = campaignService.getCampaignsWithProgress(2);

        assertThat(result).hasSize(2);
    }

    @Test
    void createCampaignAndGeneratePosts_doesNotCallChatWhenNoConversationId() {
        CreateCampaignRequest request = createCampaignReq("Test", "AI");
        Campaign savedCampaign = Campaign.builder().id(1L).build();
        when(campaignRepository.save(any())).thenReturn(savedCampaign);
        when(insightService.generateCampaignInsights(1L)).thenThrow(new RuntimeException("No insights"));
        when(aiContentService.generatePostsWithCampaign(anyString(), any(), anyString(), anyString()))
                .thenReturn(List.of());

        campaignService.createCampaignAndGeneratePosts(request);

        verify(chatService, never()).generateConclusion(any());
    }
}
