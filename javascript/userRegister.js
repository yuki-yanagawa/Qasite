$(function(){
    let backColor = $('#inputPassWord').css('background-color');
    $('.input-group-text').css('background-color', backColor);
    $('#inputPassWord').css('border-right', 'none');
    $('.input-group-text').css('border-left', 'none');
    $('#inputPassWord').css('margin-bottom', '0px');
    changePassWordDisplaySetting();
    $('#inputPassWord').on('keydown', passwordKeyDownFunction);
    $('#inputPassWord').on('focusout', passwordfocusOutFunction);
    $('#passwordToDisplay').off('click');
    $('#passwordToDisplay').on('click', passwordToDisplayFunction);
    $('#passwordToHidden').off('click');
    $('#passwordToHidden').on('click', passwordToHiddenFunction);
    $('#registerBt').on('click', registerFunction);
    if(window.navigator.userAgent.toLowerCase().indexOf('chrome') < 0) {
        $('#passwordToDisplay').hide();
        $('#passwordToHidden').hide();
    }
});


function changePassWordDisplaySetting() {
    if($('#inputPassWord').get(0).type === 'password') {
        $('#passwordToDisplay').show();
        $('#passwordToHidden').hide();
        let height = $('#inputPassWord').outerHeight();
        $('.input-group-prepend').css('height',height);
    } else {
        $('#passwordToHidden').show();
        $('#passwordToDisplay').hide();
    }
}

function passwordKeyDownFunction() {
    $('#passwordToHidden').css('opacity', 0);
    $('#passwordToDisplay').css('opacity', 0);
}

function passwordfocusOutFunction() {
    $('#passwordToHidden').css('opacity', 1);
    $('#passwordToDisplay').css('opacity', 1);
}

function passwordToDisplayFunction() {
    $('#inputPassWord').get(0).type = 'text';
    changePassWordDisplaySetting();
}

function passwordToHiddenFunction() {
    $('#inputPassWord').get(0).type = 'password';
    changePassWordDisplaySetting();
}

function registerFunction() {
    if(!checkUserName()) {
        var option = new Object();
        option.parentId = '#userRegisterBody';
        option.message = 'ユーザー名は英数字と「-」、「_」のみ有効になります';
        new Dialog(option);
        return;
    }

    if(!checkEmailAddress()) {
        var option = new Object();
        option.parentId = '#userRegisterBody';
        option.message = 'メールアドレスに誤りがあります';
        new Dialog(option);
        return;
    }

    if(!checkPassWord()) {
        var option = new Object();
        option.parentId = '#userRegisterBody';
        option.message = 'パスワードの入力に誤りがあります';
        new Dialog(option);
        return;
    }

    let mailText = $('#MailAddress').val().trim();
    let inputPassWord = $('#inputPassWord').val().trim();
    let userName = changeJapaneseToCharacterCode($('#Username').val().trim());
    inputPassWord = window.btoa(inputPassWord);
    digestMessageSHA256(inputPassWord)
    .then((digestpassWord) => {
        resisterDataSubmit(mailText, userName, digestpassWord);
    })
}

function resisterDataSubmit(mailText, userName, digestpassWord) {
    let requestParam = new Object();
    requestParam.mailText = mailText;
    requestParam.userName = userName;
    requestParam.digest = digestpassWord;
    $.ajax({
        type: 'POST',
        url: '/userRegister',
        data: requestParam,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(data) {
        if(data['result'] === 'mailExist') {
            var option = new Object();
            option.parentId = '#userRegisterBody';
            option.message = '既にこのメールアドレスは使用されています';
            new Dialog(option);
        } else if(data['result'] === 'usernameExist') {
            var option = new Object();
            option.parentId = '#userRegisterBody';
            option.message = '既にこのユーザー名は使用されています';
            new Dialog(option);
        } else if(data['result'] === 'sucsess') {
            var option = new Object();
            option.parentId = '#userRegisterBody';
            option.message = 'ユーザー登録に成功しました';
            option.originalButton = '<button id=\'gobackHomeBt\' class=\'btn btn-primary\'>ホームに戻る</button>';
            new Dialog(option);
            $('#gobackHomeBt').off('click');
            $('#gobackHomeBt').on('click', gobackHome);
        } else {
            var option = new Object();
            option.parentId = '#userRegisterBody';
            option.message = 'ユーザー登録に失敗しました。しばらくしてからご利用下さい。';
            option.originalButton = '<button id=\'gobackHomeBt\' class=\'btn btn-primary\'>ホームに戻る</button>';
            new Dialog(option);
            $('#gobackHomeBt').off('click');
            $('#gobackHomeBt').on('click', gobackHome);
        }
    })
    .fail(function() {
        var option = new Object();
        option.parentId = '#userRegisterBody';
        option.message = 'ユーザー登録に失敗しました。しばらくしてからご利用下さい。';
        option.originalButton = '<button id=\'gobackHomeBt\' class=\'btn btn-primary\'>ホームに戻る</button>';
        new Dialog(option);
        $('#gobackHomeBt').off('click');
        $('#gobackHomeBt').on('click', gobackHome);
    });
}

function checkUserName() {
    let reg = new RegExp(/[^a-zA-Z0-9-_]/g);
    let inputUserName = $('#Username').val().trim();
    if(inputUserName.match(reg) == null) {
        return true;
    }
    return false;
}

function checkEmailAddress() {
    let reg = new RegExp(/[^a-z0-9-_@\\.]/g);
    let mailText = $('#MailAddress').val().trim();
    if(mailText.match(reg) === null) {
        let attoIndex = mailText.indexOf('@');
        if(attoIndex > 0 && mailText.length > attoIndex + 1) {
            return true;
        }
        return false;
    }
    return false;
}

function checkPassWord() {
    let reg = new RegExp(/[^a-zA-Z0-9-_@\\(\\)\\{\\}\\^\\~\\.\\*\\?\\!\\^\\$]/g);
    let inputPassWord = $('#inputPassWord').val().trim();
    if(inputPassWord.match(reg) == null) {
        return true;
    }
    return false;
}

function gobackHome() {
    window.location.href = "/";
}