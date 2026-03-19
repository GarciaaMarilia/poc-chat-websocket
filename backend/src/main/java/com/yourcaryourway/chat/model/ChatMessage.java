package com.yourcaryourway.chat.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    public enum SenderType {
        USER, AGENT
    }

    @NotBlank
    @Size(max = 100)
    private String senderName;

    private SenderType senderType;

    @Size(max = 1000)
    private String content;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
