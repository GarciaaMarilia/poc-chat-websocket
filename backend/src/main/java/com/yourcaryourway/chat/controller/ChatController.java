package com.yourcaryourway.chat.controller;

import com.yourcaryourway.chat.model.ChatMessage;
import com.yourcaryourway.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Reçoit un message d'un utilisateur et le diffuse à tous les abonnés.
     * Flux : client envoie sur /app/chat.send → serveur broadcast sur /topic/chat
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatMessage sendMessage(@Valid ChatMessage message) {
        log.debug("Message reçu de {} : {}", message.getSenderName(), message.getContent());
        return chatService.processMessage(message);
    }

    /**
     * Notifie tous les abonnés qu'un utilisateur a rejoint le chat.
     */
    @MessageMapping("/chat.join")
    @SendTo("/topic/chat")
    public ChatMessage joinChat(@Valid ChatMessage message) {
        log.debug("{} a rejoint le chat", message.getSenderName());
        return chatService.buildJoinNotification(message.getSenderName());
    }
}
