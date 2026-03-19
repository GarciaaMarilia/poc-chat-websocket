package com.yourcaryourway.chat.service;

import com.yourcaryourway.chat.model.ChatMessage;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

       public ChatMessage processMessage(ChatMessage message) {
        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new IllegalArgumentException("Le contenu du message ne peut pas être vide");
        }
        return message;
    }

    public ChatMessage buildJoinNotification(String senderName) {
        return ChatMessage.builder()
                .senderName("Système")
                .senderType(ChatMessage.SenderType.AGENT)
                .content(senderName + " a rejoint le chat.")
                .build();
    }
}
