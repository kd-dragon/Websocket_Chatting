//라이브 방송 리스트
document.write("<script src='/resources/js/liveChat.js'></script>");
//테스트
var userId = "1";
var stomp = null;
var socket;
var sessionIdList = [];
var sessionId = "";
var lbSeq = "";

//방송 클릭 후 이벤트
function connect(event) {
	//userId = $("#userId").val();
	/*lbSeq = event;
	if(lbSeq == null || lbSeq == "") {
		alert("시스템 에러가 발생하였습니다.");
	} else {
		socket = new SockJS('/websocket');
		stomp.connect({}, onConnected, onError);
	}
	*/
	if(userId == null || userId == "") {
		alert("시스템 에러가 발생하였습니다.");
	} else {
		socket = new SockJS('/websocket');
		stomp = Stomp.over(socket);
		stomp.connect({}, onConnected, onError);
	}
}

//connect 이후 페이지 이동?
function onConnected() {
	console.log(typeof onMessageReceived);
	stomp.subscribe('/broker', onMessageReceived);
	stomp.send('/app/chat.join', {}, JSON.stringify( {userId : userId, type : 'JOIN'} ));
	//stomp.send('/app/chat.join/' + lbSeq, JSON.stringify( {userId : userId, type : 'JOIN'} ));
	alert("페이지 이동");
	window.location.href="/";
}

function onError(error) {
	alert("시스템 에러가 발생하였습니다. 관리자에게 문의하세요.");
}

