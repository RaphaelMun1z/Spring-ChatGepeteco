package io.github.raphaelmun1z.ChatGepeteco.dtos;

import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;

public record ChatMinDTO(
    String id,
    String title
) {
    public ChatMinDTO(Chat chat) {
        this(chat.getId(), chat.getTitle());
    }
}