package io.github.raphaelmun1z.ChatGepeteco.repositories.autorizacao;

import io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissaoRepository extends JpaRepository<Permissao, String> {
    Optional<Permissao> findByNome(String nomePermissao);
}
