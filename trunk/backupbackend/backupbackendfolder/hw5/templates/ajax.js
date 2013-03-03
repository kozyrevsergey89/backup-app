$(document).ready(function() {

$('#target table').hide();
$('#not_current').hide();

$.ajaxSetup({
    type: 'POST',  
    timeout: 180000,
    error: ajax_error,
    dataType: 'json'
});
  
var handler = function() {
    var type = $(this).data('val').toString();
        send = 1,
        confirm_text = 'Вы действительно хотите удалить все данные с телефона';
    
    if (type == 1) {
        if (!confirm(confirm_text)) {
            send = 0;
        }
    }
    
    if (send) {
        send_ajax_first(type);
        $(this).removeClass('transition').addClass('loading');    
    }
    
}

$('.reload').click(function(){
    var container = $(this).parents('.after_load'),
        type = container.data('val').toString();
    container.addClass('loading').removeClass('after_load');
    send_ajax_first(type);
});

$('div.info_block').bind('click', handler);

function send_ajax_first(type) {
    var action_name = '';
    
    switch (type) {
        case '1': action_name = 'wipe';$('#wipe').unbind('click', handler);break;
        case '2': action_name = 'get_backup';$('#check_my_backup').unbind('click', handler);break;
        case '3': action_name = 'get_gps';$('#get_gps').unbind('click', handler);break;
        case '4': action_name = 'get_info';$('#get_my_phone_info').unbind('click', handler);break;
        case '5': action_name = 'find_phone';$('#find_phone').unbind('click', handler);break;
    }
    
    $.ajax({
        url: '/jsonanswerfirst',
        data: JSON.stringify({val: type, action: action_name}),
        success: ajax_first,
    });
}

});//document ready

function ajax_first(response) {
    var type = response.val.toString(),
    action_name = '';
    
    switch (type) {
        case '1': action_name = 'wipe';break;
        case '2': action_name = 'get_backup';break;
        case '3': action_name = 'get_gps';break;
        case '4': action_name = 'get_info';break;
        case '5': action_name = 'find_phone';break;
    }
    
    setTimeout(function(){
        $.ajax({
            url: '/jsonanswer',
            data: JSON.stringify({val: type, action: action_name}),
            success: ajax_success,
        });
    },3000);    
}

function ajax_success(response) {
    switch (response.val) {
        case '1': 
            do_wipe(response);
            $('#wipe').removeClass('loading').addClass('after_load').removeClass('info_block');
        break;
        case '2': 
            do_check_my_backup(response);
            $('#check_my_backup').removeClass('loading').addClass('after_load').removeClass('info_block');
        break;
        case '3': 
            do_get_gps(response);
            $('#get_gps').removeClass('loading').addClass('after_load').removeClass('info_block');
        break;
        case '4': 
            do_get_my_phone_info(response);
            $('#get_my_phone_info').removeClass('loading').addClass('after_load').removeClass('info_block');
        break;
        case '5': 
            do_find_phone(response);
            $('#find_phone').removeClass('loading').addClass('after_load').removeClass('info_block');
        break;
    }
}

function ajax_error(response) {
    window.setTimeout('location.reload()', 3000);
}

//{val:val,success:true\false}
function do_wipe(response) {
    if (response.success == 'false') {
        $('#wipe_result').text('Не удалось выполнить операцию');
    }
    else if (response.admin == 'true') {
        $('#wipe_result').html('Удаление выполнено успешно');
    }
    else {
        $('#wipe_result').html('Не удалось! Вы не разрешили<br />удаленную очистку в мобильном приложении!');
    }
    
    
    $('#wipe table').show();
}

//{val:val,success:true\false, timestamp:time}
function do_check_my_backup(response) {
    if (response.success) {        
        if (response.timestamp) {
            $('#backup_status').text('доступна');
            $('#timestamp').text(response.timestamp);
        }
        else {
            $('#backup_status').text('недоступна');
            $('#timestamp').text('информация недоступна');
        }
    }
    
    $('#check_my_backup table').show();
}

//{val:val,success:true/false, latitude:x, longitude:y}
function do_get_gps(response) {
    var latitude = response.latitude,
        longitude = response.longitude;
    //
    
    if (response.success) {
        initialize(latitude, longitude);
    }
    
    if(response.is_current) {
        $('#not_current').hide();    
    }
    else {
        $('#not_current').show();
    }
}
//"action" == ''find_phone
//{val:val, success: true\false, ip_address:2.2.2.2, phone_number:77777, acc_list:[acc1,acc2,...]}
function do_get_my_phone_info(response) {
    if (response.success) {
        
        if (response.phone_number) {
            $('#phone').text(response.phone_number);
        }
        else {
            $('#phone').text('телефон недоступен, попробуйте позже');
        }
        
        if (response.ip_address) {
            $('#ip').text(response.ip_address);
        }
        else {
            $('#ip').text('телефон недоступен, попробуйте позже');
        }
        
        if (response.acc_list) {
            var acc_list = decodeURIComponent(response.acc_list).split(',').join('<br />');
            $('#accounts').html(acc_list);
        }
        else {
            $('#accounts').text('телефон недоступен, попробуйте позже');
        }
    }
    else {
        $('#phone').text('телефон недоступен, попробуйте позже');
        $('#ip').text('телефон недоступен, попробуйте позже');
        $('#accounts').text('телефон недоступен, попробуйте позже');
    }
    $('#get_my_phone_info table').show();
}

function do_find_phone(response) {
    if (response.success) {
        $('#find_phone_result').text('Сигнал отправлен');
    }
    else {
        $('#find_phone_result').text('телефон недоступен, попробуйте позже');
    }
    $('#find_phone table').show();
}

function initialize(latitude, longitude) {
    var myLatlng = new google.maps.LatLng(latitude, longitude);
    var mapOptions = {
      zoom: 4,
      center: myLatlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById('map_to'), mapOptions);

    var marker = new google.maps.Marker({
        position: myLatlng,
        map: map,
        title: 'Ваш телефон'
    });
}