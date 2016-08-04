$(document).ready(function(){

    var url = document.location.toString();
    /* Opens the selected tab */
    if (url.match('#')){
        $('.nav-tabs a[href=#'+url.split('#')[1]+']').tab('show');

        /* Code to open a mail directly, not working properly */
        //var msgId = url.split('#')[2].split("Some(")[1].substring(0, url.split('#')[2 ].split("Some(")[1 ].length -1);
        //$('#mail_' + msgId).click();
    }

});

