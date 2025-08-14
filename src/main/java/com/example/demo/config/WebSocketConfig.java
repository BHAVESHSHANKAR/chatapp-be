package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableWebSocketMessageBroker
@EnableAsync
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable optimized simple broker with task scheduler
        config.enableSimpleBroker("/topic", "/queue")
              .setHeartbeatValue(new long[]{10000, 10000})
              .setTaskScheduler(taskScheduler());
        
        // Designate the "/app" prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections with optimized settings
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(15000)  // Reduced for faster detection
                .setDisconnectDelay(2000)  // Faster disconnect detection
                .setStreamBytesLimit(262144)  // Increased buffer
                .setHttpMessageCacheSize(2000)  // Increased cache
                .setSessionCookieNeeded(false)
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Optimized thread pool for handling incoming messages
        registration.taskExecutor()
                   .corePoolSize(8)  // Increased core pool
                   .maxPoolSize(16)  // Increased max pool
                   .keepAliveSeconds(60)
                   .queueCapacity(1000);  // Added queue capacity
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Optimized thread pool for sending messages to clients
        registration.taskExecutor()
                   .corePoolSize(8)  // Increased core pool
                   .maxPoolSize(16)  // Increased max pool
                   .keepAliveSeconds(60)
                   .queueCapacity(1000);  // Added queue capacity
    }
    
    @Bean
    public org.springframework.scheduling.TaskScheduler taskScheduler() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler scheduler = 
            new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
    
    @Bean
    public java.util.concurrent.Executor taskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = 
            new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-chat-");
        executor.initialize();
        return executor;
    }
}