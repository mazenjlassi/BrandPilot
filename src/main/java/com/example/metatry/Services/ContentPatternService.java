package com.example.metatry.Services;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Repositories.ContentPatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentPatternService {

    private final ContentPatternRepository contentPatternRepository;

    public List<ContentPattern> getAll() {
        return contentPatternRepository.findAll();
    }

    public ContentPattern getById(Long id) {
        return contentPatternRepository.findById(id).orElse(null);
    }

    public Optional<ContentPattern> getByTopic(String topic) {
        return contentPatternRepository.findByTopic(topic);
    }

    public ContentPattern save(ContentPattern pattern) {
        return contentPatternRepository.save(pattern);
    }

    public void delete(Long id) {
        contentPatternRepository.deleteById(id);
    }

    public boolean exists(String topic) {
        return contentPatternRepository.findByTopic(topic).isPresent();
    }

    public ContentPattern update(Long id, ContentPattern updated) {
        return contentPatternRepository.findById(id).map(existing -> {
            existing.setPostFrequency(updated.getPostFrequency());
            existing.setContentLength(updated.getContentLength());
            existing.setMediaType(updated.getMediaType());
            existing.setHashtagCount(updated.getHashtagCount());
            existing.setTimingPattern(updated.getTimingPattern());
            existing.setTone(updated.getTone());
            existing.setCtaStyle(updated.getCtaStyle());
            return contentPatternRepository.save(existing);
        }).orElse(null);
    }
}