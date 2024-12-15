$(function(){
    initUpdateButton();
    getSettingField();
});

function initUpdateButton() {
    $('#update').off('click');
    $('#update').on('click', function(){
        var ret = confirm('update OK?');
        if(!ret) {
            return;
        }
        fieldUpdate();
    });
}

function fieldUpdate() {
    var param = collectFieldData();
    $.ajax({
        type: 'PUT',
        url: '/updateFieldData',
        contentType : 'application/json',
        data : param,
        dataType : 'json',
    })
    .done(function(){
        alert('Success!!!');
    })
    .fail(function(){
        alert('Failed!!!');
    })
}

function collectFieldData() {
    var fields = $('[id^="settingField-"]');
    var i;
    var param = new Object();
    for(i = 0; i < fields.length; i++) {
        var value = fields[i].value.trim();
        var id = fields[i].id.replaceAll('settingField-','');
        if(value === '') {
            continue;
        }
        param[id] = window.encodeURI(value);
    }
    return param;
}

function getSettingField() {
    $.ajax({
        type: 'POST',
        url: '/getAdminSettingFiled',
        contentType : 'application/json',
    })
    .done(function(result){
        var parentObj = $('#settingField');
        Object.keys(result).sort().forEach(e => {
            var id = 'settingField-' + String(e);
            var node = '<div><label for=\"' + id + '\">' + String(e) + '</label>' +
                '<input id=\"'+ id + '\" type=\"input\" value=\"' + window.decodeURI(result[e]) + '\"></div>';
            parentObj.append(node);
        });
    })
    .fail(function(){

    })
}