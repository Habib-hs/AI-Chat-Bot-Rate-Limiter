package com.poridhi.ai_chatbot.controller;

import com.poridhi.ai_chatbot.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final AIChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        if (message.isEmpty()) {
            return new ResponseEntity<>("Error: Message cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        String response = chatService.getResponse(message);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
