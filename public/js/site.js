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

    // Make footer flush to bottom
    //flushFooterToBottom();

    // jQuery validation defaults
    overridejQueryValidationDefaults();

    // Random BG
    randomizeBgImage();

    // Make tabs marked if any input error has occured
    makeTabsMarkedIfInputErrorOccured();

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



    // Facebook
    (function(d, s, id) {
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) return;
            js = d.createElement(s); js.id = id;
            js.src = "//connect.facebook.net/sv_SE/sdk.js#xfbml=1&version=v2.8";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));
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

function makeTabsMarkedIfInputErrorOccured(){
    $(".has-error").each(function(index){
        var id = $(this).parents(".tab-pane").attr("id");
        var item = $(".nav.nav-tabs li a[href=#" + id + "]");
        if(index == 0){
            item.trigger('click');
        }
        item.closest('li').addClass('tab-has-error');
    });
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