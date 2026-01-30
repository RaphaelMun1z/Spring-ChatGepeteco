package io.github.raphaelmun1z.ChatGepeteco.dtos;

import io.github.raphaelmun1z.ChatGepeteco.entities.Message;

import java.time.LocalDateTime;

public record MessageResponseDTO(
        String id,
        String content,
        String sender,
        LocalDateTime timestamp,
        String attachmentName
) {
    public MessageResponseDTO(Message message) {
        this(
                message.getId(),
                message.getContent(),
                message.getSender().name(),
                message.getCreatedAt(),
                message.getAttachmentName()
        );
    }
}