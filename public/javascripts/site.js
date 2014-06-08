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
});