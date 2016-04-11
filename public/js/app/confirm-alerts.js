$(document).ready(function () {

    // Confirm alerts
    $("[data-confirm-text]").click(function(e){
        var $el = $(this);
        e.preventDefault();
        var confirmText = $el.attr('data-confirm-text');
        var confirmTitle = $el.attr('data-confirm-title');
        var confirmBtnOk = $el.attr('data-confirm-btn-ok');
        var confirmBtnCancel = $el.attr('data-confirm-btn-cancel');
        var confirmType = $el.attr('data-confirm-type');
        bootbox.confirm({
            message: confirmText,
            title: confirmTitle,
            buttons: {
                confirm: {
                    label: confirmBtnOk,
                    className: 'btn-danger'
                },
                cancel: {
                    label: confirmBtnCancel,
                    className: 'btn-default'
                }
            },
            callback: function(result) {
                if (result) {
                    if(confirmType.toLowerCase() == "submit"){
                        $el.closest('form').submit();
                    }else if (confirmType.toLowerCase() == "link"){
                        window.location = $el.attr("href");
                    }

                }
            }});
    });

});