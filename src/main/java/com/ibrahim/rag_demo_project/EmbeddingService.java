package com.ibrahim.rag_demo_project;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EmbeddingService {

    private final ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    LawRepository lawRepository;

    public EmbeddingService( ChatClient.Builder builder, LawRepository lawRepository) {
        this.chatClient = builder.build();
        this.lawRepository = lawRepository;

    }

    public String generateEmbedding() {
        List<Law> laws = lawRepository.findAll();

        List<Document> doc = laws.stream()
                .filter(law -> law.getContent() != null && !law.getContent().isBlank()) // ✅ تجاهل القوانين بدون نص
                .map(law -> Document.builder()
                        .id(UUID.randomUUID().toString()) // ✅ لازم UUID للـ PgVector
                        .text(law.getContent())
                        .metadata(Map.of(
                                "lawId", String.valueOf(law.getId()), // ✅ تخزين id القانون
                                "datePublished", law.getDatePublished() != null ? law.getDatePublished().toString() : "N/A"
                        ))
                        .build())
                .toList();

        if (doc.isEmpty()) {
            return "no valid laws to embed"; // ✅ لو ما في قوانين صالحة
        }

        vectorStore.add(doc);
        return "done";
    }

}

