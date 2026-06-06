package com.example.metatry.Services;

import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ScrapedPostRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrapedPostService {

    private final ScrapedPostRepository scrapedPostRepository;

    public List<ScrapedPost> getAll() {
        return scrapedPostRepository.findAll();
    }

    public ScrapedPost getById(Long id) {
        return scrapedPostRepository.findById(id).orElse(null);
    }

    public List<ScrapedPost> getByCompanyName(String companyName) {
        return scrapedPostRepository.findByCompanyName(companyName);
    }

    public List<ScrapedPost> getByPlatform(String platform) {
        return scrapedPostRepository.findByPlatform(platform);
    }

    public List<ScrapedPost> getByTopic(String topic) {
        return scrapedPostRepository.findByTopic(topic);
    }

    public ScrapedPost save(ScrapedPost post) {
        Optional<ScrapedPost> existing = findExistingDuplicate(post);
        if (existing.isPresent()) {
            return existing.get();
        }
        return scrapedPostRepository.save(post);
    }

    private Optional<ScrapedPost> findExistingDuplicate(ScrapedPost post) {
        String url = post.getPostUrl();
        String text = post.getPostText();

        if (url != null && !url.isEmpty()) {
            String baseUrl = url.split("\\?")[0];
            boolean isUniquePostUrl = baseUrl.contains("/p/")
                || baseUrl.contains("/posts/")
                || baseUrl.contains("/photo/")
                || baseUrl.contains("/reel/")
                || baseUrl.contains("/videos/")
                || baseUrl.contains("/story.php")
                || baseUrl.contains("/permalink.php");
            if (isUniquePostUrl) {
                Optional<ScrapedPost> byUrl = scrapedPostRepository
                    .findByCompanyNameAndPlatformAndPostUrl(
                        post.getCompanyName(), post.getPlatform(), baseUrl);
                if (byUrl.isPresent()) return byUrl;
            }
        }

        if (text != null && !text.isEmpty()) {
            String cleanText = text.trim().toLowerCase();
            List<ScrapedPost> candidates = scrapedPostRepository
                .findByCompanyNameAndPlatform(post.getCompanyName(), post.getPlatform());
            for (ScrapedPost candidate : candidates) {
                if (candidate.getPostText() != null
                    && candidate.getPostText().trim().toLowerCase().equals(cleanText)) {
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();
    }

    @Transactional
    public int removeDuplicates() {
        return scrapedPostRepository.deleteDuplicates();
    }

    public void delete(Long id) {
        scrapedPostRepository.deleteById(id);
    }

    public long countByCompany(String companyName) {
        return scrapedPostRepository.countByCompanyName(companyName);
    }

    public long countByPlatform(String platform) {
        return scrapedPostRepository.countByPlatform(platform);
    }

    public List<ScrapedPost> getUnusedForPattern() {
        return scrapedPostRepository.findByUsedForPatternFalse();
    }

    public void markAsUsedForPattern(Long id) {
        scrapedPostRepository.findById(id).ifPresent(post -> {
            post.setUsedForPattern(true);
            scrapedPostRepository.save(post);
        });
    }

    public List<String> getDistinctCompanies() {
        return scrapedPostRepository.findDistinctCompanyNames();
    }
}