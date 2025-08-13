package com.example.demo.controller;

import com.example.demo.dto.LoginResponse;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.service.EmailService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            
            // Send welcome email asynchronously
            try {
                emailService.sendWelcomeEmail(registeredUser.getEmail(), registeredUser.getUsername());
            } catch (Exception e) {
                // Log the error but don't fail the registration
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }
            
            // Don't return password in response
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> loggedInUser = userService.loginUser(user.getEmail(), user.getPassword());
        if (loggedInUser.isPresent()) {
            User authenticatedUser = loggedInUser.get();
            String token = jwtUtil.generateToken(authenticatedUser.getEmail());
            
            LoginResponse response = new LoginResponse(
                token,
                authenticatedUser.getId(),
                authenticatedUser.getUsername(), 
                authenticatedUser.getEmail(),
                authenticatedUser.getProfileImageUrl()
            );
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
