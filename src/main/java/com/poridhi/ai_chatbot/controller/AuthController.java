package com.poridhi.ai_chatbot.controller;

import com.poridhi.ai_chatbot.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Map<String, Object> result = authService.authenticate(username, password);

        if (result.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", result.get("error")));
        } else {
            return ResponseEntity.ok(result);
        }
    }
}
