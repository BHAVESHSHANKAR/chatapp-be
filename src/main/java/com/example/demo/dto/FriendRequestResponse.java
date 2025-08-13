package com.example.demo.dto;

import com.example.demo.model.FriendRequest;
import java.time.LocalDateTime;

public class FriendRequestResponse {
    private Long id;
    private UserSearchResponse sender;
    private UserSearchResponse receiver;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FriendRequestResponse(FriendRequest friendRequest) {
        this.id = friendRequest.getId();
        this.sender = new UserSearchResponse(
                friendRequest.getSender().getId(),
                friendRequest.getSender().getUsername(),
                friendRequest.getSender().getEmail(),
                friendRequest.getSender().getProfileImageUrl());
        this.receiver = new UserSearchResponse(
                friendRequest.getReceiver().getId(),
                friendRequest.getReceiver().getUsername(),
                friendRequest.getReceiver().getEmail(),
                friendRequest.getReceiver().getProfileImageUrl());
        this.status = friendRequest.getStatus().toString();
        this.createdAt = friendRequest.getCreatedAt();
        this.updatedAt = friendRequest.getUpdatedAt();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserSearchResponse getSender() {
        return sender;
    }

    public void setSender(UserSearchResponse sender) {
        this.sender = sender;
    }

    public UserSearchResponse getReceiver() {
        return receiver;
    }

    public void setReceiver(UserSearchResponse receiver) {
        this.receiver = receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}