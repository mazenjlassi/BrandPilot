package com.example.metatry.integrationTest.Repositories;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ContentPatternRepositoryTest {

    @Autowired private ContentPatternRepository contentPatternRepository;

    @BeforeEach
    void setUp() {
        contentPatternRepository.deleteAll();
        contentPatternRepository.save(ContentPattern.builder()
                .topic("AI Technology").companyName("Acme")
                .platformBreakdown("{\"LINKEDIN\":4}").build());
    }

    @Test
    void findByTopic() {
        assertThat(contentPatternRepository.findByTopic("AI Technology")).isPresent();
    }

    @Test
    void findByTopicContainingIgnoreCase() {
        assertThat(contentPatternRepository.findByTopicContainingIgnoreCase("ai")).isNotEmpty();
    }

    @Test
    void findByCompanyName() {
        assertThat(contentPatternRepository.findByCompanyName("Acme")).hasSize(1);
    }

    @Test
    void findTop3ByOrderByExtractedAtDesc() {
        assertThat(contentPatternRepository.findTop3ByOrderByExtractedAtDesc()).isNotEmpty();
    }
}
