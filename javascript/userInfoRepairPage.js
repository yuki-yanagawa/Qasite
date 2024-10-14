var FIXED_IMG_WIDTH = 0;
var FIXED_IMG_HEIGHT = 0;
var imgFrameDefaultSize = 200;
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
            ctx.drawImage(img, leftPosition + newWidth, topPosition, newWidth, newHeight);
        })
        img.src = fileData.result;
    });
    fileData.readAsDataURL($('#userImgInput')[0].files[0]);
}

function initializeUpdateImage() {
    // $('#userImgInput').off('change');
    // $('#userImgInput').on('change', userImgUpdate);
    $('#inputUserImageUploadWrap').off('click');
    $('#inputUserImageUploadWrap').on('click', function(){
        $('#inputUserImageUpload').click();
    });
    $('#inputUserImageUpload').off('change');
    $('#inputUserImageUpload').on('change', uploadUserImgFile);
}

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
    $.ajax({
        type: 'POST',
        url: '/updateUserInfo',
        contentType : 'application/json',
        data : userUpdateInfoParam
    })
    .done(function(data){
        return dfd.resolve();
    })
    .fail(function(){
        return dfd.reject();
    });
}

function collectParameter() {
    return retObj  = {
        'userId' : $('#userIDArea input').val(),
        'username' : $('#userNameArea input').val(),
        'introductionText' : changeJapaneseToCharacterCode(adjustPreviweTextCreate($('#inputTextArea')[0].innerHTML)),
        //'picture' : $('#imgCanvas').css('display') === 'none' ? "" : $('#imgCanvas').get(0).toDataURL('png/jpeg')
    }; 
}


function uploadUserImgFile(e) {
    let filename = e.currentTarget.files[0].name;
    let fileReader = new FileReader();
    fileReader.onload = (function(e){
        if($('#appendImgDom').length === 1) {
            $('#appendImgDom').remove();
        }
        $('#userCircleSvg').hide();
        $('.img-area').append(appendEmptyImgDom(true));
        $('#appendImgDom').attr('src', e.currentTarget.result);
        // let index = $('.uploadImageFileListFrame').length;
        // let clone = $('.uploadImageFileListFrame').eq(0).clone();
        // let cloneDomId = 'uploadImageFileListFrame' + index;
        // clone.attr({id : cloneDomId});
        // $('#uploadedImageFileArea').append(clone);
        // $('#' + cloneDomId).show();
        // $('#' + cloneDomId + ' .openValue').val(filename);
        // $('#' + cloneDomId + ' button').off('click');
        // $('#' + cloneDomId + ' button').on('click', function(){
        //     $('#' + cloneDomId).remove();
        // });
        // $('#' + cloneDomId + ' .hiddenValue').val(e.currentTarget.result);
        // $('#inputImageFileForUpload').val('');
    })
    fileReader.readAsDataURL(e.currentTarget.files[0]);
}

function appendEmptyImgDom(onloadFlg) {
    if(onloadFlg) {
        return '<div style=\"text-align:center;\"><img id=\"appendImgDom\" onload=\"adjustImg();\"/></div>';
    }
    return '<div style=\"text-align:center;\"><img id=\"appendImgDom\"/></div>';
}

function resizeImgData() {
    if(imgFrameDefaultSize >= $('#appendImgDom').width() && imgFrameDefaultSize >= $('#appendImgDom').height()) {
        return;
    }
    if($('#appendImgDom').width() > $('appendImgDom').height()) {
        $('#appendImgDom').width(imgFrameDefaultSize);
    } else {
        $('#appendImgDom').height(imgFrameDefaultSize);
    }
    return;
}

function resizeImgDataUpdate() {
    let dfd = $.Deferred();
    const canvas = document.createElement('canvas');
    canvas.width = imgFrameDefaultSize;
    canvas.height = imgFrameDefaultSize;
    const ctx = canvas.getContext('2d');
    var img = new Image();
    img.onload = (function(){
        let leftPosition = 0;
        let topPosition = 0;
        let width = $('#appendImgDom').width();
        let height = $('#appendImgDom').height();
        leftPosition = (imgFrameDefaultSize - width) / 2;
        topPosition = (imgFrameDefaultSize - height) / 2
        ctx.drawImage(img, leftPosition, topPosition, $('#appendImgDom').width(), $('#appendImgDom').height());
        return dfd.resolve(canvas.toDataURL('image/png'));
    })
    img.src = $('#appendImgDom').attr('src');
    return dfd.promise();
}

function adjustImg() {
    resizeImgData();
    resizeImgDataUpdate()
    .then(function(binReSizeData){
        $('#appendImgDom').remove();
        $('.img-area').append(appendEmptyImgDom(false));
        $('#appendImgDom').attr('src', binReSizeData);
    })
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
