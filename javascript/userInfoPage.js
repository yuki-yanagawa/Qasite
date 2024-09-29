var FIXED_IMG_WIDTH = 0;
var FIXED_IMG_HEIGHT = 0;
$(function(){
    initializeUserImageArea();
    initializeUserInfoArea();
    let userId = getUserIdFromURI();

    loadingfunctionStart();
    $.when(
        getUserInfomation(userId),
        getUserIntroduction(userId)
    )
    .then(function(result){
        if(!result) {
            createUserPageLoadFailedDailog();
            return;
        }
        footerPositionSetting();
        loadingfunctionEnd();
    });
    
});

function initializeUserImageArea() {
    $('#imgCanvas').hide();
    FIXED_IMG_WIDTH = $('#userCircleSvg').width();
    FIXED_IMG_HEIGHT = $('#userCircleSvg').height();
    $('#imgCanvas').css('height', FIXED_IMG_WIDTH);
    $('#imgCanvas').css('width', FIXED_IMG_HEIGHT);
}

function initializeUserInfoArea() {
    let labelWidth = $('#userIDArea .input-group-text').outerWidth();
    $('#userRankArea .input-group-text').css('width', labelWidth);
    $('#userNameArea .input-group-text').css('width', labelWidth);
}

function footerPositionSetting() {
    let vhHeight = $(window).height();
    let userPageBodyHeight = $('#userInfoPageBody').height();
    let footerHight = $('.container-fluid').height();
    if(vhHeight <= userPageBodyHeight + footerHight) {
        $('.container-fluid').css('position', 'absolute');
        $('.container-fluid').css('top', userPageBodyHeight + 'px');
    }
}

function getUserIdFromURI() {
    let uri = window.location.href;
    let reg = new RegExp(/\/userInfoPage-[0-9]+/g);
    let userId = uri.match(reg)[0].replaceAll(/\/userInfoPage-/g,"");
    return userId;
}

function getUserInfomation(userId) {
    let dfd = $.Deferred();
    $.ajax({
        type: 'POST',
        url: '/getUserInfo/' + userId,
        contentType : 'application/json',
    })
    .done(function(data){
        if($.isEmptyObject(data.result)) {
            return dfd.resolve(false);
        }
        settingUserInfomaition(data.result);
        return dfd.resolve(true);
    })
    .fail(function(){
        return dfd.reject(false);
    });
    return dfd.promise();
}

function getUserIntroduction(userId) {
    let dfd = $.Deferred();
    $.ajax({
        type: 'POST',
        url: '/getUserIntroduction/' + userId,
        contentType : 'application/json',
    })
    .done(function(data){
        if($.isEmptyObject(data.result)) {
            return dfd.resolve(false);
        }
        settingUserIntroduction(data.result);
        return dfd.resolve(true);
    })
    .fail(function(){
        return dfd.reject(false);
    });
    return dfd.promise();
}

function createUserPageLoadFailedDailog() {
    var option = new Object();
    option.parentId = '#userInfoPageBody';
    option.message = 'ユーザーページの表示に失敗しました。再度リトライして下さい。';
    option.originalButton = '<button id=\'backHomePage\' class=\'btn btn-primary\'>ホーム画面に戻る</button>'
    new Dialog(option);
    $('#backHomePage').on('click', forwardHome);
}

function forwardHome() {
    window.location.href = "/";
}

function settingUserInfomaition(userData) {
    let userId = userData['userId'];
    let username = userData['username'];
    let userPicture = userData['userPicture'];
    let userLevel = userData['userLevel'];
    let userIntroduction = userData['userIntroduction'];
    $('#userIDArea input').val(String(userId));
    $('#userNameArea input').val(String(username));
    let userLevelObj = getUserLevelColor(userLevel);
    if(userLevelObj !== null) {
        $('#userRankSvg').css('color', userLevelObj.color);
        $('#userRankArea input').val(userLevelObj.text);
        $('#hiddenUserRankValue').val(String(userLevel));
    }
    if(userData['myInfoPage'] === true) {
        console.log('my info page....');
        $('#repairUserInfo').show();
        $('#repairUserInfo').off('click');
        $('#repairUserInfo').on('click', forwardUserInfoRepaierPage);
    } else {
        $('#repairUserInfo').hide();
    }
}

function getUserLevelColor(userLevel) {
    if(Number(userLevel) === Number.NaN) {
        console.error('user level getting error');
        return null;
    }
    let tmpVal = Number(userLevel);
    let userLevelObj = new Object();
    if(tmpVal === 0) {
        userLevelObj.color = '#0000ff';
        userLevelObj.text = 'ブロンズユーザー';
        return userLevelObj;
    } else if(tmpVal === 1) {
        userLevelObj.color = '#E1E3E0';
        userLevelObj.text = 'シルバーユーザー';
        return userLevelObj;
    } else if(tmpVal === 2) {
        userLevelObj.color = '#ffd700';
        userLevelObj.text = 'ゴールドユーザー';
        return userLevelObj;
    }
    console.error('user level getting error');
    return null;
}

function settingUserIntroduction(userIntroData) {
    $('#inputTextArea')[0].innerHTML = changeCharacterCodeToJapanese(userIntroData['introduction']);
}

function forwardUserInfoRepaierPage() {
    let userId = $('#userIDArea input').val();
    // let requetPag = '/userInfoRepairPage?userId=' + String(userId) + '&username=' + String($('#userNameArea input').val()) 
    //     + '&userLevel=' + String($('#hiddenUserRankValue').val());
    let requetPag = '/userInfoRepairPage?userId=' + String(userId);
    let saveObj = collectUserInfomation();
    localStorage.setItem('repairUserObj', JSON.stringify(saveObj));
    window.location.href = requetPag;
}

function collectUserInfomation() {
    let userInfo = new Object();
    userInfo['userId'] = $('#userIDArea input').val();
    userInfo['username'] = $('#userNameArea input').val();
    userInfo['userLevel'] = $('#hiddenUserRankValue').val();
    userInfo['userIntroduction'] = $('#inputTextArea').get(0).innerHTML;
    return userInfo;
}

function loadingfunctionStart() {
    let top = $('.container').position().top;
    let width = $('body').width();
    $('.block').css('top', top + 'px');
    $('.block').css('width', width + 'px');
    $('.block').show();
}

function loadingfunctionEnd() {
    $('.block').hide();
}
