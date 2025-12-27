package io.github.raphaelmun1z.ChatGepeteco.services;

import io.github.raphaelmun1z.ChatGepeteco.dtos.ChatResponseDTO;
import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import io.github.raphaelmun1z.ChatGepeteco.entities.Message;
import io.github.raphaelmun1z.ChatGepeteco.entities.enums.Role;
import io.github.raphaelmun1z.ChatGepeteco.exceptions.models.NotFoundException;
import io.github.raphaelmun1z.ChatGepeteco.repositories.ChatRepository;
import io.github.raphaelmun1z.ChatGepeteco.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    private final AIService aiService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatService(AIService aiService, ChatRepository chatRepository, MessageRepository messageRepository) {
        this.aiService = aiService;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    public Chat createChat(Chat chat) {
        chat = chatRepository.save(chat);
        return chat;
    }

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    public Chat getChatById(String chatId) {
        return chatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat não encontrado."));
    }

    @Transactional
    public Message sendMessage(String chatId, String inputMsg) {
        Chat targetChat = getChatById(chatId);
        String responseMsg = aiService.processInputMessage(inputMsg);
        Message newMessage = new Message(null, responseMsg, LocalDateTime.now(), targetChat);
        newMessage.setChat(targetChat);
        targetChat.getMessages().add(newMessage);
        return messageRepository.save(newMessage);
    }

    @Transactional
    public void deleteMessage(String chatId, String msgId) {
        Message msg = messageRepository.findById(msgId)
            .orElseThrow(() -> new NotFoundException("Mensagem não encontrada."));

        if (!msg.getChat().getId().equals(chatId)) {
            throw new IllegalArgumentException("A mensagem não pertence a este chat.");
        }

        messageRepository.delete(msg);
    }

    @Transactional
    public void deleteChat(String chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new NotFoundException("Chat não encontrado.");
        }
        chatRepository.deleteById(chatId);
    }
}
