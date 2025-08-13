package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class ServerStatusController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Server status
            status.put("server", "RUNNING");
            status.put("port", 8080);
            status.put("timestamp", LocalDateTime.now());
            status.put("uptime", getUptime());
            
            // Database status
            Map<String, Object> dbStatus = new HashMap<>();
            try (Connection connection = dataSource.getConnection()) {
                dbStatus.put("status", "CONNECTED");
                dbStatus.put("url", connection.getMetaData().getURL());
                dbStatus.put("driver", connection.getMetaData().getDriverName());
                dbStatus.put("version", connection.getMetaData().getDatabaseProductVersion());
            } catch (Exception e) {
                dbStatus.put("status", "DISCONNECTED");
                dbStatus.put("error", e.getMessage());
            }
            status.put("database", dbStatus);
            
            // WebSocket status
            Map<String, Object> wsStatus = new HashMap<>();
            wsStatus.put("status", "ACTIVE");
            wsStatus.put("endpoint", "/ws");
            wsStatus.put("protocols", new String[]{"STOMP", "SockJS"});
            status.put("websocket", wsStatus);
            
            // Memory status
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memoryStatus = new HashMap<>();
            memoryStatus.put("total", runtime.totalMemory() / (1024 * 1024) + " MB");
            memoryStatus.put("free", runtime.freeMemory() / (1024 * 1024) + " MB");
            memoryStatus.put("used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " MB");
            memoryStatus.put("max", runtime.maxMemory() / (1024 * 1024) + " MB");
            status.put("memory", memoryStatus);
            
            status.put("overall", "HEALTHY");
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("overall", "ERROR");
            status.put("error", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }
    
    private String getUptime() {
        long uptimeMs = System.currentTimeMillis() - 
            java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours % 24, minutes % 60, seconds % 60);
    }
}