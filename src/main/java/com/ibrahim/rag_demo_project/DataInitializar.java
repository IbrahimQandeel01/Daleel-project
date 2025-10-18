//package com.ibrahim.rag_demo_project;
//
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//public class DataInitializar {
//
//    private static final Logger logger = LoggerFactory.getLogger(DataInitializar.class);
//
//    @Autowired
//    private VectorStore vectorStore;
//
//    private static boolean isDataInitEnabled() {
//        String v = System.getenv("RUN_DATAINITIALIZAR_CLASS_OR_NOT");
//        boolean enabled = "true".equalsIgnoreCase(v) || "1".equals(v);
//        logger.info("Data initialization enabled: {} (env variable: {})", enabled, v);
//        return enabled;
//    }
//
//    /*
//        @Autowired
//    private VectorStore vectorStore;
//
//    @PostConstruct
//    public void initData() {
//        TextReader textReader = new TextReader(new ClassPathResource("product_details.txt"));
//
////        TokenTextSplitter splitter = new TokenTextSplitter();
//        TokenTextSplitter splitter = new TokenTextSplitter(100, 30, 5, 500, false);
//        List<Document> documents
//                = splitter.split(textReader.get());
//        vectorStore.add(documents);
//
//    }
//}*/
//
//    @PostConstruct
//    public void dataInit() {
//        logger.info("Starting data initialization process");
//
//        if (!isDataInitEnabled()) {
//            logger.info("Data initialization is disabled via environment variable. Skipping data initialization.");
//            return;
//        }
//
//        try {
//            logger.info("Reading product details from classpath resource: product_details.txt");
//            // Read the entire file content directly from the classpath resource
//            String content;
//            try (InputStream is = new ClassPathResource("product_details.txt").getInputStream()) {
//                content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//                logger.debug("Successfully read {} characters from product_details.txt", content.length());
//            } catch (Exception e) {
//                logger.error("Failed to read product_details.txt from classpath", e);
//                throw new RuntimeException("Failed to read product_details.txt", e);
//            }
//
//        // Split the content into product-sized chunks separated by one or more blank lines
//        String[] blocks = content.split("(\\r?\\n){2,}");
//        List<Document> documents = new ArrayList<>();
//        Pattern titlePattern = Pattern.compile("Title:\\s*\"([^\"]+)\"");
//            int processedCount = documents.size();
//        for (String block : blocks) {
//            String trimmed = block.trim();
//            if (trimmed.isEmpty()) continue;
//            Map<String, Object> metadata = new HashMap<>();
//            Matcher m = titlePattern.matcher(trimmed);
//            if (m.find()) {
//                metadata.put("title", m.group(1));
//            }
//            documents.add(new Document(trimmed, metadata));
//
//        }
//
//            logger.info("Successfully processed {} documents for vector storage", processedCount);
//            logger.info("Adding {} documents to vector store", documents.size());
//
//            vectorStore.add(documents);
//
//            logger.info("Data initialization completed successfully. {} documents added to vector store", documents.size());
//
//        } catch (Exception e) {
//            logger.error("Data initialization failed with error", e);
//            throw e;
//        }
//    }
//}