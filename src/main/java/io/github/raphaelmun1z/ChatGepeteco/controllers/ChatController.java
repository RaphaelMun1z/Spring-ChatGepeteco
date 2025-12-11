package io.github.raphaelmun1z.ChatGepeteco.controllers;

import io.github.raphaelmun1z.ChatGepeteco.entities.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ChatController {
    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam(value = "message") String message) {
        return ResponseEntity.ok(service.chat(message));
    }
}
