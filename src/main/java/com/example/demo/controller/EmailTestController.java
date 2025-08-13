package com.example.demo.controller;

import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/welcome-email")
    public ResponseEntity<?> testWelcomeEmail(@RequestParam String email, @RequestParam String username) {
        try {
            emailService.sendWelcomeEmail(email, username);
            return ResponseEntity.ok("Welcome email sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/friend-request-email")
    public ResponseEntity<?> testFriendRequestEmail(
            @RequestParam String toEmail,
            @RequestParam String toUsername,
            @RequestParam String fromUsername,
            @RequestParam String fromEmail) {
        try {
            emailService.sendFriendRequestEmail(toEmail, toUsername, fromUsername, fromEmail);
            return ResponseEntity.ok("Friend request email sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }
}