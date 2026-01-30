package io.github.raphaelmun1z.ChatGepeteco.dtos;

import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import io.github.raphaelmun1z.ChatGepeteco.entities.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record ChatResponseDTO(
        String id,
        String title,
        String modelName,
        String systemInstruction,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MessageResponseDTO> messages
) {
    public ChatResponseDTO(Chat chat) {
        this(
                chat.getId(),
                chat.getTitle(),
                chat.getModelName(),
                chat.getSystemInstruction(),
                chat.getCreatedAt(),
                chat.getUpdatedAt(),
                chat.getMessages().stream()
                        .sorted(Comparator.comparing(Message::getCreatedAt))
                        .map(MessageResponseDTO::new)
                        .collect(Collectors.toList())
        );
    }
}