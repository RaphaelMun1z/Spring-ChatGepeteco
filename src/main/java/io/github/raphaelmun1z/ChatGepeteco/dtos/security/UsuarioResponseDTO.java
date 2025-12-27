package io.github.raphaelmun1z.ChatGepeteco.dtos.security;

import io.github.raphaelmun1z.ChatGepeteco.entities.usuario.Usuario;

public record UsuarioResponseDTO(
    String id,
    String nome,
    String email
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail()
        );
    }
}
