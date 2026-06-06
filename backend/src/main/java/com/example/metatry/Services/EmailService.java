package com.example.metatry.Services;

import com.example.metatry.Models.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPostPublishedEmail(Post post) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo("mazenjl323@gmail.com");
        message.setSubject("Post Published ✅");
        message.setText(
                "Your post has been published successfully.\n\n" +
                        "Content:\n" + post.getContent() + "\n\n" +
                        "Published at: " + post.getPublishedAt()
        );

        mailSender.send(message);
    }
}