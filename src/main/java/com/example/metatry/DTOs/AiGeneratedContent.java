package com.example.metatry.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedContent {

    //  TITLES
    private String linkedinTitle;
    private String instagramTitle;
    private String facebookTitle;
    private String twitterTitle;

    //  CONTENT
    private String linkedinPost;
    private String instagramPost;
    private String facebookPost;
    private String twitterPost;

    //  HASHTAGS
    private List<String> linkedinHashtags;
    private List<String> instagramHashtags;
    private List<String> facebookHashtags;
    private List<String> twitterHashtags;

    //  IMAGE
    private String imagePrompt;
}