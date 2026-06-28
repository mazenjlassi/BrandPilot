package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.Conversation;
import com.example.metatry.Models.MarketingInsight;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.ConversationRepository;
import com.example.metatry.Repositories.MarketingInsightRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.MemoryContextService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class MemoryContextServiceIntegrationTest {

    @Autowired
    private MemoryContextService memoryContextService;

    @Autowired
    private ContentPatternRepository contentPatternRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MarketingInsightRepository marketingInsightRepository;

    @AfterEach
    void tearDown() {
        marketingInsightRepository.deleteAll();
        conversationRepository.deleteAll();
        postRepository.deleteAll();
        contentPatternRepository.deleteAll();
    }

    @Test
    void getRecentContext_withAllData_includesSections() {
        contentPatternRepository.save(ContentPattern.builder()
                .topic("AI").campaignName("Launch").tone("Technical").build());
        postRepository.save(Post.builder()
                .title("Test Post").content("Content")
                .platform(com.example.metatry.Enums.PlatformType.LINKEDIN).build());

        String context = memoryContextService.getRecentContext();

        assertThat(context).contains("CONTENT PATTERNS");
        assertThat(context).contains("RECENTLY GENERATED POSTS");
        assertThat(context).contains("Test Post");
        assertThat(context).contains("Launch");
    }

    @Test
    void getRecentContext_withChatConclusions_includesSection() {
        Conversation c = new Conversation();
        c.setTitle("Strategy");
        c.setConclusion("Focus on engagement");
        conversationRepository.save(c);

        String context = memoryContextService.getRecentContext();

        assertThat(context).contains("CHAT CONCLUSIONS");
        assertThat(context).contains("Focus on engagement");
    }

    @Test
    void getRecentContext_withMarketingInsights_includesSection() {
        marketingInsightRepository.save(MarketingInsight.builder()
                .platform("linkedin").description("Users want short content")
                .build());

        String context = memoryContextService.getRecentContext();

        assertThat(context).contains("MARKETING INSIGHTS");
        assertThat(context).contains("Users want short content");
    }

    @Test
    void getRecentContext_withNoData_returnsEmptySections() {
        String context = memoryContextService.getRecentContext();

        assertThat(context).contains("RECENT CONTEXT");
        assertThat(context).doesNotContain("CONTENT PATTERNS");
        assertThat(context).doesNotContain("RECENTLY GENERATED POSTS");
    }

    @Test
    void getMatchingContext_exactTopicMatch_returnsPattern() {
        contentPatternRepository.save(ContentPattern.builder()
                .topic("AI").tone("Technical").avgEngagementScore(0.7).build());

        String context = memoryContextService.getMatchingContext("AI");

        assertThat(context).contains("AI");
        assertThat(context).contains("Technical");
        assertThat(context).contains("HIGH");
    }

    @Test
    void getMatchingContext_keywordMatch_findsByKeyword() {
        contentPatternRepository.save(ContentPattern.builder()
                .topic("Cloud Security").tone("Educational").build());

        String context = memoryContextService.getMatchingContext("Security");

        assertThat(context).contains("Cloud Security");
    }

    @Test
    void getMatchingContext_noMatch_returnsHeaderOnly() {
        String context = memoryContextService.getMatchingContext("nonexistent");

        assertThat(context).contains("MATCHING PATTERNS FOR TOPIC: nonexistent");
        assertThat(context).doesNotContain("- Campaign:");
    }
}
