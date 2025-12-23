package io.github.raphaelmun1z.ChatGepeteco.dtos;

public record ChatRequestDTO(
    String title,
    String modelName,
    String systemInstruction
) {
}