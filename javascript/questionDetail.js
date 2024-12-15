//depend on common.js
var QUESTIONID = -1;
var INIT_BODY_HEIGHT_SIZE = -1;
var ANSWERID_LIST = [];
var INTEXT_IMGSIZE = 300;
$(function(){
    loadingfunctionStart();
    QUESTIONID = getQuestionId();
    $('#answer-form').hide();
    $("#inputPreview").hide();
    $('#answerBt').off('click');
    $('#answerBt').on('click', clickAnswer);
    // $('#previewInputSwitchBt').off('click');
    // $('#previewInputSwitchBt').on('click', switchBttunClick);
    // $('#submitBt').off('click');
    // $('#submitBt').on('click', answerSubmit);
    INIT_BODY_HEIGHT_SIZE = $('body').get(0).scrollHeight;
    initImageFileEvent();
    INTEXT_IMGSIZE = Math.floor($('.questionDetailText').width() / 3);
    $.when(
        getQuestionDetailData(),
        getAnswerDetailData()
    )
    .then(function(){
        getAnswerImgAndLinkData()
        .then(function(retData){
            if(!retData) {
                console.error("answer link data and img data getting error.");
            }
            loadingfunctionEnd();
            if(INIT_BODY_HEIGHT_SIZE < $('body').get(0).scrollHeight) {
                $('body').css('height', 'auto');
                INIT_BODY_HEIGHT_SIZE = -1;
            }
        });
    });
});

// function answerSubmit() {
//     let inputText = $('#inputTextArea')[0].innerHTML;
//     if(inputText.trim === '') {
//         //alert("Pelase input question.");
//         var option = new Object();
//         option.parentId = '#questionDetailBody';
//         option.message = '回答を入力して下さい';
//         new Dialog(option);
//         //return dfd.resolve();
//         return;
//     }

//     var option = new Object();
//     option.parentId = '#questionDetailBody';
//     option.message = '回答を投稿しますか？';
//     option.originalButton = '<button id=\'postAnswerFuncId\' class=\'btn btn-primary\'>投稿する</button>' +
//     ' &nbsp;<button id=\'revertAnswerFuncId\' class=\'btn btn-primary\'>取り戻す</button>';
//     var dialog = new Dialog(option);
//     $('#postAnswerFuncId').off('click');
//     $('#revertAnswerFuncId').off('click');
    
//     $('#postAnswerFuncId').on('click', function() {
//         dialog.closeFunc();
//         answerPost(inputText);
//     });

//     $('#revertAnswerFuncId').on('click', function() {
//         dialog.closeFunc();
//     });
// }

// function answerPost(inputText) {
//     let dfd = $.Deferred();
//     // let uri = window.location.href;
//     // let reg = new RegExp(/\/getQuestionDetail-[0-9]+/g);
//     // let questionId = uri.match(reg)[0].replaceAll(/\/getQuestionDetail-/g,"");
//     let questionId = QUESTIONID;

    
//     inputText = adjustPreviweTextCreate(inputText);
//     let answerParam = new Object;
//     answerParam['text'] = changeJapaneseToCharacterCode(inputText);

//     $.ajax({
//         type: 'POST',
//         url: '/postAnswer/' + questionId,
//         contentType : 'application/json',
//         data : answerParam,
//         dataType : 'json'
//     })
//     .done(function(data){
//         debugger
//         if(data === null || $.isEmptyObject(data)) {
//             //alert("answer post erorr..");
//             var option = new Object();
//             option.parentId = '#questionDetailBody';
//             option.message = '回答の投稿に失敗しました';
//             new Dialog(option);
//             return dfd.resolve();
//         } else {
//             var option = new Object();
//             option.parentId = '#questionDetailBody';
//             option.message = '回答の投稿に成功しました';
//             option.originalButton = '<button id=\'answerFinshedId\' class=\'btn btn-primary\'>ページを更新する</button>';
//             var dialog = new Dialog(option);
//             $('#answerFinshedId').off('click');
//             $('#answerFinshedId').on('click', function() {
//                 dialog.closeFunc();
//                 window.location.href = '/getQuestionDetail-' + questionId;
//             });
//             return dfd.resolve();
//         }
//         //return dfd.resolve();
//     })
//     .fail(function(){
//         //alert("answer post erorr..");
//         var option = new Object();
//         option.parentId = '#questionDetailBody';
//         option.message = '回答の投稿に失敗しました';
//         new Dialog(option);
//         return dfd.reject();
//     });
//     return dfd.promise();
// }

function getAnswerDetailData() {
    let dfd = $.Deferred();
    let questionId = QUESTIONID;
    $.ajax({
        type: 'POST',
        url: '/getAnswerDetail/' + questionId,
        contentType : 'application/json',
    })
    .done(function(data){
        debugger
        if(data === null || $.isEmptyObject(data)) {
            console.log(questionId + " / answer no data");
            $('#answerDetail-area').hide();
            return dfd.resolve();
        }
        let parentSelector = $('#answerDetail-area');
        let idNameFrame = 'answerDetail-';
        let i = 0;
        let answerCount = data.length;
        $('#answerselectorLabel')[0].innerHTML = "回答(" + String(answerCount) + "件)";
        $('#answerDetail-area').show();
        for(i = 0; i < data.length; i++) {
            let cloneData = $('#answerDetail').clone();
            let idName = idNameFrame + data[i]['answerId'];
            ANSWERID_LIST.push(data[i]['answerId']);
            cloneData.attr('id', idName);
            parentSelector.append(cloneData);
            $('#' + idName + ' #answerUserName')[0].innerHTML = data[i]['useName'] + ' ' + $('#' + idName + ' #answerUserName')[0].innerHTML;
            let linkText = "userInfoPage-" + String(data[i]['answeredUserId']);
            $('#' + idName + ' #answerUserName').attr({href : linkText});
            $('#' + idName + ' #answerDate')[0].innerHTML = data[i]['answerUpdateDate'];
            $('#' + idName + ' .answerDetailText')[0].innerHTML = changeCharacterCodeToJapanese(data[i]['answerDetailData']);
            debugger
            if(data[i]['goodPointAction']) {
                $('#' + idName + ' #goodNoAct').hide();
            } else {
                $('#' + idName + ' #goodAct').hide();
            }
            $('#' + idName + ' #goodText')[0].innerHTML = "いいね(" + Number(data[i]['goodPointCount']) + ")";

            if(data[i]['helpfulPointAction']) {
                $('#' + idName + ' #helpfulNoAct').hide();
            } else {
                $('#' + idName + ' #helpfulAct').hide();
            }
            $('#' + idName + ' #helpfulText')[0].innerHTML = "参考になりました(" + Number(data[i]['helpfulPointCount']) + ")";

            $('#' + idName + ' #goodNoAct').off('click');
            $('#' + idName + ' #goodNoAct').on('click', clickGootAction);
            $('#' + idName + ' #goodAct').off('click');
            $('#' + idName + ' #goodAct').on('click', clickGootAction);
            $('#' + idName + ' #helpfulNoAct').off('click');
            $('#' + idName + ' #helpfulNoAct').on('click', clickHelpfulAction);
            $('#' + idName + ' #helpfulAct').off('click');
            $('#' + idName + ' #helpfulAct').on('click', clickHelpfulAction);
            $('#' + idName).show();
        }

        return dfd.resolve();
    })
    .fail(function(){
        $('#answerDetail-area').hide();
        alert("answer post erorr..");
        return dfd.reject();
    });
    return dfd.promise();
}

function getQuestionDetailData() {
    let dfd = $.Deferred();
    // let uri = window.location.href;
    // let reg = new RegExp(/\/getQuestionDetail-[0-9]+/g);
    // let questionId = uri.match(reg)[0].replaceAll(/\/getQuestionDetail-/g,"");
    let questionId = QUESTIONID;
    var reqObj = new Object();
    reqObj['intext_imgsize'] = INTEXT_IMGSIZE;
    $.ajax({
        type: 'POST',
        url: '/getQuestionDetail/' + questionId,
        contentType : 'application/json',
        data : reqObj,
        dataType : 'json',
    })
    .done(function(data){
        if(data === null || $.isEmptyObject(data)) {
            alert("getDetail data erorr..");
            window.href.location = "/";
            return dfd.resolve();
        }
        $('.questionDetailText')[0].innerHTML = changeCharacterCodeToJapanese(data['questionDetailData']);
        $('#questionUserName')[0].innerHTML =  data['questionUserName'] + ' ' + $('#questionUserName')[0].innerHTML;
        let linkText = "userInfoPage-" + String(data['questionUserId']);
        $('#questionUserName').attr({href : linkText});
        $('#questionDate')[0].innerHTML = data['questionDate'];
        if(data['questionImageData'] !== undefined) {
            $('#questionDetail #appendImgDomWrap #imgRawDataRequest').append('<img id=\"appendImgDom\"/>');
            $('#questionDetail #appendImgDom').attr({src : data['questionImageData']});
            $('#questionDetail #appendImgDomWrap #imgRawDataRequest').attr('href', '/getQuestionImgRawData/' + questionId);
        }
        if(data['quetionLinkFile'] !== undefined && data['quetionLinkFile'].length >= 1) {
            $('.questionDetailText').append('<div id=\"questLinkFileWrap\" class=\"text-muted\" style=\"font-size: 0.8rem;\">添付ファイル</div>');
            let linkFileList = data['quetionLinkFile'];
            for(let i = 0; i < linkFileList.length; i++) {
                let filename = changeCharacterCodeToJapanese(linkFileList[i]['linkFileName']);
                $('#questLinkFileWrap').append('</br><a href=' + linkFileList[i]['linkFileData'] + ' download style=\"font-size: 0.8rem;\">' + filename + '</a>');
            }
        }
        return dfd.resolve();
    })
    .fail(function(){
        alert("getDetail data erorr..");
        window.href.location = "/";
        return dfd.reject();;
    });
    return dfd.promise();
}

function clickAnswer() {
    localStorage.setItem("questionDataForAnswerFrame", $('#questionDetail')[0].innerHTML);
    localStorage.setItem("tmpQuestionId", QUESTIONID);
    new InlineFrame("inlineAnswerFrame", reloadAfterAnswer);
    // if($('#answerBt')[0].innerHTML === '質問に回答する') {
    //     $('#answerBt')[0].innerHTML = '回答欄を閉じる';
    //     openAnswerForm();
    // } else {
    //     $('#answerBt')[0].innerHTML = '質問に回答する';
    //     closeAnswerForm();
    // }
}

// function closeAnswerForm() {
//     $('#answer-form').hide();
//     let moveY = $('#answerBt').position().top;
//     window.scrollTo(0, moveY);
//     if(INIT_BODY_HEIGHT_SIZE !== -1) {
//         $('body').css('height', INIT_BODY_HEIGHT_SIZE);
//     }
// }

// function openAnswerForm() {
//     let height = $('body').height();
//     let inputHeight = height / 2;
//     $('#inputTextArea').height(inputHeight);
//     $('#answer-form').show();
//     let moveY = $('#answerBt').position().top;
//     window.scrollTo(0, moveY);
//     if(INIT_BODY_HEIGHT_SIZE !== -1 && INIT_BODY_HEIGHT_SIZE < $('body').get(0).scrollHeight) {
//         $('body').css('height', 'auto');
//     }
// }

// function switchBttunClick() {
//     if($('#previewInputSwitchBt')[0].innerHTML === 'プレビュー') {
//         let hight = $('#inputTextArea').height();
//         $('#previewInputSwitchBt')[0].innerHTML = '回答に戻る';
//         let inputText = $('#inputTextArea').val();
//         $('#inputPreview')[0].innerHTML = '';
//         if(inputText.trim() !== '') {
//             $('#inputPreview')[0].innerHTML = createPreviewText(inputText);
//             let noImageFlg = $('#uploadedImageFileArea .hiddenValue').eq(1).length === 0;
//             if(!noImageFlg) {
//                 $('#inputPreview').append(appendEmptyImgDom(true));
//                 let binData = $('#uploadedImageFileArea .hiddenValue').eq(1).val();
//                 $('#appendImgDom').attr('src', binData);
//             }
//         }
//         $('#inputTextArea').hide();
//         $('#inputPreview').height(hight);
//         $('#inputPreview').show();
//     } else {
//         $('#previewInputSwitchBt')[0].innerHTML = 'プレビュー';
//         $('#inputTextArea').show();
//         $('#inputPreview').hide();
//     }
// }

// function appendEmptyImgDom(onloadFlg) {
//     if(onloadFlg) {
//         return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><img id=\"appendImgDom\" onload=\"adjustImg();\"/></div>';
//     }
//     return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><img id=\"appendImgDom\"/></div>';
// }

// function adjustImg() {
//     resizeImgData();
//     resizeImgDataUpdate()
//     .then(function(binReSizeData){
//         $('#appendImgDomWrap').remove();
//         $('#inputPreview').append(appendEmptyImgDom(false));
//         $('#appendImgDom').attr('src', binReSizeData);
//     })
// }

function getQuestionId() {
    let uri = window.location.href;
    let reg = new RegExp(/\/getQuestionDetail-[0-9]+/g);
    let questionId = uri.match(reg)[0].replaceAll(/\/getQuestionDetail-/g,"");
    return questionId;
}

function loadingfunctionStart() {
    let top = $('.container').position().top;
    let width = $('body').width();
    $('.block').css('top', top);
    $('.block').css('width', width);
    $('.block').show();
}

function loadingfunctionEnd() {
    $('.block').hide();
}

function clickGootAction(e, data) {
    console.log(e);
    console.log(data);
    let granpaParentId = e.currentTarget.parentNode.parentNode.id;
    let answerId = granpaParentId.replaceAll(/answerDetail-/g,"");
    let act = 0;
    let currentTargetId = e.currentTarget.id;
    if(currentTargetId === 'goodNoAct') {
        act = 1;
    } else {
        act = -1;
    }
    let actionGoodParam = new Object();
    actionGoodParam['action'] = act;
    $.ajax({
        type: 'POST',
        url: '/actionGood/' + answerId,
        contentType : 'application/json',
        data : actionGoodParam,
        dataType : 'json'
    })
    .done(function(data){
        if(isNaN(data['result']) || data['result'] == 'failed') {
            return;
        } else {
            if(currentTargetId === 'goodNoAct') {
                $('#' + granpaParentId + ' #goodNoAct').hide();
                $('#' + granpaParentId + ' #goodAct').show();
                $('#' + granpaParentId + ' #goodText')[0].innerHTML = "いいね(" + Number(data['result']) + ")";
            } else {
                $('#' + granpaParentId + ' #goodAct').hide();
                $('#' + granpaParentId + ' #goodNoAct').show();
                $('#' + granpaParentId + ' #goodText')[0].innerHTML = "いいね(" + Number(data['result']) + ")";
            }
        }
    })
    .fail(function(){
        
    });
}

function clickHelpfulAction(e) {
    let granpaParentId = e.currentTarget.parentNode.parentNode.id;
    let answerId = granpaParentId.replaceAll(/answerDetail-/g,"");
    let act = 0;
    let currentTargetId = e.currentTarget.id;
    if(currentTargetId === 'helpfulNoAct') {
        act = 1;
    } else {
        act = -1;
    }
    let actionHelpfulParam = new Object();
    actionHelpfulParam['action'] = act;
    $.ajax({
        type: 'POST',
        url: '/actionHelpful/' + answerId,
        contentType : 'application/json',
        data : actionHelpfulParam,
        dataType : 'json'
    })
    .done(function(data){
        if(isNaN(data['result'])) {

        } else {
            if(currentTargetId === 'helpfulNoAct') {
                $('#' + granpaParentId + ' #helpfulNoAct').hide();
                $('#' + granpaParentId + ' #helpfulAct').show();
                $('#' + granpaParentId + ' #helpfulText')[0].innerHTML = "参考になりました(" + Number(data['result']) + ")";
            } else {
                $('#' + granpaParentId + ' #helpfulAct').hide();
                $('#' + granpaParentId + ' #helpfulNoAct').show();
                $('#' + granpaParentId + ' #helpfulText')[0].innerHTML = "参考になりました(" + Number(data['result']) + ")";
            }
        }
    })
    .fail(function(){
        
    });
}

function gobackHome() {
    window.href.location = "/";
}

function onloadDisyPlay() {
    setTimeout(function(){
       $('#questionDetailBody').css('opacity', 1);
   }, 200);
 }

 function createPreviewText(inputText) {
    let retAdjustText = '';
    retAdjustText = inputText.replaceAll(/\n/g, '</br>');

    let regexlinkPattern = retAdjustText.match(/https*:\/\/[\w-_/\.]+/g);
    if(regexlinkPattern === null) {
        return retAdjustText;
    }
    let i = 0;
    for(i = 0; i < regexlinkPattern.length; i++) {
        let changeHtmlText = regexlinkPattern[i].trim();
        changeHtmlText = '<a href=\'' + changeHtmlText + '\' target="_blank">' + changeHtmlText + '</a>';
        retAdjustText = retAdjustText.replace(regexlinkPattern[i], changeHtmlText);
    }
    return retAdjustText;
}

function initImageFileEvent() {
    $('#inputImageFileForUploadWrap').off('click');
    $('#inputImageFileForUploadWrap').on('click', function(){
        $('#inputImageFileForUpload').click();
    });
    $('#inputImageFileForUpload').off('change');
    $('#inputImageFileForUpload').on('change', uploadImageFileData);
}

function uploadImageFileData(e) {
    let filename = e.currentTarget.files[0].name;
    let fileReader = new FileReader();
    if(filename.match(/.*\.(png|jpeg)/g) === null) {
        $('#inputImageFileForUpload').val('');
        var option = new Object();
        option.parentId = '#questionDetailBody';
        option.message = 'この拡張子のファイルは対応しておりません。';
        new Dialog(option);
        return;
    }
    fileReader.onload = (function(e){
        let checkSize = $('.uploadImageFileListFrame').length;
        if(checkSize >= 2) {
            let i = 0;
            for(i = 1; i < checkSize; i++) {
                $('.uploadImageFileListFrame').eq(i).remove();
            }
        }
        let index = $('.uploadImageFileListFrame').length;
        let clone = $('.uploadImageFileListFrame').eq(0).clone();
        let cloneDomId = 'uploadImageFileListFrame' + index;
        clone.attr({id : cloneDomId});
        $('#uploadedImageFileArea').append(clone);
        $('#' + cloneDomId).show();
        $('#' + cloneDomId + ' .openValue').val(filename);
        $('#' + cloneDomId + ' button').off('click');
        $('#' + cloneDomId + ' button').on('click', function(){
            $('#' + cloneDomId).remove();
        });
        $('#' + cloneDomId + ' .hiddenValue').val(e.currentTarget.result);
        $('#inputImageFileForUpload').val('');
    })
    fileReader.readAsDataURL(e.currentTarget.files[0]);
}

function reloadAfterAnswer() {
    let questionId = localStorage.getItem('tmpQuestionIdFromFrame');
    if(questionId !== null) {
        localStorage.removeItem('tmpQuestionIdFromFrame');
        window.location.href = 'getQuestionDetail-' + String(questionId);
    }
}

function getAnswerImgAndLinkData(currentIndex) {
    let dfd = $.Deferred();
    if(ANSWERID_LIST.length === 0) {
        return dfd.resolve(true);
    }
    if(currentIndex === undefined) {
        currentIndex = 0;
    }
    let i = 0;
    for(i = 0; i < ANSWERID_LIST.length; i++) {
        getAnswerImgAndLinkDataByAnswerId(ANSWERID_LIST[i])
        .then(function(retData){
            if(!retData) {
                console.error("getting error : " + ANSWERID_LIST[i]);
            }
            if(i >= ANSWERID_LIST.length - 1) {
                return dfd.resolve(true);
            }
        });
    }
    return dfd.promise();
}

function getAnswerImgAndLinkDataByAnswerId(answerId) {
    let dfd = $.Deferred();
    var reqObj = new Object();
    reqObj['intext_imgsize'] = INTEXT_IMGSIZE;
    $.ajax({
        type: 'POST',
        url: '/getAnswerImgAndLinkData/' + answerId,
        contentType : 'application/json',
        data : reqObj,
        dataType : 'json',
    })
    .done(function(data){
        //update answer area
        debugger
        let answerDetailAreaId = '#answerDetail-' + String(answerId);
        if(data['answerImgData'] !== undefined) {
            let imgInsertId = answerDetailAreaId + ' #appendImgDomWrap #imgRawDataRequest';
            if($(imgInsertId).length === 1) {
                $(imgInsertId).append('<img id=\"appendImgDom\"/>');
                $(answerDetailAreaId + ' #appendImgDom').attr({src:data['answerImgData']});
                $(imgInsertId).attr('href', '/getAnswerImgRawData/' + answerId);
            }
        }
        if(data['answerLinkFile'] !== undefined && data['answerLinkFile'].length >= 1) {
            let answerLinkFileWrapId = 'answerLinkFileWrapId-' + String(answerId);
            $(answerDetailAreaId + ' .answerDetailText').append('<div id=\"' + answerLinkFileWrapId + '\" class=\"text-muted\" style=\"font-size: 0.8rem;\">添付ファイル</div>');
            let linkFileList = data['answerLinkFile'];
            for(let i = 0; i < linkFileList.length; i++) {
                let filename = changeCharacterCodeToJapanese(linkFileList[i]['linkFileName']);
                $('#' + answerLinkFileWrapId).append('</br><a href=' + linkFileList[i]['linkFileData'] + ' download style=\"font-size: 0.8rem;\">' + filename + '</a>');
            }
        }
        return dfd.resolve(true);
    })
    .fail(function(){
        console.error("Error occured.Then getting answer link and img");
        return dfd.resolve(true);
    });
    return dfd.promise();
}