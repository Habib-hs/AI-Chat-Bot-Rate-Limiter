package com.poridhi.ai_chatbot.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int WINDOW_SECONDS = 60;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generalized fixed window rate limit: allows up to 'limit' requests per 'windowSeconds' for a given key.
     */
    public boolean isAllowedByFixedWindow(String key, int limit, int windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) count = 1L;
        if (count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count <= limit;
    }

    private long getCurrentWindow() {
        return Instant.now().getEpochSecond() / WINDOW_SECONDS;
    }

    // Legacy method for guest IPs (default 3/min)
    public boolean isAllowedByFixedWindow(String clientIp) {
        String key = "rate_limit:" + clientIp + ":" + getCurrentWindow();
        return isAllowedByFixedWindow(key, 3, WINDOW_SECONDS);
    }
}
