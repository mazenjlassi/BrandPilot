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
            StringBuilder sb = new StringBuilder();
            sb.append("=== CRASH at ").append(LocalDateTime.now()).append(" ===\n");
            sb.append("Exception class: ").append(t.getClass().getName()).append("\n");
            sb.append("Message: ").append(t.getMessage()).append("\n");
            sb.append("Cause: ");
            if (t.getCause() == null) {
                sb.append("null\n");
            } else {
                sb.append(t.getCause().getClass().getName())
                  .append(": ").append(t.getCause().getMessage()).append("\n");
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            sb.append("Full trace:\n").append(sw.toString());
            String result = sb.toString();
            System.out.println(result);
            System.out.flush();
            try (PrintWriter fw = new PrintWriter(new FileWriter("/tmp/metatry-crash.log"))) {
                fw.print(result);
                fw.flush();
            } catch (Exception e) {
                // ignore
            }
            throw t;
        }
    }

}
