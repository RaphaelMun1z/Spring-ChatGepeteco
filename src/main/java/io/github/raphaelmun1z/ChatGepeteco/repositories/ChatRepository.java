package io.github.raphaelmun1z.ChatGepeteco.repositories;

import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, String> {
    List<Chat> findAllByUserEmailOrderByUpdatedAtDesc(String email);

    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.messages WHERE c.id = :chatId")
    Optional<Chat> findByIdWithMessages(@Param("chatId") String chatId);

    @Query("""
        SELECT DISTINCT c
        FROM Chat c
        LEFT JOIN FETCH c.messages
        LEFT JOIN FETCH c.pdfIndexes
        WHERE c.id = :chatId
    """)
    Optional<Chat> findByIdWithMessagesAndPdfIndexes(@Param("chatId") String chatId);
}
