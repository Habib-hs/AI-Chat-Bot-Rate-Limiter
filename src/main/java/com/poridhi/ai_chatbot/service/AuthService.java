package com.poridhi.ai_chatbot.service;

import com.poridhi.ai_chatbot.model.User;
import com.poridhi.ai_chatbot.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> authenticate(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        /*
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            result.put("token", generateToken(user.getUsername(), user.getUserType()));
            result.put("userType", user.getUserType());
        } else {
            result.put("error", "Invalid username or password");
        }
        return result;
        */

        // Use Optional<User> from repository
        return userRepository.findByUsername(username)
            .filter(user -> user.getPassword().equals(password))
            .map(user -> {
                result.put("token", generateToken(user.getUsername(), user.getUserType()));
                result.put("userType", user.getUserType());
                return result;
            })
            .orElseGet(() -> {
                result.put("error", "Invalid username or password");
                return result;
            });
    }

    private String generateToken(String username, String userType) {
        String tokenPayload = username + ":" + userType;
        return Base64.getEncoder().encodeToString(tokenPayload.getBytes());
    }
}
