package com.example.metatry.unitTest.DTOs;
import com.example.metatry.DTOs.*;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BuilderDtoUnitTest {

    @Test
    void postDto() {
        PostDto dto = PostDto.builder()
                .id(1L).title("Title").content("Content")
                .hashtags("#tag").platform("LINKEDIN")
                .scheduledAt(LocalDateTime.now())
                .campaignId(10L).campaignName("Campaign")
                .imageUrl("https://img.com/img.jpg")
                .permanent(true).status("DRAFT")
                .link("https://link.com")
                .likes(100).commentsCount(10).shares(5)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Title");
        assertThat(dto.getPlatform()).isEqualTo("LINKEDIN");
        assertThat(dto.getCampaignId()).isEqualTo(10L);
        assertThat(dto.getCampaignName()).isEqualTo("Campaign");
        assertThat(dto.isPermanent()).isTrue();
        assertThat(dto.getStatus()).isEqualTo("DRAFT");
        assertThat(dto.getLikes()).isEqualTo(100);
    }

    @Test
    void postSummaryDTO() {
        PostSummaryDTO dto = PostSummaryDTO.builder()
                .id(1L).title("Summary").platform("FACEBOOK")
                .status("PUBLISHED").likes(50).commentsCount(5).shares(2)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Summary");
        assertThat(dto.getPlatform()).isEqualTo("FACEBOOK");
        assertThat(dto.getStatus()).isEqualTo("PUBLISHED");
        assertThat(dto.getLikes()).isEqualTo(50);
    }

    @Test
    void campaignDTO() {
        CampaignDTO dto = CampaignDTO.builder()
                .id(1L).name("Campaign").topic("AI")
                .platform("LINKEDIN").status("ACTIVE").postCount(5)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Campaign");
        assertThat(dto.getTopic()).isEqualTo("AI");
        assertThat(dto.getPostCount()).isEqualTo(5);
    }

    @Test
    void campaignDTO_noArgsConstructor() {
        CampaignDTO dto = new CampaignDTO();
        dto.setName("Test");
        assertThat(dto.getName()).isEqualTo("Test");
    }

    @Test
    void campaignProgressDTO() {
        CampaignProgressDTO dto = CampaignProgressDTO.builder()
                .id(1L).name("Progress").topic("AI")
                .totalPosts(10).publishedPosts(6).status("IN_PROGRESS")
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTotalPosts()).isEqualTo(10);
        assertThat(dto.getPublishedPosts()).isEqualTo(6);
        assertThat(dto.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void patternResponse() {
        PatternResponse dto = PatternResponse.builder()
                .id(1L).topic("Tech").campaignName("Campaign")
                .platformBreakdown("{\"LINKEDIN\":4}")
                .postFrequency("daily").tone("professional")
                .totalPostsAnalyzed(50)
                .status("COMPLETED").message("Done")
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTopic()).isEqualTo("Tech");
        assertThat(dto.getPlatformBreakdown()).isEqualTo("{\"LINKEDIN\":4}");
        assertThat(dto.getTotalPostsAnalyzed()).isEqualTo(50);
        assertThat(dto.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void patternAnalysisRequest() {
        PatternAnalysisRequest dto = PatternAnalysisRequest.builder()
                .topic("AI").platform("LINKEDIN")
                .minPostsRequired(10).companyName("Acme")
                .build();

        assertThat(dto.getTopic()).isEqualTo("AI");
        assertThat(dto.getPlatform()).isEqualTo("LINKEDIN");
        assertThat(dto.getMinPostsRequired()).isEqualTo(10);
        assertThat(dto.getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void scrapeRequest() {
        ScrapeRequest dto = ScrapeRequest.builder()
                .companyName("Acme")
                .linkedin("https://linkedin.com/company/acme")
                .instagram("https://instagram.com/acme")
                .facebook("https://facebook.com/acme")
                .build();

        assertThat(dto.getCompanyName()).isEqualTo("Acme");
        assertThat(dto.getLinkedin()).isEqualTo("https://linkedin.com/company/acme");
        assertThat(dto.getInstagram()).isEqualTo("https://instagram.com/acme");
        assertThat(dto.getFacebook()).isEqualTo("https://facebook.com/acme");
    }

    @Test
    void scrapeResponse() {
        ScrapedPostDTO postDTO = ScrapedPostDTO.builder()
                .platform("LINKEDIN").postText("Text")
                .postedAt("2026-05-01").url("https://link.com")
                .build();

        ScrapeResponse dto = ScrapeResponse.builder()
                .companyName("Acme").totalPosts(1)
                .results(Map.of("LINKEDIN", List.of(postDTO)))
                .status("SUCCESS").message("Scraped")
                .build();

        assertThat(dto.getCompanyName()).isEqualTo("Acme");
        assertThat(dto.getTotalPosts()).isEqualTo(1);
        assertThat(dto.getResults()).containsKey("LINKEDIN");
        assertThat(dto.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void scrapedPostDTO() {
        ScrapedPostDTO dto = ScrapedPostDTO.builder()
                .platform("FACEBOOK").postText("Post")
                .postedAt("2026-05-01").url("https://fb.com/p")
                .build();

        assertThat(dto.getPlatform()).isEqualTo("FACEBOOK");
        assertThat(dto.getPostText()).isEqualTo("Post");
        assertThat(dto.getPostedAt()).isEqualTo("2026-05-01");
        assertThat(dto.getUrl()).isEqualTo("https://fb.com/p");
    }

    @Test
    void userStatsDTO() {
        UserStatsDTO dto = UserStatsDTO.builder()
                .totalUsers(100).totalMarketing(30)
                .activeUsers(80).bannedUsers(5)
                .build();

        assertThat(dto.getTotalUsers()).isEqualTo(100);
        assertThat(dto.getTotalMarketing()).isEqualTo(30);
        assertThat(dto.getActiveUsers()).isEqualTo(80);
        assertThat(dto.getBannedUsers()).isEqualTo(5);
    }

    @Test
    void weeklyComparisonDTO() {
        WeeklyComparisonDTO dto = WeeklyComparisonDTO.builder()
                .thisWeek(150).lastWeek(120)
                .percentage(25.0).increased(true)
                .build();

        assertThat(dto.getThisWeek()).isEqualTo(150);
        assertThat(dto.getLastWeek()).isEqualTo(120);
        assertThat(dto.getPercentage()).isEqualTo(25.0);
        assertThat(dto.isIncreased()).isTrue();
    }

    @Test
    void timingAnalysisDTO() {
        TimingAnalysisDTO dto = TimingAnalysisDTO.builder()
                .facebookBestHour("10:00").instagramBestHour("14:00")
                .facebookTotalComments(100).instagramTotalComments(50)
                .hourlyDistribution(Map.of(10, 20, 14, 15))
                .dailyDistribution(Map.of(1, 30, 2, 25))
                .recommendation("Post at 10 AM")
                .build();

        assertThat(dto.getFacebookBestHour()).isEqualTo("10:00");
        assertThat(dto.getInstagramBestHour()).isEqualTo("14:00");
        assertThat(dto.getFacebookTotalComments()).isEqualTo(100);
        assertThat(dto.getInstagramTotalComments()).isEqualTo(50);
        assertThat(dto.getHourlyDistribution()).containsEntry(10, 20);
        assertThat(dto.getRecommendation()).isEqualTo("Post at 10 AM");
    }

    @Test
    void calendarEventDTO() {
        CalendarEventDTO dto = CalendarEventDTO.builder()
                .id(1L).title("Event").content("Content")
                .scheduledAt(LocalDateTime.now())
                .status(PostStatus.SCHEDULED)
                .platform(PlatformType.LINKEDIN)
                .imageUrl("https://img.com/img.jpg")
                .campaignId(10L).campaignName("Campaign")
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Event");
        assertThat(dto.getStatus()).isEqualTo(PostStatus.SCHEDULED);
        assertThat(dto.getPlatform()).isEqualTo(PlatformType.LINKEDIN);
        assertThat(dto.getCampaignId()).isEqualTo(10L);
    }

    @Test
    void postInsightDTO() {
        PostInsightDTO dto = PostInsightDTO.builder()
                .overallSentiment("POSITIVE")
                .positiveRatio(0.7).negativeRatio(0.1).neutralRatio(0.2)
                .topComplaints(List.of("Too long"))
                .topPositives(List.of("Great content"))
                .summary("Good performance")
                .advice("Keep posting")
                .ideas(List.of("Video content"))
                .build();

        assertThat(dto.getOverallSentiment()).isEqualTo("POSITIVE");
        assertThat(dto.getPositiveRatio()).isEqualTo(0.7);
        assertThat(dto.getTopComplaints()).containsExactly("Too long");
        assertThat(dto.getSummary()).isEqualTo("Good performance");
        assertThat(dto.getAdvice()).isEqualTo("Keep posting");
    }

    @Test
    void messageDTO() {
        MessageDTO dto = MessageDTO.builder()
                .id(1L).role("USER")
                .content("Hello").timestamp(LocalDateTime.now())
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getRole()).isEqualTo("USER");
        assertThat(dto.getContent()).isEqualTo("Hello");
    }

    @Test
    void conversationDTO() {
        ConversationDTO dto = ConversationDTO.builder()
                .id(1L).title("Chat")
                .conclusion("Summary").createdAt(LocalDateTime.now())
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Chat");
        assertThat(dto.getConclusion()).isEqualTo("Summary");
    }

    @Test
    void postStatsResponse() {
        PostStatsResponse dto = new PostStatsResponse(
                100L, 50L, 30L, 20L, 40L, 30L, 30L
        );

        assertThat(dto.getTotalPosts()).isEqualTo(100);
        assertThat(dto.getPublishedPosts()).isEqualTo(50);
        assertThat(dto.getDraftPosts()).isEqualTo(30);
        assertThat(dto.getApprovedPosts()).isEqualTo(20);
        assertThat(dto.getFacebookPosts()).isEqualTo(40);
        assertThat(dto.getInstagramPosts()).isEqualTo(30);
        assertThat(dto.getLinkedinPosts()).isEqualTo(30);
    }
}
