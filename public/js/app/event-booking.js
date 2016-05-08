$(document).ready(function(){
    $(".select-date-button").on("click", function(){
        var uuid = $(this).data("selected-uuid");
        var date = $(this).data("selected-date");
        var time = $(this).data("selected-time");

        $("#eventDateId").val(uuid);
        $("#booking-chosen-date").text(date);
        $("#booking-chosen-time").text(time);
        $("#booking-form").removeClass("hidden");
    });
});
