package io.github.raphaelmun1z.ChatGepeteco.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.raphaelmun1z.ChatGepeteco.entities.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonIgnore
    private Chat chat;

    public Message() {
    }

    public Message(String content, Role role, LocalDateTime createdAt, Chat chat) {
        this.content = content;
        this.role = role;
        this.createdAt = createdAt;
        this.chat = chat;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id) && Objects.equals(content, message.content) && role == message.role && Objects.equals(createdAt, message.createdAt) && Objects.equals(chat, message.chat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, role, createdAt, chat);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Message{");
        sb.append("id='").append(id).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", role=").append(role);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", chat=").append(chat);
        sb.append('}');
        return sb.toString();
    }
}
