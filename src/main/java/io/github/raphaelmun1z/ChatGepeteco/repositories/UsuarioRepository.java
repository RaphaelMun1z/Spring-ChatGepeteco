package io.github.raphaelmun1z.ChatGepeteco.repositories;

import io.github.raphaelmun1z.ChatGepeteco.entities.usuario.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    @EntityGraph(attributePaths = {"papel", "papel.permissoes"})
    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.papel p JOIN FETCH p.permissoes WHERE u.email = :email")
    Optional<Usuario> findByEmailWithRolesAndPermissions(@Param("email") String email);
}
