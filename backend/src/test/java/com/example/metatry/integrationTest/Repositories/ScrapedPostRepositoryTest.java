package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ScrapedPostRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ScrapedPostRepositoryTest {

    @Autowired private ScrapedPostRepository scrapedPostRepository;

    @BeforeEach
    void setUp() {
        scrapedPostRepository.deleteAll();
        scrapedPostRepository.save(ScrapedPost.builder()
                .companyName("Acme").platform("LINKEDIN")
                .postText("Great post!").postUrl("https://li.com/p1")
                .topic("AI").usedForPattern(false).build());
    }

    @Test
    void findByCompanyNameAndPlatformAndPostUrl() {
        assertThat(scrapedPostRepository.findByCompanyNameAndPlatformAndPostUrl(
                "Acme", "LINKEDIN", "https://li.com/p1")).isPresent();
    }

    @Test
    void findByCompanyNameAndPlatformAndPostText() {
        assertThat(scrapedPostRepository.findByCompanyNameAndPlatformAndPostText(
                "Acme", "LINKEDIN", "Great post!")).isPresent();
    }

    @Test
    void findByCompanyName() {
        assertThat(scrapedPostRepository.findByCompanyName("Acme")).hasSize(1);
    }

    @Test
    void findByPlatform() {
        assertThat(scrapedPostRepository.findByPlatform("LINKEDIN")).hasSize(1);
    }

    @Test
    void findByTopic() {
        assertThat(scrapedPostRepository.findByTopic("AI")).hasSize(1);
    }

    @Test
    void findByCompanyNameAndPlatform() {
        assertThat(scrapedPostRepository.findByCompanyNameAndPlatform("Acme", "LINKEDIN")).hasSize(1);
    }

    @Test
    void findByUsedForPatternFalse() {
        assertThat(scrapedPostRepository.findByUsedForPatternFalse()).isNotEmpty();
    }

    @Test
    void countByUsedForPatternFalse() {
        assertThat(scrapedPostRepository.countByUsedForPatternFalse()).isPositive();
    }

    @Test
    void countByCompanyName() {
        assertThat(scrapedPostRepository.countByCompanyName("Acme")).isPositive();
    }

    @Test
    void findDistinctCompanyNames() {
        assertThat(scrapedPostRepository.findDistinctCompanyNames()).contains("Acme");
    }
}
