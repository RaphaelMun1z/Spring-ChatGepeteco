package io.github.raphaelmun1z.ChatGepeteco.controllers;

import io.github.raphaelmun1z.ChatGepeteco.dtos.ChatRequestDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.ChatResponseDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.MessageRequestDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.MessageResponseDTO;
import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import io.github.raphaelmun1z.ChatGepeteco.entities.Message;
import io.github.raphaelmun1z.ChatGepeteco.services.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> createChat(@RequestBody ChatRequestDTO request) {
        Chat chatEntity = new Chat();
        chatEntity.setTitle(request.title());
        chatEntity.setModelName(request.modelName());
        chatEntity.setSystemInstruction(request.systemInstruction());

        Chat createdChat = chatService.createChat(chatEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ChatResponseDTO(createdChat));
    }

    @GetMapping
    public ResponseEntity<List<ChatResponseDTO>> getAllChats() {
        List<ChatResponseDTO> list = chatService.getAllChats().stream()
            .map(ChatResponseDTO::new)
            .toList();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatResponseDTO> getChatById(@PathVariable String id) {
        Chat chat = chatService.getChatById(id);
        return ResponseEntity.ok(new ChatResponseDTO(chat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable String id) {
        chatService.deleteChat(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(@PathVariable String chatId, @RequestBody MessageRequestDTO request) {
        Message sentMessage = chatService.sendMessage(chatId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponseDTO(sentMessage));
    }

    @DeleteMapping("/{chatId}/messages/{msgId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String chatId, @PathVariable String msgId) {
        chatService.deleteMessage(chatId, msgId);
        return ResponseEntity.noContent().build();
    }
}
