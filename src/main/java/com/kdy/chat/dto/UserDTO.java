package com.kdy.chat.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserDTO implements Serializable {

	private static final long serialVersionUID = 2538112150691686592L;
	
	private String userId;		//사용자 아이디
	private String sessionId;	//세션 아이디
	private String roomId; 		//채팅방 번호
	private String userName;
}
