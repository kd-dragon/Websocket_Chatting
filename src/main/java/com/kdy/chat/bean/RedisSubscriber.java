package com.kdy.chat.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdy.chat.dto.ChatDTO;

@Component
public class RedisSubscriber implements MessageListener {
	
	private final Logger logger = LoggerFactory.getLogger(RedisSubscriber.class);
	
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final SimpMessageSendingOperations messagingTemplate;
	
	@Autowired
	public RedisSubscriber(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, SimpMessageSendingOperations messagingTemplate) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.messagingTemplate = messagingTemplate;
	}
	
	/**
	 * <메시지 Subscribe>
	 * 1. Deserialize - ChatDTO
	 * 2. STOMP - convertAndSend('/broker/{roomId}', ChatDTO)	 * 
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		
		try {
			
			//Deserialize - ChatDTO
			String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
			ChatDTO chatDto = objectMapper.readValue(publishMessage, ChatDTO.class);
			
			//STOMP - convertAndSend
			messagingTemplate.convertAndSend("/broker/" + chatDto.getLbSeq(), chatDto);
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			for(StackTraceElement st : e.getStackTrace()) {
				if(st.toString().startsWith("com.kdy.chat")) {
					logger.error(st.toString());
				}
			}
		}
	}

}
