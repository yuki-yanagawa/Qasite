function InlineFrame(htmlFileName, endFunc) {
    this.blockWindow();
    this.createWizard(htmlFileName);
    
    let adjustWidth = $(document).width() * 0.85;
    $('#frameId').width(adjustWidth);
    let paddingWidth = $(document).width() - $('#frameId').width();
    let leftPosition = paddingWidth / 2;
    $('#frameId').css('left', String(leftPosition) + 'px');
    let frameHeght = screen.height * 0.8;
    $('#frameId').height(frameHeght);
    let topPositon = screen.height - frameHeght > 30 ? '30px' : '10px';  
    $('#frameId').css('top',topPositon);

    this.createCloseButton();
    $('#closeFunc').off('click');
    let that = $(this)[0];
    $('#closeFunc').on('click', function(){
        that.closeFunc();
        if(endFunc !== undefined) {
            endFunc();
        }
    });
    return this;
};

InlineFrame.prototype.createCloseButton = function() {
    let closeFrame = $('<div id=\"closeBtArea\"><button id=\"closeFunc\">' +
        '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x-lg" viewBox="0 0 16 16">' +
        '<path d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854Z"/></svg>' +
         '</button></div>');
    closeFrame.css('z-index', '3');
    closeFrame.css('position', 'fixed');
    closeFrame.css('top', '0px');
    let leftPosiotn = $('#frameId').position().left
    closeFrame.css('left', leftPosiotn);
    $('body').append(closeFrame);
};

InlineFrame.prototype.createWizard = function(htmlFileName) {
    let inlineFrame = $('<iframe src=\"' + htmlFileName +'\" id=\"frameId\" frameborder=\"1\" style=\"opacity:0;\" onload=\"openInlineFrame();\"></iframe>');
    inlineFrame.css('z-index', '2');
    //inlineFrame.css('position', 'absolute');
    inlineFrame.css('position', 'fixed');
    inlineFrame.css('background-color', 'white');
    $('body').append(inlineFrame);
};

InlineFrame.prototype.blockWindow = function() {
    let height = $(document).height();
    let width = $(document).width();
    let blockWindow = $('<div id=\'blockWindow\'></div>');
    blockWindow.css('background-color', 'black');
    blockWindow.css('z-index', '1');
    blockWindow.css('position', 'absolute');
    blockWindow.css('top', '0px');
    blockWindow.css('left', '0px');
    blockWindow.css('width', width);
    blockWindow.css('height', height);
    blockWindow.css('opacity', '0.5');
    //blockWindow.css('overflow', 'hidden');
    $('body').append(blockWindow);
};

InlineFrame.prototype.closeFunc = function() {
    $('#closeBtArea').remove();
    $('#blockWindow').remove();
    $('#frameId').remove();
};

function openInlineFrame() {
    if(Number($('#frameId').css('opacity')) !== 0) {
        $('#closeFunc').click();
        return;
    }
    $('#frameId').css('opacity', '1');
}