package com.yourcaryourway.chat.service;

import com.yourcaryourway.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatServiceTest {

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService();
    }

    @Test
    void processMessage_shouldReturnMessageUnchanged() {
        ChatMessage message = ChatMessage.builder()
                .senderName("Alice")
                .senderType(ChatMessage.SenderType.USER)
                .content("Bonjour, j'ai un problème avec ma réservation.")
                .build();

        ChatMessage result = chatService.processMessage(message);

        assertThat(result.getContent()).isEqualTo(message.getContent());
        assertThat(result.getSenderName()).isEqualTo("Alice");
    }

    @Test
    void buildJoinNotification_shouldIncludeSenderName() {
        ChatMessage notification = chatService.buildJoinNotification("Alice");

        assertThat(notification.getContent()).contains("Alice");
        assertThat(notification.getSenderType()).isEqualTo(ChatMessage.SenderType.AGENT);
    }
}
