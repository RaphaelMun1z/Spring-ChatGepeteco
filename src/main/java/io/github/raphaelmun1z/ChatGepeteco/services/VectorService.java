package io.github.raphaelmun1z.ChatGepeteco.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.raphaelmun1z.ChatGepeteco.entities.Chat;
import io.github.raphaelmun1z.ChatGepeteco.entities.PdfIndex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VectorService {
    private final Map<String, List<VectorChunk>> index = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void indexPdf(String pdfId, String pdfText) {
        List<VectorChunk> chunks = splitIntoChunks(pdfText).stream()
                .map(chunk -> new VectorChunk(chunk, computeEmbedding(chunk)))
                .collect(Collectors.toList());
        index.put(pdfId, chunks);
    }

    @Transactional(readOnly = true)
    public List<String> findTopChunksForChat(Chat chat, String question, int k) {
        List<VectorChunk> allChunks = chat.getPdfIndexes().stream()
                .map(this::convertPdfIndexToChunk)
                .toList();

        index.values().forEach(allChunks::addAll);

        double[] questionEmbedding = computeEmbedding(question);

        return allChunks.stream()
                .sorted(Comparator.comparingDouble(c -> -cosineSimilarity(questionEmbedding, c.embedding)))
                .limit(k)
                .map(c -> c.text)
                .toList();
    }

    private VectorChunk convertPdfIndexToChunk(PdfIndex pdfIndex) {
        double[] embedding = parseEmbedding(pdfIndex.getEmbeddingJson());
        return new VectorChunk(pdfIndex.getChunkText(), embedding);
    }

    private List<String> splitIntoChunks(String text) {
        int chunkSize = 500;
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    private double[] computeEmbedding(String text) {
        return text.chars().mapToDouble(c -> c).toArray();
    }

    private double cosineSimilarity(double[] v1, double[] v2) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < Math.min(v1.length, v2.length); i++) {
            dot += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }

    private double[] parseEmbedding(String embeddingJson) {
        try {
            return objectMapper.readValue(embeddingJson, double[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao converter embedding JSON", e);
        }
    }

    private static class VectorChunk {
        String text;
        double[] embedding;

        public VectorChunk(String text, double[] embedding) {
            this.text = text;
            this.embedding = embedding;
        }
    }
}