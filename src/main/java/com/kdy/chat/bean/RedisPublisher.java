package com.kdy.chat.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import com.kdy.chat.dto.ChatDTO;


@Component
public class RedisPublisher {
	
	private final Logger logger = LoggerFactory.getLogger(RedisPublisher.class);

	private final RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public RedisPublisher(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	/**
	 * <메시지 Publish>
	 * STOMP - convertAndSend
	 * @param topic
	 * @param dto
	 */
	public void publish(ChannelTopic topic, ChatDTO dto) {
		redisTemplate.convertAndSend(topic.getTopic(), dto);
	}
}
