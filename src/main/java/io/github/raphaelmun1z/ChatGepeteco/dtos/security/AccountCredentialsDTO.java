package io.github.raphaelmun1z.ChatGepeteco.dtos.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record AccountCredentialsDTO(
        @NotBlank(message = "O email não pode ser vazio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "A senha não pode ser vazia")
        String senha
) implements Serializable {
}
