//06.17 라이브 목록 > 상세 페이지 채팅 js

//브라우저 종료시에 채팅방 나가기 적용
$(window).bind("beforeunload", function(e) {
	var msg = { type: "LEAVE", sessionId: sessionId };
	disconnect(msg);
});

//날짜 형식
function nowDate() {
	var date = new Date();
	var month = date.getMonth();
	var day = date.getDate();
	var hour = date.getHours();
	var minute = date.getMinutes();

	if (month.length == 1) {
		month = "0" + month;
	}

	if (day.length == 1) {
		day = "0" + day;
	}

	if (hour.length == 1) {
		hour = "0" + hour;
	}

	if (minute.length == 1) {
		minute = "0" + minute;
	}
	return date.getFullYear() + "-" + month + "-" + day + " " + hour + ":" + minute;
}

//메시지 전송하기 전 메시지 내용 체크
function sendChk() {
	var msg;
	if ($("#textMessage").val() == null || $("#textMessage").val() == "") {
		return;
	} else {
		msg = $("#textMessage").val();
	}
	var sendMsg = { type: "SEND", content: msg, sessionId: sessionId };
	send(sendMsg);
}

function send(event) {
	if (stomp) {
		var sendMsg = {
			userId: userId,
			content: event.content,
			type: event.type,
			sessionId: event.sessionId
		};
		stomp.send('/app/chat.send', {}, JSON.stringify(sendMsg));
		//stomp.send('/app/chat.send' + lb_seq, {}, JSON.stringify(sendMsg));

		$("#textMessage").val("");
		$("#textMessage").focus();
	}
}

function roomOut() {
	var msg = { type: "LEAVE", sessionId : sessionId };
	disconnect(msg);
}

function disconnect(event) {
	if (stomp) {
		var sendMsg = {
			userId: userId,
			type: event.type,
			sessionId: event.sessionId
		};
	}
	stomp.send('/app/chat.leave', {}, JSON.stringify(sendMsg));
	//stomp.send('/app/chat.leave' + lb_seq, {}, JSON.stringify(sendMsg));
}

//type에 따라 실행되는 함수
function onMessageReceived(payload) {
	var msg = JSON.parse(payload.body);
	console.log('awd');
	if (sessionId == "") {
		sessionId = msg.sessionId;
	}

	if (sessionIdList.length == 0) {
		sessionIdList.push(msg.sessionId);
	}

	if (msg.type === 'JOIN') {
		if (msg.status === 'SUCCESS') {
			//$("#chatWrap").css("display", "block");
			$("#systemBox").css("display", "block");
			$("#textMessage").focus();

			var html = "";
			html += "<div class=\"chat-unit\">";

			//아이디는 같으나 세션 아이디가 다른 경우
			if (userId == msg.userId && !sessionIdList.includes(msg.sessionId)) {
				html += "<span class=\"user-info\"><i>" + msg.userId + "님이 채팅방에 입장하였습니다. (" + nowDate() + ")</i></span>";
				sessionIdList.push(msg.sessionId);
				//자신이 채팅에 참여한 경우
			} else if (userId == msg.userId) {
				html += "<span class=\"user-info\"><i>채팅방에 입장하였습니다. (" + nowDate() + ")</i></span>";
				//아이디가 다른 경우
			} else {
				html += "<span class=\"user-info\"><i>" + msg.userId + "님이 채팅방에 입장하였습니다. (" + nowDate() + ")</i></span>";
			}

			html += "<div>";

			$("#chatBox").append(html);
		} else {
			alert("오류");
		}
		var userCnt = "";
		userCnt += Object.keys(msg.userInfoMap).length;
		$("#totalUser").html(userCnt);
	}
}