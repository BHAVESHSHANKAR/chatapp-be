package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final Cloudinary cloudinary;
    private final EncryptionUtil encryptionUtil;

    public String uploadProfileImage(MultipartFile file, String username) throws IOException {
        try {
            // Generate unique filename with encryption
            String uniqueFilename = generateEncryptedFilename(username);

            // Upload to Cloudinary with transformation for profile pictures
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "public_id", "profile_pics/" + uniqueFilename,
                    "folder", "chat_app_profiles",
                    "width", 200,
                    "height", 200,
                    "crop", "fill",
                    "gravity", "face",
                    "quality", "auto:good",
                    "format", "jpg",
                    "secure", true,
                    "overwrite", true);

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    public void deleteProfileImage(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.contains("cloudinary.com")) {
                // Extract public_id from URL
                String publicId = extractPublicIdFromUrl(imageUrl);
                if (publicId != null) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                }
            }
        } catch (Exception e) {
            // Log error but don't throw - deletion failure shouldn't break profile update
            System.err.println("Failed to delete old profile image: " + e.getMessage());
        }
    }

    private String generateEncryptedFilename(String username) {
        try {
            // Create a unique identifier combining username and timestamp
            String identifier = username + "_" + System.currentTimeMillis();
            // Encrypt the identifier for security
            String encrypted = encryptionUtil.encrypt(identifier);
            // Replace special characters that might cause issues in URLs
            return encrypted.replaceAll("[^a-zA-Z0-9]", "") + "_" + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            // Fallback to simple UUID if encryption fails
            return "profile_" + UUID.randomUUID().toString();
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Extract public_id from Cloudinary URL
            // URL format:
            // https://res.cloudinary.com/cloud_name/image/upload/v123456/folder/public_id.jpg
            String[] parts = imageUrl.split("/");
            if (parts.length >= 2) {
                String filename = parts[parts.length - 1];
                String folder = parts[parts.length - 2];
                // Remove file extension
                String publicId = filename.split("\\.")[0];
                return folder + "/" + publicId;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract public_id from URL: " + imageUrl);
        }
        return null;
    }
}