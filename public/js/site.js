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


    // Disable double click
    $(document).ready(function(){
        $("*").dblclick(function(e){
            e.preventDefault();
        });
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

    ga('create', 'UA-52171001-1', 'hdc-web.herokuapp.com');
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
        editor_selector : "richtextrecipe",
        theme : "modern",
        skin: "light",
        height: 200,
        content_css : "/assets/stylesheets/richtext.min.css",
        menubar: false,
        plugins: [ "paste" ],
        paste_as_text: true,
        extended_valid_elements: "b,i,em,strong,li,ul,ol",
        statusbar : false,
        toolbar: "bullist numlist | bold italic | undo redo"
    });

});