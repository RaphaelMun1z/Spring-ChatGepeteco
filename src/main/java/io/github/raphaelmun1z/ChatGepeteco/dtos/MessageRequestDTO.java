package io.github.raphaelmun1z.ChatGepeteco.dtos;

import jakarta.validation.constraints.NotBlank;

public record MessageRequestDTO(
    @NotBlank(message = "O conteúdo da mensagem não pode estar vazio")
    String content
) {
}
