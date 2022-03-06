package com.kdy.chat.dto;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class SystemMemoryDTO {
	
	private Map<String, BlockingQueue<String>> roomChatQueueMap = new ConcurrentHashMap<>();
	
	private Map<String, ChannelTopic> topics = new ConcurrentHashMap<>();
	
	public enum ChatHashKey {
		USER, BACKUP
	}
	
	private String notifyMessage;
	
	public enum RedisHashKeyword {
		BACKUP, RECORD, RECENABLE, LIVESTATUS, RECSTARTDATE, RECDURATION
	}
	

}
