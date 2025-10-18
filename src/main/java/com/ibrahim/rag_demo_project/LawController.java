package com.ibrahim.rag_demo_project;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibrahim.rag_demo_project.Law;

@RestController
@RequestMapping("/laws-add")
public class LawController {

    @Autowired
    private LawService lawService;

    @PostMapping
    public ResponseEntity<Law,HttpStatus> addLaw(@RequestBody String request) {
        Law law = lawService.addNewLaw(
                request
        );
        return new ResponseEntity<>(law, HttpStatus.OK);
    }
}


