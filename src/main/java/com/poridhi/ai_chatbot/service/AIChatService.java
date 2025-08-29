package com.poridhi.ai_chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AIChatService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Please enter a message.";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", geminiApiKey);

            Map<String, Object> part = new HashMap<>();
            part.put("text", message);
            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));
            List<Map<String, Object>> contents = Collections.singletonList(content);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiApiUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object candidates = response.getBody().get("candidates");
                if (candidates instanceof List && !((List<?>) candidates).isEmpty()) {
                    Map candidate = (Map) ((List<?>) candidates).get(0);
                    Map contentMap = (Map) candidate.get("content");
                    List partsList = (List) contentMap.get("parts");
                    if (partsList != null && !partsList.isEmpty()) {
                        Map firstPart = (Map) partsList.get(0);
                        Object text = firstPart.get("text");
                        return text != null ? text.toString() : "No reply from AI.";
                    }
                }
                return "No reply from AI.";
            } else {
                return "Error: Gemini API returned status " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error communicating with Gemini API: " + e.getMessage();
        }
    }
}
