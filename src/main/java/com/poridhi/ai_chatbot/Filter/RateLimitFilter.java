package com.poridhi.ai_chatbot.Filter;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Rate limiting is now handled directly in ChatController
        // This filter just passes through all requests
        chain.doFilter(request, response);
    }
}
