package io.github.raphaelmun1z.ChatGepeteco.repositories;

import io.github.raphaelmun1z.ChatGepeteco.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, String> {

}
