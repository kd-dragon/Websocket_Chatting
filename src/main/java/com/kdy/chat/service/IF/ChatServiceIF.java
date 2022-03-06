package com.kdy.chat.service.IF;

import java.util.List;

import com.kdy.chat.dto.ChatDTO;
import com.kdy.chat.dto.LiveViewsVO;

public interface ChatServiceIF {
	
	public void initChatRoom(ChatDTO chatDto) throws Exception;
	
	public ChatDTO enterChatRoom(ChatDTO chatDto) throws Exception;
	
	public ChatDTO leaveChatRoom(ChatDTO chatDto) throws Exception;
	
	public ChatDTO sendMessageInChatRoom(ChatDTO chatDto) throws Exception;
	
	public ChatDTO deleteMessageInChatRoom(ChatDTO chatDto) throws Exception;
	
	//접속자수 가져오기 (monitor)
	public List<LiveViewsVO> getLiveViewsCount(List<String> broadcastList) throws Exception;
}
