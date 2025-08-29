package com.poridhi.ai_chatbot.repository;

import com.poridhi.ai_chatbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
}
