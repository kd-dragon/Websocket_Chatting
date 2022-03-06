package com.kdy.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WsChatApplication {
	
	/**
	 * Configuration Bean Package: com.kdy.chat.config
	 * 
	 * Core: WebSocketConfig.class
	 * 
	 * 
	 * @param kdy
	 */
	
	public static void main(String[] args) {
		SpringApplication.run(WsChatApplication.class, args);
	}

}
