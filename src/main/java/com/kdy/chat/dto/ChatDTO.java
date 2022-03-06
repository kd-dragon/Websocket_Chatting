package com.kdy.chat.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ChatDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userId;			//사용자 아이디
	private String userName;
	private String content;			//메시지 내용
	private ChatType type;			//채팅 관련 타입
	private String status;			//상태값
	private String sessionId;		//세션 아이디
	private String messageId;		//메시지 아이디
	private String lbSeq;			//방송 시퀀스
	private long roomUserCnt;		//채팅방 별 인원
	private long pcUserCnt;			//채팅방 PC 인원
	private String date; 			// 채팅 시간 
	private String lbTitle;
	private String recordMessage; 	//채팅 기록
	private String userType; 		//사용자 기기 타입 (0:PC, 1:Mobile)
	private String notifyYn;		//채팅 공지유무
	private String notifyMessage;	//채팅 공지메시지
	private String adminChk;		//관리자 채팅 분리
	
	private String vodSaveYn;     //녹화 여부
	private long vodChatTime;
	
	//채팅 타입 지정
	public enum ChatType {
		SEND, JOIN, LEAVE, DELETE, INIT
	}
}
