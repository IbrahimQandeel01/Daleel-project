package com.ibrahim.rag_demo_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin()
public class OpenAiController {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiController.class);

    ChatClient chatClient ;
    @Autowired
    VectorStore vectorStore ;

    private HistoryMessage historyMessage ;

    public OpenAiController (ChatClient.Builder builder, HistoryMessage historyMessage) {
        this.historyMessage = historyMessage ;
        this.chatClient = builder
                .build() ;
        logger.info("VertexAI Controller initialized successfully");
    }

    @PostMapping("/api/rag")
    public String hadleRagRequest(@RequestParam String userInput) {
        logger.info("Processing RAG request: '{}'", userInput);

        if (userInput == null || userInput.trim().isEmpty()) {
            return "Please provide a valid question.";
        }

        try {
            String aiModel = System.getenv("VERTEX_AI_CHAT_MODEL");
            if (aiModel == null || aiModel.isEmpty()) {
                aiModel = "gemini-1.5-flash"; // Default model
            }
            logger.info("the vertex ai chat Model is: '{}'", aiModel);
            String response = askQuestion(userInput);

            logger.info("RAG request completed successfully");
            return response;

        } catch (Exception e) {
            logger.error("Error processing RAG request", e);
            return "Sorry, I encountered an error processing your request. Please try again.";
        }
    }





    @GetMapping("/api/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("App is healthy now");
        return ResponseEntity.ok("App is healthy");
    }

    private String askQuestion(String question) {


        var history = historyMessage.getAllMessages().stream()
                .map(m -> {
                    if ("user".equalsIgnoreCase(m.getRole())) {
                        return new UserMessage(m.getContent());
                    } else {
                        return new AssistantMessage(m.getContent());
                    }
                })
                .toList();


        var query = Query.builder()
                .text(question)
                .history(history.toArray(new org.springframework.ai.chat.messages.Message[0]))
                .build();

        var queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();



        var transformedQuery = queryTransformer.transform(query);


        logger.info("Query: {}", transformedQuery);

        String content = chatClient.prompt()
                .system("\n" +
                        "                        - أنت مساعد قانوني متخصص في قانون الجرائم الإلكترونية الأردني لعام 2023.\n" +
                        "                        - أجب بدقة وباختصار اعتماداً فقط على (النصوص) في (السياق) التالي.\n" +
                        "                        - (إن قمت باستخدام مواد فقط) (أرقام مواد) فاذكرها بصيغة: (المادة X).\n" +
                        "                        - لا تخترع مواد أو عقوبات غير موجودة.\n" +
                        "                        - لا تترجم النص العربي.\n" +
                        "                        - فقط اذا كان السؤال باللغة الإنجليزية اجب عليه باللغة الانجليزية.\n" +
                        "                        - قد تجد بالسياق نصوص من قوانين مختلقة، عليك فقط الاجابة بناءا على القوانين المتعلقة بالسؤال.\n" +
                        "                        - لا تهلوس بالإجابة.\n" +
                        "                        - كن لطيفًا بالإجابة." +
                        "خاص بالقانون الاردني فقط")
                .user(transformedQuery.text())
                .call()
                .content();

        logger.info("Question answer: {}", content);

        historyMessage.saveMessage("user", question);
        historyMessage.saveMessage("assistant", content);

        return content;

    }

}
