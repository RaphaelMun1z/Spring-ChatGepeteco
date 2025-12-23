package io.github.raphaelmun1z.ChatGepeteco.dtos;

import io.github.raphaelmun1z.ChatGepeteco.entities.Message;
import io.github.raphaelmun1z.ChatGepeteco.entities.enums.Role;

import java.time.LocalDateTime;

public record MessageResponseDTO(
    String id,
    String content,
    Role role,
    LocalDateTime createdAt
) {
    public MessageResponseDTO(Message message) {
        this(
            message.getId(),
            message.getContent(),
            message.getRole(),
            message.getCreatedAt()
        );
    }
}