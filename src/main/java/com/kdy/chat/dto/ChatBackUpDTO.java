package com.kdy.chat.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ChatBackUpDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String roomId;
	private StringBuilder fullChat = new StringBuilder();
	
}
