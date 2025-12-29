package io.github.raphaelmun1z.ChatGepeteco.services;

import io.github.raphaelmun1z.ChatGepeteco.dtos.ChatMinDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.ChatRequestDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.ChatResponseDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.MessageResponseDTO;
import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import io.github.raphaelmun1z.ChatGepeteco.entities.Message;
import io.github.raphaelmun1z.ChatGepeteco.entities.enums.MessageSender;
import io.github.raphaelmun1z.ChatGepeteco.entities.usuario.Usuario;
import io.github.raphaelmun1z.ChatGepeteco.exceptions.models.NotFoundException;
import io.github.raphaelmun1z.ChatGepeteco.repositories.ChatRepository;
import io.github.raphaelmun1z.ChatGepeteco.repositories.MessageRepository;
import io.github.raphaelmun1z.ChatGepeteco.repositories.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static reactor.core.publisher.Mono.delay;

@Service
public class ChatService {
    private final AIService aiService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UsuarioRepository usuarioRepository;

    public ChatService(AIService aiService, ChatRepository chatRepository, MessageRepository messageRepository, UsuarioRepository usuarioRepository) {
        this.aiService = aiService;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ChatResponseDTO createChat(ChatRequestDTO chatRequestDTO) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailDoUsuario = authentication.getName();

        Usuario currentUser = usuarioRepository.findByEmail(emailDoUsuario)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o email: " + emailDoUsuario));

        Chat chat = Chat.builder()
            .title((chatRequestDTO.title() != null && !chatRequestDTO.title().isEmpty())
                ? chatRequestDTO.title()
                : "Novo Chat")
            .user(currentUser)
            .modelName(chatRequestDTO.modelName())
            .systemInstruction(chatRequestDTO.systemInstruction())
            .createdAt(LocalDateTime.now())
            .build();

        chatRepository.save(chat);
        return new ChatResponseDTO(chat);
    }

    @Transactional(readOnly = true)
    public List<ChatResponseDTO> findAllChats() {
        List<Chat> chats = chatRepository.findAll();

        return chats.stream()
            .map(ChatResponseDTO::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatResponseDTO getChatById(String chatId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Chat não encontrado"));

        return new ChatResponseDTO(chat);
    }

    @Transactional
    public MessageResponseDTO sendMessage(String chatId, String inputMsg) {
        Chat targetChat = chatRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Chat não encontrado"));

        Message userMessage = new Message(inputMsg, MessageSender.USER, targetChat);
        messageRepository.save(userMessage);

        String responseMsg = aiService.processInputMessage(inputMsg);

        Message botMessage = new Message(responseMsg, MessageSender.BOT, targetChat);
        messageRepository.save(botMessage);

        return new MessageResponseDTO(botMessage);
    }

    @Transactional
    public void updateChatTitle(String chatId, String newTitle) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat não encontrado."));

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogado = authentication.getName();

        if (!chat.getUser().getEmail().equals(emailUsuarioLogado)) {
            throw new NotFoundException("Chat não encontrado ou permissão negada.");
        }

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("O título não pode estar vazio.");
        }

        chat.setTitle(newTitle);
        chat.setUpdatedAt(LocalDateTime.now());

        chatRepository.save(chat);
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

    @Transactional(readOnly = true)
    public List<ChatMinDTO> getUserChats() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return chatRepository.findAllByUserEmailOrderByUpdatedAtDesc(email)
            .stream()
            .map(ChatMinDTO::new)
            .toList();
    }
}
