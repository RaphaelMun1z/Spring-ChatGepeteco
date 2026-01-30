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
import java.util.Comparator;
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

    private static final String SYSTEM_ENFORCEMENT =
            "\n\n[INSTRUÇÃO DE RESPOSTA]\n" +
                    "Responda diretamente à pergunta do usuário.\n" +
                    "NÃO repita a pergunta.\n" +
                    "NÃO inicie a resposta com 'Usuário:', 'Assistente:' ou qualquer outro rótulo.\n" +
                    "Forneça apenas o conteúdo da resposta.";

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

    /* =================================================================
       CRUD DE CHATS
       ================================================================= */

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
                .updatedAt(LocalDateTime.now())
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
        validateUserOwner(chat);
        return new ChatResponseDTO(chat);
    }

    @Transactional(readOnly = true)
    public List<ChatMinDTO> getUserChats() {
        String email = getCurrentUser().getEmail();
        return chatRepository.findAllByUserEmailOrderByUpdatedAtDesc(email)
                .stream()
                .map(ChatMinDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateChatTitle(String chatId, String newTitle) {
        Chat chat = getChatEntityById(chatId);
        validateUserOwner(chat);

        if (newTitle == null || newTitle.trim().isEmpty())
            throw new IllegalArgumentException("O título não pode estar vazio.");

        chat.setTitle(newTitle);
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);
    }

    @Transactional
    public void deleteChat(String chatId) {
        Chat chat = getChatEntityById(chatId);
        validateUserOwner(chat);
        chatRepository.delete(chat);
    }

    /* =================================================================
       INTERAÇÕES COM IA (CORE)
       ================================================================= */

    @Transactional
    public MessageResponseDTO simpleChat(String chatId, String question) {
        Chat chat = getChatEntityById(chatId);
        validateUserOwner(chat);

        saveMessage(chat, question, MessageSender.USER, null);

        String history = buildHistoryContext(chat);
        String finalPrompt = history + "\nUsuário: " + question + SYSTEM_ENFORCEMENT + "\nAssistente:";

        String aiResponse = aiService.processInputMessage(finalPrompt);
        aiResponse = sanitizeResponse(aiResponse);

        Message botMessage = saveMessage(chat, aiResponse, MessageSender.BOT, null);

        return new MessageResponseDTO(botMessage);
    }

    @Transactional
    public MessageResponseDTO contextualChat(String chatId, MultipartFile pdf, String question) {
        Chat chat = getChatEntityById(chatId);
        validateUserOwner(chat);

        String fileName = (pdf != null) ? pdf.getOriginalFilename() : null;
        saveMessage(chat, question, MessageSender.USER, fileName);

        String pdfText = pdfService.extractText(pdf);
        String history = buildHistoryContext(chat);

        String finalPrompt = String.format(
                "%s\n\n--- CONTEXTO DO DOCUMENTO ANEXADO (%s) ---\n%s\n---------------------------------------\nUsuário: %s%s\nAssistente:",
                history, fileName, pdfText, question, SYSTEM_ENFORCEMENT
        );

        String aiResponse = aiService.processInputMessage(finalPrompt);
        aiResponse = sanitizeResponse(aiResponse);

        Message botMessage = saveMessage(chat, aiResponse, MessageSender.BOT, null);

        return new MessageResponseDTO(botMessage);
    }

    @Transactional
    public MessageResponseDTO ragChat(String chatId, String question) {
        Chat chat = getChatEntityById(chatId);
        validateUserOwner(chat);

        saveMessage(chat, question, MessageSender.USER, null);

        List<String> topChunks = vectorService.findTopChunksForChat(chat, question, 5);
        String contextBlock = String.join("\n\n", topChunks);
        String history = buildHistoryContext(chat);

        String finalPrompt = String.format(
                "%s\n\n--- INFORMAÇÃO RECUPERADA (RAG) ---\n%s\n-----------------------------------\nUsuário: %s%s\nAssistente:",
                history, contextBlock, question, SYSTEM_ENFORCEMENT
        );

        String aiResponse = aiService.processInputMessage(finalPrompt);
        aiResponse = sanitizeResponse(aiResponse);

        Message botMessage = saveMessage(chat, aiResponse, MessageSender.BOT, null);

        return new MessageResponseDTO(botMessage);
    }

    /* =================================================================
       GERENCIAMENTO DE MENSAGENS MANUAIS
       ================================================================= */

    @Transactional
    public MessageResponseDTO sendMessage(String chatId, String inputMsg) {
        Chat chat = getChatEntityById(chatId);
        validateUserOwner(chat);

        Message userMessage = saveMessage(chat, inputMsg, MessageSender.USER, null);

        return new MessageResponseDTO(userMessage);
    }

    @Transactional
    public void deleteMessage(String chatId, String msgId) {
        Message msg = messageRepository.findById(msgId)
                .orElseThrow(() -> new NotFoundException("Mensagem não encontrada."));

        if (!msg.getChat().getId().equals(chatId))
            throw new IllegalArgumentException("A mensagem não pertence a este chat.");

        validateUserOwner(msg.getChat());
        messageRepository.delete(msg);
    }

    /* =================================================================
       MÉTODOS AUXILIARES
       ================================================================= */

    private Chat getChatEntityById(String chatId) {
        return chatRepository
                .findByIdWithMessagesAndPdfIndexes(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat não encontrado com ID: " + chatId));
    }

    private Message saveMessage(Chat chat, String content, MessageSender sender, String attachmentName) {
        Message msg = new Message();
        msg.setChat(chat);
        msg.setContent(content);
        msg.setSender(sender);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setAttachmentName(attachmentName);

        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        return messageRepository.save(msg);
    }

    private String buildHistoryContext(Chat chat) {
        int maxHistory = 10;

        List<Message> sortedMessages = chat.getMessages().stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());

        int start = Math.max(0, sortedMessages.size() - maxHistory);
        List<Message> recentMessages = sortedMessages.subList(start, sortedMessages.size());

        if (chat.getSystemInstruction() != null && !chat.getSystemInstruction().isEmpty()) {
            return "Instrução do Sistema: " + chat.getSystemInstruction() + "\n\n" +
                    recentMessages.stream()
                            .map(this::formatMessageForPrompt)
                            .collect(Collectors.joining("\n"));
        }

        return recentMessages.stream()
                .map(this::formatMessageForPrompt)
                .collect(Collectors.joining("\n"));
    }

    private String formatMessageForPrompt(Message msg) {
        return (msg.getSender() == MessageSender.USER ? "Usuário: " : "Assistente: ") + msg.getContent();
    }

    private String sanitizeResponse(String response) {
        if (response == null) return "";
        String trimmed = response.trim();

        if (trimmed.startsWith("Assistente:")) {
            trimmed = trimmed.substring("Assistente:".length()).trim();
        }

        if (trimmed.startsWith("Usuário:") && trimmed.contains("Assistente:")) {
            int assistentIndex = trimmed.indexOf("Assistente:");
            if (assistentIndex != -1) {
                trimmed = trimmed.substring(assistentIndex + "Assistente:".length()).trim();
            }
        }

        return trimmed;
    }

    private Usuario getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado no contexto de segurança."));
    }

    private void validateUserOwner(Chat chat) {
        Usuario currentUser = getCurrentUser();
        if (!chat.getUser().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Chat não encontrado ou acesso negado.");
        }
    }
}