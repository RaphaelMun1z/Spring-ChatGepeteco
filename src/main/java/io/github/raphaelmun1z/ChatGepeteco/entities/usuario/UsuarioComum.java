package io.github.raphaelmun1z.ChatGepeteco.entities.usuario;

import io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao.Papel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tb_usuario_comum")
@PrimaryKeyJoinColumn(name = "usuario_comum_id")
public class UsuarioComum extends Usuario implements Serializable {
    public UsuarioComum(String nome, String email, String senha, Papel papel) {
        super(nome, email, senha, papel);
    }
}