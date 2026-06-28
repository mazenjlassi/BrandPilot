package com.example.metatry.unitTest.Services;
import com.example.metatry.Services.*;

import com.example.metatry.Models.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
    }

    @Test
    void sendPostPublishedEmail_sendsCorrectMessage() {
        Post post = Post.builder()
                .id(1L)
                .title("Test Post")
                .content("This is the post content")
                .publishedAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .build();

        emailService.sendPostPublishedEmail(post);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();

        assertThat(sent.getTo()).containsExactly("mazenjl323@gmail.com");
        assertThat(sent.getSubject()).contains("Post Published");
        assertThat(sent.getText()).contains("This is the post content");
        assertThat(sent.getText()).contains("2024-01-15T10:30");
    }
}
