package com.example.metatry.DTOs;

public class FacebookPostRequest {
    private String message;
    private String pageId;
    private String accessToken;


    public FacebookPostRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}