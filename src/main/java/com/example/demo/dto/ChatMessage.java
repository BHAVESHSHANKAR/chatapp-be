package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;
    private String content;
    private String senderUsername;
    private String receiverUsername;
    private Long senderId;
    private Long receiverId;
    private String senderProfileImageUrl;
    private String receiverProfileImageUrl;
    private String messageType;
    private LocalDateTime timestamp;
    private Boolean isRead;
    private String roomId;
    
    public enum Type {
        CHAT, JOIN, LEAVE, TYPING, STOP_TYPING
    }
    
    private Type type = Type.CHAT;
}