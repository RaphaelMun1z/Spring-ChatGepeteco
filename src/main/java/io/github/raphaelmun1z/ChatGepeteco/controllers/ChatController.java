package io.github.raphaelmun1z.ChatGepeteco.controllers;

import io.github.raphaelmun1z.ChatGepeteco.dtos.*;
import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import io.github.raphaelmun1z.ChatGepeteco.entities.Message;
import io.github.raphaelmun1z.ChatGepeteco.entities.enums.MessageSender;
import io.github.raphaelmun1z.ChatGepeteco.services.AIService;
import io.github.raphaelmun1z.ChatGepeteco.services.ChatService;
import io.github.raphaelmun1z.ChatGepeteco.services.PdfService;
import io.github.raphaelmun1z.ChatGepeteco.services.VectorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /*** CRUD Chats ***/
    @PostMapping
    public ResponseEntity<ChatResponseDTO> createChat(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO createdChat = chatService.createChat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChat);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChatResponseDTO>> getAllChats() {
        return ResponseEntity.ok(chatService.findAllChats());
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

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateChatTitle(@PathVariable String id,
                                                @RequestBody UpdateChatTitleDTO dto) {
        chatService.updateChatTitle(id, dto.title());
        return ResponseEntity.noContent().build();
    }

    /*** Mensagens ***/
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponseDTO> sendMessage(@PathVariable String chatId,
                                                          @RequestBody MessageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(chatId, request.content()));
    }

    @DeleteMapping("/{chatId}/messages/{msgId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String chatId,
                                              @PathVariable String msgId) {
        chatService.deleteMessage(chatId, msgId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ChatMinDTO>> getUserChats() {
        return ResponseEntity.ok(chatService.getUserChats());
    }

    /*** Chats com IA ***/
    @PostMapping("/simple")
    public ResponseEntity<MessageResponseDTO> simpleChat(@RequestParam String chatId,
                                                         @RequestParam String question) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.simpleChat(chatId, question));
    }

    @PostMapping("/contextual")
    public ResponseEntity<MessageResponseDTO> contextualChat(@RequestParam String chatId,
                                                             @RequestParam MultipartFile pdf,
                                                             @RequestParam String question) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.contextualChat(chatId, pdf, question));
    }

    @PostMapping("/rag")
    public ResponseEntity<MessageResponseDTO> ragChat(@RequestParam String chatId,
                                                      @RequestParam String question) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.ragChat(chatId, question));
    }
}
