package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.PostCommentRepository;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PostService postService;
    private final PostMetricRepository postMetricRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PerformanceFeedbackService performanceFeedbackService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${facebook.page-access-token}")
    private String token;

    @Value("${facebook.page-id}")
    private String pageId;

    @Value("${instagram.business-id}")
    private String instagramBusinessId;

    // ================= MAIN =================

    public void collectMetricsForPublishedPosts(){

        List<Post> posts = postService.getLastPublishedPosts(20);

        // Batch-fetch all Instagram media metrics in one call
        Map<String, int[]> igMetricsCache = fetchAllInstagramMetrics();

        for(Post post : posts){

            if(post.getPlatformPostId() == null) continue;

            try {

                switch (post.getPlatform()){

                    case FACEBOOK -> {
                        fetchFacebookMetrics(post);
                        fetchFacebookComments(post);
                    }

                    case INSTAGRAM -> {
                        fetchInstagramMetrics(post, igMetricsCache);
                        fetchInstagramComments(post);
                    }

                    case LINKEDIN -> { continue; }
                }

            } catch (Exception e){
                System.out.println("❌ Error fetching analytics for post " + post.getPlatformPostId());
                e.printStackTrace();
            }
        }

        performanceFeedbackService.updatePatternsFromPerformance();
    }

    // ================= FACEBOOK METRICS =================

    private void fetchFacebookMetrics(Post post){

        String postId = post.getPlatformPostId();

        if(postId != null && !postId.contains("_")){
            postId = pageId + "_" + postId;
        }

        String url = "https://graph.facebook.com/v19.0/" + postId +
                "?fields=reactions.summary(true),comments.summary(true),shares" +
                "&access_token=" + token;

        System.out.println("➡️ Facebook Metrics API: " + postId);

        try {

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if(response == null) return;

            int likes = extractReactions(response);
            int comments = extractComments(response);
            int shares = extractShares(response);

            saveAndUpdate(post, likes, comments, shares, 0);

        } catch (Exception e){
            System.out.println("❌ Facebook metrics failed: " + postId);
            e.printStackTrace();
        }
    }

    // ================= FACEBOOK COMMENTS (FINAL FIX) =================

    private void fetchFacebookComments(Post post){

        String postId = post.getPlatformPostId();

        System.out.println("➡️ Fetching comments for: " + postId);

        try {

            String url = "https://graph.facebook.com/v19.0/" + postId +
                    "/comments?fields=id,message,created_time&access_token=" + token;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            System.out.println("RAW FB COMMENTS RESPONSE: " + response);

            if(response == null || response.get("data") == null) return;

            List<Map<String, Object>> comments =
                    (List<Map<String, Object>>) response.get("data");

            System.out.println("COMMENTS SIZE: " + comments.size());

            for(Map<String, Object> c : comments){

                String commentId = (String) c.get("id");

                if(postCommentRepository.existsByExternalCommentId(commentId)){
                    continue;
                }

                String content = (String) c.get("message");
                if(content == null) continue;

                Map<String, Object> from = (Map<String, Object>) c.get("from");
                String author = from != null ? (String) from.get("name") : "Unknown";

                String createdTime = (String) c.get("created_time");

                PostComment comment = PostComment.builder()
                        .externalCommentId(commentId)
                        .commentText(content)
                        .authorName(author)
                        .createdAt(parseFacebookDate(createdTime))
                        .sentiment(analyzeSentiment(content))
                        .post(post)
                        .build();

                postCommentRepository.save(comment);
            }

            // ✅ Update from DB (REAL comments)
            long realComments = postCommentRepository.countByPostId(post.getId());
            post.setCommentsCount((int) realComments);

            postRepository.save(post);

        } catch (Exception e){
            System.out.println("❌ Failed to fetch comments for: " + postId);
            e.printStackTrace();
        }
    }

    // ================= INSTAGRAM =================

    private Map<String, int[]> fetchAllInstagramMetrics() {
        Map<String, int[]> cache = new HashMap<>();
        try {
            String url = "https://graph.facebook.com/v19.0/" + instagramBusinessId +
                    "/media?fields=id,like_count,comments_count&limit=100" +
                    "&access_token=" + token;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || response.get("data") == null) return cache;

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            for (Map<String, Object> item : data) {
                String id = (String) item.get("id");
                int likes = item.get("like_count") != null
                        ? ((Number) item.get("like_count")).intValue() : 0;
                int comments = item.get("comments_count") != null
                        ? ((Number) item.get("comments_count")).intValue() : 0;
                cache.put(id, new int[]{likes, comments});
            }

            System.out.println("✅ Batch-fetched " + cache.size() + " Instagram media metrics");
        } catch (Exception e) {
            System.out.println("❌ Failed to batch-fetch Instagram metrics: " + e.getMessage());
        }
        return cache;
    }

    private void fetchInstagramMetrics(Post post, Map<String, int[]> cache) {
        int[] metrics = cache.get(post.getPlatformPostId());
        if (metrics != null) {
            saveAndUpdate(post, metrics[0], metrics[1], 0, 0);
            return;
        }

        String url = "https://graph.facebook.com/v19.0/" + post.getPlatformPostId() +
                "?fields=like_count,comments_count" +
                "&access_token=" + token;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if(response == null) return;

        int likes = response.get("like_count") != null
                ? ((Number) response.get("like_count")).intValue()
                : 0;

        int comments = response.get("comments_count") != null
                ? ((Number) response.get("comments_count")).intValue()
                : 0;

        saveAndUpdate(post, likes, comments, 0, 0);
    }

    // ================= INSTAGRAM COMMENTS =================

    private void fetchInstagramComments(Post post) {
        String mediaId = post.getPlatformPostId();
        System.out.println("➡️ Fetching Instagram comments for: " + mediaId);

        try {
            String url = "https://graph.facebook.com/v19.0/" + mediaId +
                    "/comments?fields=id,text,timestamp,username&access_token=" + token;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            System.out.println("RAW IG COMMENTS RESPONSE: " + response);

            if (response == null || response.get("data") == null) return;

            List<Map<String, Object>> comments =
                    (List<Map<String, Object>>) response.get("data");

            System.out.println("IG COMMENTS SIZE: " + comments.size());

            for (Map<String, Object> c : comments) {
                String commentId = (String) c.get("id");
                if (postCommentRepository.existsByExternalCommentId(commentId)) {
                    continue;
                }

                String text = (String) c.get("text");
                if (text == null) continue;

                String username = (String) c.get("username");
                String author = username != null ? username : "Instagram User";

                String timestamp = (String) c.get("timestamp");

                PostComment comment = PostComment.builder()
                        .externalCommentId(commentId)
                        .commentText(text)
                        .authorName(author)
                        .createdAt(parseInstagramDate(timestamp))
                        .sentiment(analyzeSentiment(text))
                        .post(post)
                        .build();

                postCommentRepository.save(comment);
            }

            long realComments = postCommentRepository.countByPostId(post.getId());
            post.setCommentsCount((int) realComments);
            postRepository.save(post);

        } catch (Exception e) {
            System.out.println("❌ Failed to fetch Instagram comments for: " + mediaId);
            e.printStackTrace();
        }
    }

    private LocalDateTime parseInstagramDate(String date) {
        try {
            if (date == null) return LocalDateTime.now();
            return LocalDateTime.parse(date.substring(0, 19));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    // ================= EXTRACTORS =================

    private int extractReactions(Map<String, Object> response){
        try{
            Map<String, Object> reactions = (Map<String, Object>) response.get("reactions");
            Map<String, Object> summary = (Map<String, Object>) reactions.get("summary");
            return ((Number) summary.get("total_count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    private int extractComments(Map<String, Object> response){
        try{
            Map<String, Object> comments = (Map<String, Object>) response.get("comments");
            Map<String, Object> summary = (Map<String, Object>) comments.get("summary");
            return ((Number) summary.get("total_count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    private int extractShares(Map<String, Object> response){
        try{
            Map<String, Object> shares = (Map<String, Object>) response.get("shares");
            return ((Number) shares.get("count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    // ================= SAVE + UPDATE =================

    private void saveAndUpdate(Post post, int likes, int comments, int shares, int impressions){

        PostMetric metric = PostMetric.builder()
                .post(post)
                .likes(likes)
                .comments(comments)
                .shares(shares)
                .impressions(impressions)
                .collectedAt(LocalDateTime.now())
                .build();

        postMetricRepository.save(metric);

        int currentLikes = post.getLikes() != null ? post.getLikes() : 0;
        int currentComments = post.getCommentsCount() != null ? post.getCommentsCount() : 0;
        int currentShares = post.getShares() != null ? post.getShares() : 0;
        int currentImpressions = post.getImpressions() != null ? post.getImpressions() : 0;

        post.setLikes(Math.max(currentLikes, likes));
        post.setCommentsCount(Math.max(currentComments, comments));
        post.setShares(Math.max(currentShares, shares));
        post.setImpressions(Math.max(currentImpressions, impressions));

        int safeImpressions = post.getImpressions() > 0
                ? post.getImpressions()
                : (post.getLikes() + post.getCommentsCount() + post.getShares());

        double engagement = safeImpressions > 0
                ? (double)(post.getLikes() + post.getCommentsCount() * 2 + post.getShares() * 3)
                / safeImpressions
                : 0;

        post.setEngagementScore(engagement);

        postRepository.save(post);
    }

    // ================= HELPERS =================

    private LocalDateTime parseFacebookDate(String date){
        try {
            return LocalDateTime.parse(date.substring(0,19));
        } catch (Exception e){
            return LocalDateTime.now();
        }
    }

    private static final Set<String> POSITIVE_WORDS = Set.of(
        "good", "great", "love", "awesome", "amazing", "excellent", "fantastic",
        "wonderful", "beautiful", "perfect", "brilliant", "incredible", "superb",
        "nice", "happy", "delicious", "lovely", "outstanding", "phenomenal",
        "terrific", "fabulous", "marvelous", "splendid", "magnificent", "stunning",
        "gorgeous", "elegant", "charming", "delightful", "joy", "joyful", "glad",
        "pleased", "impressed", "breathtaking", "genius", "masterpiece",
        "legendary", "epic", "flawless", "remarkable", "extraordinary",
        "wow", "yay", "woohoo", "bravo", "best", "fun", "cool", "sweet",
        "fresh", "solid", "smooth", "clean", "neat", "tidy", "vibrant",
        "colorful", "bright", "shiny", "polished", "refined", "tasty"
    );

    private static final Set<String> NEGATIVE_WORDS = Set.of(
        "bad", "hate", "terrible", "awful", "horrible", "disgusting",
        "dreadful", "appalling", "worst", "sucks", "ugly", "nasty",
        "annoying", "disappointing", "frustrating", "boring", "stupid",
        "ridiculous", "pathetic", "shameful", "hideous", "repulsive",
        "garbage", "trash", "crap", "horrendous", "atrocious", "abysmal",
        "lousy", "mediocre", "poor", "wretched", "miserable", "painful",
        "useless", "hopeless", "hateful", "angry", "furious", "upset",
        "disgust", "gross", "fail", "sickening",
        "never", "avoid", "warning", "scam",
        "broke", "broken", "crash", "slow", "laggy", "buggy",
        "waste", "overpriced", "expensive", "cheap", "dirty",
        "silly", "dumb"
    );

    private static final Set<String> POSITIVE_EMOJIS = Set.of(
        "\uD83D\uDE0D",  // 😍
        "\u2764",        // ❤
        "\uD83D\uDC4D",  // 👍
        "\uD83D\uDD25",  // 🔥
        "\uD83D\uDCAF",  // 💯
        "\uD83C\uDF89",  // 🎉
        "\uD83D\uDE0A",  // 😊
        "\uD83D\uDE04",  // 😄
        "\uD83D\uDE4C",  // 🙌
        "\uD83D\uDCAA",  // 💪
        "\u2728",        // ✨
        "\uD83E\uDD70",  // 🥰
        "\uD83D\uDE01",  // 😁
        "\uD83D\uDE0E",  // 😎
        "\uD83E\uDD29",  // 🤩
        "\uD83D\uDC95",  // 💕
        "\uD83D\uDE18",  // 😘
        "\uD83C\uDF1F",  // 🌟
        "\uD83D\uDC4F",  // 👏
        "\uD83C\uDF8A",  // 🎊
        "\uD83D\uDE0B",  // 😋
        "\uD83D\uDE06",  // 😆
        "\uD83E\uDD73",  // 🥳
        "\uD83D\uDE0F",  // 😏
        "\uD83E\uDD1F",  // 🤟
        "\uD83E\uDD1D",  // 🤝
        "\uD83C\uDF40"   // 🍀
    );

    private static final Set<String> NEGATIVE_EMOJIS = Set.of(
        "\uD83D\uDE21",  // 😡
        "\uD83D\uDE20",  // 😠
        "\uD83D\uDC4E",  // 👎
        "\uD83E\uDD2C",  // 🤬
        "\uD83D\uDCA9",  // 💩
        "\uD83D\uDE24",  // 😤
        "\uD83D\uDE12",  // 😒
        "\uD83D\uDE44",  // 🙄
        "\uD83D\uDE29",  // 😩
        "\uD83D\uDE2D",  // 😭
        "\uD83D\uDE2B",  // 😫
        "\uD83D\uDD95",  // 🖕
        "\uD83D\uDE22",  // 😢
        "\uD83D\uDE1E",  // 😞
        "\uD83D\uDE15",  // 😕
        "\uD83D\uDE25",  // 😥
        "\uD83D\uDE31",  // 😱
        "\uD83D\uDE28",  // 😨
        "\uD83D\uDE30",  // 😰
        "\uD83D\uDE23",  // 😣
        "\uD83D\uDE2E",  // 😮
        "\uD83D\uDE26",  // 😦
        "\uD83E\uDD2E",  // 🤮
        "\uD83E\uDD2F",  // 🤯
        "\uD83E\uDD22",  // 🤢
        "\uD83D\uDC80",  // 💀
        "\u2622"         // ☢
    );

    private String analyzeSentiment(String content){
        String lower = content.toLowerCase();

        for (String word : POSITIVE_WORDS) {
            if (lower.contains(word)) return "POSITIVE";
        }
        for (String emoji : POSITIVE_EMOJIS) {
            if (content.contains(emoji)) return "POSITIVE";
        }

        for (String word : NEGATIVE_WORDS) {
            if (lower.contains(word)) return "NEGATIVE";
        }
        for (String emoji : NEGATIVE_EMOJIS) {
            if (content.contains(emoji)) return "NEGATIVE";
        }

        return "NEUTRAL";
    }
}