package com.ibrahim.rag_demo_project;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataInitializar_v2 {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializar_v2.class);

    @Autowired
    private VectorStore vectorStore;

    private static boolean isDataInitEnabled() {
        String v = System.getenv("RUN_DATAINITIALIZAR_CLASS_OR_NOT");
        boolean enabled = "true".equalsIgnoreCase(v) || "1".equals(v);
        logger.info("Data initialization enabled: {} (env variable: {})", enabled, v);
        return enabled;
    }

    @PostConstruct
    public void dataInit() {

        if (!isDataInitEnabled()) {
            logger.info("Data initialization is disabled via environment variable. Skipping data initialization.");
            return;
        }

        logger.info("Starting data initialization process");

        try {
            logger.info("Reading legal text from classpath resource: file-text");
            // Read the entire file content directly from the classpath resource
            String content;
            try (InputStream is = new ClassPathResource("product_details.txt").getInputStream()) {
                content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                logger.debug("Successfully read {} characters from file-text", content.length());
            } catch (Exception e) {
                logger.error("Failed to read file-text from classpath", e);
                throw new RuntimeException("Failed to read file-text", e);
            }

            // Split the content into chunks based on (*****) separator
            String[] blocks = content.split("\\*{5}");
            List<Document> documents = new ArrayList<>();
            logger.info("all blocks"+ Arrays.stream(blocks).toList());

            // Pattern to extract article number from each block
            Pattern articlePattern = Pattern.compile("(المادة|مادة)\\s+(\\d+)");

            for (int i = 0; i < blocks.length; i++) {
                String trimmed = blocks[i].trim();
                if (trimmed.isEmpty()) continue;

                Map<String, Object> metadata = new HashMap<>();

                // Extract article number if found
                Matcher matcher = articlePattern.matcher(trimmed);
                if (matcher.find()) {
                    metadata.put("article_number", matcher.group(2));
                    metadata.put("article_type", "legal_article");
                }

                // Add chunk index for reference
                metadata.put("chunk_index", i);
                metadata.put("source", "cyber_crimes_law_2023");

                documents.add(new Document(trimmed, metadata));
                logger.debug("Created document for chunk {} with article number: {}",
                    i, metadata.get("article_number"));
            }

            logger.info("Successfully processed {} documents for vector storage", documents.size());
            logger.info("Adding {} documents to vector store", documents.size());

            vectorStore.add(documents);

            logger.info("Data initialization completed successfully. {} documents added to vector store", documents.size());

        } catch (Exception e) {
            logger.error("Data initialization failed with error", e);
            throw e;
        }
    }
}