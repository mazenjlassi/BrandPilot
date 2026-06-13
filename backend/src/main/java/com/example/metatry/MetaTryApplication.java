package com.example.metatry;

import java.io.FileWriter;
import java.time.Instant;

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
            dumpCrash(t);
            System.exit(1);
        }
    }

    private static void dumpCrash(Throwable t) {
        try (FileWriter fw = new FileWriter("/tmp/metatry-crash.log")) {
            fw.write("=== CRASH at " + Instant.now() + " ===\n"); fw.flush();
            writeException(t, fw, 0);
            Throwable cause = t.getCause();
            int depth = 0;
            while (cause != null && depth < 20) {
                fw.write("--- Cause[" + depth + "] ---\n"); fw.flush();
                writeException(cause, fw, 0);
                cause = cause.getCause();
                depth++;
            }
            Throwable[] suppressed = t.getSuppressed();
            for (int i = 0; i < suppressed.length; i++) {
                fw.write("--- Suppressed[" + i + "] ---\n"); fw.flush();
                writeException(suppressed[i], fw, 0);
            }
        } catch (Exception e) {
            // cannot log further
        }
    }

    private static void writeException(Throwable t, FileWriter fw, int indent) throws Exception {
        for (int i = 0; i < indent; i++) fw.write("  ");
        fw.write(t.getClass().getName() + ": " + t.getMessage() + "\n"); fw.flush();
        for (StackTraceElement ste : t.getStackTrace()) {
            for (int i = 0; i < indent; i++) fw.write("  ");
            fw.write("  at " + ste + "\n"); fw.flush();
        }
    }
}
