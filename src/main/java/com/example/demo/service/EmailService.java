package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.name}")
    private String fromName;

    @Async("taskExecutor")
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            System.out.println("Attempting to send welcome email to: " + toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to PlayChat! 🎉");

            String htmlContent = buildWelcomeEmailContent(username);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Welcome email sent successfully to: " + toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Failed to send welcome email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    @Async("taskExecutor")
    public void sendFriendRequestEmail(String toEmail, String toUsername, String fromUsername, String fromEmail) {
        try {
            System.out.println("Attempting to send friend request email to: " + toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(this.fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("New Friend Request from " + fromUsername + " 👋");

            String htmlContent = buildFriendRequestEmailContent(toUsername, fromUsername, fromEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Friend request email sent successfully to: " + toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Failed to send friend request email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send friend request email", e);
        }
    }

    private String buildWelcomeEmailContent(String username) {
        String template = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to PlayChat</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #0F2027, #2c5364); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; background: linear-gradient(135deg, #0F2027, #2c5364); color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Welcome to PlayChat!</h1>
                            <p>Your journey to amazing conversations starts here</p>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>We're thrilled to have you join our PlayChat community! You've successfully created your account and you're all set to start connecting with friends.</p>

                            <h3>What you can do now:</h3>
                            <ul>
                                <li>🔍 Search for friends by username or email</li>
                                <li>💬 Send real-time encrypted messages</li>
                                <li>📸 Upload and share your profile picture</li>
                                <li>🔒 Enjoy end-to-end encrypted conversations</li>
                                <li>📱 Access your chats from anywhere</li>
                            </ul>

                            <p>Ready to start chatting? Click the button below to log in to your account:</p>
                            <a href="#" class="button">Start Chatting Now</a>

                            <p>If you have any questions or need help getting started, feel free to reach out to our support team.</p>

                            <p>Happy chatting! 🚀</p>
                            <p><strong>The PlayChat Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>This email was sent to you because you created an account on PlayChat.</p>
                            <p>© 2024 PlayChat. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
        return String.format(template, username);
    }

    private String buildFriendRequestEmailContent(String toUsername, String fromUsername, String fromEmail) {
        String template = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>New Friend Request</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #0F2027, #2c5364); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .friend-card { background: white; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #2c5364; }
                        .button { display: inline-block; background: linear-gradient(135deg, #0F2027, #2c5364); color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; margin: 10px 5px; }
                        .button.secondary { background: #6c757d; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>👋 New Friend Request!</h1>
                            <p>Someone wants to connect with you</p>
                        </div>
                        <div class="content">
                            <h2>Hello %s!</h2>
                            <p>You have received a new friend request on PlayChat!</p>

                            <div class="friend-card">
                                <h3>📨 Friend Request Details:</h3>
                                <p><strong>From:</strong> %s</p>
                                <p><strong>Email:</strong> %s</p>
                                <p><strong>To:</strong> %s</p>
                            </div>

                            <p>%s would like to connect with you and start chatting. You can accept or decline this request by logging into your PlayChat account.</p>

                            <div style="text-align: center; margin: 30px 0;">
                                <a href="#" class="button">View Friend Request</a>
                                <a href="#" class="button secondary">Log in to PlayChat</a>
                            </div>

                            <p><strong>Why connect?</strong></p>
                            <ul>
                                <li>💬 Start real-time conversations</li>
                                <li>🔒 Enjoy encrypted messaging</li>
                                <li>📱 Chat anytime, anywhere</li>
                                <li>🎉 Share moments with friends</li>
                            </ul>

                            <p>Don't keep them waiting - log in now to respond to their request!</p>

                            <p>Best regards,<br><strong>The PlayChat Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>This email was sent because someone sent you a friend request on PlayChat.</p>
                            <p>© 2024 PlayChat. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
        return String.format(template, toUsername, fromUsername, fromEmail, toUsername, fromUsername);
    }
}