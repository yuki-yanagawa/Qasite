//var DEFAULT_MY_WIDTH = 100;
var DEFAULT_MY_HEIGHT = 100;

function Dialog(option) {
    let parentId = option.parentId.indexOf('#') === 0 ? option.parentId : '#' + option.parentId;
    let beforeBodyZindex = $(parentId).css('z-index');
    $(parentId).css('z-index', 0);
    let bodyWidth = $(parentId).innerWidth();
    let bodyheight = $(parentId).innerHeight();

    let height = option.height === undefined ? DEFAULT_MY_HEIGHT : option.height;

    let backWidowHeight = $(document).height();
    let backWindowWidth = $(document).width();
    let blockWindow = this.createBackBlockWindow(backWindowWidth, backWidowHeight);

    let originalButton = option.originalButton;
    let dialogWindow = this.createDialogWindow(option.message, height, bodyheight, originalButton)
    $(parentId).append(blockWindow);
    $(parentId).append(dialogWindow);

    // setting opacite
    $('#blockWindow').css('opacity', 0.1);

    // setting dialog witdh position
    let width = dialogWindow.width();
    diff = bodyWidth - width;
    let leftPosition = diff / 2;
    dialogWindow.css('left', leftPosition + 'px');


    // close Bt action
    $('#dialogCloseBt').off('click');
    var current = this;
    $('#dialogCloseBt').on('click', function() {
        $(parentId).css('z-index', beforeBodyZindex);
        current.closeFunc();
    });
    return this;
};

Dialog.prototype.testCall = function() {
    console.log("dialog OK");
}

Dialog.prototype.createBackBlockWindow = function(width, height) {
    let blockWindow = $('<div id=\'blockWindow\'></div>');
    blockWindow.css('background-color', 'black');
    blockWindow.css('z-index', '1');
    blockWindow.css('position', 'absolute');
    blockWindow.css('top', '0px');
    blockWindow.css('left', '0px');
    blockWindow.css('width', width);
    blockWindow.css('height', height);
    return blockWindow;
}

Dialog.prototype.createDialogWindow = function(message, height, bodyheight, originalButton, body) {
    message = message === undefined ? "" : message;
    let dialogWindow = $('<div id=\'dialogWindow\' class=\'card text-black\' style=\'width: 18rem;\'></div>');
    let mainParts = '<div class=\'card-body questionDetailText\'><span>' + message + '</span>';
    if(body !== undefined) {
        mainParts += body;
    }
    mainParts += '</div>'
    dialogWindow.append(mainParts);
    if(originalButton !== undefined) {
        dialogWindow.append('<div class=\'card-footer\' style=\'text-align: center\'>' + originalButton + '</div>');
    } else {
        dialogWindow.append('<div class=\'card-footer\' style=\'text-align: right\'>'
            + '<button id=\'dialogCloseBt\' class=\'btn btn-secondary\'>close</button></div>');
    }
    dialogWindow.addClass('container-md');
    dialogWindow.css('z-index', '2');
    dialogWindow.css('position', 'absolute');
    let topPosition = bodyheight / 2 - height;
    dialogWindow.css('top', topPosition + 'px');
    dialogWindow.css('left', '0px');
    return dialogWindow;
}

Dialog.prototype.closeFunc = function() {
    $('#blockWindow').remove();
    $('#dialogWindow').remove();
}
