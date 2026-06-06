package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.*;
import com.example.metatry.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemoryContextServiceTest {

    @Mock
    private ContentPatternRepository contentPatternRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MarketingInsightRepository marketingInsightRepository;

    private MemoryContextService memoryContextService;

    @BeforeEach
    void setUp() {
        memoryContextService = new MemoryContextService(
                contentPatternRepository, postRepository,
                conversationRepository, marketingInsightRepository);
    }

    @Test
    void getRecentContext_whenAllEmpty_returnsMinimal() {
        when(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).thenReturn(List.of());
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc()).thenReturn(List.of());
        when(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).thenReturn(List.of());

        String result = memoryContextService.getRecentContext();

        assertThat(result).contains("RECENT CONTEXT");
        assertThat(result).doesNotContain("CONTENT PATTERNS");
        assertThat(result).doesNotContain("RECENTLY GENERATED POSTS");
    }

    @Test
    void getRecentContext_includesPatterns() {
        ContentPattern pattern = ContentPattern.builder()
                .id(1L).topic("AI Marketing").tone("Professional")
                .contentLength("Medium").ctaStyle("Question")
                .platformBreakdown("60% LinkedIn, 40% Instagram")
                .build();
        when(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).thenReturn(List.of(pattern));
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc()).thenReturn(List.of());
        when(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).thenReturn(List.of());

        String result = memoryContextService.getRecentContext();

        assertThat(result).contains("CONTENT PATTERNS");
        assertThat(result).contains("AI Marketing");
        assertThat(result).contains("Professional");
        assertThat(result).contains("Question");
    }

    @Test
    void getRecentContext_includesRecentPosts() {
        Post post = Post.builder()
                .id(1L).title("My Post").platform(PlatformType.LINKEDIN)
                .content("This is a test post content that is long enough to be truncated")
                .build();
        when(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).thenReturn(List.of());
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of(post));
        when(conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc()).thenReturn(List.of());
        when(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).thenReturn(List.of());

        String result = memoryContextService.getRecentContext();

        assertThat(result).contains("RECENTLY GENERATED POSTS");
        assertThat(result).contains("[LINKEDIN]");
        assertThat(result).contains("\"My Post\"");
    }

    @Test
    void getRecentContext_truncatesLongPostContent() {
        Post post = Post.builder()
                .id(1L).title(null).platform(PlatformType.FACEBOOK)
                .content("A".repeat(100))
                .build();
        when(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).thenReturn(List.of());
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of(post));
        when(conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc()).thenReturn(List.of());
        when(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).thenReturn(List.of());

        String result = memoryContextService.getRecentContext();

        assertThat(result).contains("...");
    }

    @Test
    void getRecentContext_includesChatConclusions() {
        Conversation conv = Conversation.builder()
                .id(1L).conclusion("Focus on automation benefits")
                .build();
        when(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).thenReturn(List.of());
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc()).thenReturn(List.of(conv));
        when(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).thenReturn(List.of());

        String result = memoryContextService.getRecentContext();

        assertThat(result).contains("CHAT CONCLUSIONS");
        assertThat(result).contains("Focus on automation benefits");
    }

    @Test
    void getRecentContext_includesMarketingInsights() {
        MarketingInsight insight = MarketingInsight.builder()
                .platform("INSTAGRAM")
                .description("Users prefer short-form video content over static images for product demonstrations")
                .build();
        when(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).thenReturn(List.of());
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc()).thenReturn(List.of());
        when(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).thenReturn(List.of(insight));

        String result = memoryContextService.getRecentContext();

        assertThat(result).contains("MARKETING INSIGHTS");
        assertThat(result).contains("[INSTAGRAM]");
        assertThat(result).contains("short-form video");
    }

    @Test
    void getMatchingContext_whenExactTopicFound_returnsPattern() {
        ContentPattern pattern = ContentPattern.builder()
                .id(1L).topic("AI Marketing").tone("Professional")
                .contentLength("Medium").mediaType("Image")
                .hashtagCount("5-8").timingPattern("Morning")
                .ctaStyle("Question").platformBreakdown("60/40")
                .avgEngagementScore(0.65).totalPostsGenerated(100)
                .performanceAdvice("Use more storytelling")
                .campaignName("AI Campaign")
                .build();
        when(contentPatternRepository.findByTopic("AI Marketing")).thenReturn(Optional.of(pattern));

        String result = memoryContextService.getMatchingContext("AI Marketing");

        assertThat(result).contains("AI Marketing");
        assertThat(result).contains("AI Campaign");
        assertThat(result).contains("Professional");
        assertThat(result).contains("Medium");
        assertThat(result).contains("Image");
        assertThat(result).contains("5-8");
        assertThat(result).contains("Morning");
        assertThat(result).contains("Question");
        assertThat(result).contains("HIGH");
        assertThat(result).containsPattern("0[.,]65");
        assertThat(result).contains("100");
        assertThat(result).contains("Use more storytelling");
    }

    @Test
    void getMatchingContext_whenNoExactMatch_searchesByKeyword() {
        when(contentPatternRepository.findByTopic("AI Marketing Trends")).thenReturn(Optional.empty());
        ContentPattern keywordMatch = ContentPattern.builder()
                .id(2L).topic("AI Trends").tone("Casual").build();
        when(contentPatternRepository.findByTopicContainingIgnoreCase("Marketing")).thenReturn(List.of());
        when(contentPatternRepository.findByTopicContainingIgnoreCase("Trends")).thenReturn(List.of(keywordMatch));

        String result = memoryContextService.getMatchingContext("AI Marketing Trends");

        assertThat(result).contains("AI Trends");
        assertThat(result).contains("Casual");
    }

    @Test
    void getMatchingContext_skipsShortKeywords() {
        when(contentPatternRepository.findByTopic("AI IS New Tech")).thenReturn(Optional.empty());
        when(contentPatternRepository.findByTopicContainingIgnoreCase("New")).thenReturn(List.of());
        when(contentPatternRepository.findByTopicContainingIgnoreCase("Tech")).thenReturn(List.of(
                ContentPattern.builder().id(1L).topic("New Tech").build()
        ));

        String result = memoryContextService.getMatchingContext("AI IS New Tech");

        assertThat(result).contains("New Tech");
    }

    @Test
    void getMatchingContext_whenNoMatch_returnsHeaderOnly() {
        when(contentPatternRepository.findByTopic("xyz")).thenReturn(Optional.empty());

        String result = memoryContextService.getMatchingContext("xyz");

        assertThat(result).contains("MATCHING PATTERNS FOR TOPIC: xyz");
        assertThat(result).doesNotContain("Campaign:");
    }

    @Test
    void getRecentContext_truncatesLongInsightDescriptions() {
        MarketingInsight insight = MarketingInsight.builder()
                .platform("INSTAGRAM")
                .description("A".repeat(200))
                .build();
        when(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).thenReturn(List.of());
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(conversationRepository.findTop2ByConclusionIsNotNullOrderByCreatedAtDesc()).thenReturn(List.of());
        when(marketingInsightRepository.findTop2ByOrderByCreatedAtDesc()).thenReturn(List.of(insight));

        String result = memoryContextService.getRecentContext();

        assertThat(result).contains("...");
        assertThat(result.length()).isLessThan(800);
    }
}
