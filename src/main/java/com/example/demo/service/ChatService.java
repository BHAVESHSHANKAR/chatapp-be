package com.example.demo.service;

import com.example.demo.dto.ChatMessage;
import com.example.demo.model.ChatRoom;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        try {
            // Validate input
            if (chatMessage == null) {
                throw new IllegalArgumentException("Chat message cannot be null");
            }
            if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Message content cannot be empty");
            }
            if (chatMessage.getSenderId() == null || chatMessage.getReceiverId() == null) {
                throw new IllegalArgumentException("Sender and receiver IDs are required");
            }

            User sender = userRepository.findById(chatMessage.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Sender not found with ID: " + chatMessage.getSenderId()));
            User receiver = userRepository.findById(chatMessage.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found with ID: " + chatMessage.getReceiverId()));

            // Create or get chat room (optimized with caching)
            ChatRoom chatRoom = getOrCreateChatRoomOptimized(sender, receiver);

            // Encrypt message content
            String encryptedContent = encryptionUtil.encrypt(chatMessage.getContent().trim());

            // Create and save message
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setEncryptedContent(encryptedContent);
            
            // Set message type with default fallback
            String messageType = chatMessage.getMessageType();
            if (messageType == null || messageType.trim().isEmpty()) {
                messageType = "TEXT";
            }
            message.setMessageType(Message.MessageType.valueOf(messageType.toUpperCase()));
            
            message.setCreatedAt(chatMessage.getTimestamp() != null ? chatMessage.getTimestamp() : LocalDateTime.now());
            message.setIsRead(false);

            Message savedMessage = messageRepository.save(message);

            // Update chat room last message time asynchronously
            updateChatRoomAsync(chatRoom, savedMessage.getCreatedAt());

            // Convert to DTO and decrypt for response
            return convertToDTO(savedMessage);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save message: " + e.getMessage(), e);
        }
    }

    private void updateChatRoomAsync(ChatRoom chatRoom, LocalDateTime timestamp) {
        // Update chat room in background to avoid blocking
        try {
            chatRoom.setLastMessageAt(timestamp);
            chatRoomRepository.save(chatRoom);
        } catch (Exception e) {
            // Silent fail for background operations
        }
    }

    private ChatRoom getOrCreateChatRoomOptimized(User user1, User user2) {
        // Try to find existing room first
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUsers(user1, user2);

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Create new room if not exists
        ChatRoom newRoom = new ChatRoom();
        newRoom.setUser1(user1);
        newRoom.setUser2(user2);
        newRoom.setLastMessageAt(LocalDateTime.now());
        return chatRoomRepository.save(newRoom);
    }

    public List<ChatMessage> getMessagesBetweenUsers(Long userId1, Long userId2, int page, int size) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findMessagesBetweenUsers(user1, user2, pageable);

        return messages.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        List<Message> unreadMessages = messageRepository.findMessagesBetweenUsers(sender, receiver)
                .stream()
                .filter(m -> m.getReceiver().equals(receiver) && !m.getIsRead())
                .collect(Collectors.toList());

        unreadMessages.forEach(message -> message.setIsRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    public Long getUnreadMessageCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.countUnreadMessages(user);
    }

    private ChatMessage convertToDTO(Message message) {
        ChatMessage dto = new ChatMessage();
        dto.setId(message.getId());
        dto.setContent(encryptionUtil.decrypt(message.getEncryptedContent()));
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setReceiverUsername(message.getReceiver().getUsername());
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setSenderProfileImageUrl(message.getSender().getProfileImageUrl());
        dto.setReceiverProfileImageUrl(message.getReceiver().getProfileImageUrl());
        dto.setMessageType(message.getMessageType().toString());
        dto.setTimestamp(message.getCreatedAt());
        dto.setIsRead(message.getIsRead());

        // Generate room ID
        Long smallerId = Math.min(message.getSender().getId(), message.getReceiver().getId());
        Long largerId = Math.max(message.getSender().getId(), message.getReceiver().getId());
        dto.setRoomId(smallerId + "_" + largerId);

        return dto;
    }
}