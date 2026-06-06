package com.example.metatry.Controllers;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.DTOs.PatternResponse;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Services.PatternAnalysisService;
import com.example.metatry.Services.PerformanceFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternAnalysisService patternAnalysisService;
    private final PerformanceFeedbackService performanceFeedbackService;

    @PostMapping("/analyze")
    public ResponseEntity<PatternResponse> analyzePattern(@RequestBody PatternAnalysisRequest request) {
        if (request.getTopic() == null || request.getTopic().isBlank()) {
            return ResponseEntity.badRequest().body(
                PatternResponse.builder()
                    .status("error")
                    .message("topic is required")
                    .build()
            );
        }
        PatternResponse response = patternAnalysisService.analyzePattern(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze-batch")
    public ResponseEntity<String> analyzeBatch(@RequestParam String companyName) {
        int saved = patternAnalysisService.analyzeUnanalyzedBatch(companyName);
        return ResponseEntity.ok("Saved " + saved + " patterns from batch for " + companyName);
    }

    @GetMapping
    public ResponseEntity<List<ContentPattern>> getAllPatterns(
            @RequestParam(required = false) String companyName) {
        List<ContentPattern> patterns;
        if (companyName != null && !companyName.isEmpty()) {
            patterns = patternAnalysisService.getPatternsByCompany(companyName);
        } else {
            patterns = patternAnalysisService.getAllPatterns();
        }
        return ResponseEntity.ok(patterns);
    }

    @GetMapping("/{topic}")
    public ResponseEntity<ContentPattern> getPatternByTopic(@PathVariable String topic) {
        ContentPattern pattern = patternAnalysisService.getPatternByTopic(topic);
        if (pattern == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pattern);
    }

    @GetMapping("/match")
    public ResponseEntity<List<ContentPattern>> matchPattern(@RequestParam String topic) {
        List<ContentPattern> patterns = patternAnalysisService.findMatchingPatterns(topic);
        if (patterns.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patterns);
    }

    @GetMapping("/performance")
    public ResponseEntity<List<ContentPattern>> getPatternsByPerformance() {
        List<ContentPattern> patterns = performanceFeedbackService.getPatternsByPerformance();
        return ResponseEntity.ok(patterns);
    }

    @PostMapping("/feedback/run")
    public ResponseEntity<String> runFeedback() {
        performanceFeedbackService.updatePatternsFromPerformance();
        return ResponseEntity.ok("Performance feedback executed");
    }
}