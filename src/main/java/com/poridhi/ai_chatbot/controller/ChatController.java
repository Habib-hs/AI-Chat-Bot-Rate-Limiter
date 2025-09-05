package com.poridhi.ai_chatbot.controller;

import com.poridhi.ai_chatbot.service.AIChatService;
import com.poridhi.ai_chatbot.service.SimpleRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final AIChatService chatService;
    private final SimpleRateLimitService simpleRateLimitService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String message = request.getOrDefault("message", "");
        if (message.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Message cannot be empty.");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Check rate limit first
        String clientIp = getClientIpAddress(httpRequest);
        String authHeader = httpRequest.getHeader("Authorization");

        Map<String, Object> rateLimitResult = simpleRateLimitService.checkAndIncrementRateLimit(authHeader, clientIp);
        boolean allowed = (Boolean) rateLimitResult.get("allowed");
        int remaining = (Integer) rateLimitResult.get("remaining");
        String userType = (String) rateLimitResult.get("userType");
        int limit = (Integer) rateLimitResult.get("limit");

        if (!allowed) {
            Map<String, Object> rateLimitResponse = new HashMap<>();
            rateLimitResponse.put("success", false);

            String timeWindow = "1 hour";
            String errorMsg = "";
            if ("guest".equals(userType)) {
                errorMsg = "Too many requests. Guest users can make " + limit + " requests per " + timeWindow + ".";
            } else if ("free".equals(userType)) {
                errorMsg = "Too many requests. Free users can make " + limit + " requests per " + timeWindow + ".";
            } else if ("premium".equals(userType)) {
                errorMsg = "Too many requests. Premium users can make " + limit + " requests per " + timeWindow + ".";
            }

            rateLimitResponse.put("error", errorMsg);
            rateLimitResponse.put("userType", userType);
            rateLimitResponse.put("remaining_requests", remaining);
            return new ResponseEntity<>(rateLimitResponse, HttpStatus.TOO_MANY_REQUESTS);
        }

        // Get AI response
        String aiResponse = chatService.getResponse(message);

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", aiResponse);
        successResponse.put("userType", userType);
        successResponse.put("remaining_requests", remaining);

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(HttpServletRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String clientIp = getClientIpAddress(request);

        // Use the simple service to get current status
        Map<String, Object> rateLimitInfo = simpleRateLimitService.getStatus(authHeader, clientIp);

        // Format response as requested - remove "used" and keep only required fields
        Map<String, Object> response = new HashMap<>();
        response.put("identifier", rateLimitInfo.get("identifier"));
        response.put("userType", rateLimitInfo.get("userType"));
        response.put("limit", rateLimitInfo.get("limit"));
        response.put("remaining", rateLimitInfo.get("remaining"));
        response.put("windowType", "1 hour");

        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String ip = null;
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            ip = xForwardedFor.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        // Normalize IPv6 localhost to IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}
