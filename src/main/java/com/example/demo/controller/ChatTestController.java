package com.example.demo.controller;

import com.example.demo.dto.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatTestController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<?> testSendMessage(@RequestBody ChatMessage chatMessage) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (chatMessage == null) {
                response.put("success", false);
                response.put("error", "Chat message is null");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Message content is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (chatMessage.getSenderId() == null || chatMessage.getReceiverId() == null) {
                response.put("success", false);
                response.put("error", "Sender or receiver ID is missing");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Set defaults if missing
            if (chatMessage.getMessageType() == null) {
                chatMessage.setMessageType("TEXT");
            }
            
            if (chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(LocalDateTime.now());
            }
            
            // Try to save the message
            ChatMessage savedMessage = chatService.saveMessage(chatMessage);
            
            response.put("success", true);
            response.put("message", "Message saved successfully");
            response.put("savedMessage", savedMessage);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getClass().getSimpleName());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Chat service is running");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}