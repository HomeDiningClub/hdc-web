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
   $(".host-level-icon").tooltip();

    // Host - Handles the carousel thumbnails
    $('[id^=carousel-selector-]').click( function(){
        var id_selector = $(this).attr("id");
        var id = id_selector.substr(id_selector.length -1);
        id = parseInt(id);
        $("#host-main-carousel").carousel(id);
        $("[id^=carousel-selector-]").removeClass("selected");
        $(this).addClass("selected");
    });

    // HOST - When the carousel slides, auto update
    $("#host-main-carousel").on("slid", function (e) {
        var id = $(".item.active").data("slide-number");
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


});