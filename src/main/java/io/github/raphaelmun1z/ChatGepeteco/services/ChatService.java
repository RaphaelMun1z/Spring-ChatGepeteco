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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static reactor.core.publisher.Mono.delay;

@Service
public class ChatService {
    private final AIService aiService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UsuarioRepository usuarioRepository;
    private final VectorService vectorService;
    private final PdfService pdfService;

    public ChatService(AIService aiService,
                       ChatRepository chatRepository,
                       MessageRepository messageRepository,
                       UsuarioRepository usuarioRepository,
                       VectorService vectorService,
                       PdfService pdfService) {
        this.aiService = aiService;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.usuarioRepository = usuarioRepository;
        this.vectorService = vectorService;
        this.pdfService = pdfService;
    }

    /*** CHAT CRUD ***/
    @Transactional
    public ChatResponseDTO createChat(ChatRequestDTO chatRequestDTO) {
        Usuario user = getCurrentUser();

        Chat chat = Chat.builder()
                .title((chatRequestDTO.title() != null && !chatRequestDTO.title().isEmpty())
                        ? chatRequestDTO.title()
                        : "Novo Chat")
                .user(user)
                .modelName(chatRequestDTO.modelName())
                .systemInstruction(chatRequestDTO.systemInstruction())
                .createdAt(LocalDateTime.now())
                .build();

        chatRepository.save(chat);
        return new ChatResponseDTO(chat);
    }

    @Transactional(readOnly = true)
    public List<ChatResponseDTO> findAllChats() {
        return chatRepository.findAll()
                .stream()
                .map(ChatResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatResponseDTO getChatById(String chatId) {
        Chat chat = getChatEntityById(chatId);
        return new ChatResponseDTO(chat);
    }

    @Transactional(readOnly = true)
    public Chat getChatEntityById(String chatId) {
        return chatRepository
                .findByIdWithMessagesAndPdfIndexes(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat não encontrado"));
    }

    @Transactional
    public void updateChatTitle(String chatId, String newTitle) {
        Chat chat = getChatEntityById(chatId);
        validateUser(chat);

        if (newTitle == null || newTitle.trim().isEmpty())
            throw new IllegalArgumentException("O título não pode estar vazio.");

        chat.setTitle(newTitle);
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);
    }

    @Transactional
    public void deleteChat(String chatId) {
        if (!chatRepository.existsById(chatId))
            throw new NotFoundException("Chat não encontrado.");

        chatRepository.deleteById(chatId);
    }

    @Transactional(readOnly = true)
    public List<ChatMinDTO> getUserChats() {
        String email = getCurrentUser().getEmail();
        return chatRepository.findAllByUserEmailOrderByUpdatedAtDesc(email)
                .stream()
                .map(ChatMinDTO::new)
                .collect(Collectors.toList());
    }

    /*** MENSAGENS ***/
    @Transactional
    public MessageResponseDTO sendMessage(String chatId, String inputMsg) {
        Chat chat = getChatEntityById(chatId);

        Message userMessage = saveMessage(chat, inputMsg, MessageSender.USER);
        String botResponse = aiService.processInputMessage(inputMsg);
        Message botMessage = saveMessage(chat, botResponse, MessageSender.BOT);

        return new MessageResponseDTO(botMessage);
    }

    @Transactional
    public Message saveBotMessage(String chatId, String botResponse) {
        Chat chat = getChatEntityById(chatId);
        return saveMessage(chat, botResponse, MessageSender.BOT);
    }

    @Transactional
    public void deleteMessage(String chatId, String msgId) {
        Message msg = messageRepository.findById(msgId)
                .orElseThrow(() -> new NotFoundException("Mensagem não encontrada."));
        if (!msg.getChat().getId().equals(chatId))
            throw new IllegalArgumentException("A mensagem não pertence a este chat.");

        messageRepository.delete(msg);
    }

    /*** CHATS COM IA (Histórico + RAG + PDF) ***/

    public MessageResponseDTO simpleChat(String chatId, String question) {
        Chat chat = getChatEntityById(chatId);

        String prompt = buildPromptWithHistory(chat, question);

        String response = aiService.processInputMessage(prompt);
        Message botMessage = saveMessage(chat, response, MessageSender.BOT);
        return new MessageResponseDTO(botMessage);
    }

    public MessageResponseDTO contextualChat(String chatId, MultipartFile pdf, String question) {
        Chat chat = getChatEntityById(chatId);

        String pdfText = pdfService.extractText(pdf);
        String prompt = buildPromptWithHistory(chat, question) + "\nContexto do PDF:\n" + pdfText;

        String response = aiService.processInputMessage(prompt);
        Message botMessage = saveMessage(chat, response, MessageSender.BOT);
        return new MessageResponseDTO(botMessage);
    }

    public MessageResponseDTO ragChat(String chatId, String question) {
        Chat chat = getChatEntityById(chatId);

        List<String> topChunks = vectorService.findTopChunksForChat(chat, question, 5);
        String prompt = buildPromptWithHistory(chat, question) + "\nContexto:\n" + String.join("\n", topChunks);

        String response = aiService.processInputMessage(prompt);
        Message botMessage = saveMessage(chat, response, MessageSender.BOT);
        return new MessageResponseDTO(botMessage);
    }

    /*** FUNÇÕES AUXILIARES ***/

    private Message saveMessage(Chat chat, String content, MessageSender sender) {
        Message msg = new Message(content, sender, chat);
        messageRepository.save(msg);
        return msg;
    }

    private String buildPromptWithHistory(Chat chat, String question) {
        List<Message> lastMessages = new ArrayList<>(chat.getMessages());
        int maxMessages = 10;
        if (lastMessages.size() > maxMessages) {
            lastMessages = lastMessages.subList(lastMessages.size() - maxMessages, lastMessages.size());
        }

        String history = lastMessages.stream()
                .map(msg -> msg.getSender() == MessageSender.USER ? "Usuário: " + msg.getContent() : "Assistente: " + msg.getContent())
                .collect(Collectors.joining("\n"));

        return history + "\nUsuário: " + question + "\nAssistente:";
    }

    private Usuario getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    private void validateUser(Chat chat) {
        if (!chat.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new NotFoundException("Chat não encontrado ou permissão negada.");
        }
    }
}
