package com.example.demo.repository;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    // Check if friend request already exists between two users
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "(fr.sender = :user1 AND fr.receiver = :user2) OR " +
           "(fr.sender = :user2 AND fr.receiver = :user1)")
    Optional<FriendRequest> findBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    // Get all friend requests sent by a user
    List<FriendRequest> findBySender(User sender);
    
    // Get all friend requests received by a user
    List<FriendRequest> findByReceiver(User receiver);
    
    // Get friend requests by status for a user (both sent and received)
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "(fr.sender = :user OR fr.receiver = :user) AND fr.status = :status")
    List<FriendRequest> findByUserAndStatus(@Param("user") User user, 
                                           @Param("status") FriendRequest.FriendRequestStatus status);
    
    // Get pending friend requests received by a user
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.FriendRequestStatus status);
}