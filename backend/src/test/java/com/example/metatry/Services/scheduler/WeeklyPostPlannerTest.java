package com.example.metatry.Services.scheduler;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.prompts.WeeklyPostPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyPostPlannerTest {

    @Mock private PostRepository postRepository;
    @Mock private GeminiService geminiService;
    @Mock private WeeklyPostPromptBuilder promptBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WeeklyPostPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new WeeklyPostPlanner(postRepository, geminiService, promptBuilder, objectMapper);
    }

    @Test
    void generateWeeklyPosts_createsPostsWithSequentialCampaigns() {
        MarketingStrategy strategy = MarketingStrategy.builder().id(1L).title("S1").build();
        Campaign c1 = Campaign.builder().id(10L).name("Camp1").topic("Topic1").build();
        Campaign c2 = Campaign.builder().id(20L).name("Camp2").topic("Topic2").build();

        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt");

        String aiResponse = """
                {"posts":[
                  {"title":"P1","content":"C1","hashtags":["#ai"],"platform":"LINKEDIN","scheduledDay":"2026-07-27","scheduledHour":9,"needsImage":false,"permanent":false,"link":"https://3lm-solutions2.odoo.com/contactus"},
                  {"title":"P2","content":"C2","hashtags":["#tech"],"platform":"FACEBOOK","scheduledDay":"2026-07-28","scheduledHour":10,"needsImage":true,"permanent":false,"link":"https://3lm-solutions2.odoo.com/contactus"},
                  {"title":"P3","content":"C3","hashtags":["#insta"],"platform":"INSTAGRAM","scheduledDay":"2026-07-29","scheduledHour":11,"needsImage":false,"permanent":false,"link":"https://3lm-solutions2.odoo.com/contactus"}
                ]}
                """;
        when(geminiService.generate("prompt")).thenReturn(aiResponse);

        when(postRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<Post> posts = planner.generateWeeklyPosts(strategy, List.of(c1, c2));

        assertThat(posts).hasSize(3);
        assertThat(posts.get(0).getTitle()).isEqualTo("P1");
        assertThat(posts.get(0).getCampaign()).isEqualTo(c1);
        assertThat(posts.get(0).getNeedsImage()).isFalse();
        assertThat(posts.get(1).getTitle()).isEqualTo("P2");
        assertThat(posts.get(1).getCampaign()).isEqualTo(c1);
        assertThat(posts.get(1).getNeedsImage()).isTrue();
        assertThat(posts.get(2).getTitle()).isEqualTo("P3");
        assertThat(posts.get(2).getCampaign()).isEqualTo(c2);
        assertThat(posts.get(2).getNeedsImage()).isTrue();

        assertThat(posts.get(0).getApproved()).isFalse();
        assertThat(posts.get(0).getStatus()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    void generateWeeklyPosts_whenAiReturnsEmpty_returnsEmpty() {
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt");
        when(geminiService.generate("prompt")).thenReturn("{\"posts\":[]}");

        List<Post> posts = planner.generateWeeklyPosts(
                MarketingStrategy.builder().build(), List.of());

        assertThat(posts).isEmpty();
    }

    @Test
    void generateWeeklyPosts_whenAiReturnsNoPostsKey_returnsEmpty() {
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt");
        when(geminiService.generate("prompt")).thenReturn("{}");

        List<Post> posts = planner.generateWeeklyPosts(
                MarketingStrategy.builder().build(), List.of());

        assertThat(posts).isEmpty();
    }

    @Test
    void generateWeeklyPosts_skipsInvalidPlatform() {
        when(postRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn("prompt");
        when(geminiService.generate("prompt")).thenReturn("""
                {"posts":[
                  {"title":"P1","content":"C1","hashtags":["#ai"],"platform":"INVALID","scheduledDay":"2026-07-27","scheduledHour":9,"needsImage":false}
                ]}
                """);

        when(postRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<Post> posts = planner.generateWeeklyPosts(
                MarketingStrategy.builder().build(), List.of());

        assertThat(posts).isEmpty();
    }

}
