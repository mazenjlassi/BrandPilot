package com.example.metatry.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class AiPostRequest {

    private String topic;

    private List<String> platforms;

    private Boolean generateImage;

    private Boolean generateVideo;
}