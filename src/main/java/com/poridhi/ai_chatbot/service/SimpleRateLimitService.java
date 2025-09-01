package com.poridhi.ai_chatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class  SimpleRateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRateLimitService.class);

    // In-memory storage for rate limiting
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStartTimes = new ConcurrentHashMap<>();
    private static final int WINDOW_DURATION_MS = 2 * 60 * 1000; // 2 minutes

    public Map<String, Object> checkAndIncrementRateLimit(String authHeader, String clientIp) {
        // Determine user type and limits
        String userType = "guest";
        String identifier = clientIp;
        int limit = 3; // Default for guest

        if (authHeader != null && !authHeader.isEmpty()) {
            try {
                String token = authHeader.replace("Bearer ", "").trim();
                String decoded = new String(java.util.Base64.getDecoder().decode(token));
                String[] parts = decoded.split(":");
                String username = parts[0];
                userType = parts.length > 1 ? parts[1] : "free";
                identifier = username;

                if ("premium".equalsIgnoreCase(userType)) {
                    limit = 10; // Premium users: 10 requests per 2 minutes
                } else {
                    limit = 5; // Free users: 5 requests per 2 minutes
                }
            } catch (Exception e) {
                // Invalid token, treat as guest
                userType = "guest";
                identifier = clientIp;
                limit = 3;
            }
        }

        String key = userType + ":" + identifier;
        long currentTime = System.currentTimeMillis();

        synchronized (this) {
            // Check if window has expired
            Long windowStart = windowStartTimes.get(key);
            if (windowStart == null || (currentTime - windowStart) >= WINDOW_DURATION_MS) {
                // Reset window
                windowStartTimes.put(key, currentTime);
                requestCounts.put(key, 1);
                logger.info("[SimpleRateLimit] New window started for key: {} | Count: 1 | Limit: {}", key, limit);

                Map<String, Object> result = new HashMap<>();
                result.put("allowed", true);
                result.put("used", 1);
                result.put("remaining", limit - 1);
                result.put("userType", userType);
                result.put("identifier", identifier);
                result.put("limit", limit);
                return result;
            } else {
                // Increment current window
                Integer currentCount = requestCounts.getOrDefault(key, 0);
                currentCount++;
                requestCounts.put(key, currentCount);

                boolean allowed = currentCount <= limit;
                int remaining = Math.max(0, limit - currentCount);

                logger.info("[SimpleRateLimit] Incremented key: {} | Count: {} | Limit: {} | Allowed: {}",
                           key, currentCount, limit, allowed);

                Map<String, Object> result = new HashMap<>();
                result.put("allowed", allowed);
                result.put("used", currentCount);
                result.put("remaining", remaining);
                result.put("userType", userType);
                result.put("identifier", identifier);
                result.put("limit", limit);
                return result;
            }
        }
    }

    public Map<String, Object> getStatus(String authHeader, String clientIp) {
        // Determine user type and limits
        String userType = "guest";
        String identifier = clientIp;
        int limit = 3; // Default for guest

        if (authHeader != null && !authHeader.isEmpty()) {
            try {
                String token = authHeader.replace("Bearer ", "").trim();
                String decoded = new String(java.util.Base64.getDecoder().decode(token));
                String[] parts = decoded.split(":");
                String username = parts[0];
                userType = parts.length > 1 ? parts[1] : "free";
                identifier = username;

                if ("premium".equalsIgnoreCase(userType)) {
                    limit = 10;
                } else {
                    limit = 5;
                }
            } catch (Exception e) {
                userType = "guest";
                identifier = clientIp;
                limit = 3;
            }
        }

        String key = userType + ":" + identifier;
        long currentTime = System.currentTimeMillis();

        synchronized (this) {
            Long windowStart = windowStartTimes.get(key);
            Integer currentCount = requestCounts.get(key);

            // Check if window has expired
            if (windowStart == null || (currentTime - windowStart) >= WINDOW_DURATION_MS) {
                currentCount = 0; // Window expired, reset count
            }

            if (currentCount == null) currentCount = 0;

            int remaining = Math.max(0, limit - currentCount);

            logger.info("[SimpleRateLimit] Status check - Key: {} | Used: {} | Remaining: {} | Limit: {}",
                       key, currentCount, remaining, limit);

            Map<String, Object> result = new HashMap<>();
            result.put("userType", userType);
            result.put("identifier", identifier);
            result.put("limit", limit);
            result.put("used", currentCount);
            result.put("remaining", remaining);
            result.put("windowType", "2 minutes");
            return result;
        }
    }
}
