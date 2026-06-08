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
            Throwable cur = t;
            int depth = 0;
            while (cur != null && depth < 5) {
                sb.append("[").append(depth).append("] ")
                  .append(cur.getClass().getName())
                  .append(": ").append(cur.getMessage()).append("\n");
                StackTraceElement[] st = cur.getStackTrace();
                for (int i = 0; i < Math.min(st.length, 5); i++) {
                    sb.append("   at ").append(st[i]).append("\n");
                }
                cur = cur.getCause();
                depth++;
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
