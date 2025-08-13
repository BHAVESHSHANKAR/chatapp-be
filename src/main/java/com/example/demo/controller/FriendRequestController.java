package com.example.demo.controller;

import com.example.demo.dto.FriendRequestResponse;
import com.example.demo.model.FriendRequest;
import com.example.demo.service.FriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendRequestController {

    @Autowired
    private FriendRequestService friendRequestService;

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<?> sendFriendRequest(
            @PathVariable Long receiverId,
            Authentication authentication) {
        try {
            String senderEmail = authentication.getName();
            FriendRequest friendRequest = friendRequestService.sendFriendRequest(senderEmail, receiverId);
            return ResponseEntity.ok(new FriendRequestResponse(friendRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/respond/{requestId}")
    public ResponseEntity<?> respondToFriendRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, Boolean> response,
            Authentication authentication) {
        try {
            String receiverEmail = authentication.getName();
            boolean accept = response.getOrDefault("accept", false);
            
            FriendRequest friendRequest = friendRequestService.respondToFriendRequest(
                requestId, receiverEmail, accept);
            
            return ResponseEntity.ok(new FriendRequestResponse(friendRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendRequestResponse>> getPendingRequests(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<FriendRequest> requests = friendRequestService.getPendingFriendRequests(userEmail);
            
            List<FriendRequestResponse> response = requests.stream()
                .map(FriendRequestResponse::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
        }
    }

    @GetMapping("/sent")
    public ResponseEntity<List<FriendRequestResponse>> getSentRequests(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<FriendRequest> requests = friendRequestService.getSentFriendRequests(userEmail);
            
            List<FriendRequestResponse> response = requests.stream()
                .map(FriendRequestResponse::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FriendRequestResponse>> getFriends(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<FriendRequest> friends = friendRequestService.getFriends(userEmail);
            
            List<FriendRequestResponse> response = friends.stream()
                .map(FriendRequestResponse::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
        }
    }
}