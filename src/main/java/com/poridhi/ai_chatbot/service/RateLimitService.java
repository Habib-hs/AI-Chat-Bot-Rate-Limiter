package com.poridhi.ai_chatbot.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int LIMIT = 3;
    private static final int WINDOW_SECONDS = 60;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Fixed window rate limit: allows up to LIMIT requests per IP per WINDOW_SECONDS.
     */
    public boolean isAllowedByFixedWindow(String clientIp) {
        String key = "rate_limit:" + clientIp + ":" + getCurrentWindow();
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        return count <= LIMIT;
    }

    private long getCurrentWindow() {
        return Instant.now().getEpochSecond() / WINDOW_SECONDS;
    }
}
