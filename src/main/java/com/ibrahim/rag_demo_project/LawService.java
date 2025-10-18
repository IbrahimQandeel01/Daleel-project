package com.ibrahim.rag_demo_project;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class LawService {

    @Autowired
    private LawRepository lawRepository;

    @Autowired
    private EmbeddingService embeddingService;

    public Law addNewLaw(String request ) {


        String embedding = embeddingService.generateEmbedding();

        log.info("Embedding: {}", embedding);
        Law law = new Law();
        law.setContent(request);
        law.setDatePublished(LocalDate.now());

        return lawRepository.save(law);
    }
}

