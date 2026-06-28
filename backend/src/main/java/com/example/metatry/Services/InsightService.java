package com.example.metatry.Services;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.aspectj.util.LangUtil.safeList;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AiInsightService aiInsightService;
    private final ObjectMapper objectMapper;

    // ================= POST INSIGHT (RULE BASED) =================

    public PostInsightDTO generatePostInsights(Long postId){

        List<PostComment> comments = commentRepository.findByPostId(postId);

        if(comments.isEmpty()){
            return emptyInsight();
        }

        return buildInsightFromComments(comments);
    }

    // ================= CAMPAIGN INSIGHT (AI) =================
    public PostInsightDTO generateCampaignInsights(Long campaignId){

        List<PostComment> comments = commentRepository.findByPostCampaignId(campaignId);
        List<Post> posts = postRepository.findByCampaignId(campaignId);

        int totalLikes = posts.stream().mapToInt(p -> p.getLikes() != null ? p.getLikes() : 0).sum();
        int totalComments = posts.stream().mapToInt(p -> p.getCommentsCount() != null ? p.getCommentsCount() : 0).sum();
        int reach = posts.stream().mapToInt(p -> p.getImpressions() != null ? p.getImpressions() : 0).sum();
        int postCount = posts.size();
        double engagementRate = postCount > 0 ? (double) (totalLikes + totalComments) / postCount : 0;

        if(comments.isEmpty()){
            return PostInsightDTO.builder()
                    .overallSentiment("NEUTRAL")
                    .positiveRatio(0)
                    .negativeRatio(0)
                    .neutralRatio(1)
                    .topComplaints(List.of())
                    .topPositives(List.of())
                    .summary("No comments available")
                    .advice("No data available yet")
                    .ideas(List.of("Start engaging your audience"))
                    .totalLikes(totalLikes)
                    .totalComments(totalComments)
                    .engagementRate(engagementRate)
                    .reach(reach)
                    .build();
        }

        // ✅ 1. CALCULATE RATIOS FROM DB
        int total = comments.size();

        long positive = comments.stream().filter(c -> "POSITIVE".equals(c.getSentiment())).count();
        long negative = comments.stream().filter(c -> "NEGATIVE".equals(c.getSentiment())).count();
        long neutral  = comments.stream().filter(c -> "NEUTRAL".equals(c.getSentiment())).count();

        double posRatio = (double) positive / total;
        double negRatio = (double) negative / total;
        double neuRatio = (double) neutral  / total;

        // ✅ 2. SEND TEXT TO AI
        List<String> texts = comments.stream()
                .map(PostComment::getCommentText)
                .limit(20)
                .toList();

        try {
            String aiResponse = aiInsightService.analyzeComments(texts);
            PostInsightDTO aiInsight = parseAiResponse(aiResponse);

            // ✅ 3. MERGE RESULTS
            aiInsight.setPositiveRatio(posRatio);
            aiInsight.setNegativeRatio(negRatio);
            aiInsight.setNeutralRatio(neuRatio);
            aiInsight.setTotalLikes(totalLikes);
            aiInsight.setTotalComments(totalComments);
            aiInsight.setEngagementRate(engagementRate);
            aiInsight.setReach(reach);

            return aiInsight;

        } catch (Exception e){
            PostInsightDTO fallback = buildInsightFromComments(comments);
            fallback.setTotalLikes(totalLikes);
            fallback.setTotalComments(totalComments);
            fallback.setEngagementRate(engagementRate);
            fallback.setReach(reach);
            return fallback;
        }
    }

    // ================= AI PARSER =================

    private List<String> safeList(Object value){

        if(value instanceof List<?> list){
            return list.stream()
                    .map(Object::toString)
                    .toList();
        }

        if(value instanceof String str){
            // 🔥 convert sentence → list
            return List.of(str);
        }

        return List.of();
    }

    private PostInsightDTO parseAiResponse(String json){

        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            return PostInsightDTO.builder()
                    .overallSentiment((String) map.getOrDefault("overallSentiment", "NEUTRAL"))
                    .topPositives(safeList(map.get("topPositives")))
                    .topComplaints(safeList(map.get("topComplaints")))
                    .topNeutral(safeList(map.get("topNeutral")))
                    .summary((String) map.getOrDefault("summary", ""))
                    .advice((String) map.getOrDefault("advice", ""))
                    .ideas(safeList(map.get("ideas")))
                    .build();

        } catch (Exception e){

            System.out.println("⚠️ JSON parsing failed:");
            System.out.println(json);

            return PostInsightDTO.builder()
                    .overallSentiment("NEUTRAL")
                    .summary("AI response parsing failed")
                    .advice("Try again")
                    .ideas(List.of("Retry"))
                    .build();
        }
    }

    // ================= RULE ENGINE =================

    private PostInsightDTO buildInsightFromComments(List<PostComment> comments){

        int total = comments.size();

        long positive = comments.stream().filter(c -> "POSITIVE".equals(c.getSentiment())).count();
        long negative = comments.stream().filter(c -> "NEGATIVE".equals(c.getSentiment())).count();
        long neutral  = comments.stream().filter(c -> "NEUTRAL".equals(c.getSentiment())).count();

        double posRatio = (double) positive / total;
        double negRatio = (double) negative / total;
        double neuRatio = (double) neutral  / total;

        String overall = getOverallSentiment(posRatio, negRatio, neuRatio);

        List<String> positives = extractKeywords(
                comments.stream()
                        .filter(c -> "POSITIVE".equals(c.getSentiment()))
                        .map(PostComment::getCommentText)
                        .toList()
        );

        List<String> negatives = extractKeywords(
                comments.stream()
                        .filter(c -> "NEGATIVE".equals(c.getSentiment()))
                        .map(PostComment::getCommentText)
                        .toList()
        );

        List<String> neutralKeywords = extractKeywords(
                comments.stream()
                        .filter(c -> "NEUTRAL".equals(c.getSentiment()))
                        .map(PostComment::getCommentText)
                        .toList()
        );

        String summary = buildSummary(overall, positives, negatives, neutralKeywords);
        String advice = generateAdvice(overall, positives, negatives, neutralKeywords);
        List<String> ideas = generateIdeas(overall, positives, negatives, neutralKeywords);

        return PostInsightDTO.builder()
                .overallSentiment(overall)
                .positiveRatio(posRatio)
                .negativeRatio(negRatio)
                .neutralRatio(neuRatio)
                .topPositives(positives)
                .topComplaints(negatives)
                .topNeutral(neutralKeywords)
                .summary(summary)
                .advice(advice)
                .ideas(ideas)
                .build();
    }

    // ================= HELPERS =================

    private String getOverallSentiment(double pos, double neg, double neu){
        if(pos > neg && pos > neu) return "POSITIVE";
        if(neg > pos && neg > neu) return "NEGATIVE";
        return "NEUTRAL";
    }

    private List<String> extractKeywords(List<String> texts){

        Map<String, Integer> freq = new HashMap<>();

        List<String> stopWords = List.of(
                "good","great","nice","cool","love","very","this","that","with","have",
                "just","like","really","think","want","know","make","time","people",
                "would","could","should","will","does","been","were","said","one",
                "get","got","way","thing","things","see","look","need","feel","still",
                "much","many","more","some","come","take","even","also","back","well"
        );

        for(String text : texts){
            if(text == null) continue;

            String[] words = text.toLowerCase().split("\\W+");

            for(String word : words){
                if(word.length() < 4 || stopWords.contains(word)) continue;

                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }

        return freq.entrySet().stream()
                .sorted((a,b) -> b.getValue() - a.getValue())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String buildSummary(String overall, List<String> positives, List<String> negatives, List<String> neutralKeywords){

        return "Overall sentiment is " + overall +
                ". Users like: " + positives +
                ". Complaints: " + negatives +
                ". Neutral mentions: " + neutralKeywords;
    }

    private String generateAdvice(String overall, List<String> positives, List<String> negatives, List<String> neutralKeywords){

        if("NEGATIVE".equals(overall)){
            return "Content needs improvement. Focus on clarity, shorter posts, and a more engaging tone.";
        }

        if("NEUTRAL".equals(overall)){
            if(!neutralKeywords.isEmpty()){
                return "Content is neutral. Consider adding more engaging elements. Neutral topics: " + neutralKeywords;
            }
            return "Content is average. Add stronger hooks, questions, or emotional triggers.";
        }

        if(!negatives.isEmpty()){
            return "Content performs well but can be improved. Address: " + negatives;
        }

        return "Content performs very well. Keep the same style and engagement strategy.";
    }

    private List<String> generateIdeas(String overall, List<String> positives, List<String> negatives, List<String> neutralKeywords){

        List<String> ideas = new ArrayList<>();

        if("NEGATIVE".equals(overall)){
            ideas.add("Use shorter sentences");
            ideas.add("Improve clarity");
            ideas.add("Make tone more engaging");
        }

        if("NEUTRAL".equals(overall)){
            ideas.add("Add questions");
            ideas.add("Use emojis");
            ideas.add("Try storytelling");
            if(!neutralKeywords.isEmpty()){
                ideas.add("Explore neutral topics: " + neutralKeywords);
            }
        }

        if("POSITIVE".equals(overall)){
            ideas.add("Reuse this content style");
            ideas.add("Create similar posts");
            ideas.add("Scale into campaign");
        }

        return ideas;
    }

    private PostInsightDTO emptyInsight(){
        return PostInsightDTO.builder()
                .overallSentiment("NEUTRAL")
                .positiveRatio(0)
                .negativeRatio(0)
                .neutralRatio(1)
                .topComplaints(List.of())
                .topPositives(List.of())
                .summary("No comments available")
                .advice("No data available yet")
                .ideas(List.of("Start engaging your audience"))
                .build();
    }
}