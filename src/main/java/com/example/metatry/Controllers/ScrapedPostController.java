package com.example.metatry.Controllers;

import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Services.ScrapedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scraped-posts")
@RequiredArgsConstructor
public class ScrapedPostController {

    private final ScrapedPostService scrapedPostService;

    @GetMapping
    public List<ScrapedPost> getAll(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String topic
    ) {
        if (companyName != null && !companyName.isEmpty()) {
            return scrapedPostService.getByCompanyName(companyName);
        }
        if (platform != null && !platform.isEmpty()) {
            return scrapedPostService.getByPlatform(platform);
        }
        if (topic != null && !topic.isEmpty()) {
            return scrapedPostService.getByTopic(topic);
        }
        return scrapedPostService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScrapedPost> getById(@PathVariable Long id) {
        ScrapedPost post = scrapedPostService.getById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<ScrapedPost> create(@RequestBody ScrapedPost post) {
        return ResponseEntity.ok(scrapedPostService.save(post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScrapedPost> update(@PathVariable Long id, @RequestBody ScrapedPost post) {
        ScrapedPost existing = scrapedPostService.getById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        post.setId(id);
        return ResponseEntity.ok(scrapedPostService.save(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scrapedPostService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/companies")
    public List<String> getCompanies() {
        return scrapedPostService.getDistinctCompanies();
    }

    @GetMapping("/count")
    public Map<String, Long> getCount(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String platform
    ) {
        if (companyName != null && !companyName.isEmpty()) {
            return Map.of("count", scrapedPostService.countByCompany(companyName));
        }
        if (platform != null && !platform.isEmpty()) {
            return Map.of("count", scrapedPostService.countByPlatform(platform));
        }
        return Map.of("count", (long) scrapedPostService.getAll().size());
    }
}