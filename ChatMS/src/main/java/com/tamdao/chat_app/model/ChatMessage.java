package com.tamdao.chat_app.model;

import jakarta.persistence.*;
import lombok.*;

import java.awt.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String content;

    private String sender;

    private String recipient;

    private String color;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    public enum MessageType {
        CHAT, PRIVATE_MESSAGE, JOIN, LEAVE, TYPING
    }
}
