/**
 * Add general scripts in this file
 */


// Grade texts
//$(document).ready(function () {
//    $(".rating").rating(
//        {starCaptions: {
//            1: i18n("grade.rating.1"),
//            2: "Poor",
//            3: "Ok",
//            4: "Good",
//            5: "Very Good"}
//        });
//})


$(document).ready(function () {

    // Activate tooltips
    $(".host-level-icon,.host-header-link").tooltip();

    // Activate popovers
    $(".popover-trigger").popover();

    // Disable any disabled tabs
    $(".nav .disabled>a").on("click", function(e) { e.preventDefault(); return false; });

    // Collapse text if found
    if($(".truncate-text-review-boxes").length){
        $(".truncate-text-review-boxes").collapser({
            mode: "lines",
            effect: "slide",
            truncate: 3,
            speed: 100,
            showText: '<span class="glyphicon glyphicon-chevron-down"></span>&nbsp;',
            hideText: '<span class="glyphicon glyphicon-chevron-up"></span>&nbsp;',
            lockHide: false,
            showClass: "truncate-text-show",
            hideClass: "truncate-text-hide",
            controlBtn: "more-text-ctrl",
            dynamic: false
            //controlBtn: function(){
            //    return $(this).parent().find('.more-text-ctrl');
            //}
        });
    }

//   $(".btn-rate-popover").popover({
//       trigger: "click",
//       placement: "bottom",
//       container: ".rate-popover-placeholder",
//       html : true,
//       content : $(".rate-popover").html()
//   });


    // Override Bootstrap tab click to include focusing on tabs
    // disable old listener
    // Not working?
/*
    $('body').off('click.tab.data-api');

    // attach new listener
    $('body').on('click.scrolling-tabs', '[data-toggle="tab"], [data-toggle="pill"]', function (e) {
        //e.preventDefault()
        $(this).tab('show')
    });
    */

    // Menu color change
    $(document).scroll(function() {
        var alpha = Math.min(0.5 + 0.4 * $(this).scrollTop() / 110, 0.8);
        var channelr = Math.round(alpha * 89);
        var channelg = Math.round(alpha * 161);
        var channelb = Math.round(alpha * 148);
        //$(".nav-main-menu-wrapper").css('opacity', alpha);
        $(".nav-main-menu-wrapper").css('background-color', 'rgba(' + channelr + ',' + channelg + ',' + channelb + ',' + alpha + ')');
    });

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

    // Make footer flush to bottom
    //flushFooterToBottom();

    // jQuery validation defaults
    overridejQueryValidationDefaults();

    // Random BG
    randomizeBgImage();

    // Disable double click
    $("button,a,input").dblclick(function(e){
        e.preventDefault();
    });

    // Clicking on carousel thumbnails
    $('[id^=carousel-selector-]').click( function(){
        var id_selector = $(this).attr("id");
        var id = id_selector.substr(id_selector.length -1);
        id = parseInt(id);
        $(".hdc-carousel").carousel(id);
        $("[id^=carousel-selector-]").removeClass("selected");
        $(this).addClass("selected");
    });

    // When the carousel slides, auto update
    $(".hdc-carousel").on("slid.bs.carousel", function () {
        var id = $(".hdc-carousel .item.active").data("slide-number");
        id = parseInt(id);
        $("[id^=carousel-selector-]").removeClass("selected");
        $("[id^=carousel-selector-" + id + "]").addClass("selected");
    });

    // GA include
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    ga('create', 'UA-52171001-2', 'auto');
    ga('send', 'pageview');


    // Facebook
    (function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) return;
        js = d.createElement(s); js.id = id;
        js.src = "//connect.facebook.net/sv_SE/sdk.js#xfbml=1&appId=481898671955836&version=v2.0";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));


    // TinyMCE
    tinymce.init({
        mode : "specific_textareas",
        editor_selector : "richtext",
        theme : "modern",
        skin: "lightgray",
        statusbar : true,
        height: 200,
        content_css : "/assets/stylesheets/richtext.min.css",
        plugins: [
            "advlist autolink link image lists charmap print preview hr anchor pagebreak spellchecker",
            "searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking",
            "table contextmenu directionality emoticons template paste textcolor"
        ],
        toolbar: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | print preview media fullpage | forecolor backcolor emoticons"
    });

    tinymce.init({
        mode : "specific_textareas",
        editor_selector : "richtextuser",
        theme : "modern",
        skin: "light",
        content_css : "/assets/stylesheets/richtext.min.css",
        menubar: false,
        plugins: [ "paste" ],
        paste_as_text: true,
        extended_valid_elements: "b,i,em,strong",
        statusbar : false,
        toolbar: "bold italic"
    });

    tinymce.init({
        mode : "specific_textareas",
        editor_selector : "richtext-extra",
        theme : "modern",
        skin: "light",
        height: 300,
        content_css : "/assets/stylesheets/richtext.min.css",
        menubar: false,
        plugins: [ "paste" ],
        paste_as_text: true,
        extended_valid_elements: "b,i,em,strong,li,ul,ol",
        statusbar : false,
        toolbar: "bullist numlist | bold italic | undo redo"
    });

});


function randomizeBgImage(){
    $("body").css("background-image", "url('/assets/images/general/body-bg-faded/2048x1360-" + randomIntFromInterval(1,11).toString() + ".jpg')");
}

function randomIntFromInterval(min,max) {
    return Math.floor(Math.random()*(max-min+1)+min);
}

function flushFooterToBottom(){

    var docHeight = $(window).height();
    var footerHeight = $('#footer').height();
    var footerTop = $('#footer').position().top + footerHeight;

    if (footerTop < docHeight) {
        $('#footer').css('margin-top', -1+ (docHeight - footerTop) + 'px');
    }
}

function isGuid(expression){
    if (expression != "undefined"){
        return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(expression);
    }
    return false;
}

function isInList(value, list) {
    for (var i = 0; i < list.length; i++) {
        if (list[i] == value) {
            return true;
        }
    }
    return false;
}

function overridejQueryValidationDefaults(){
    $.validator.setDefaults({
        errorElement: "span",
        errorClass: "help-block",
        highlight: function (element, errorClass, validClass) {
            $(element).closest('.form-group').addClass('has-error');
        },
        unhighlight: function (element, errorClass, validClass) {
            $(element).closest('.form-group').removeClass('has-error');
        },
        errorPlacement: function (error, element) {
            if (element.parent('.input-group').length || element.prop('type') === 'checkbox' || element.prop('type') === 'radio') {
                error.insertAfter(element.parent());
            } else {
                error.insertAfter(element);
            }
        }
    });
}