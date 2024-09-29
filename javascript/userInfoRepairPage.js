var FIXED_IMG_WIDTH = 0;
var FIXED_IMG_HEIGHT = 0;
var pictureUpdateFlg = false;
var introductionTextUpdateFlg = false;
$(function() {
    initializeButton();
    initializeUpdateImage();
    initializeUserImageArea();
    initializeUserInfoArea();
    settingUserInfoArea();
    footerPositionSetting();
    // $('#imgCanvas').hide();
    // $('#userImgInput').off('change');
    // $('#userImgInput').on('change', userImgUpdate);
    // FIXED_IMG_WIDTH = $('#userCircleSvg').width();
    // FIXED_IMG_HEIGHT = $('#userCircleSvg').height();
    // $('#imgCanvas').css('height', FIXED_IMG_WIDTH);
    // $('#imgCanvas').css('width', FIXED_IMG_HEIGHT);
});

function userImgUpdate(e) {
    let imgPath = $('#userImgInput').val();
    if(imgPath === '') {
        $('#imgCanvas').hide();
        $('#userCircleSvg').show();
        return;
    }
    let fileData = new FileReader();
    fileData.onload = (function() {
        $('#userCircleSvg').hide();
        $('#imgCanvas').show();
        $('#imgCanvas').css('width', FIXED_IMG_WIDTH + 20);
        $('#imgCanvas').css('height', FIXED_IMG_HEIGHT + 20);
        var img = new Image();
        img.onload = (function() {
            var ctx = $('#imgCanvas').get(0).getContext('2d');
            debugger
            if(img.naturalHeight < img.naturalWidth) {
                var newWidth = FIXED_IMG_WIDTH;
                var newHeight = img.naturalHeight * (FIXED_IMG_HEIGHT / img.naturalWidth);
                var topPosition = (FIXED_IMG_HEIGHT + 20 - newHeight);
                var leftPosition = 0;
                // console.log("width : " + newWidth);
                // console.log("height : " + newHeight);
            } else {
                var newHeight = FIXED_IMG_HEIGHT;
                var newWidth = img.naturalWidth * (FIXED_IMG_WIDTH / img.naturalHeight);
                var leftPosition = (FIXED_IMG_WIDTH + 20 - newWidth)/2;
                var topPosition = 0;
                // console.log("left : " + leftPosition);
                // console.log("newWidth : " + newWidth);
                // console.log("width : " + (FIXED_IMG_WIDTH + 20));
            }
            pictureUpdateFlg = true;
            ctx.drawImage(img, leftPosition + newWidth, topPosition, newWidth, newHeight);
        })
        img.src = fileData.result;
    });
    fileData.readAsDataURL($('#userImgInput')[0].files[0]);
}

function initializeUpdateImage() {
    $('#userImgInput').off('change');
    $('#userImgInput').on('change', userImgUpdate);
}

function initializeUserImageArea() {
    $('#imgCanvas').hide();
    FIXED_IMG_WIDTH = $('#userCircleSvg').width();
    FIXED_IMG_HEIGHT = $('#userCircleSvg').height();
    $('#imgCanvas').css('height', FIXED_IMG_WIDTH);
    $('#imgCanvas').css('width', FIXED_IMG_HEIGHT);
}

function initializeUserInfoArea() {
    //let labelWidth = $('#userIDArea .input-group-text').outerWidth();
    //$('#userRankArea .input-group-text').css('width', labelWidth);
    //$('#userNameArea .input-group-text').css('width', labelWidth);
}

function footerPositionSetting() {
    let vhHeight = $(window).height();
    let userPageBodyHeight = $('#userInfoRepqirPageBody').height();
    let footerHight = $('.container-fluid').height();
    if(vhHeight <= userPageBodyHeight + footerHight) {
        $('.container-fluid').css('position', 'relative');
        //$('.container-fluid').css('top', userPageBodyHeight + 'px');
    }
}

function initializeButton() {
    $('#inputPreview').hide();
    $('#updateUserInfoBt').off('click');
    $('#previewInputSwitchBt').off('click');
    $('#previewInputSwitchBt').on('click', switchBttunClick);
    $('#updateUserInfoBt').on('click', updateUserInfo);
}

function switchBttunClick() {
    if($('#previewInputSwitchBt')[0].innerHTML === 'プレビュー') {
        let height = $('#inputTextArea').height();
        $('#previewInputSwitchBt')[0].innerHTML = '自己紹介の作成';
        let inputText = $('#inputTextArea')[0].innerHTML;
        $('#inputPreview')[0].innerHTML = '';
        if(inputText.trim() !== '') {
            $('#inputPreview')[0].innerHTML = adjustPreviweTextCreate(inputText);
        }
        $('#inputTextArea').hide();
        $('#inputPreview').height(height);
        $('#inputPreview').show();
    } else {
        $('#previewInputSwitchBt')[0].innerHTML = 'プレビュー';
        $('#inputTextArea').show();
        $('#inputPreview').hide();
    }
}

function updateUserInfo() {
    let userUpdateInfoParam = collectParameter();
    let hiddenUserName = $('#userNameHidden').val();
    updateUserName({'username' : userUpdateInfoParam['username'], 'userId' : userUpdateInfoParam['userId']})
    .done(function(result){
        if(!result) {
            return;
        }
        $.when(
            updateUserImage(),
            updateUserIntroduction({'userId' : userUpdateInfoParam['userId'], 'userIntroduction' : userUpdateInfoParam['introductionText']})
        )
        .done(function(){
            var option = new Object();
            option.parentId = '#userInfoRepqirPageBody';
            option.message = 'ユーザー情報を更新致しました。';
            new Dialog(option);
        })
        .fail(function(){
            var option = new Object();
            option.parentId = '#userInfoRepqirPageBody';
            option.message = '一部ユーザー情報の編集に失敗しました。';
            new Dialog(option);
        })
    })
    .fail(function(data){
        var option = new Object();
        option.parentId = '#userInfoRepqirPageBody';
        option.message = 'ユーザー情報の編集に失敗しました。';
        new Dialog(option);
    });
}

function updateUserName(userUpdateData) {
    let dfd = $.Deferred();
    if(userUpdateData['username'] === $('#userNameHidden').val()) {
        return dfd.resolve(true);
    }
    $.ajax({
        type: 'PUT',
        url: '/updateUserName',
        contentType : 'application/json',
        data : userUpdateData
    })
    .done(function(){
        return dfd.resolve(true);
    })
    .fail(function(){
        return dfd.reject(false);
    });
    return dfd.promise();
}

function updateUserImage() {
    let dfd = $.Deferred();
    if(!pictureUpdateFlg) {
        return dfd.resolve();
    }
    return dfd.promise();
}

function updateUserIntroduction(userUpdateData) {
    let dfd = $.Deferred();
    // if(!introductionTextUpdateFlg) {
    //     return dfd.resolve();
    // }
    $.ajax({
        type: 'PUT',
        url: '/updateUserIntroduction',
        contentType: 'application/json',
        data: userUpdateData
    })
    .done(function(){
        var option = new Object();
        option.parentId = '#userInfoRepqirPageBody';
        option.message = '自己紹介を更新致しました。';
        new Dialog(option);
    })
    .fail(function(){
        var option = new Object();
        option.parentId = '#userInfoRepqirPageBody';
        option.message = '自己紹介の更新に失敗致しました。';
        new Dialog(option);
    });
    return dfd.promise();
}

function collectParameter() {
    return retObj  = {
        'userId' : $('#userIDArea input').val(),
        'username' : $('#userNameArea input').val(),
        'introductionText' : changeJapaneseToCharacterCode(adjustPreviweTextCreate($('#inputTextArea')[0].innerHTML)),
        'picture' : $('#imgCanvas').css('display') === 'none' ? "" : $('#imgCanvas').get(0).toDataURL('png/jpeg')
    }; 
}

function settingUserInfoArea() {
    let obj = localStorage.getItem('repairUserObj');
    if(obj === null) {
        var option = new Object();
        option.parentId = '#userInfoPageBody';
        option.message = 'ユーザーページの表示に失敗しました。再度リトライして下さい。';
        option.originalButton = '<button id=\'backHomePage\' class=\'btn btn-primary\'>ホーム画面に戻る</button>'
        new Dialog(option);
        $('#backHomePage').on('click', forwardHome);
        return;
    }
    obj = JSON.parse(obj);
    localStorage.removeItem('repairUserObj');
    $('#userIDArea input').val(obj['userId']);
    $('#userNameArea input').val(obj['username']);
    $('#userNameHidden').val(obj['username']);
    let userLevel = obj['userLevel'];
    let userLevelObj = getUserLevelColor(userLevel);
    if(userLevelObj !== null) {
        $('#userRankSvg').css('color', userLevelObj.color);
        $('#userRankArea input').val(userLevelObj.text);
        // $('#hiddenUserRankValue').val(String(userLevel));
    }
    $('#inputTextArea').get(0).innerHTML = obj['userIntroduction'];
}

function forwardHome() {
    window.location.href = "/";
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