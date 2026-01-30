package io.github.raphaelmun1z.ChatGepeteco.services;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private final OllamaChatModel model;

    public AIService(OllamaChatModel model) {
        this.model = model;
    }

    public String processInputMessage(String input) {
        return model.call(input);
    }

    public String processInputWithContext(String context, String question) {
        String prompt = context + "\n\nPergunta: " + question;
        return model.call(prompt);
    }
}
