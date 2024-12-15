var imgFrameDefaultSize = 200;
var parentQuestionId = -1;
$(function(){
    $('.block').hide();
    let bodyHeight = $(document).height() * 0.95;
    $('body').height(bodyHeight)
    $('#inputPreview').hide();
    parentQuestionId = localStorage.getItem("tmpQuestionId");
    localStorage.removeItem("tmpQuestionId");

    initQuestTextAreaSetting();
    inputAreaSetting();
    initButtonEvent();
    initPreviewButtonEvent();
    initImageFileEvent();
    initFileUploadEvent();

    settingQuestionTextArea();
});

function initQuestTextAreaSetting() {
    $('#closeSvg').addClass('buttonDisplayNone');
    $('#questTextViewControllBt').off('click');
    $('#questTextViewControllBt').on('click', switchBtText);
}

function inputAreaSetting() {
    let height = $('body').height();
    let inputHeight = height / 2;
    $('#inputTextArea').height(inputHeight);
    
}

function switchBtText() {
    if($('#openSvg').hasClass('buttonDisplayNone')) {
        $('#closeSvg').addClass('buttonDisplayNone');
        $('#openSvg').removeClass('buttonDisplayNone');
    } else {
        $('#openSvg').addClass('buttonDisplayNone');
        $('#closeSvg').removeClass('buttonDisplayNone');
    }
}

function initButtonEvent() {
    $('#previewInputSwitchBt').off('click');
    $('#previewInputSwitchBt').on('click', previewButtonClick);
}

function initPreviewButtonEvent() {
    $('#revertBt').off('click');
    $('#submitBt').off('click');
    $('#revertBt').on('click', revertFunction);
    $('#submitBt').on('click', submitFunction);
}

function initImageFileEvent() {
    $('#inputImageFileForUploadWrap').off('click');
    $('#inputImageFileForUploadWrap').on('click', function(){
        $('#inputImageFileForUpload').click();
    });
    $('#inputImageFileForUpload').off('change');
    $('#inputImageFileForUpload').on('change', uploadImageFileData);
}

function initFileUploadEvent() {
    $('#inputFileForUploadWrap').off('click');
    $('#inputFileForUploadWrap').on('click', function(){
        $('#inputFileForUpload').click();
    });
    $('#inputFileForUpload').off('change');
    $('#inputFileForUpload').on('change', uploadFileData);
}

function settingQuestionTextArea() {
    let cloneText = localStorage.getItem("questionDataForAnswerFrame");
    if(cloneText !== null) {
        localStorage.removeItem("questionDataForAnswerFrame");
        $('#collapseQuestionTextArea')[0].innerHTML = cloneText;
        if($('#collapseQuestionTextArea #appendImgDom').length === 1) {
            $('#collapseQuestionTextArea #appendImgDom').attr({id:'appendImgDomCopy'})
        }
        if($('#collapseQuestionTextArea #appendImgDomWrap').length === 1) {
            $('#collapseQuestionTextArea #appendImgDomWrap').attr({id:'appendImgDomWrapCopy'})
        }
        $('.card-footer').remove();
    }
}


function revertFunction() {
    previewToInputFormAdjust();
}

function previewToInputFormAdjust() {
    $('#inputTextArea').show();
    $('#inputPreview').hide();
    $('#imgLinkArea').show();
    $('#fileUpLoadArea').show();
    $('#buttonAreaForm').show();
    $('#buttonAreaFormPreview').hide();

    let i = 0;
    let linkCount = $('#fileUpLoadAreaPreview a').length;
    for(i=0; i < linkCount; i++) {
        $('#fileUpLoadAreaPreview a').eq(i).remove();
    }
    $('#fileUpLoadAreaPreview').hide();
}

function previewButtonClick() {
    // console.log(parentQuestionId);
    let inputText = $('#inputTextArea').val();
    if(inputText.trim() === '') {
        var option = new Object();
        option.parentId = '#inlineAnswerFrameBody';
        option.message = '回答を入力して下さい';
        new Dialog(option);
        return;
    }
    switchPreview();
    $('#inputPreview')[0].innerHTML = createPreviewText(inputText);
    let noImageFlg = $('#uploadedImageFileArea .hiddenValue').eq(1).length === 0;
    if(!noImageFlg) {
        loadingfunctionInnerAnswerStart();
        $('#inputPreview').append(appendEmptyImgDom());
        let binData = $('#uploadedImageFileArea .hiddenValue').eq(1).val();
        $('#appendImgDomWrap a').prop('href', binData);
        requestResizeImgData(binData)
        .then(function(resizeBindata){
            if(resizeBindata !== undefined) {
                $('#appendImgDom').attr('src', resizeBindata);
            }
            loadingfunctionInnerAnswerEnd();
        });
    }

    //Link file
    let linkFileCount = $('.uploadFileListFrame').length - 1;
    if(linkFileCount === 0) {
        return;
    }
    let i = 0;
    for(i = 1; i <= linkFileCount; i++) {
        let id = '#uploadFileIndex' + i;
        let filename = $(id + ' .openValue').val();
        let fileData = $(id + ' .hiddenValue').val();
        let linkId = 'uploadFileLinkIndex' + i;
        $('#fileUpLoadAreaPreview').append('<a id=\"' + linkId + '\" href=' + fileData + ' download>' + filename + '</a>');
    }
    $('#fileUpLoadAreaPreview').show();
}

function switchPreview() {
    $('#inlineAnswerFrameBody').css('opacity', 0);
    inputToPreviewFormAdjust();
    setTimeout(function(){
        $('#inlineAnswerFrameBody').css('opacity', 1);
    }, 200);
}

function inputToPreviewFormAdjust() {
    let textHeight = $('#inputTextArea').height();
    $('#inputTextArea').hide();
    $('#inputPreview').show();
    if($('#inputPreview').height() < textHeight) {
        $('#inputPreview').height(textHeight);
    }
    $('#imgLinkArea').hide();
    $('#fileUpLoadArea').hide();
    $('#buttonAreaForm').hide();
    $('#buttonAreaFormPreview').show();
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

function uploadImageFileData(e) {
    let filename = e.currentTarget.files[0].name;
    let fileReader = new FileReader();
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

function uploadFileData(e) {
    let filename = e.currentTarget.files[0].name;
    let fileReader = new FileReader();
    fileReader.onload = (function(e){
        //$('#uploadedFileArea').append('<a href=' + e.currentTarget.result + ' download>' + filename + '</a>')
        let checkSize = $('.uploadFileListFrame').length;
        if(checkSize >= 2) {
            let i = 0;
            for(i = 1; i < checkSize; i++) {
                $('.uploadFileListFrame').eq(i).remove();
            }
        }
        let index = $('.uploadFileListFrame').length;
        let clone = $('.uploadFileListFrame').eq(0).clone();
        let cloneDomId = 'uploadFileIndex' + index;
        clone.attr({id : cloneDomId});
        $('#uploadedFileArea').append(clone);
        $('#' + cloneDomId).show();
        $('#' + cloneDomId + ' .openValue').val(filename);
        $('#' + cloneDomId + ' button').off('click');
        $('#' + cloneDomId + ' button').on('click', function(){
            $('#' + cloneDomId).remove();
        });
        $('#' + cloneDomId + ' .hiddenValue').val(e.currentTarget.result);
        $('#inputFileForUpload').val('');
    })
    fileReader.readAsDataURL(e.currentTarget.files[0]);
}

// function appendEmptyImgDom(onloadFlg) {
//     if(onloadFlg) {
//         return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><img id=\"appendImgDom\" onload=\"adjustImg();\"/></div>';
//     }
//     return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><img id=\"appendImgDom\"/></div>';
// }

function appendEmptyImgDom() {
    //return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><img id=\"appendImgDom\"/></div>';
    return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><a id=\"imgRawDataRequest\" data-lightbox=\"image-1\"><img id=\"appendImgDom\"/></a></div>';
}

function adjustImg() {
    resizeImgData();
    resizeImgDataUpdate()
    .then(function(binReSizeData){
        $('#appendImgDomWrap').remove();
        $('#inputPreview').append(appendEmptyImgDom(false));
        $('#appendImgDom').attr('src', binReSizeData);
    })
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
    img.src = $('#uploadedImageFileArea .hiddenValue').eq(1).val();
    return dfd.promise();
}

function submitFunction() {
    var option = new Object();
    option.parentId = '#inlineAnswerFrameBody';
    option.message = '回答しますか？';
    // option.originalButton = '<button id=\'postDataFuncId\' class=\'btn btn-primary\'>回答する</button>' +
    // ' &nbsp;<button id=\'revertDataFuncId\' class=\'btn btn-primary\'>取り戻す</button>';
    option.originalButton = '<button id=\'revertDataFuncId\' class=\'btn btn-secondary\'>取り戻す</button>' +
    ' &nbsp;<button id=\'postDataFuncId\' class=\'btn btn-primary\'>回答する</button>';
    var dialog = new Dialog(option);
    $('#postDataFuncId').off('click');
    $('#revertDataFuncId').off('click');
    
    $('#postDataFuncId').on('click', function() {
        dialog.closeFunc();
        submitFormData();
    });

    $('#revertDataFuncId').on('click', function() {
        dialog.closeFunc();
    });
}

function submitFormData() {
    loadingfunctionInnerAnswerStart();
    let binData = $('#uploadedImageFileArea .hiddenValue').eq(1).val();
    var tmp = $('#inputPreview').clone(true);
    tmp.find('#appendImgDom').remove();
    tmp.find('#imgRawDataRequest').removeAttr('href');
    let inputText = tmp[0].innerHTML;

    requestAnswerId()
    .then(function(data){
        debugger
        if(isNaN(data['userId']) || Number(data['userId']) === -1) {
            persuadeLoginDialog();
            return;
        }
        if(isNaN(data['answerId']) || Number(data['answerId']) === -1) {
            postAnswerFailedDailog();
            return;
        }
        $.when(
            submitAnswerTextData(data['answerId'], data['userId'], inputText),
            submitTextImgData(data['answerId'], binData),
            submitLinkData(data['answerId'])
        )
        .then(function(resultText, resultImg, resultLinkData){
            loadingfunctionInnerAnswerEnd();
            if(resultText && resultImg && resultLinkData) {
                commitSubmitData(data['answerId']);
            } else {
                revertSubmitData(data['answerId']);
            }
        });
    })

    // submitTextData(inputText)
    // .then(function(result){
    //     if(Number(result) === -1) {
    //         var option = new Object();
    //         option.parentId = '#inlineAnswerFrameBody';
    //         option.message = '回答に失敗しました';
    //         option.originalButton = '<button id=\'revertCloesd\' class=\'btn btn-primary\'>閉じる</button>';
    //         var dialog = new Dialog(option);
    //         $('#revertCloesd').off('click');
    //         $('#revertCloesd').on('click', function() {
    //             revertFunction();
    //             dialog.closeFunc();
    //         });
    //         return;
    //     } else if(result === 'sessionIdExpired') {
    //         var option = new Object();
    //         option.parentId = '#inlineAnswerFrameBody';
    //         option.message = 'ログイン後に回答お願いします。';
    //         option.originalButton = '<button id=\'revertCloesd\' class=\'btn btn-primary\'>閉じる</button>';
    //         var dialog = new Dialog(option);
    //         $('#revertCloesd').off('click');
    //         $('#revertCloesd').on('click', function() {
    //             revertFunction();
    //             dialog.closeFunc();
    //         });
    //         return;
    //     }
    //     let answerId = result;
    //     submitTextImgData(answerId, textImgBinData)
    //     .then(function(answerId){
    //         if(Number(answerId) === -1) {
    //             revertSubmitData(answerId);
    //             return;
    //         }
    //         submitLinkData(answerId)
    //         .then(function(answerId){
    //             if(Number(answerId) > 0) {
    //                 commitSubmitData(answerId);
    //                 return;
    //             } else {
    //                 revertSubmitData(answerId);
    //                 return;
    //             }
    //         });
    //     })
    // });
}

function requestAnswerId() {
    let dfd = $.Deferred();
    $.ajax({
        type: 'POST',
        url: '/requestAnswerId',
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(data){
        return dfd.resolve(data);
    })
    .fail(function(){
        return dfd.resolve(-1);
    });
    return dfd.promise();
}

function submitAnswerTextData(answerId, userId, inputText) {
    let dfd = $.Deferred();
    let requestObj = new Object;
    requestObj['text'] = changeJapaneseToCharacterCode(inputText);
    requestObj['userId'] = userId;
    requestObj['questionId'] = parentQuestionId;
    $.ajax({
        type: 'POST',
        url: '/postAnswerTextData/' + String(answerId),
        data: requestObj,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        dfd.resolve(true);
    })
    .fail(function(){
        dfd.reject(false);
    });
    return dfd.promise();
}


function commitSubmitData(answerId) {
    $.ajax({
        type: 'PUT',
        url: '/commitAnswer/' + String(answerId),
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        var option = new Object();
        option.parentId = '#inlineAnswerFrameBody';
        option.message = '回答に成功しました';
        option.originalButton = '<button id=\'reloadCloesd\' class=\'btn btn-primary\'>閉じる</button>';
        new Dialog(option);
        $('#reloadCloesd').off('click');
        $('#reloadCloesd').on('click', submitEndAction);
        return;
    })
    .fail(function(){
        var option = new Object();
        option.parentId = '#inlineAnswerFrameBody';
        option.message = '回答に失敗しました';
        option.originalButton = '<button id=\'revertCloesd\' class=\'btn btn-primary\'>閉じる</button>';
        var dialog = new Dialog(option);
        $('#revertCloesd').off('click');
        $('#revertCloesd').on('click', function() {
            revertFunction();
            dialog.closeFunc();
        });
        return;
    });
}

function revertSubmitData(answerId) {
    $.ajax({
        type: 'PUT',
        url: '/revertAnswer/' + String(answerId),
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        var option = new Object();
        option.parentId = '#inlineAnswerFrameBody';
        option.message = '回答に失敗しました';
        option.originalButton = '<button id=\'revertCloesd\' class=\'btn btn-primary\'>閉じる</button>';
        var dialog = new Dialog(option);
        $('#revertCloesd').off('click');
        $('#revertCloesd').on('click', function() {
            revertFunction();
            dialog.closeFunc();
        });
        return;
    })
    .fail(function(){
        var option = new Object();
        option.parentId = '#inlineAnswerFrameBody';
        option.message = '回答に失敗しました';
        option.originalButton = '<button id=\'revertCloesd\' class=\'btn btn-primary\'>閉じる</button>';
        var dialog = new Dialog(option);
        $('#revertCloesd').off('click');
        $('#revertCloesd').on('click', function() {
            revertFunction();
            dialog.closeFunc();
        });
        return;
    });
}

function postAnswerFailedDailog() {
    var option = new Object();
        option.parentId = '#inlineAnswerFrameBody';
        option.message = '申し訳ありません。回答の投稿に失敗しました';
        option.originalButton = '<button id=\'postAnswerFailed\' class=\'btn btn-primary\'>回答の作成に戻る</button>';
        var dialog = new Dialog(option);
        $('#postAnswerFailed').off('click');
        $('#postAnswerFailed').on('click', function() {
            revertFunction();
            dialog.closeFunc();
        });
}

function persuadeLoginDialog() {
    var option = new Object();
        option.parentId = '#inlineAnswerFrameBody';
        option.message = 'ログイン情報が取得できませんでした。ログインし直してから回答をお願い致します。';
        option.originalButton = '<button id=\'postAnswerFailed\' class=\'btn btn-primary\'>閉じる</button>';
        new Dialog(option);
        $('#postAnswerFailed').off('click');
        $('#postAnswerFailed').on('click', submitEndAction);
}

function submitTextData(inputText) {
    let dfd = $.Deferred();
    let requestObj = new Object;
    requestObj['text'] = changeJapaneseToCharacterCode(inputText);
    $.ajax({
        type: 'POST',
        url: '/postAnswer/' + String(parentQuestionId),
        data: requestObj,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(data){
        return dfd.resolve(data.result);
    })
    .fail(function(){
        return dfd.resolve(-1);
    });
    return dfd.promise();
}

function submitTextImgData(answerId, textImgBinData) {
    let dfd = $.Deferred();
    if(textImgBinData === undefined) {
        return dfd.resolve(true);
    }
    let requestParamObj = new Object;
    requestParamObj['textImage'] = textImgBinData;
    $.ajax({
        type: 'POST',
        url: '/postAnswerImgData/' + String(answerId),
        data: requestParamObj,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        return dfd.resolve(true);
    })
    .fail(function(){
        return dfd.resolve(false);
    });
    return dfd.promise();
}

function submitLinkData(answerId) {
    let dfd = $.Deferred();
    if($('#fileUpLoadAreaPreview a').length === 0) {
        return dfd.resolve(true);
    }
    let i = 0;
    for(i = 0; i < $('#fileUpLoadAreaPreview a').length; i++) {
        submitLinkDataFunc(answerId, i)
        .then(function(requestSucess){
            if(!requestSucess) {
                console.error("answerId : " + String(answerId) + " link : " + String(i));
                return dfd.resolve(false);
            }
            if(i >= $('#fileUpLoadAreaPreview a').length - 1) {
                return dfd.resolve(true);
            }
        });
    }
    return dfd.promise();
    // recursiveSubmitLinkData(answerId, 0)
    // .then(function(requestSucess){
    //     if(requestSucess) {
    //         //return dfd.resolve(questionId, true);
    //         return dfd.resolve(true);
    //     } else {
    //         // return dfd.resolve(questionId, false);
    //         return dfd.resolve(false);
    //     }
    // });
    // return dfd.promise();
}

function recursiveSubmitLinkData(answerId, currentIndex) {
    submitLinkDataFunc(answerId, currentIndex)
    .then(function(requestSucess) {
        if(!requestSucess) {
            return dfd.resolve(false);
        }
        if(currentIndex + 1 >= $('#fileUpLoadAreaPreview a').length) {
            return dfd.resolve(true);
        }
        recursiveSubmitLinkData(questionId, currentIndex + 1);
    });
    // .then(function(retData) {
    //     if(Number(retData) === -1) {
    //         return dfd.resolve(-1);
    //     }
    //     if(currentIndex + 1 >= $('#fileUpLoadAreaPreview a').length) {
    //         return dfd.resolve(answerId);
    //     }
    //     recursiveSubmitLinkData(answerId, currentIndex + 1);
    // });
}

function submitLinkDataFunc(answerId, currentIndex) {
    let dfd = $.Deferred();
    let requestParam = new Object;
    requestParam['linkFilename'] = changeJapaneseToCharacterCode($('#fileUpLoadAreaPreview a').eq(currentIndex)[0].innerHTML);
    requestParam['linkFileData'] = $('#fileUpLoadAreaPreview a').eq(currentIndex).attr('href');
    requestParam['linkFileId'] = currentIndex + 1;
    $.ajax({
        type: 'POST',
        url: '/postAnswerLinkData/' + String(answerId),
        data: requestParam,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        // return dfd.resolve(answerId);
        return dfd.resolve(true);
    })
    .fail(function(){
        // return dfd.resolve(-1);
        return dfd.resolve(false);
    });
    return dfd.promise();
}

function submitEndAction() {
    localStorage.setItem('tmpQuestionIdFromFrame', parentQuestionId);
    window.location.href = "/inlineEndDummy";
}

function loadingfunctionInnerAnswerStart() {
    let top = $('#inlineAnswerFrameBody').position().top;
    let width = $('body').width();
    $('.block').css('top', top);
    $('.block').css('width', width);
    $('.block').show();
}

function loadingfunctionInnerAnswerEnd() {
    $('.block').hide();
}

function requestResizeImgData(binData) {
    var dfd = $.Deferred();
    var reqObj = new Object();
    reqObj['imgData'] = binData;
    //reqObj['dataSize'] = 300;
    reqObj['dataSize'] = Math.floor($('#inputTextArea').width() / 3);
    $.ajax({
        type: 'POST',
        url: '/requestResizeDataImg',
        data: reqObj,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(resizeBinData){
        return dfd.resolve(resizeBinData['resizeData']);
    })
    .fail(function(){
        return dfd.reject();
    })
    return dfd.promise();
}