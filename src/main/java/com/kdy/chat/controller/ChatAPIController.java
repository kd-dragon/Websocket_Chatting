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
	
	
	/*
	 * @author : KGH
	 * @Date : 21.06.23
	 * @Script : 
	 * 			live 스케줄러에서 방송 종료시 종료된 방송의 채팅 기록을 text 확장자로 출력 
	 * */
	
	/*
	@CrossOrigin(origins="*")
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/serviceTermination")
	public void getRequest(@RequestParam Map<String, Object> param) throws Exception{
		
		java.util.Date date = new java.util.Date();
		String year = new SimpleDateFormat("yyyy").format(new Date());
		String month = new SimpleDateFormat("MM").format(new Date());
		String day = new SimpleDateFormat("dd").format(new Date());
		
		String prefixFileName = year + "-" + month + "-" + day;
		String chatFileName = "";
	
		
		System.out.println(" param ==> " + param.toString());
		
		String lbSeq = param.get("lbSeq").toString();
		String liveBroadcastTitle = param.get("liveBroadcastTitle").toString();

		chatFileName = chatPath + year + "/" + month + "/" + prefixFileName + "_" + lbSeq + "_chat.txt";
		
		ChatBackUpDTO backUpDTO = (ChatBackUpDTO) hashOperations.get(ChatHashKey.BACKUP.toString(), lbSeq);
	
		File dir = new File(chatPath + year + "/" + month);
		
		if(!dir.isDirectory()) {
			dir.mkdirs();
		}
		
		File file = new File(chatFileName);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		writer.write(backUpDTO.getFullChat().toString());
		
		writer.close();
	} 
	*/
	
	/*
	 * @author : KGH
	 * @Date : 21.06.23
	 * @Script : 
	 * 			지정한 시간동안 파일 이어 쓰기  
	 * */
	
	/*
	@SuppressWarnings("unchecked")
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/backupChatRequest")
	public void backupChatRequest() throws Exception{
		
		java.util.Date date = new java.util.Date();
		String year = new SimpleDateFormat("yyyy").format(new Date());
		String month = new SimpleDateFormat("MM").format(new Date());
		String day = new SimpleDateFormat("dd").format(new Date());
		
		String prefixFileName = year + "-" + month + "-" + day;
		String chatFileName = "";
	
		File dir = new File(chatPath + year + "/" + month + "/backup/");
		
		if(!dir.isDirectory()) {
			dir.mkdirs();
		}
	
		Map<String, BlockingQueue<String>> backupChatRepo = systemDto.getRoomChatQueueMap();
		
		if(backupChatRepo != null) {
			for(Map.Entry<String, BlockingQueue<String>> entry : backupChatRepo.entrySet()) {
				chatFileName = chatPath + year + "/" + month + "/backup/" + prefixFileName + "_" + entry.getKey() + "_chat.txt";
				File file = new File(chatFileName);
				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
				
				BlockingQueue<String> chatQueue = backupChatRepo.get(entry.getKey());
				
				while(!chatQueue.isEmpty()) {
					writer.write(chatQueue.poll());
				}
				writer.close();
				systemDto.getRoomChatQueueMap().remove(entry.getKey());
			}
		}
	}
	*/
	
	
	//접속자수 가져오기
	@RequestMapping("/getLiveViewsCount")
	public LiveViewsDTO getLiveViewsCount(@RequestBody Map<String, Object> map) throws Exception{
		
		List<String> broadcastList = (List<String>)map.get("broadcastList");
		
		if(broadcastList == null || broadcastList.size() == 0) {
			return null;
		}
		
		LiveViewsDTO dto = new LiveViewsDTO();
		dto.setViewsList(chatService.getLiveViewsCount(broadcastList));
		
		return dto;
	}
	
	
} 
