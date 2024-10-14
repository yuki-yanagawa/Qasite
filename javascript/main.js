var objLine = [];
var cmdLineGlobal = '[user@TestUser]# ';
//var cmdLineAddStr = 'Hello,statistics world!';
var containerHeightDefault; 

$(function(){
   loadingfunctionStart();
   $('#cmdInput').val('');
   $('#cmdInput').val(cmdLineGlobal);

   $('#defaultCheck1').click();
   $('#defaultCheck2').click();

   var text = $('#cmdInput').val();
   cmdInputHello(text, cmdLineAddStr, 0);

   createCategoryMenu();

   $('#questPostBt').on('click', questPostPageRequest);

   settingUserInfoEvent();
   // settingUserCircleSize();
   settingSearchEvent();

   let containerHeightDefault = $('.container').get(0).scrollHeight;
   // console.log('containerHeightDefault : ' + containerHeightDefault);

   // getQuestionAllData()
   $('#inlineCheckbox1').on('change', updateQuestionData)
   $('#inlineCheckbox2').on('change', updateQuestionData)
   $.when(
      getQuestionAllData(),
      getUserInfoData()
   )
   .then(function(){
      loadingfunctionEnd();
      if(containerHeightDefault < $('.container').get(0).scrollHeight) {
         $('.container').css('height', 'auto');
      }
   });
});

function getUserInfoData() {
   let dfd = $.Deferred();
   $.ajax({
      type: 'POST',
        url: '/getLoginUserInfo',
        contentType : 'application/json',
   })
   .done(function(userInfo){
      if($.isEmptyObject(userInfo.result)) {
         // settingUserCircleClickEvent();
         return dfd.resolve();
      }
      settingUserInfoArea(userInfo.result);
      return dfd.resolve();
   })
   .fail(function(){
      return dfd.resolve();
   })
   return dfd.promise();
}

function getQuestionAllData(answerPattern) {
   if(answerPattern === undefined) {
      answerPattern = 0;
   }
   var reqObj = new Object();
   reqObj['answerPattern'] = answerPattern;
   let dfd = $.Deferred();
   $.ajax({
      type: 'POST',
      url: '/getQuestionAllData',
      contentType : 'application/json',
      data : reqObj,
      dataType : 'json'
   })
   .done(function(data){
      if(data.length === 0) {
         return dfd.resolve();
      }
      createQuestionDataArea(data);
      return dfd.resolve();
   })
   .fail(function(){
      console.warn("question All Data getError");
      return dfd.reject();
   });
   return dfd.promise();
}

function timesleep(waitMsec) {
   var startMsec = new Date();
  while (new Date() - startMsec < waitMsec);
}

function cmpCharInput(text, addStr, index) {
   var dfd = $.Deferred();
   setTimeout(function(){
      text = text + addStr[index];
      $('#cmdInput').val(text);
      return dfd.resolve(index + 1);
   },100)
   return dfd.promise();
}


function cmdInputHello(text, addStr, index) {
   if(addStr.length <= index) {
      return;
   }
   cmpCharInput(text, addStr, index)
   .then(function(newIndex){
      var newText = $('#cmdInput').val();
      cmdInputHello(newText, addStr, newIndex);
   });
}

function questPostPageRequest() {
   localStorage.setItem('beforeHrefRequest', "/questPostPageRequest");
   window.location.href = "/questPostPageRequest";
}

function createTitle(questionInfo, limitCount) {
   let questInfo = questionInfo.replaceAll(/<.*?>/g, "");
   if(questInfo.length > limitCount) {
      questInfo = questInfo.substring(0, limitCount) + "...";
   }
   return questInfo;
}

function loadingfunctionStart() {
   let tmpDom = '<div class=\"block\"><div class=\"text-center\"><div class=\"spinner-border\" role=\"status\">'
    + '<span class=\"sr-only\">Loading...</span></div><div><span>Loading...</span></div></div></div>';
   let top = $('.container').position().top;
   //let width = $('body').width();
   let width = $(document).width();
   let height = $(document).height();
   $('body').append(tmpDom);
   $('.block').css('top', top);
   $('.block').css('width', width);
   $('.block').css('height', height);
   $('.block').css('z-index', 1);
   $('.block').show();
}

function loadingfunctionEnd() {
   $('.block').remove();
}

// function settingUserCircleSize() {
//    let targetSettingSize = $('#navUserInfoGroup').height();
//    $('#userCircleSvg').css('height', targetSettingSize);
//    $('#userCircleSvg').css('width', targetSettingSize);
// }

// function settingUserCircleClickEvent() {
//    $('#userCircleSvg').show();
//    $('#userCircleSvg').off('click');
//    $('#userCircleSvg').on('click', forwardUserInfoPage);
// }

function settingUserInfoArea(loginUserData) {
   $('#userInfoLink').get(0).innerHTML = "ユーザー(" +  loginUserData.userId + ")";
   $('#userInfoId').val(loginUserData.userId);
   $('#userInfoName').val(loginUserData.username);
}

function forwardUserInfoPage() {
   let userId = $('#userInfoId').val();
   let hrefPage = '/';
   if(userId === '' || Number(userId) === Number.NaN) {
      hrefPage = '/login';
   } else {
      hrefPage = '/userInfoPage-' + String(userId);
   }
   window.location.href = hrefPage;
}

function settingUserInfoEvent() {
   $('#userInfoLink').off('click');
   $('#userInfoLink').on('click', forwardUserInfoPage);
}

function onloadDisyPlay() {
   setTimeout(function(){
      $('body').css('opacity', 1);
  }, 200);
}

function updateQuestionData() {
   loadingfunctionStart();
   let check1 = $('#inlineCheckbox1').prop('checked');
   let check2 = $('#inlineCheckbox2').prop('checked');
   if(!check1 && !check2) {
      removeQuestionData();
      loadingfunctionEnd();
      return;
   }

   removeQuestionData();
   getQuestionAllData(getAnswerPattern())
   .then(function(){
      showQuestionData();
      loadingfunctionEnd();
   });
}

function showQuestionData() {
   let i = 0;
   let quetionDataLength = $('[id^="cardQuest-"]').length;
   for(i = 0; i < quetionDataLength; i++) {
      $('[id^="cardQuest-"]').eq(i).show();
   }
}

function removeQuestionData() {
   let i = 0;
   let quetionDataLength = $('[id^="cardQuest-"]').length;
   for(i = 0; i < quetionDataLength; i++) {
      $('[id^="cardQuest-"]').eq(0).remove();
   }
}

function settingSearchEvent() {
   $('#searchText').off('keydown');
   $('#searchText').on('keydown', monitorKeyDown);
   $('#addon-wrapping').off('click');
   $('#addon-wrapping').on('click', function(){
      searchProcessMain();
   });
}

function monitorKeyDown(e) {
   if(e.keyCode !== 13) {
      return;
   }
   searchProcessMain();
}

function disabledSearchParts() {
   $('#searchText').prop({'disabled':true});
   $('#addon-wrapping').off('click');
}

function enableSearchParts() {
   $('#searchText').prop({'disabled':false});
   $('#addon-wrapping').off('click');
   $('#addon-wrapping').on('click', function(){
      searchProcessMain();
   });
}

function searchProcessMain() {
   let inputText = $('#searchText').val();
   if(inputText.trim() === '') {
      updateQuestionData();
      return;
   }
   disabledSearchParts();
   loadingfunctionStart();
   searchSameCharcterCodeFromDB(inputText)
   .then(function() {
      enableSearchParts();
      loadingfunctionEnd();
   });
}

function searchSameCharcterCodeFromDB(inputText) {
   let dfd = $.Deferred();
   let text = changeJapaneseToCharacterCode(inputText);
   
   removeQuestionData();
   var reqObj = new Object();
   reqObj['searchText'] = text;
   reqObj['answerPattern'] = getAnswerPattern();
   $.ajax({
      type: 'POST',
      url: '/searchQuestionData',
      contentType : 'application/json',
      data : reqObj,
      dataType : 'json'
   })
   .done(function(data){
      debugger
      if(data.length === 0) {
         return dfd.resolve();
      }
      createQuestionDataArea(data);
      showQuestionData();
      return dfd.resolve();
   })
   .fail(function(){
      return dfd.reject();
   })
   return dfd.promise();
}

function getAnswerPattern() {
   let check1 = $('#inlineCheckbox1').prop('checked');
   let check2 = $('#inlineCheckbox2').prop('checked');
   let answerPattern = 0;
   if(check1 && check2) {
      answerPattern = 0;
   } else if(check1) {
      answerPattern = 1;
   } else {
      answerPattern = 2;
   }
   return answerPattern;
}

function createQuestionDataArea(data) {
   let copyCardIndex = 1;
   let idFrameName = 'cardQuest-';
   let linkUriTextFrame = '/getQuestionDetail-';
   let parentSelector = $('.container');
   let i = 0;
   let witdhCard = $('.card.quest .card-body').width();
   let fontSize = Number($('.card.quest .card-body a').css('font-size').replace('px', ''));
   let limitCharCount = witdhCard/fontSize;
   for(i = 0; i < data.length; i++) {
      let copyNode = $('.card.quest').eq(copyCardIndex).clone();
      let idName = idFrameName + String(i + 1);
      copyNode.attr('id', idName);
      parentSelector.append(copyNode);
      let linkUriText = linkUriTextFrame + String(data[i]['questionId']);
      let linkerSelector = $('#' + idName + ' .card-body a');
      //linkerSelector[0].innerHTML = createTitle(changeCharacterCodeToJapanese(data[i]['questionDetailData']), limitCharCount);
      linkerSelector[0].innerHTML = changeCharacterCodeToJapanese(data[i]['questionTitle']);
      linkerSelector.attr('href', linkUriText);
   
      //body title
      let bodyTitleSelector = $('#' + idName + ' .card-body .card-body-genre');
      bodyTitleSelector[0].innerHTML = changeCategoryType(data[i]['questionType']);
   
      //footer
      let footer =  idName + ' .card-footer';
      let footerQuestDateSelecotr = $('#' + footer + ' #quest-date');
      footerQuestDateSelecotr[0].innerHTML = footerQuestDateSelecotr[0].innerHTML + data[i]['questionUpdateDate'];
      let footerQuestTypeSelecotr = $('#' + footer + ' #questType');
      footerQuestTypeSelecotr[0].innerHTML = footerQuestTypeSelecotr[0].innerHTML + data[i]['questionType'];
      let footerCommentSelecotr = $('#' + footer + ' #commentcount');
      footerCommentSelecotr[0].innerHTML = footerCommentSelecotr[0].innerHTML + data[i]['answerCount'];
      let answerlatestDateSelecotr = $('#' + footer + ' #update-date');
      answerlatestDateSelecotr[0].innerHTML = answerlatestDateSelecotr[0].innerHTML + data[i]['answerUpdateDate'];
   }
   $('.card.quest').eq(copyCardIndex).hide();
}

function createCategoryMenu() {
   var parentObj = $('#categoryMenu');
   var length = Object.keys(categoRizeObj).length;
    var i;
    for(i = 0; i < length; i++) {
        var val = categoRizeObj['categoRize' + String(i)];
        var node = '<a class="dropdown-item" href=\"category-' + String(i + 1) + '\">' + val + '</a>'
        parentObj.append(node);
    }
}