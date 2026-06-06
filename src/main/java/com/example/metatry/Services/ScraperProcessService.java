package com.example.metatry.Services;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI;

@Service
public class ScraperProcessService {

    private static final String HEALTH_URL = "http://host.docker.internal:3001/health";
    private static final long MAX_WAIT_MS = 15000;

    public synchronized void ensureRunning() {
        if (isAlive()) return;
        waitForReady();
    }

    public boolean isAlive() {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(HEALTH_URL).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForReady() {
        long deadline = System.currentTimeMillis() + MAX_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(HEALTH_URL).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);
                int code = conn.getResponseCode();
                conn.disconnect();
                if (code == 200) {
                    System.out.println("Scraper is ready");
                    return;
                }
            } catch (Exception ignored) {}
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Scraper did not become ready within " + (MAX_WAIT_MS / 1000) + "s");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("ScraperProcessService cleanup (host-managed, no action needed)");
    }
}
