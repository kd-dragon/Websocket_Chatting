package com.kdy.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.kdy.chat.bean.RedisPublisher;
import com.kdy.chat.dto.ChatDTO;
import com.kdy.chat.dto.SystemMemoryDTO;
import com.kdy.chat.dto.SystemMemoryDTO.ChatHashKey;
import com.kdy.chat.service.IF.ChatServiceIF;

@Controller
public class ChatController {
	
	private Logger logger = LoggerFactory.getLogger(ChatController.class);
	
	private final RedisPublisher redisPublisher;
	private final ChatServiceIF chatService;
	private final SystemMemoryDTO systemDto;
	
	
	@Autowired
	public ChatController(RedisPublisher redisPublisher, ChatServiceIF chatService, SystemMemoryDTO systemDto) {
		this.redisPublisher = redisPublisher;
		this.chatService = chatService;
		this.systemDto = systemDto;
	}
	
	@MessageMapping("/chat.init/{lbSeq}")
	public ChatDTO init(@Payload ChatDTO dto) {
		
		try {
			chatService.initChatRoom(dto);
			dto.setStatus("SUCCESS");
			
		} catch(Exception e) {
			dto.setStatus("FAIL");
			logger.error(e.getMessage());
			for(StackTraceElement st : e.getStackTrace()) {
				if(st.toString().startsWith("com.kdy.chat")) {
					logger.error(st.toString());
				}
			}
		} 
		return dto;
	}
	
	// 채팅 참여
	@MessageMapping("/chat.join/{lbSeq}")
	//@SendTo("/broker/{lbSeq}")
	public ChatDTO join(@Payload ChatDTO dto, SimpMessageHeaderAccessor accessor) {
		accessor.getSessionAttributes().put("userId", dto.getUserId());
		String sessionId = accessor.getSessionId();
		dto.setSessionId(sessionId);
		
		try {
			chatService.enterChatRoom(dto);
//			dto.setNotifyMessage(systemDto.getNotifyMessage());
			dto.setStatus("SUCCESS");
		} catch(Exception e) {
			dto.setStatus("FAIL");
			logger.error(e.getMessage());
			for(StackTraceElement st : e.getStackTrace()) {
				if(st.toString().startsWith("com.kdy.chat")) {
					logger.error(st.toString());
				}
			}
		} finally {
			ChannelTopic topic =  systemDto.getTopics().get(dto.getLbSeq());
			redisPublisher.publish(topic, dto);
		}
		
		return dto;
	}

	// 채팅 나가기
	@MessageMapping("/chat.leave/{lbSeq}")
	//@SendTo("/broker/{lbSeq}")
	public ChatDTO leave(@Payload ChatDTO dto) {
		try {
			
			if (dto.getUserId() != null && dto.getSessionId() != null) {
				chatService.leaveChatRoom(dto);
				dto.setStatus("SUCCESS");
			} else {
				dto.setStatus("FAIL");
			}
			
		} catch (Exception e) {
			dto.setStatus("FAIL");
			logger.error(e.getMessage());
			for(StackTraceElement st : e.getStackTrace()) {
				if(st.toString().startsWith("com.kdy.chat")) {
					logger.error(st.toString());
				}
			}
		} finally {
			ChannelTopic topic = systemDto.getTopics().get(dto.getLbSeq());
			redisPublisher.publish(topic, dto);
		}
		return dto;
	}

	// 채팅 보내기
	@MessageMapping("/chat.send/{lbSeq}")
	//@SendTo("/broker/{lbSeq}")
	public ChatDTO send(@Payload ChatDTO dto, SimpMessageHeaderAccessor accessor) {
		try {
			
			if (dto.getUserId() != null && dto.getContent() != null) {
				chatService.sendMessageInChatRoom(dto);
				dto.setStatus("SUCCESS");
			} else {
				dto.setStatus("FAIL");
			}
		} catch(Exception e) {
			dto.setStatus("FAIL");
			logger.error(e.getMessage());
			for(StackTraceElement st : e.getStackTrace()) {
				if(st.toString().startsWith("com.kdy.chat")) {
					logger.error(st.toString());
				}
			}
		} finally {
			ChannelTopic topic = systemDto.getTopics().get(dto.getLbSeq());
			//ChannelTopic rec_topic = systemDto.getTopics().get(ChatHashKey.BACKUP.toString()+dto.getLbSeq());
			redisPublisher.publish(topic, dto);
			//redisPublisher.publish(rec_topic, dto);
		}
		return dto;
	}
	
	//삭제
	@MessageMapping("/chat.delete/{lbSeq}")
	//@SendTo("/broker/{lbSeq}")
	public ChatDTO msgDelete(@Payload ChatDTO dto) {
		
		try {
			if(dto.getContent() != null && dto.getMessageId() != null) {
				chatService.deleteMessageInChatRoom(dto);
				dto.setStatus("SUCCESS");
			} else {
				dto.setStatus("FAIL");
			}
		} catch(Exception e) {
			dto.setStatus("FAIL");
			logger.error(e.getMessage());
			for(StackTraceElement st : e.getStackTrace()) {
				if(st.toString().startsWith("com.kdy.chat")) {
					logger.error(st.toString());
				}
			}
		} finally {
			ChannelTopic topic = systemDto.getTopics().get(dto.getLbSeq());
			redisPublisher.publish(topic, dto);
		}
		
		return dto;
	}
	
}