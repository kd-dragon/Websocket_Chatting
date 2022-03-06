 <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page session="false" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
<html lang="ko">
<head>
<!-- CSS&JS -->
<link href="/resources/css/total.css" type="text/css" rel="stylesheet"  media="screen" />
<!-- <link href="/resources/css/build.css" type="text/css" rel="stylesheet"  media="screen" /> -->
<script src="/resources/js/jquery-3.3.1.min.js" language="javascript" type="text/javascript"></script>
<script src="/resources/js/video.js" language="javascript" type="text/javascript"></script>
<!--// CSS&JS -->

<!-- sockjs -->
<script src="/resources/js/sockjs.min.js"></script>
<!-- //sockjs -->

<!-- stomp -->
<script src="/resources/js/stomp.min.js"></script>
<!-- //stomp -->

<title>채팅 테스트0616</title>
</head>

<script type="text/javascript">
//인터넷 창 종료된 경우에 소켓 종료하기 위해 사용
$(window).bind("beforeunload", function(e) {
	var msg = {type : "LEAVE", sessionId : sessionId};
	disconnect(msg);
});

//var loginYn = false;
var userId = "";
var stomClient = null;
var socket;
var sIdList = [];
var sessionId = "";

//소켓 연결(시작 부분)
function connect(event) {
	userId = $("#userId").val();
	
	if(userId == null || userId == "") {
		alert("아이디를 입력해주세요.");
	} else {
		if(userId.length > 12) {
			alert("아이디는 12자를 넘을 수 없습니다.");
			return;
		} else {
			socket = new SockJS('/websocket');
			//socket = new SockJS('http://192.168.0.132:8080/websocket');
			//STOMP를 초기화 하기 위해서 over()를 이용해 SockJS 정보 기반으로 설정
			stompClient = Stomp.over(socket);
			//연결된 경우에 실행할 함수
			stompClient.connect({}, onConnected, onError);
			console.log("connected!!!");
		}
	}
}

//connect 이후에 실행되는 함수
function onConnected() {
	//정상적으로 연결이 된 경우에 해당 설정으로 구독을 한 후에 onMessageReceived를 실행
	stompClient.subscribe('/broker', onMessageReceived);
	
	//로그인 한 경우에 addUser로 Messagemapping
	stompClient.send("/app/chat.join", {}, JSON.stringify( {userId : userId, type : 'JOIN'} ));
	alert("연결되었습니다.");
}

//에러(시스템 에러 및 연결이 끊긴 경우)
function onError(error) {
	alert("시스템 에러가 발생하였습니다.");
}

//메시지 전송하기 전 메시지 체크
function sendChk() {
	var msg;
	if($("#textMessage").val() == null || $("#textMessage").val() == "") {
		return;
	} else {
		msg = $("#textMessage").val();
	}
	var sendMsg = {type : "SEND", content : msg, sessionId : sessionId};
	send(sendMsg);		
}

//채팅방 나가기
function roomOut() {
	var msg = {type : "LEAVE", sessionId : sessionId};
	disconnect(msg);
}

//메시지 전송
function send(event) {
	if(stompClient) {
		var sendMsg = {
			userId : userId,
			content : event.content,
			type : event.type,
			sessionId : event.sessionId
		};
		
		stompClient.send("/app/chat.send", {}, JSON.stringify(sendMsg));
		//메시지 초기화
		$("#textMessage").val("");
		$("#textMessage").focus();
	}
}

//채팅방 나가기
function disconnect(event) {
	if(stompClient) {
		var sendMsg = {
				userId : userId,
				type : event.type,
				sessionId : event.sessionId
		};
	}
	//설정된 경로로 leave로 전달
	stompClient.send("/app/chat.leave", {}, JSON.stringify(sendMsg));
}

//메시지를 받을 때 동작하는 함수(onconnected 이후에 실행되는 함수로, 서버에서 전송된 메시지 안에 type에 따라서 실행)
function onMessageReceived(payload) {
	var msg = JSON.parse(payload.body);
	
	//세션 리스트 검사(처음에 세션이 존재하지 않기 때문에)
	if(sessionId == "") {
		sessionId = msg.sessionId;		
	}
	
	//메시지 전송, 채팅 입장, 채팅 나가기에 사용(아이디가 같은 경우 세션 아이디로 구분하기 위함)
	if(sIdList.length == 0) {
		sIdList.push(msg.sessionId);
	}
		
	//메시지 type이 JOIN인 경우(채팅 참여)
	if(msg.type === 'JOIN') {
		if(msg.status === 'SUCCESS') {
		$("#systemBox").css("display", "block");
		$("#roomBox").css("display", "none");
		$("#userId").attr("disabled", true);
		$("#loginBtn").attr("disabled", true);
		$("#textMessage").focus();
		
		var html = "";
		html += "<div class=\"chat-unit\">";
		//아이디가 같은 경우에 세션 아이디로 비교 후에 세션 아이디가 다른 경우
		if(userId == msg.userId && !sIdList.includes(msg.sessionId)) {
			html += "<span class=\"user-info\"><i>"+msg.userId+"님이 채팅방에 입장하였습니다. ("+nowDate()+")</i></span>";
			sIdList.push(msg.sessionId);
		//자신이 입장한 경우
		} else if(userId == msg.userId) { 
			html += "<span class=\"user-info\"><i>채팅방에 입장하였습니다. ("+nowDate()+")</i></span>";
		//아이디가 다른 경우
		} else {
			html += "<span class=\"user-info\"><i>"+msg.userId+"님이 채팅방에 입장하였습니다. ("+nowDate()+")</i></span>";
		}
		
		html += "</div>";
		
		$("#chatBox").append(html);
		} else {
			alert("로그인 작업이 정상적으로 이루어지지 않았습니다.");	
		}
		
		//채팅 입장한 경우에 인원 표시
		var userCntHtml = "";
		userCntHtml += Object.keys(msg.userInfoMap).length;
		$("#totalUser").html(userCntHtml);
		
	//메시지 type이 LEAVE인 경우(채팅 나가기)
	} else if(msg.type === 'LEAVE') {
		if(msg.status === "SUCCESS") {
			var html = "";
			//자신의 아이디와 다른 사람의 아이디가 같고, 세션 아이디는 다른 경우(본인이 나간 경우 X)
			if((userId == msg.userId && sessionId != msg.sessionId)) {
				sIdList.pop(msg.sessionId);
				html += "<div class=\"chat-unit\">";
				html += "<span class=\"user-info\">"+msg.userId+"님이 채팅방을 나갔습니다.<i>"+nowDate()+"</i></span>";
				html += "</div>";
			
				$("#chatBox").append(html);
			//자신이 나간 경우
			} else if(userId == msg.userId) {
				alert("채팅방을 나갔습니다.");
				$("#systemBox").css("display", "none");
				$("#roomBox").css("display", "block");
				$("#userId").attr("disabled", false);
				$("#loginBtn").attr("disabled", false);
				//채팅방 나간 후에 연결 끊음
				stompClient.disconnect();
				location.reload();
				console.log("disconnected!!!");
				
			//아이디와 세션 아이디가 모두 다른 경우(본인이 나간 경우 X)
			} else {
				html += "<div class=\"chat-unit\">";
				html += "<span class=\"user-info\">"+msg.userId+"님이 채팅방을 나갔습니다.<i>"+nowDate()+"</i></span>";
				html += "</div>";
			
				$("#chatBox").append(html);
			}
		} else {
			alert("다시 시도해주시기 바랍니다.");
		}
		
		//채팅에서 사용자가 나간 뒤에 숫자를 다시 표시해주기 위함
		var userCntHtml = "";
		userCntHtml += Object.keys(msg.userInfoMap).length;
		$("#totalUser").html(userCntHtml);
		
	//메시지 type이 SEND인 경우(메시지 전송)
	} else if(msg.type === 'SEND'){
		if(msg.status === "SUCCESS") {
			var html = "";
			html += "<div class=\"chat-unit\">";
			//아이디가 같은 경우 세션 아이디를 비교 후에 세션 아이디가 다른 경우에 메시지 출력
			if(userId == msg.userId && sessionId != msg.sessionId) {
				html += "<span class=\"user-info\"><b>"+msg.userId+"</b><i>"+nowDate()+"</i></span>";
			//자신이 보낸 경우
			} else if(userId == msg.userId) {
				html += "<span class=\"user-info\"><b>내가 보냄</b><i>"+nowDate()+"</i></span>";
			//아이디가 다른 경우
			} else {
				html += "<span class=\"user-info\"><b>"+msg.userId+"</b><i>"+nowDate()+"</i></span>";
			}
			html += "<p>"+msg.content+"</p>";
			html += "</div>";
		
			$("#chatBox").append(html);
		} else {
			alert("메시지를 전송하는데 실패했습니다.");
		}
	}
	//chatBox 내부에 스크롤 바 유지
	$("#chatBox").scrollTop($("#chatBox").prop("scrollHeight"));		
}

//캠 전송
function sendCam() {
	console.log(camCanvas.toDataURL('image/jpeg'));
}

function camHandler(){
	camDrawCanvas();
	sendCam();
}
</script>

<body>

<!-- WebSocket:<label id="connectStatus"></label> -->

<!-- container -->
<div class="container" onload="load()">
	<section class="card user-card" id="loginBox">
		<div class="card-title">
			<h4>대기</h4>
		</div>
		<ul class="user-list" id="waitUserList">
			<!-- <li>userid1</li> -->
		</ul>
		<div class="editing-area">
			<input type="text" class="" title="" value="" id="userId" onkeydown="if(event.keyCode==13) javascript:connect();"/>
			<button type="button" class="btn" id="loginBtn" onclick="connect()">로그인</button>
		</div>
	</section>

	<!-- <section class="card room-box" id="roomBox">
		<div class="card-title">
			<h4>채팅방</h4>
		</div>
		<ul class="room-list" id="roomList">
			<li>roomname1</li>
		</ul>
		<div class="editing-area">
			<input type="text" class="" title="" value="" id="roomName"/>
			<button type="button" class="btn" onclick="makeRoom()">만들기</button>
		</div>
	</section> -->

	<section class="card system-box" id="systemBox" style="display:none;">
	<input type="hidden" id="roomKey"/>
		<div class="card-title">
			<h4 id="roomTitle">채팅</h4>
			<div class="title-elements">
				<button type="button" class="btn" onclick="roomOut()">나가기</button>
			</div>
		</div>
		<div class="video-box">
			<div class="video-mine">
				<div class="video-cover">
					<video autoplay="true" id="videoElement"></video>
					<canvas id="camCanvas" style="display:none"></canvas>
				</div>
			</div>
			<div class="video-user">
				<!-- <ul class="">
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/2018/12/12/tip014d18100695.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/2017/07/05/cm08282685.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/2017/07/05/cm08282367.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/2017/07/05/cm08279313.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/2015/10/30/cb047006828.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/2016/04/25/tip034j16030177.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/Premium/4/5/0/B0/x/pc002095630.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/Premium/4/5/0/77/f/pc002063480.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/2014/01/31/yaytg254281.jpg" alt=""/>
						</div>
					</li>
					<li>
						<div class="video-cover">
							<img src="http://previewnw.clipartkorea.co.kr/Premium/4/5/0/75/p/pc002060832.jpg" alt=""/>
						</div>
					</li>
				</ul> -->
			</div>
			<div class="text-left" id="totalUser" style="font-size: x-large; font-weight: bold; color: blue;"></div>
		</div>
		<div class="chat-box" id="chatBox">
			<!-- <div class="chat-unit">
				<span class="user-info"><b>userid1</b><i>2020-10-30 11:14</i></span>
				<p>입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.입력한 챗팅 내용이 노출되는 영역입니다.</p>
			</div> -->
		</div>
		<div class="editing-area">
			<input type="text" class="" title="" value="" id="textMessage" onkeydown="if(event.keyCode==13) javascript:sendChk();"/>
			<button type="button" class="btn" onclick="sendChk()">전송</button>
		</div>
	</section>
</div> <!--// container -->
<footer>chatting service test ver 0.1 2020-10-29</footer>
</body>

<script>

/* function loginChk(){
	if(!loginYn) return false;
	if($("#userId").val() != userId) return false;
	return true;
} */

function nowDate() {
	var date = new Date();
	
	var month = date.getMonth()+1;
	if(month.length == 1){ 
		  month = "0" + month; 
	}
	
	var day = date.getDate();
	if(day.length == 1){ 
	  day = "0" + day; 
	}
	
	var hour = date.getHours();
	if(hour.length == 1){ 
		hour = "0" + hour; 
	}
	
	var minute = date.getMinutes();
	if(minute.length == 1){ 
		minute = "0" + minute; 
	}
	return date.getFullYear()+"-"+month+"-"+day+" "+hour+":"+minute;
}
</script>
</html>