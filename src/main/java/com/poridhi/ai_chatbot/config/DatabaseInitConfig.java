package com.poridhi.ai_chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import com.poridhi.ai_chatbot.model.User;
import com.poridhi.ai_chatbot.repository.UserRepository;

@Configuration
public class DatabaseInitConfig {

    @Bean
    public CommandLineRunner createUsers(UserRepository userRepository) {
        return args -> {
            for (int i = 1; i <= 5; i++) {
                String userType = i <= 3 ? "free" : "premium";
                String username = "user" + i;
                String password = "pass" + i;
                if (!userRepository.existsByUsername(username)) {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setUserType(userType);
                    userRepository.save(user);
                }
            }
        };
    }
}
