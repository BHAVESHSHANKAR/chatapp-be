package com.example.demo.controller;

import com.example.demo.dto.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        try {
            System.out.println("📨 Received message: " + chatMessage.getContent() +
                    " from " + chatMessage.getSenderUsername() +
                    " to " + chatMessage.getReceiverUsername());

            // Save message to database
            ChatMessage savedMessage = chatService.saveMessage(chatMessage);

            System.out.println("💾 Message saved with ID: " + savedMessage.getId());

            // Send to receiver
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiverUsername(),
                    "/queue/messages",
                    savedMessage);

            // Send confirmation back to sender
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getSenderUsername(),
                    "/queue/messages",
                    savedMessage);

            System.out.println("📤 Message sent to both users");

        } catch (Exception e) {
            System.err.println("❌ Error processing message: " + e.getMessage());
            e.printStackTrace();

            // Send error message back to sender
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setContent("Failed to send message: " + e.getMessage());
            errorMessage.setType(ChatMessage.Type.CHAT);
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderUsername(),
                    "/queue/errors",
                    errorMessage);
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

}