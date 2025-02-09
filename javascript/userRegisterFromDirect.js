$(function(){
    let uri = location.href;
    let reg = new RegExp(/\/userRegisterFromMail-.*/g)
    let tmpUriKey = uri.match(reg)[0].replace(/\/userRegisterFromMail-/g,"");
    var obj = new Object();
    obj['tmpUrl'] = tmpUriKey;
    $.ajax({
        type: 'POST',
        data: obj,
        url: '/userRegisterFromMail',
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(data){
        if(data['result'] === 'success') {
            successDialog();
        } else {
            failDialog();
        }
    })
    .fail(function(){
        failDialog();
    })
});

function successDialog() {
    var option = new Object();
    option.parentId = '#userRegisterBody';
    option.message = 'ユーザーの登録に成功しました';
    option.originalButton = '<button id=\'userRegisterFromDirectly\' class=\'btn btn-primary\'>ホームに戻る</button>';
     new Dialog(option);
    $('#userRegisterFromDirectly').off('click');
    $('#userRegisterFromDirectly').on('click', gobackHome);
}

function failDialog() {
    var option = new Object();
    option.parentId = '#userRegisterBody';
    option.message = '申し訳ありません。ユーザーの登録に失敗しました。しばらくしてから今一度再度登録をお願いします。';
    option.originalButton = '<button id=\'userRegisterFromDirectly\' class=\'btn btn-primary\'>ホームに戻る</button>';
    new Dialog(option);
    $('#userRegisterFromDirectly').off('click');
    $('#userRegisterFromDirectly').on('click', gobackHome);
}

function gobackHome() {
    window.location.href = "/";
}