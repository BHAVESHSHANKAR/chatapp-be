package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.ImageUploadService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ImageUploadService imageUploadService;

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("image") MultipartFile file,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select an image file");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Please upload a valid image file");
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("Image size should be less than 5MB");
            }

            // Delete old profile image if exists
            if (user.getProfileImageUrl() != null) {
                imageUploadService.deleteProfileImage(user.getProfileImageUrl());
            }

            // Upload new image
            String imageUrl = imageUploadService.uploadProfileImage(file, user.getUsername());
            
            // Update user profile
            user.setProfileImageUrl(imageUrl);
            userService.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile image updated successfully");
            response.put("profileImageUrl", imageUrl);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "profileImageUrl", imageUrl
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/delete-image")
    public ResponseEntity<?> deleteProfileImage(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Delete image from Cloudinary
            if (user.getProfileImageUrl() != null) {
                imageUploadService.deleteProfileImage(user.getProfileImageUrl());
                user.setProfileImageUrl(null);
                userService.save(user);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile image deleted successfully");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "profileImageUrl", (Object) null
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : null
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get user profile: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}