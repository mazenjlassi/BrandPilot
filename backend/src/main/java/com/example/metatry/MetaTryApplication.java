package com.example.metatry;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
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
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            String trace = sw.toString();
            System.out.println("=== METATRY CRASH ===");
            System.out.println(trace);
            System.out.flush();
            try (PrintWriter fw = new PrintWriter(new FileWriter("/tmp/metatry-crash.log"))) {
                fw.println("=== CRASH at " + LocalDateTime.now() + " ===");
                fw.print(trace);
                fw.flush();
            } catch (Exception e) {
                System.err.println("Failed to write crash log: " + e);
            }
            throw t;
        }
    }

}
