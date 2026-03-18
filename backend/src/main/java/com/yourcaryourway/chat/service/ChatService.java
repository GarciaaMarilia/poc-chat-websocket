package com.yourcaryourway.chat.service;

import com.yourcaryourway.chat.model.ChatMessage;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    public ChatMessage processMessage(ChatMessage message) {
        // Dans le PoC, le message est retourné tel quel.
        // En production : persistance en base (SupportMessage),
        // association à un ticket (SupportTicket), etc.
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
