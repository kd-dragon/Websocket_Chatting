package com.kdy.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.kdy.chat.bean.RedisSubscriber;
import com.kdy.chat.dto.ChatDTO;
import com.kdy.chat.dto.LiveViewsVO;
import com.kdy.chat.dto.SystemMemoryDTO;
import com.kdy.chat.dto.SystemMemoryDTO.ChatHashKey;
import com.kdy.chat.dto.SystemMemoryDTO.RedisHashKeyword;
import com.kdy.chat.dto.UserDTO;
import com.kdy.chat.service.IF.ChatServiceIF;

@Service
public class ChatService implements ChatServiceIF {
	
	private Logger logger = LoggerFactory.getLogger(ChatService.class);
	
	@Autowired
	private RedisMessageListenerContainer messageListener;
	
	@Autowired
	private RedisSubscriber subscriber;
	
	@Autowired
	private SystemMemoryDTO systemDto;
	
	@Resource(name="redisTemplate")
	private SetOperations<String, String> setOperations;
	
	@Resource(name="redisTemplate")
	private ValueOperations<String, Object> valueOperations;
	
	@Resource(name="redisTemplate")
	private ListOperations<String, ChatDTO> listOperations; 
	
	//vod 녹화 시 채팅 녹화 여부
	@Value("${chat.rec.enabled}")
	private Boolean chatRecEnabled;
	
	/**
	 * <채팅방 초기화>
	 * 토픽 유무 확인 및 등록
	 */
	@Override
	public void initChatRoom(ChatDTO chatDto) throws Exception {
		// 토픽 유무 확인 및 등록
		Map<String, ChannelTopic> topics = systemDto.getTopics();
		ChannelTopic topic = topics.get(chatDto.getLbSeq());
		if(topic == null) {
			topic = new ChannelTopic(chatDto.getLbSeq());
			messageListener.addMessageListener(subscriber, topic);
			topics.put(chatDto.getLbSeq(), topic);
		}
	}
	
	/**
	 *  <채팅방 입장>
	 *  1. 사용자 정보 사용자 목록에 추가
	 *  2. 채팅방에 사용자 세션 아이디 저장
	 *  3. 채팅방 접속자 수 갱신
	 */
	@Override
	public ChatDTO enterChatRoom(ChatDTO chatDto) throws Exception {
		
		UserDTO userDto = new UserDTO();
		userDto.setUserId(chatDto.getUserId());
		userDto.setSessionId(chatDto.getSessionId());
		userDto.setRoomId(chatDto.getLbSeq());
		userDto.setUserName(chatDto.getUserName());
		
		// 사용자 정보 사용자 목록에 추가 (CHAT_USER, Session-ID, UserDTO)
		valueOperations.set(chatDto.getSessionId(), userDto, 1, TimeUnit.DAYS);
		
		if(valueOperations.get("chat" + chatDto.getLbSeq()) != null) {
			chatDto.setNotifyMessage((String) valueOperations.get("chat" + chatDto.getLbSeq()));
		}
		
		// 채팅방에 사용자 세션 아이디 저장 (Room-ID, Session-ID)
		setOperations.add(chatDto.getLbSeq(), chatDto.getSessionId());
		
		logger.info("[Room-User: Members]");
		for (String s : setOperations.members(chatDto.getLbSeq())) {
			logger.info(s);
		}
		
		// 채팅방 접속자 수 갱신
		chatDto.setRoomUserCnt(setOperations.size(chatDto.getLbSeq()));
		if(chatDto.getUserType().equals("0")) {
			setOperations.add(chatDto.getLbSeq() + "PC", chatDto.getSessionId());
		}
		
		chatDto.setPcUserCnt(setOperations.size(chatDto.getLbSeq() + "PC"));

		return chatDto;
	}
	
	/**
	 *  <채팅방 나가기>
	 *  1. 채팅방 접속 여부 확인
	 *  2. 채팅방 저장소 내 해당 사용자 제거
	 *  3. 접속자 수 갱신
	 */
	@Override
	public ChatDTO leaveChatRoom(ChatDTO chatDto) throws Exception {
		
		// 채팅방 접속 여부 확인
		if(setOperations.isMember(chatDto.getLbSeq(), chatDto.getSessionId())) {
			// 채팅방 저장소 내 해당 사용자 제거
			setOperations.remove(chatDto.getLbSeq(), chatDto.getSessionId());
			
			if(setOperations.isMember(chatDto.getLbSeq() + "PC", chatDto.getSessionId())) {
				setOperations.remove(chatDto.getLbSeq() + "PC", chatDto.getSessionId());
				chatDto.setPcUserCnt(setOperations.size(chatDto.getLbSeq() + "PC"));
			}
		}
		// 접속자 수 갱신
		chatDto.setRoomUserCnt(setOperations.size(chatDto.getLbSeq()));
		
		return chatDto;
	}
	
	/**
	 *  <채팅방 메시지 보내기 (채팅 기록)>
	 *  1. 채팅 내용 저장소 확인
	 *  2. 채팅방 전체 채팅에 이어붙이기
	 *  3. 전체 채팅 메모리 저장
	 */
	@Override
	public ChatDTO sendMessageInChatRoom(ChatDTO chatDto) throws Exception {
		
		
		
		if (chatDto.getUserId() != null && chatDto.getContent() != null) {
			if(chatDto.getNotifyYn() != null && chatDto.getNotifyYn().equals("Y")) {
				valueOperations.set("chat" + chatDto.getLbSeq(), chatDto.getContent(), 7, TimeUnit.DAYS);
				chatDto.setNotifyMessage(chatDto.getContent());
			}

			if(chatRecEnabled) {
				
				String key   = RedisHashKeyword.RECENABLE    + chatDto.getLbSeq();
				String key2  = RedisHashKeyword.RECSTARTDATE + chatDto.getLbSeq();
				String key3  = RedisHashKeyword.RECDURATION  + chatDto.getLbSeq();
				
				//vod 녹화 상태일 때
				if(valueOperations.get(key) != null && valueOperations.get(key).toString().equalsIgnoreCase("Y")) {
					chatDto.setVodSaveYn("Y");
					
					long recStartDate = Long.parseLong(valueOperations.get(key2).toString());
					long recDuration = Long.parseLong(valueOperations.get(key3).toString());
					long vodChatTime =  recDuration + ((System.currentTimeMillis() -recStartDate)/1000);
					chatDto.setVodChatTime(vodChatTime);
					
				}else {
					chatDto.setVodSaveYn("N");
				}
				
			}
		    
	    	listOperations.leftPush(ChatHashKey.BACKUP.toString() + chatDto.getLbSeq(), chatDto);
			
	    	
		} else {
			chatDto.setStatus("FAIL");
		}
		return chatDto;
	}

	/**
	 * <채팅방 메시지 삭제>
	 * 1. 해당 채팅 내용 '삭제된 메시지입니다' 로 치환
	 */
	@Override
	public ChatDTO deleteMessageInChatRoom(ChatDTO chatDto) throws Exception {
		chatDto.setContent("삭제된 메시지입니다.");
		return chatDto;
	}
	
	/**
	 * [monitor]
	 * 접속자 수 가져오기
	 */
	@Override
	public List<LiveViewsVO> getLiveViewsCount(List<String> broadcastList) throws Exception {
		
		List<LiveViewsVO> list = new ArrayList<LiveViewsVO>();		
		LiveViewsVO vo = null;
		for(String lbSeq : broadcastList) {
			vo = new LiveViewsVO();
			
			Long total = setOperations.size(lbSeq);
			Long pcCnt = setOperations.size(lbSeq + "PC");
			Long mobileCnt = total - pcCnt;
			
			vo.setLbSeq(lbSeq);
			vo.setAccessTotalCnt(total);
			vo.setAccessPcCnt(pcCnt);
			vo.setAccessMobileCnt(mobileCnt);
			
			list.add(vo);
		}
		
		return list;
	}

}
