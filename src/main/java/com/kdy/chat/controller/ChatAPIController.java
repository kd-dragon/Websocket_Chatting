package com.kdy.chat.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdy.chat.dto.ChatDTO;
import com.kdy.chat.dto.LiveViewsDTO;
import com.kdy.chat.dto.SystemMemoryDTO;
import com.kdy.chat.dto.SystemMemoryDTO.ChatHashKey;
import com.kdy.chat.service.IF.ChatServiceIF;

@RestController
@RequestMapping("api")
public class ChatAPIController {
	
	@Autowired
	private SystemMemoryDTO systemDto;
	
	//@Resource(name="redisTemplate")
	//private HashOperations<String, String, Object> hashOperations;
	
	@Resource(name="redisTemplate")
	private ListOperations<String, Object> listOperations;

	Logger logger = LoggerFactory.getLogger(ChatAPIController.class);
	
	@Autowired
	private ChatServiceIF chatService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * REDIS 채팅 목록 조회 API 테스트
	 * @author KDY
	 * @param lbSeq
	 * @throws Exception
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(method = {RequestMethod.GET}, path = "/getChatHistory")
	public void getRequest(@RequestParam String lbSeq) throws Exception{
		
		List<ChatDTO> chatDtoList = objectMapper.convertValue(listOperations.range(ChatHashKey.BACKUP.toString() + lbSeq, 0, -1), new TypeReference<List<ChatDTO>>() {});
		
		for(ChatDTO chatDto : chatDtoList) {
			logger.info(chatDto.toString());
		}
	} 
	
	
	//접속자수 가져오기
	@RequestMapping("/getLiveViewsCount")
	public LiveViewsDTO getLiveViewsCount(@RequestBody Map<String, Object> map) throws Exception{
		
		@SuppressWarnings("unchecked")
		List<String> broadcastList = (List<String>)map.get("broadcastList");
		
		if(broadcastList == null || broadcastList.size() == 0) {
			return null;
		}
		
		LiveViewsDTO dto = new LiveViewsDTO();
		dto.setViewsList(chatService.getLiveViewsCount(broadcastList));
		
		return dto;
	}
	
	
} 
