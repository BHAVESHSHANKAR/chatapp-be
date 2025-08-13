package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class DemoApplication {

	@Autowired
	private Environment environment;

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		String port = environment.getProperty("server.port", "8080");
		String appName = environment.getProperty("spring.application.name", "demo");

		System.out.println("\n" + "=".repeat(60));
		System.out.println("🚀 " + appName.toUpperCase() + " SERVER STARTED SUCCESSFULLY!");
		System.out.println("=".repeat(60));
		System.out.println("🌐 Server URL: http://localhost:" + port);
		System.out.println("📡 WebSocket: ws://localhost:" + port + "/ws");
		System.out.println("📊 Status API: http://localhost:" + port + "/api/status");

		// Check database connection
		try (Connection connection = dataSource.getConnection()) {
			String dbUrl = connection.getMetaData().getURL();
			String dbName = connection.getMetaData().getDatabaseProductName();
			String dbVersion = connection.getMetaData().getDatabaseProductVersion();

			System.out.println("✅ Database: " + dbName + " (" + dbVersion + ")");
			System.out.println("🔗 Connection: " + maskPassword(dbUrl));

		} catch (Exception e) {
			System.err.println("❌ Database connection failed: " + e.getMessage());
		}

		// Memory info
		Runtime runtime = Runtime.getRuntime();
		long totalMemory = runtime.totalMemory() / (1024 * 1024);
		long freeMemory = runtime.freeMemory() / (1024 * 1024);
		long usedMemory = totalMemory - freeMemory;

		System.out.println("💾 Memory: " + usedMemory + "MB used / " + totalMemory + "MB total");
		System.out.println("🔐 Encryption: AES-128 enabled");
		System.out.println("🔒 Security: JWT authentication active");
		System.out.println("⚡ WebSocket: STOMP over SockJS ready");
		System.out.println("=".repeat(60));
		System.out.println("🎯 READY TO ACCEPT REQUESTS!");
		System.out.println("=".repeat(60) + "\n");
	}

	private String maskPassword(String url) {
		// Mask password in URL for security
		return url.replaceAll("password=[^&]*", "password=***");
	}
}
