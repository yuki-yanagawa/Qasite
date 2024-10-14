function changeJapaneseToCharacterCode(japanese) {
    return window.btoa(window.encodeURI(japanese));
}

function changeCharacterCodeToJapanese(charctercode) {
    return window.decodeURI(window.atob(charctercode));
}

function adjustPreviweTextCreate(inputText) {
    let regexPattern = inputText.match(/<.*?>/g);
    let i = 0;
    debugger
    if(regexPattern === null) {
        if(inputText.match(/https*:\/\/[\w-_/\.]+/g) === null) {
            return inputText = '<div>' + inputText +'</div>';
        } else {
            //let reghtmlText = inputText.match(/https*:\/\/[\w-_/\.]+/g);
            let reghtmlText = inputText.match(/https*:\/\/[\?\=\w-_/\.]+/g);
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

    //let regexlinkPattern = inputText.match(/https*:\/\/[\w-_/\.]+/g);
    let regexlinkPattern = inputText.match(/https*:\/\/[\?\=\w-_/\.]+/g);
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


async function digestMessageSHA256(message) {
    if(!window.isSecureContext || crypto.subtle === undefined) {
        return message;
    }
    const msgUint8 = new TextEncoder().encode(message);                           // encode as (utf-8) Uint8Array
    const hashBuffer = await crypto.subtle.digest('SHA-256', msgUint8);           // hash the message
    const hashArray = Array.from(new Uint8Array(hashBuffer));                     // convert buffer to byte array
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join(''); // convert bytes to hex string
    return hashHex;
}

function changeCategoryType(num) {
    let number = Number(num);
    return categoRizeObj['categoRize' + String(number - 1)];
}