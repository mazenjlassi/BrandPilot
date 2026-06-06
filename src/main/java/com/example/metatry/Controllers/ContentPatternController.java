package com.example.metatry.Controllers;

import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Services.ContentPatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patterns/crud")
@RequiredArgsConstructor
public class ContentPatternController {

    private final ContentPatternService contentPatternService;

    @GetMapping
    public List<ContentPattern> getAll() {
        return contentPatternService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentPattern> getById(@PathVariable Long id) {
        ContentPattern pattern = contentPatternService.getById(id);
        if (pattern == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pattern);
    }

    @PostMapping
    public ResponseEntity<ContentPattern> create(@RequestBody ContentPattern pattern) {
        return ResponseEntity.ok(contentPatternService.save(pattern));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentPattern> update(@PathVariable Long id, @RequestBody ContentPattern pattern) {
        ContentPattern updated = contentPatternService.update(id, pattern);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentPatternService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam String topic) {
        return ResponseEntity.ok(contentPatternService.exists(topic));
    }
}