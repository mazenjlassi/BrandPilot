package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Repositories.ContentPatternRepository;
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
class ContentPatternServiceUnitTest {

    @Mock
    private ContentPatternRepository contentPatternRepository;

    private ContentPatternService contentPatternService;

    @BeforeEach
    void setUp() {
        contentPatternService = new ContentPatternService(contentPatternRepository);
    }

    @Test
    void getAll_returnsAllPatterns() {
        List<ContentPattern> expected = List.of(
                ContentPattern.builder().id(1L).topic("AI").build(),
                ContentPattern.builder().id(2L).topic("Cloud").build()
        );
        when(contentPatternRepository.findAll()).thenReturn(expected);

        List<ContentPattern> result = contentPatternService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTopic()).isEqualTo("AI");
        verify(contentPatternRepository).findAll();
    }

    @Test
    void getById_whenFound_returnsPattern() {
        ContentPattern pattern = ContentPattern.builder().id(1L).topic("AI").build();
        when(contentPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));

        ContentPattern result = contentPatternService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTopic()).isEqualTo("AI");
    }

    @Test
    void getById_whenNotFound_returnsNull() {
        when(contentPatternRepository.findById(99L)).thenReturn(Optional.empty());

        ContentPattern result = contentPatternService.getById(99L);

        assertThat(result).isNull();
    }

    @Test
    void getByTopic_returnsOptional() {
        ContentPattern pattern = ContentPattern.builder().id(1L).topic("AI").build();
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(pattern));

        Optional<ContentPattern> result = contentPatternService.getByTopic("AI");

        assertThat(result).isPresent();
        assertThat(result.get().getTopic()).isEqualTo("AI");
    }

    @Test
    void save_delegatesToRepository() {
        ContentPattern pattern = ContentPattern.builder().topic("New Pattern").build();
        when(contentPatternRepository.save(pattern)).thenReturn(pattern);

        ContentPattern result = contentPatternService.save(pattern);

        assertThat(result).isEqualTo(pattern);
        verify(contentPatternRepository).save(pattern);
    }

    @Test
    void delete_delegatesToRepository() {
        contentPatternService.delete(1L);
        verify(contentPatternRepository).deleteById(1L);
    }

    @Test
    void exists_whenFound_returnsTrue() {
        when(contentPatternRepository.findByTopic("AI")).thenReturn(Optional.of(ContentPattern.builder().build()));
        assertThat(contentPatternService.exists("AI")).isTrue();
    }

    @Test
    void exists_whenNotFound_returnsFalse() {
        when(contentPatternRepository.findByTopic("Unknown")).thenReturn(Optional.empty());
        assertThat(contentPatternService.exists("Unknown")).isFalse();
    }

    @Test
    void update_whenFound_updatesAndReturns() {
        ContentPattern existing = ContentPattern.builder().id(1L).topic("AI").tone("Old Tone").build();
        ContentPattern updated = ContentPattern.builder()
                .postFrequency("Daily")
                .contentLength("Long")
                .mediaType("Video")
                .hashtagCount("10")
                .timingPattern("Evening")
                .tone("New Tone")
                .ctaStyle("Share")
                .build();
        when(contentPatternRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(contentPatternRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ContentPattern result = contentPatternService.update(1L, updated);

        assertThat(result).isNotNull();
        assertThat(result.getTone()).isEqualTo("New Tone");
        assertThat(result.getPostFrequency()).isEqualTo("Daily");
        assertThat(result.getContentLength()).isEqualTo("Long");
        verify(contentPatternRepository).save(existing);
    }

    @Test
    void update_whenNotFound_returnsNull() {
        when(contentPatternRepository.findById(99L)).thenReturn(Optional.empty());
        ContentPattern result = contentPatternService.update(99L, ContentPattern.builder().build());
        assertThat(result).isNull();
        verify(contentPatternRepository, never()).save(any());
    }
}
