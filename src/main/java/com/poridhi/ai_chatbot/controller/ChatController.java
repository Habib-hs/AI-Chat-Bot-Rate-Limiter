package com.poridhi.ai_chatbot.controller;

import com.poridhi.ai_chatbot.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final AIChatService chatService;

    @GetMapping("/chat")
    public ResponseEntity<String> chat() {
        String response = chatService.getResponse();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
