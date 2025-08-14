package com.example.demo.controller;

import com.example.demo.dto.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // Send message immediately to both users for instant delivery
        sendMessageInstantly(chatMessage);
        
        // Save to database asynchronously to avoid blocking
        saveMessageAsync(chatMessage);
    }
    
    private void sendMessageInstantly(ChatMessage chatMessage) {
        try {
            // Add timestamp for immediate delivery
            chatMessage.setTimestamp(java.time.LocalDateTime.now());
            
            // Send to receiver immediately
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverUsername(),
                    "/queue/messages",
                    chatMessage);

            // Send confirmation back to sender immediately
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderUsername(),
                    "/queue/messages",
                    chatMessage);

        } catch (Exception e) {
            sendErrorMessage(chatMessage.getSenderUsername(), "Failed to send message");
        }
    }
    
    @Async("taskExecutor")
    public CompletableFuture<Void> saveMessageAsync(ChatMessage chatMessage) {
        try {
            // Save message to database in background
            ChatMessage savedMessage = chatService.saveMessage(chatMessage);
            
            // Update the message with database ID if needed
            if (savedMessage.getId() != null) {
                // Send updated message with real ID to both users
                messagingTemplate.convertAndSendToUser(
                        savedMessage.getReceiverUsername(),
                        "/queue/message-update",
                        savedMessage);
                        
                messagingTemplate.convertAndSendToUser(
                        savedMessage.getSenderUsername(),
                        "/queue/message-update",
                        savedMessage);
            }
            
        } catch (Exception e) {
            // Silent fail for background operations to avoid disrupting UX
        }
        return CompletableFuture.completedFuture(null);
    }
    
    private void sendErrorMessage(String username, String errorText) {
        ChatMessage errorMessage = new ChatMessage();
        errorMessage.setContent(errorText);
        errorMessage.setType(ChatMessage.Type.CHAT);  // Use CHAT type for errors
        errorMessage.setMessageType("ERROR");
        messagingTemplate.convertAndSendToUser(username, "/queue/errors", errorMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage) {
        // Notify that user joined
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessage chatMessage) {
        // Send typing indicator to receiver
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverUsername(),
                "/queue/typing",
                chatMessage);
    }

}