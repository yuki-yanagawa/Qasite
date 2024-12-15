//encoder encode method is return Uint8
//Base64 do not think about Unsign Or sign
//Because post request 2STEP IS NEED
// const encoder = new TextEncoder("utf-8");

$(function(){
    $('#loginSubmitBt').on('click', loginDataSubmit);
 });

function passSha256() {
    let dfd = $.Deferred();
    let b64 = window.btoa($('#Password').val().trim());
    let uintB64 = encoder.encode(b64);
    crypto.subtle.digest('SHA-256', uintB64)
    .then(function(cryptoData){
        let retData = window.btoa(new Int8Array(cryptoData));
        return dfd.resolve(retData);
    });
    return dfd.promise();
}


function loginDataSubmit() {
    var requestParam = new Object();
    requestParam['name'] = changeUserNameToBase64($('#Username').val().trim());
    if(window.isSecureContext) {
        passSha256Create($('#Password').val().trim())
        .then(function(pass){
            requestParam['password'] = pass;
            requestParam['checked'] = true;
            submitProcess(requestParam);
        });
    } else {
        requestParam['password'] = window.btoa($('#Password').val().trim());
        requestParam['checked'] = false;
        submitProcess(requestParam);
    }
    
}

function submitProcess(param) {
    debugger
    $.ajax({
        type: 'POST',
        url: '/loginInfo',
        data: param,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(data) {
        if(data['loginResult'] == true) {
            var option = new Object();
            option.parentId = '#loginBody';
            option.message = 'ログインに成功しました';
            option.originalButton = '<button id=\'loginOKBt\'class=\'btn btn-primary\'>画面に戻る</button>'
            new Dialog(option);
            $('#loginOKBt').on('click', backForwardGui);
        } else {
            var option = new Object();
            option.parentId = '#loginBody';
            option.message = 'ログインに失敗しました（入力した情報に誤りがあると思われます）';
            new Dialog(option);
        }
    })
    .fail(function() {
        var option = new Object();
        option.parentId = '#loginBody';
        option.message = 'ログインに失敗しました';
        new Dialog(option);
    });
}

function backForwardGui() {
    let hrefPage = localStorage.getItem('beforeHrefRequest');
    localStorage.setItem('beforeHrefRequest', "");
    if(hrefPage === null || hrefPage === "") {
    	hrefPage = "/";
    }
    window.location.href = hrefPage;
}