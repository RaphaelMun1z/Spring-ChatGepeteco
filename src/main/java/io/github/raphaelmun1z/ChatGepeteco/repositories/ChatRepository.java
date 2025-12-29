package io.github.raphaelmun1z.ChatGepeteco.repositories;

import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, String> {
    List<Chat> findAllByUserEmailOrderByUpdatedAtDesc(String email);
}
