package io.github.raphaelmun1z.ChatGepeteco.entities;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final OllamaChatModel model;

    public ChatService(OllamaChatModel model) {
        this.model = model;
    }

    public String chat(String input) {
        return model.call(input);
    }
}
