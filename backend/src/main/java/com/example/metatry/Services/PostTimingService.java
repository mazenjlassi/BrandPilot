package com.example.metatry.Services;

import com.example.metatry.DTOs.TimingAnalysisDTO;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostTimingService {

    private final PostCommentRepository commentRepository;

    public TimingAnalysisDTO analyzeBestPostingTimes() {
        LocalDateTime since = LocalDateTime.now().minusDays(60);

        List<PostComment> facebookComments = commentRepository.findByPlatformAndCreatedAtAfter(
            PlatformType.FACEBOOK, since
        );

        List<PostComment> instagramComments = commentRepository.findByPlatformAndCreatedAtAfter(
            PlatformType.INSTAGRAM, since
        );

        Map<Integer, Integer> hourlyDistribution = new HashMap<>();
        Map<Integer, Integer> dailyDistribution = new HashMap<>();

        for (PostComment comment : facebookComments) {
            if (comment.getCreatedAt() != null) {
                int hour = comment.getCreatedAt().getHour();
                int day = comment.getCreatedAt().getDayOfWeek().getValue();

                hourlyDistribution.put(hour, hourlyDistribution.getOrDefault(hour, 0) + 1);
                dailyDistribution.put(day, dailyDistribution.getOrDefault(day, 0) + 1);
            }
        }

        for (PostComment comment : instagramComments) {
            if (comment.getCreatedAt() != null) {
                int hour = comment.getCreatedAt().getHour();
                int day = comment.getCreatedAt().getDayOfWeek().getValue();

                hourlyDistribution.put(hour, hourlyDistribution.getOrDefault(hour, 0) + 1);
                dailyDistribution.put(day, dailyDistribution.getOrDefault(day, 0) + 1);
            }
        }

        int facebookBestHour = findBestHour(facebookComments);
        int instagramBestHour = findBestHour(instagramComments);

        String facebookBestHourStr = formatHour(facebookBestHour);
        String instagramBestHourStr = formatHour(instagramBestHour);

        String recommendation = buildRecommendation(facebookBestHour, instagramBestHour);

        return TimingAnalysisDTO.builder()
                .facebookBestHour(facebookBestHourStr)
                .instagramBestHour(instagramBestHourStr)
                .facebookTotalComments(facebookComments.size())
                .instagramTotalComments(instagramComments.size())
                .hourlyDistribution(hourlyDistribution)
                .dailyDistribution(dailyDistribution)
                .recommendation(recommendation)
                .build();
    }

    private int findBestHour(List<PostComment> comments) {
        Map<Integer, Integer> hourCount = new HashMap<>();

        for (PostComment comment : comments) {
            if (comment.getCreatedAt() != null) {
                int hour = comment.getCreatedAt().getHour();
                hourCount.put(hour, hourCount.getOrDefault(hour, 0) + 1);
            }
        }

        return hourCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(12);
    }

    private String formatHour(int hour) {
        if (hour == 0) return "12:00 AM";
        if (hour == 12) return "12:00 PM";
        if (hour < 12) return hour + ":00 AM";
        return (hour - 12) + ":00 PM";
    }

    private String buildRecommendation(int facebookHour, int instagramHour) {
        return "Based on comment activity, post on Facebook at " + formatHour(facebookHour) +
               " and Instagram at " + formatHour(instagramHour) + " for maximum engagement.";
    }
}