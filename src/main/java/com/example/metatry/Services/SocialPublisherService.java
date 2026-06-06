package com.example.metatry.Services;

import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialPublisherService {

    private final InstagramService instagramService;
    private final FacebookService facebookService;
    private final LinkedInService linkedInService;
    private final EmailService emailService;
    private final PostRepository postRepository;
    public Post publishPost(Post post){

        if(!Boolean.TRUE.equals(post.getApproved())){
            throw new RuntimeException("Post must be approved before publishing");
        }

        String caption = buildCaption(post);
        PostImage image = selectBestImage(post);

        switch (post.getPlatform()) {
            case INSTAGRAM -> publishInstagram(post, image, caption);
            case FACEBOOK -> publishFacebook(post, image, caption);
            case LINKEDIN -> publishLinkedIn(post, image, caption);
        }

        // ✅ Set publish state
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());

        // ✅ EMAIL (SAFE)
        if (post.getPlatformPostId() != null
                && post.getStatus() == PostStatus.PUBLISHED
                && !post.isNotificationSent()) {

            try {
                emailService.sendPostPublishedEmail(post);
                post.setNotificationSent(true);
            } catch (Exception e) {
                System.out.println("❌ Email failed: " + e.getMessage());
            }
        }

        return postRepository.save(post);
    }

    private String buildCaption(Post post){
        String caption = post.getContent();
        
        if (post.getLink() != null && !post.getLink().isBlank()) {
            caption += "\n\n" + post.getLink();
        }
        
        if (post.getHashtags() != null && !post.getHashtags().isBlank()) {
            caption += "\n\n" + post.getHashtags();
        }
        
        return caption;
    }

    /**
     *  select  image for the platform
     */
    private PostImage selectBestImage(Post post){

        PostImage image = post.getImage();

        if(image == null){
            return null;
        }

        ImageSize preferredSize = switch(post.getPlatform()) {
            case INSTAGRAM -> ImageSize.SQUARE;
            case LINKEDIN, FACEBOOK -> ImageSize.LANDSCAPE;
        };


        return image;
    }

    /**
     * Instagram publishing (image required)
     */
    private void publishInstagram(Post post, PostImage image, String caption){

        if(image == null || image.getImageUrl() == null){
            throw new RuntimeException("Instagram requires an image to publish");
        }

        Map<String,Object> response =
                instagramService.postPhotoFromUrl(
                        image.getImageUrl(),
                        caption
                );

        if(Boolean.TRUE.equals(response.get("success"))){
            post.setPlatformPostId((String) response.get("mediaId"));
        }
    }

    /**
     * Facebook publishing
     */
    private void publishFacebook(Post post, PostImage image, String caption){

        Map<String,Object> response;

        if(image != null && image.getImageUrl() != null){
            response = facebookService.postPhotoFromUrl(
                    image.getImageUrl(),
                    caption
            );
        } else {
            response = facebookService.postText(caption);
        }

        //  Facebook returns "id"
        if(response != null && response.get("id") != null){
            post.setPlatformPostId((String) response.get("id"));
        }
    }

    /**
     * LinkedIn publishing
     */
    private void publishLinkedIn(Post post, PostImage image, String caption){

        Map<String,Object> response;

        if(image != null && image.getImageUrl() != null){

            response = linkedInService.postArticleWithImage(
                    caption,
                    image.getImageUrl(),
                    "AI Generated Post"
            );

        } else {

            response = linkedInService.postText(caption);

        }

        if(Boolean.TRUE.equals(response.get("success"))){
            post.setPlatformPostId((String) response.get("postId"));
        }
    }
}