//날짜 형식
function nowDate() {
	var date = new Date();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	var hour = date.getHours();
	var minute = date.getMinutes();
	
	if(month.length == 1) {
		month = "0" + month;
	}
	
	if(day.length == 1) {
		day = "0" + day;
	}
	
	if(hour.length == 1) {
		hour = "0" + hour;
	}
	
	if(minute.length == 1) {
		minute = "0" + minute;
	}
	return date.getFullYear() + "-" + month + "-" + day + " " + hour + ":" + minute;
}

//브라우저 종료시에 채팅방 나가기 적용
$(window).bind("beforeunload", function(e) {
	var msg = {type : "LEAVE", sessionId : sessionId};
	disconnect(msg);
});

var userId = "";
var stomp = null;
var socket;
var sessionIdList = [];
var sessionId = "";

//초기 로그인 및 세션 연결
function connect(event) {
	userId = $("#userId").val();
	
	if(userId == null || userId == "") {
		alert("아이디를 입력해주세요.");
	} else {
		if(userId.length > 12) {
			alert("아이디는 12자를 넘을 수 없습니다.");
			$("#userId").val("");
			$("#userId").focus();
			return;
		} else {
			socket = new SockJS('http://192.168.0.132:8080/websocket');
			stomp = Stomp.over(socket);
			stomp.connect({}, onConnected, onError);
			console.log("connected!!!");
		}
	}
}

//connect가 정상적인 경우 실행
function onConnected() {
	stomp.subscribe('/broker', onMessageReceived);
	stomp.send('/app/chat.join', {}, JSON.stringify( { userId : userId, type : 'JOIN'} ));
	alert("로그인되었습니다. 채팅창으로 이동합니다.");
}

//connect가 실패인 경우 실행
function onError(error) {
	alert("시스템 에러가 발생하였습니다. 관리자에게 문의하세요.");
}

//채팅방 나가기
function roomOut() {
	var msg = { type : "LEAVE", sessionId : sessionId };
	disconnect(msg);
}

//연결 종료
function disconnect(event) {
	if(stomp) {
		var sendMsg = {
			userId : userId,
			type : event.type,
			sessionId : event.sessionId
		};
	}
	stomp.send('/app/chat.leave', {}, JSON.stringify(sendMsg));
}

//메시지 전송 전 체크
function sendChk() {
	var msg;
	
	if($("#textMessage").val() == null || $("#textMessage").val() == "") {
		return;
	} else {
		msg = $("#textMessage").val();
	}
	var sendMsg = { type : "SEND", content : msg, sessionId : sessionId };
	send(sendMsg);	
}

//메시지 전송
function send(event) {
	if(stomp) {
		var sendMsg = {
			userId : userId,
			content : event.content,
			type : event.type,
			sessionId : event.sessionId
		};
		stomp.send('/app/chat.send', {}, JSON.stringify(sendMsg));
		
		$("#textMessage").val("");
		$("#textMessage").focus();
	}
}

//type에 따라 실행되는 함수(서버에서 클라이언트로 전달된 msg.type)
function onMessageReceived(payload) {
	var msg = JSON.parse(payload.body);
	
	if(sessionId == "") {
		sessionId = msg.sessionId;
	}
	
	if(sessionIdList.length == 0) {
		sessionIdList.push(msg.sessionId);
	}
	
	//채팅방 참여인 경우
	if(msg.type === 'JOIN') {
		if(msg.status === 'SUCCESS') {
			$("#systemBox").css("display", "block");
			$("#roomBox").css("display", "none");
			$("#userId").attr("disabled", true);
			$("#loginBtn").attr("disabled", true);
			$("#textMessage").focus();
			
			var html = "";
			html += "<div class=\"chat-unit\">";
			
			//아이디는 같으나 세션 아이디가 다른 경우
			if(userId == msg.userId && !sessionIdList.includes(msg.sessionId)) {
				html += "<span class=\"user-info\"><i>" + msg.userId + "님이 채팅방에 입장하였습니다. ("+nowDate()+")</i></span>";
				sessionIdList.push(msg.sessionId);
			//자신이 채팅에 참여한 경우
			} else if(userId == msg.userId) {
				html += "<span class=\"user-info\"><i>채팅방에 입장하였습니다. ("+nowDate()+")</i></span>";
			//아이디가 다른 경우
			} else {
				html += "<span class=\"user-info\"><i>" + msg.userId + "님이 채팅방에 입장하였습니다. ("+nowDate()+")</i></span>";
			}
			
			html += "<div>";
			
			$("#chatBox").append(html);
		} else {
			alert("로그인 작업이 정상적으로 이루어지지 않았습니다.");
		}
		
		var userCnt = "";
		userCnt += Object.keys(msg.userInfoMap).length;
		$("#totalUser").html(userCnt);
		
	//채팅방 나가기인 경우
	} else if(msg.type === 'LEAVE') {
		if(msg.status === 'SUCCESS') {
			var html = "";
			
			//아이디는 같으나 세션 아이디가 다른 경우
			if((userId == msg.userId && sessionId != msg.sessionId)) {
				sessionIdList.pop(msg.sessionId);
				
				html += "<div class=\"chat-unit\">";
				html += "<span class=\"user-info\">" + msg.userId + "님이 채팅방을 나갔습니다. <i>" + nowDate() + "</i></span>";
				html += "</div>";
				
				$("#chatBox").append(html);
			//자신이 나가는 경우			
			} else if(userId == msg.userId) {
				alert("채팅방을 나갔습니다.");
				$("#systemBox").css("display", "none");
				$("#roomBox").css("display", "block");
				$("#userId").attr("disabled", false);
				$("#loginBtn").attr("disabled", false);
				
				stomp.disconnect();
				location.reload();
				console.log("disconnected!!!");
			//다른 사람이 나가는 경우
			} else {
				html += "<div class=\"chat-unit\">";
				html += "<span class=\"user-info\">" + msg.userId + "님이 채팅방을 나갔습니다. <i>" + nowDate() + "</i></span>";
				html += "</div>";
				
				$("#chatBox").append(html);	
			} 
		} else {
			alert("다시 시도해주시기 바랍니다.");
		}
		
		var userCnt = "";
		userCnt += Object.keys(msg.userInfoMap).length;
		$("#totalUser").html(userCnt);
		
	//메시지 전송인 경우
	} else if(msg.type === 'SEND') {
		if(msg.status === 'SUCCESS') {
			var html = "";
			
			html += "<div class=\"chat-unit\">";
			
			//아이디는 같으나 세션 아이디가 다른 경우
			if(userId == msg.userId && sessionId != msg.sessionId) {
				html += "<span class=\"user-info\"><b>" + msg.userId + "</b><i>" + nowDate() + "</i></span>";
			//자신이 전송하는 경우
			} else if(userId == msg.userId) {
				html += "<span class=\"user-info\"><b>내가 보냄</b><i>" + nowDate() + "</i></span>";
			//다른 사람이 전송하는 경우
			} else {
				html += "<span class=\"user-info\"><b>" + msg.userId + "</b><i>" + nowDate() + "</i></span>";
			}
			html += "<p>" + msg.content + "</p>";
			html += "</div>";
			
			$("#chatBox").append(html);
		} else {
			alert("메시지를 전송하는데 실패했습니다.");
		}
	}
	
	$("#chatBox").scrollTop($("#chatBox").prop("#scrollHeight"));
}

function sendCam() {
	console.log(camCanvas.toDataURL("image/jpeg"));
}

function camHandler() {
	camDrawCanvas();
	sendCam();
}