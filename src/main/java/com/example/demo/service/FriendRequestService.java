package com.example.demo.service;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import com.example.demo.repository.FriendRequestRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public FriendRequest sendFriendRequest(String senderEmail, Long receiverId) throws RuntimeException {
        Optional<User> sender = userRepository.findByEmail(senderEmail);
        Optional<User> receiver = userRepository.findById(receiverId);

        if (sender.isEmpty()) {
            throw new RuntimeException("Sender not found");
        }
        if (receiver.isEmpty()) {
            throw new RuntimeException("Receiver not found");
        }

        User senderUser = sender.get();
        User receiverUser = receiver.get();

        // Check if users are trying to send request to themselves
        if (senderUser.getId().equals(receiverUser.getId())) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        // Check if friend request already exists
        Optional<FriendRequest> existingRequest = friendRequestRepository.findBetweenUsers(senderUser, receiverUser);
        if (existingRequest.isPresent()) {
            FriendRequest existing = existingRequest.get();
            if (existing.getStatus() == FriendRequest.FriendRequestStatus.PENDING) {
                throw new RuntimeException("Friend request already pending");
            } else if (existing.getStatus() == FriendRequest.FriendRequestStatus.ACCEPTED) {
                throw new RuntimeException("You are already friends");
            }
        }

        FriendRequest friendRequest = new FriendRequest(senderUser, receiverUser);
        FriendRequest savedRequest = friendRequestRepository.save(friendRequest);
        
        // Send email notification to receiver asynchronously
        try {
            emailService.sendFriendRequestEmail(
                receiverUser.getEmail(), 
                receiverUser.getUsername(),
                senderUser.getUsername(),
                senderUser.getEmail()
            );
        } catch (Exception e) {
            // Log the error but don't fail the friend request
            System.err.println("Failed to send friend request email: " + e.getMessage());
        }
        
        return savedRequest;
    }

    public FriendRequest respondToFriendRequest(Long requestId, String receiverEmail, boolean accept) throws RuntimeException {
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new RuntimeException("Friend request not found");
        }

        FriendRequest friendRequest = requestOpt.get();

        // Verify the receiver is the one responding
        if (!friendRequest.getReceiver().getEmail().equals(receiverEmail)) {
            throw new RuntimeException("You can only respond to friend requests sent to you");
        }

        // Check if request is still pending
        if (friendRequest.getStatus() != FriendRequest.FriendRequestStatus.PENDING) {
            throw new RuntimeException("Friend request has already been responded to");
        }

        // Update status
        friendRequest.setStatus(accept ? 
            FriendRequest.FriendRequestStatus.ACCEPTED : 
            FriendRequest.FriendRequestStatus.REJECTED);

        return friendRequestRepository.save(friendRequest);
    }

    public List<FriendRequest> getPendingFriendRequests(String userEmail) {
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return friendRequestRepository.findByReceiverAndStatus(
            user.get(), 
            FriendRequest.FriendRequestStatus.PENDING
        );
    }

    public List<FriendRequest> getSentFriendRequests(String userEmail) {
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return friendRequestRepository.findBySender(user.get());
    }

    public List<FriendRequest> getFriends(String userEmail) {
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return friendRequestRepository.findByUserAndStatus(
            user.get(), 
            FriendRequest.FriendRequestStatus.ACCEPTED
        );
    }
}