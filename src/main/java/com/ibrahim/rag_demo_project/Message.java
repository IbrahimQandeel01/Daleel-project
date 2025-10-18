package com.ibrahim.rag_demo_project;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String role;
    @Column(columnDefinition = "TEXT")
    String content;

    LocalDateTime time;


}