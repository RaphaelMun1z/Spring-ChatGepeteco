package io.github.raphaelmun1z.ChatGepeteco.repositories;

import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, String> {

}
