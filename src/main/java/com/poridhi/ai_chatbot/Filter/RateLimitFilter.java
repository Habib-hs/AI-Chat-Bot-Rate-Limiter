package com.poridhi.ai_chatbot.Filter;

import com.poridhi.ai_chatbot.service.RateLimitService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIpAddress(httpRequest);

        String authHeader = httpRequest.getHeader("Authorization");
        String key;
        int limit;
        int windowSeconds = 60;
        boolean allowed;

        if (authHeader != null && !authHeader.isEmpty()) {
            // Token format: Base64(username:userType)
            try {
                String token = authHeader.replace("Bearer ", "").trim();
                String decoded = new String(java.util.Base64.getDecoder().decode(token));
                String[] parts = decoded.split(":");
                String username = parts[0];
                String userType = parts.length > 1 ? parts[1] : "free";
                if ("premium".equalsIgnoreCase(userType)) {
                    limit = 10;
                } else {
                    limit = 5;
                }
                key = "rate_limit:user:" + username + ":" + userType + ":" + System.currentTimeMillis() / 60000;
                allowed = rateLimitService.isAllowedByFixedWindow(key, limit, windowSeconds);
            } catch (Exception e) {
                // Invalid token, treat as guest
                key = "rate_limit:" + clientIp + ":" + System.currentTimeMillis() / 60000;
                allowed = rateLimitService.isAllowedByFixedWindow(key, 3, windowSeconds);
            }
        } else {
            // Guest user
            key = "rate_limit:" + clientIp + ":" + System.currentTimeMillis() / 60000;
            allowed = rateLimitService.isAllowedByFixedWindow(key, 3, windowSeconds);
        }

        if (allowed) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
