package com.example.demo.controller;

import com.example.demo.dto.UserSearchResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam String query,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        List<User> users = userRepository.searchUsers(query);
        
        // Filter out current user from search results
        List<UserSearchResponse> response = users.stream()
            .filter(user -> !user.getEmail().equals(currentUserEmail))
            .map(user -> new UserSearchResponse(user.getId(), user.getUsername(), user.getEmail(), user.getProfileImageUrl()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}