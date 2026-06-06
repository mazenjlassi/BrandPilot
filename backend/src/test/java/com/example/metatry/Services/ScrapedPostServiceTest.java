package com.example.metatry.Services;

import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ScrapedPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScrapedPostServiceTest {

    @Mock
    private ScrapedPostRepository scrapedPostRepository;

    private ScrapedPostService scrapedPostService;

    @BeforeEach
    void setUp() {
        scrapedPostService = new ScrapedPostService(scrapedPostRepository);
    }

    @Test
    void getAll_returnsAllPosts() {
        when(scrapedPostRepository.findAll()).thenReturn(List.of(
                ScrapedPost.builder().id(1L).build(),
                ScrapedPost.builder().id(2L).build()
        ));
        assertThat(scrapedPostService.getAll()).hasSize(2);
    }

    @Test
    void getById_whenFound_returnsPost() {
        ScrapedPost post = ScrapedPost.builder().id(1L).companyName("Acme").build();
        when(scrapedPostRepository.findById(1L)).thenReturn(Optional.of(post));
        assertThat(scrapedPostService.getById(1L).getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void getById_whenNotFound_returnsNull() {
        when(scrapedPostRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(scrapedPostService.getById(99L)).isNull();
    }

    @Test
    void getByCompanyName_delegatesToRepository() {
        scrapedPostService.getByCompanyName("Acme");
        verify(scrapedPostRepository).findByCompanyName("Acme");
    }

    @Test
    void getByPlatform_delegatesToRepository() {
        scrapedPostService.getByPlatform("linkedin");
        verify(scrapedPostRepository).findByPlatform("linkedin");
    }

    @Test
    void getByTopic_delegatesToRepository() {
        scrapedPostService.getByTopic("AI");
        verify(scrapedPostRepository).findByTopic("AI");
    }

    @Test
    void save_whenNoDuplicate_savesAndReturns() {
        ScrapedPost post = ScrapedPost.builder()
                .companyName("Acme")
                .platform("linkedin")
                .postUrl("https://linkedin.com/posts/123")
                .postText("Hello world")
                .build();
        when(scrapedPostRepository.findByCompanyNameAndPlatformAndPostUrl(
                "Acme", "linkedin", "https://linkedin.com/posts/123"))
                .thenReturn(Optional.empty());
        when(scrapedPostRepository.findByCompanyNameAndPlatform("Acme", "linkedin"))
                .thenReturn(List.of());
        when(scrapedPostRepository.save(post)).thenReturn(post);

        ScrapedPost result = scrapedPostService.save(post);
        assertThat(result).isEqualTo(post);
        verify(scrapedPostRepository).save(post);
    }

    @Test
    void save_whenDuplicateByUrl_returnsExisting() {
        ScrapedPost existing = ScrapedPost.builder().id(1L).companyName("Acme").build();
        ScrapedPost post = ScrapedPost.builder()
                .companyName("Acme")
                .platform("linkedin")
                .postUrl("https://linkedin.com/posts/123")
                .build();
        when(scrapedPostRepository.findByCompanyNameAndPlatformAndPostUrl(
                "Acme", "linkedin", "https://linkedin.com/posts/123"))
                .thenReturn(Optional.of(existing));

        ScrapedPost result = scrapedPostService.save(post);
        assertThat(result).isEqualTo(existing);
        verify(scrapedPostRepository, never()).save(post);
    }

    @Test
    void save_whenDuplicateByText_returnsExisting() {
        ScrapedPost existing = ScrapedPost.builder().id(1L).companyName("Acme")
                .platform("linkedin").postText("Hello world").build();
        ScrapedPost post = ScrapedPost.builder()
                .companyName("Acme")
                .platform("linkedin")
                .postText("Hello world")
                .build();
        when(scrapedPostRepository.findByCompanyNameAndPlatform("Acme", "linkedin"))
                .thenReturn(List.of(existing));

        ScrapedPost result = scrapedPostService.save(post);
        assertThat(result).isEqualTo(existing);
        verify(scrapedPostRepository, never()).save(post);
    }

    @Test
    void save_ignoresQueryParametersInUrl() {
        ScrapedPost existing = ScrapedPost.builder().id(1L).companyName("Acme")
                .platform("instagram").build();
        ScrapedPost post = ScrapedPost.builder()
                .companyName("Acme")
                .platform("instagram")
                .postUrl("https://instagram.com/p/ABC123/?utm_source=share")
                .build();
        when(scrapedPostRepository.findByCompanyNameAndPlatformAndPostUrl(
                "Acme", "instagram", "https://instagram.com/p/ABC123/"))
                .thenReturn(Optional.of(existing));

        ScrapedPost result = scrapedPostService.save(post);
        assertThat(result).isEqualTo(existing);
    }

    @Test
    void save_whenUrlNotUniquePost_skipsUrlCheck() {
        ScrapedPost post = ScrapedPost.builder()
                .companyName("Acme")
                .platform("linkedin")
                .postUrl("https://example.com/page")
                .postText("Unique text")
                .build();
        when(scrapedPostRepository.findByCompanyNameAndPlatform("Acme", "linkedin"))
                .thenReturn(List.of());
        when(scrapedPostRepository.save(post)).thenReturn(post);

        scrapedPostService.save(post);
        verify(scrapedPostRepository, never()).findByCompanyNameAndPlatformAndPostUrl(any(), any(), any());
    }

    @Test
    void removeDuplicates_delegatesToRepository() {
        when(scrapedPostRepository.deleteDuplicates()).thenReturn(5);
        assertThat(scrapedPostService.removeDuplicates()).isEqualTo(5);
    }

    @Test
    void delete_delegatesToRepository() {
        scrapedPostService.delete(1L);
        verify(scrapedPostRepository).deleteById(1L);
    }

    @Test
    void countByCompany_delegatesToRepository() {
        when(scrapedPostRepository.countByCompanyName("Acme")).thenReturn(10L);
        assertThat(scrapedPostService.countByCompany("Acme")).isEqualTo(10L);
    }

    @Test
    void countByPlatform_delegatesToRepository() {
        when(scrapedPostRepository.countByPlatform("linkedin")).thenReturn(5L);
        assertThat(scrapedPostService.countByPlatform("linkedin")).isEqualTo(5L);
    }

    @Test
    void getUnusedForPattern_delegatesToRepository() {
        scrapedPostService.getUnusedForPattern();
        verify(scrapedPostRepository).findByUsedForPatternFalse();
    }

    @Test
    void markAsUsedForPattern_whenFound_updatesAndSaves() {
        ScrapedPost post = ScrapedPost.builder().id(1L).usedForPattern(false).build();
        when(scrapedPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(scrapedPostRepository.save(post)).thenReturn(post);

        scrapedPostService.markAsUsedForPattern(1L);

        assertThat(post.getUsedForPattern()).isTrue();
        verify(scrapedPostRepository).save(post);
    }

    @Test
    void markAsUsedForPattern_whenNotFound_doesNothing() {
        when(scrapedPostRepository.findById(99L)).thenReturn(Optional.empty());
        scrapedPostService.markAsUsedForPattern(99L);
        verify(scrapedPostRepository, never()).save(any());
    }

    @Test
    void getDistinctCompanies_delegatesToRepository() {
        when(scrapedPostRepository.findDistinctCompanyNames()).thenReturn(List.of("Acme", "Beta"));
        assertThat(scrapedPostService.getDistinctCompanies()).hasSize(2);
    }
}
