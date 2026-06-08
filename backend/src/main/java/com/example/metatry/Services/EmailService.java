package com.example.metatry.Services;

import com.example.metatry.Models.Post;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void sendPostPublishedEmail(Post post) {
        if (mailSender == null) return;
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
