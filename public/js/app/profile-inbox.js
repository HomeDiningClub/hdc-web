$(document).ready(function(){

    /* Mark mail as read, also read more functionality */
    $(".mail-inbox-row").click(function(){
        var btnArray = this.id.split("mail_");
        var mailRowId = "#mail_" + btnArray[1];
        if($(mailRowId + " .unread").is(":visible")){
            $.get( "/profile/mark?msgId=" + btnArray[1] + "&tid=" + new Date().toString(), function (data, status){
                $(mailRowId + " .read").show();
                $(mailRowId + " .unread").hide();
            });
        }
    });

});