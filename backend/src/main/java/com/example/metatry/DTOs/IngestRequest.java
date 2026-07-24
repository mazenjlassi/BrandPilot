package com.example.metatry.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class IngestRequest {
    private String companyName;
    private List<PlatformResult> results;

    @Data
    public static class PlatformResult {
        private String platform;
        private List<PostData> posts;
    }

    @Data
    public static class PostData {
        private String postText;
        private String postedAt;
        private String url;
    }
}
