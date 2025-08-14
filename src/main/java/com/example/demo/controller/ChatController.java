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
        try {
            // Validate message
            if (chatMessage == null || chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
                sendErrorMessage(chatMessage != null ? chatMessage.getSenderUsername() : "unknown",
                        "Message content cannot be empty");
                return;
            }

            if (chatMessage.getSenderUsername() == null || chatMessage.getReceiverUsername() == null) {
                sendErrorMessage(chatMessage.getSenderUsername(), "Invalid sender or receiver");
                return;
            }

            // Send message immediately to both users for instant delivery
            sendMessageInstantly(chatMessage);

            // Save to database asynchronously to avoid blocking
            saveMessageAsync(chatMessage);

        } catch (Exception e) {
            sendErrorMessage(chatMessage != null ? chatMessage.getSenderUsername() : "unknown",
                    "Failed to send message: " + e.getMessage());
        }
    }

    private void sendMessageInstantly(ChatMessage chatMessage) {
        try {
            // Add timestamp for immediate delivery
            if (chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(java.time.LocalDateTime.now());
            }

            // Set default type if not set
            if (chatMessage.getType() == null) {
                chatMessage.setType(ChatMessage.Type.CHAT);
            }

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
            sendErrorMessage(chatMessage.getSenderUsername(), "Failed to send message: " + e.getMessage());
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> saveMessageAsync(ChatMessage chatMessage) {
        try {
            // Save message to database in background
            ChatMessage savedMessage = chatService.saveMessage(chatMessage);

            // Update the message with database ID if needed
            if (savedMessage != null && savedMessage.getId() != null) {
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
            // Send error notification to sender
            sendErrorMessage(chatMessage.getSenderUsername(), "Message sent but failed to save to database");
        }
        return CompletableFuture.completedFuture(null);
    }

    private void sendErrorMessage(String username, String errorText) {
        try {
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setContent(errorText);
            errorMessage.setType(ChatMessage.Type.CHAT);
            errorMessage.setMessageType("ERROR");
            errorMessage.setTimestamp(java.time.LocalDateTime.now());
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", errorMessage);
        } catch (Exception e) {
            // Silent error handling
        }
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

    @MessageMapping("/ping")
    public void handlePing(@Payload Object pingMessage) {
        // Simple ping handler to keep connection alive
        // No response needed, just acknowledges the ping
    }

}