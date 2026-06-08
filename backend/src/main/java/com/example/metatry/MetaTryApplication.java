package com.example.metatry;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MetaTryApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(MetaTryApplication.class, args);
        } catch (Throwable t) {
            try (PrintWriter pw = new PrintWriter(new FileWriter("/tmp/metatry-crash.log", true))) {
                pw.println("=== CRASH at " + LocalDateTime.now() + " ===");
                t.printStackTrace(pw);
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw t;
        }
    }

}
