package com.example.metatry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MetaTryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetaTryApplication.class, args);
    }

}
