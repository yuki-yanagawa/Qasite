var imgFrameDefaultSize = 200;
$(function() {
    let height = $('body').height();
    let inputHeight = height / 2;
    $('#inputTextArea').height(inputHeight);
    let titleWidth = $('.input-group-prepend').eq(0).width();
    $('.input-group-text').eq(1).css('width', titleWidth);
    let tileTextWidth = $('.form-control').eq(0).width();
    $('#titleText').css('width', tileTextWidth);
    $('.input-group').eq(1).css('margin-top', '5px');
    createSelector();
    initButtonEvent();
    initPreviewButtonEvent();
    initFileUploadEvent();
    initImageFileEvent();
});

function initButtonEvent() {
    $('#previewInputSwitchBt').off('click');
    $('#cancelBt').off('click');
    $('#previewInputSwitchBt').on('click', previewButtonClick);
    $('#cancelBt').on('click', cancelBtClick);
}

function initPreviewButtonEvent() {
    $('#revertBt').off('click');
    $('#submitBt').off('click');
    $('#revertBt').on('click', revertFunction);
    $('#submitBt').on('click', submitFunction);
}

function revertFunction() {
    previewToInputFormAdjust();
}

function previewButtonClick() {
    switchPreview();
    //Image File
    let inputText = $('#inputTextArea').val();
    if(inputText.trim() === '') {
        //alert("Pelase input question.");
        var option = new Object();
        option.parentId = '#requestPostBody';
        option.message = '質問を入力して下さい';
        new Dialog(option);
        return;
    }
    let inputTitle = $('#titleText').val();
    if(inputTitle.trim() === '') {
        $('#titleText').val(_autoCreateTitleText(inputText));
    }
    $('#inputPreview')[0].innerHTML = createPreviewText(inputText);
    let noImageFlg = $('#uploadedImageFileArea .hiddenValue').eq(1).length === 0;
    if(!noImageFlg) {
        $('#inputPreview').append(appendEmptyImgDom(true));
        let binData = $('#uploadedImageFileArea .hiddenValue').eq(1).val();
        $('#appendImgDom').attr('src', binData);
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

function cancelBtClick() {
    window.location.href = "/";
}

function onloadDisyPlay() {
    setTimeout(function(){
        $('#requestPostBody').css('opacity', 1);
    }, 200);
}

function switchPreview() {
    $('#requestPostBody').css('opacity', 0);
    inputToPreviewFormAdjust();
    setTimeout(function(){
        $('#requestPostBody').css('opacity', 1);
    }, 200);
}

function switchInput() {
    $('#requestPostBody').css('opacity', 0);
    previewToInputFormAdjust();
    setTimeout(function(){
        $('#requestPostBody').css('opacity', 1);
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

function appendEmptyImgDom(onloadFlg) {
    if(onloadFlg) {
        return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><img id=\"appendImgDom\" onload=\"adjustImg();\"/></div>';
    }
    return '</br><div id=\"appendImgDomWrap\" style=\"text-align:center;\"><img id=\"appendImgDom\"/></div>';
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

function adjustPreviweText(inputText) {
    let regexPattern = inputText.match(/<.*?>/g);
    let i = 0;
    if(regexPattern === null) {
        if(inputText.match(/https*:\/\/[\w-_/\.]+/g) === null) {
            return inputText = '<div>' + inputText +'</div>';
        } else {
            let reghtmlText = inputText.match(/https*:\/\/[\w-_/\.]+/g);
            let changeText = '<div><a href=\'' + reghtmlText[0] + '\' target="_blank">' + reghtmlText[0] + '</a></div>';
            return inputText.replace(reghtmlText, changeText);
        }
    }

    let retAdjustText = '';
    let firstIndex = inputText.indexOf(regexPattern[0]);
    if(firstIndex !== 0) {
        let headText = inputText.substring(0, firstIndex);
        headText = '<div>' + headText +'</div>'
        retAdjustText =  headText + inputText.substring(firstIndex);
    } else {
        retAdjustText = inputText;
    }

    let regexlinkPattern = inputText.match(/https*:\/\/[\w-_/\.]+/g);
    if(regexlinkPattern === null) {
        return retAdjustText;
    }
    i = 0;
    for(i = 0; i < regexlinkPattern.length; i++) {
        let changeHtmlText = regexlinkPattern[i].trim();
        changeHtmlText = '<a href=\'' + changeHtmlText + '\' target="_blank">' + changeHtmlText + '</a>';
        retAdjustText = retAdjustText.replace(regexlinkPattern[i], changeHtmlText);
    }
    return retAdjustText;
}

function submitFunction() {
    debugger
    let inputTitle = $('#titleText').val();
    if(inputTitle.trim() === '') {
        $('#titleText').val(_autoCreateTitleText($('#inputTextArea').val()));
    }
    var option = new Object();
    option.parentId = '#requestPostBody';
    option.message = '質問を投稿しますか？';
    option.originalButton = '<button id=\'postDataFuncId\' class=\'btn btn-primary\'>投稿する</button>' +
    ' &nbsp;<button id=\'revertDataFuncId\' class=\'btn btn-primary\'>取り戻す</button>';
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
    
    let textImgBinData = $('#inputPreview img').attr('src');
    $('#inputPreview img').remove();

    let inputText = $('#inputPreview')[0].innerHTML;
    submitTextData(inputText)
    .then(function(questionId){
        if(isNaN(questionId) || Number(questionId) === -1) {
            postQuestFailedDailog();
            return;
        }
        submitTextImgData(questionId, textImgBinData)
        .then(function(questionId, requestSucess){
            if(!requestSucess) {
                revertSubmitData(questionId);
                return;
            }
            submitLinkData(questionId)
            .then(function(questionId, requestSucess){
                if(requestSucess) {
                    commitSubmitData(questionId);
                    return;
                } else {
                    revertSubmitData(questionId);
                    return;
                }
            });
        })
    });
}

function commitSubmitData(questionId) {
    $.ajax({
        type: 'PUT',
        url: '/commitQuestion/' + String(questionId),
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        var option = new Object();
        option.parentId = '#requestPostBody';
        option.message = '質問の投稿に成功しました';
        option.originalButton = '<button id=\'postQuestionSucsessCheck\' class=\'btn btn-primary\'>ホームに戻る</button>';
        new Dialog(option);
        $('#postQuestionSucsessCheck').off('click');
        $('#postQuestionSucsessCheck').on('click', gobackHome);
        return;
    })
    .fail(function(){
        postQuestFailedDailog();
        return;
    });
}

function revertSubmitData(questionId) {
    $.ajax({
        type: 'PUT',
        url: '/revertQuestion/' + String(questionId),
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        postQuestFailedDailog();
        return;
    })
    .fail(function(){
        postQuestFailedDailog();
        return;
    });
}

function postQuestFailedDailog() {
    var option = new Object();
        option.parentId = '#requestPostBody';
        option.message = '質問の投稿に失敗しました';
        option.originalButton = '<button id=\'postQuestionFailed\' class=\'btn btn-primary\'>質問作成に戻る</button>';
        new Dialog(option);
        $('#postQuestionFailed').off('click');
        $('#postQuestionFailed').on('click', revertFunction);
}

function submitTextData(inputText) {
    let dfd = $.Deferred();
    let questObj = new Object;
    questObj['title'] = changeJapaneseToCharacterCode($('#titleText').val());
    questObj['text'] = changeJapaneseToCharacterCode(inputText);
    questObj['type'] = $('#inputGroupSelect01').val();
    $.ajax({
        type: 'POST',
        url: '/postQuestion',
        data: questObj,
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

function submitTextImgData(questionId, textImgBinData) {
    let dfd = $.Deferred();
    if(textImgBinData === undefined) {
        return dfd.resolve(questionId, true);
    }
    let questObj = new Object;
    questObj['textImage'] = textImgBinData;
    $.ajax({
        type: 'POST',
        url: '/postQuestionImgData/' + String(questionId),
        data: questObj,
        contentType : 'application/json',
        dataType : 'json'
    })
    .done(function(){
        return dfd.resolve(questionId, true);
    })
    .fail(function(){
        return dfd.resolve(questionId, false);
    });
    return dfd.promise();
}

function submitLinkData(questionId) {
    let dfd = $.Deferred();
    if($('#fileUpLoadAreaPreview a').length === 0) {
        return dfd.resolve(questionId, true);
    }
    recursiveSubmitLinkData(questionId, 0)
    .then(function(questionId, requestSucess){
        if(requestSucess) {
            return dfd.resolve(questionId, true);
        } else {
            return dfd.resolve(questionId, false);
        }
    });
    return dfd.promise();
}

function recursiveSubmitLinkData(questionId, currentIndex) {
    let dfd = $.Deferred();
    submitLinkDataFunc(questionId, currentIndex)
    .then(function(requestSucess) {
        if(!requestSucess) {
            return dfd.resolve(questionId, false);
        }
        if(currentIndex + 1 >= $('#fileUpLoadAreaPreview a').length) {
            return dfd.resolve(questionId, true);
        }
        recursiveSubmitLinkData(questionId, currentIndex + 1);
    });
    return dfd.promise();
}

function submitLinkDataFunc(questionId, currentIndex) {
    let dfd = $.Deferred();
    let questObj = new Object;
    questObj['linkFilename'] = changeJapaneseToCharacterCode($('#fileUpLoadAreaPreview a').eq(currentIndex)[0].innerHTML);
    questObj['linkFileData'] = $('#fileUpLoadAreaPreview a').eq(currentIndex).attr('href');
    questObj['linkFileId'] = currentIndex + 1;
    $.ajax({
        type: 'POST',
        url: '/postQuestionLinkData/' + String(questionId),
        data: questObj,
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



function registByteData(inputText) {
    let bytes = [];
    let i = 0;
    for(i = 0; i < inputText.length; i++) {
        bytes.push(inputText.charCodeAt(i));
    }
    return bytes;
}

function gobackHome() {
    window.location.href = "/";
}

function _autoCreateTitleText(inputText) {
    let matches = inputText.match(/\n/g);
    if(matches === null) {
        return inputText;
    }

    if(inputText.indexOf(matches[0]) > 0) {
        return inputText.substring(0, inputText.indexOf(matches[0]));
    }

    let charlen = matches[0].length;
    inputText = inputText.substring(charlen, inputText.length - 1);
    matches = inputText.match(/<.*?>/g);
    if(matches === null) {
        return null;
    }
    return inputText.substring(0, inputText.indexOf(matches[0]));
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

function uploadFileData(e) {
    let filename = e.currentTarget.files[0].name;
    let fileReader = new FileReader();
    fileReader.onload = (function(e){
        //$('#uploadedFileArea').append('<a href=' + e.currentTarget.result + ' download>' + filename + '</a>')
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

function createSelector() {
    var parentObj = $('#inputGroupSelect01');
    var length = Object.keys(categoRizeObj).length;
    var i;
    for(i = 0; i < length; i++) {
        var val = categoRizeObj['categoRize' + String(i)];
        var node;
        if(i == 0) {
            node = '<option value=\"1\" selected>' + val + '</option>'
        } else {
            node = '<option value=\"' + String(i + 1) + '\">' + val + '</option>'
        }
        parentObj.append(node);
    }
}