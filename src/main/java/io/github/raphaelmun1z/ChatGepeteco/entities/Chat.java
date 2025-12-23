package io.github.raphaelmun1z.ChatGepeteco.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String title;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private String userId;
    private String modelName;

    @Column(columnDefinition = "TEXT")
    private String systemInstruction;

    public Chat() {
    }

    public Chat(String id, String title, List<Message> messages, LocalDateTime updatedAt, LocalDateTime createdAt, String userId, String modelName, String systemInstruction) {
        this.id = id;
        this.title = title;
        this.messages = messages;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.userId = userId;
        this.modelName = modelName;
        this.systemInstruction = systemInstruction;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getSystemInstruction() {
        return systemInstruction;
    }

    public void setSystemInstruction(String systemInstruction) {
        this.systemInstruction = systemInstruction;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(id, chat.id) && Objects.equals(title, chat.title) && Objects.equals(messages, chat.messages) && Objects.equals(updatedAt, chat.updatedAt) && Objects.equals(createdAt, chat.createdAt) && Objects.equals(userId, chat.userId) && Objects.equals(modelName, chat.modelName) && Objects.equals(systemInstruction, chat.systemInstruction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, messages, updatedAt, createdAt, userId, modelName, systemInstruction);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Chat{");
        sb.append("id='").append(id).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", messages=").append(messages);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", modelName='").append(modelName).append('\'');
        sb.append(", systemInstruction='").append(systemInstruction).append('\'');
        sb.append('}');
        return sb.toString();
    }
}