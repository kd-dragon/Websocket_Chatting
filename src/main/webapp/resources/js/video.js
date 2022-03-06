$(function() {
	
	var constraints = { audio: true, video: { width: 300, height: 200 } };

	navigator.mediaDevices.getUserMedia(constraints)
	.then(function(mediaStream) {
	  var video = document.getElementById('videoElement');
	  video.srcObject = mediaStream;
	  video.onloadedmetadata = function(e) {
	    video.play();
	  };
	})
	.catch(function(err) { console.log(err.name + ": " + err.message); videoError = true });
});

var camCanvas = document.getElementById("camCanvas");
var cam_context=camCanvas.getContext('2d');

function camDrawCanvas(){
	console.log(camCanvas);
	console.log(cam_context);
	cam_context.drawImage(video,0,0,camCanvas.width, camCanvas.height);
}