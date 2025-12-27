package io.github.raphaelmun1z.ChatGepeteco.repositories.autorizacao;

import io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao.Papel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PapelRepository extends JpaRepository<Papel, String> {
    Optional<Papel> findByNome(String nome);
}
