package com.kdy.chat.bean;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdy.chat.dto.ChatDTO;
import com.kdy.chat.dto.SystemMemoryDTO;

@Component
public class BackUpSubscriber implements MessageListener {
	
	private final Logger logger = LoggerFactory.getLogger(BackUpSubscriber.class);
	
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final SystemMemoryDTO systemDto;
	
	@Autowired
	public BackUpSubscriber(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, SystemMemoryDTO systemDto) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.systemDto = systemDto;
	}
	
	/**
	 * <메시지 Back-Up Subscribe>
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		
		try {
			
			//Deserialize - ChatDTO
			//String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
			ChatDTO chatDto = objectMapper.readValue(message.getBody(), ChatDTO.class);
			
			//BackUpQueue add (recordMessage)
			Map<String, BlockingQueue<String>> roomChatQueueMap = systemDto.getRoomChatQueueMap();
			if(roomChatQueueMap.get(chatDto.getLbSeq()) != null) {
				roomChatQueueMap.get(chatDto.getLbSeq()).add(chatDto.getRecordMessage());
			} else {
				roomChatQueueMap.put(chatDto.getLbSeq(), new LinkedBlockingQueue<String>());
				roomChatQueueMap.get(chatDto.getLbSeq()).add(chatDto.getRecordMessage());
			}
			
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
