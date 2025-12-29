package io.github.raphaelmun1z.ChatGepeteco.controllers;

import io.github.raphaelmun1z.ChatGepeteco.dtos.*;
import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import io.github.raphaelmun1z.ChatGepeteco.entities.Message;
import io.github.raphaelmun1z.ChatGepeteco.services.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> createChat(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO createdChat = chatService.createChat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChat);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChatResponseDTO>> getAllChats() {
        List<ChatResponseDTO> list = chatService.findAllChats();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatResponseDTO> getChatById(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getChatById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable String id) {
        chatService.deleteChat(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(@PathVariable String chatId, @RequestBody MessageRequestDTO request) {
        MessageResponseDTO sentMessage = chatService.sendMessage(chatId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(sentMessage);
    }

    @DeleteMapping("/{chatId}/messages/{msgId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String chatId, @PathVariable String msgId) {
        chatService.deleteMessage(chatId, msgId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ChatMinDTO>> getUserChats() {
        List<ChatMinDTO> userChats = chatService.getUserChats();
        return ResponseEntity.ok(userChats);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateChatTitle(@PathVariable String id, @RequestBody UpdateChatTitleDTO dto) {
        chatService.updateChatTitle(id, dto.title());
        return ResponseEntity.noContent().build();
    }
}
