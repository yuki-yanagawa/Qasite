var webSocket;

$(function(){
    initButtonData();
});

function initButtonData() {
    $('#enterBt').off('click');
    $('#enterBt').on('click', webSocketCreate);
    $('#postBt').off('click');
    $('#postBt').on('click', sendMessage);
}

function checkSecureStatement(url) {
    return window.isSecureContext && url.startsWith('https');
}

function webSocketCreate() {
    $('#enterBt').attr('disabled', true);
    var url = window.location.href;
    var hostname = url.replaceAll(/https*:\/\//g, "").replaceAll(/\/tobaccoRoom/g, "");
    if(checkSecureStatement(url)) {
        webSocket = new WebSocket("wss://" + hostname + "/upgradeWebSocketConneting");
    } else {
        webSocket = new WebSocket("ws://" + hostname + "/upgradeWebSocketConneting");
    };

    webSocket.onopen = function() {
	    console.log("open....");
        alert("web socket connect!!!");

    };

    webSocket.onerror = function() {
	    console.error("web socket connect error");
        alert("web socket connect error");
        $('#enterBt').attr('disabled', false);
    };

    webSocket.onmessage = function(e) {
        var node = '<p>' + e.data + '</p>'
        $('#commentArea').append(node);
    };

    webSocket.onclose = function() {
        console.log("close....");
        alert("web socket close");
        $('#enterBt').attr('disabled', false);
    };
}

function sendMessage() {
    var val = $('#text').val();
    webSocket.send(val);
}