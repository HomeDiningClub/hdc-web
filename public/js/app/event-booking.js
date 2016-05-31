$(document).ready(function(){

    // Init booking
    var dateBookForm = $("#booking-form");
    var costDiv = $("#booking-total-cost");
    activateGuestSelector();
    activateDateSelector();

    function activateDateSelector(){
        var $datePicker = $("#event-date-list-picker");
        var $datePickerResults = $("#event-date-list-result");
        var eventTimeRoute = $datePicker.data("get-time-route");
        var availableDatesRoute = $datePicker.data("get-all-available-dates-route");
        var datePar = "date";
        var eventUUIDPar = "eventUUID";
        var eventUUID = $datePicker.data("event-uuid");
        var dateFormat = "YYYY-MM-DD";

        $.getJSON(availableDatesRoute + '?' + eventUUIDPar + '=' + eventUUID, function(data) {
            var availableDates = [];
            if(data) {
                $datePickerResults.toggleClass("hidden");

                $.each(data, function(key, val) {
                    availableDates.push(val);
                });

                //var availableDates = ["2016-05-17","2016-05-16","2016-05-30","2016-05-25"];

                var datePickerOptions = {
                    locale: "sv",
                    minDate: "now",
                    useCurrent: false,
                    format: dateFormat,
                    inline: true,
                    enabledDates: availableDates
                };

                $datePicker.datetimepicker(datePickerOptions).on("dp.change", function(e){
                    $datePickerResults.load(eventTimeRoute + '?' + eventUUIDPar + '=' + eventUUID + "&" + datePar + '=' + e.date.format(dateFormat), function(){
                        $(this).removeClass("flipOutX").addClass("flipInX");
                        dateBookForm.addClass("hidden");
                        activateBookingButtons();
                    });
                });

                $datePicker.slideDown();
            }else{
                //$("#event-date-list-suggest-date").hide().trigger("click");
                $("#event-date-list-result-no-results, .suggest-date-btn-text-no-options").removeClass("hidden");
                $(".suggest-date-btn-text").addClass("hidden");
                $("#event-date-list-suggest-date").addClass("btn-request").removeClass("btn-primary-flat");
            }

        });



    }


    function activateBookingButtons(){
        $(".select-date-button").on("click", function(){
            // Don't activate buttons if user is not logged in
            if(costDiv.length > 0){
                var uuid = $(this).data("selected-uuid");
                var date = $(this).data("selected-date");
                var time = $(this).data("selected-time");

                updatePrice();

                dateBookForm.find("#book-eventDateId").val(uuid);
                dateBookForm.find("#booking-chosen-date").text(date);
                dateBookForm.find("#booking-chosen-time").text(time);
            }

            $(".event-cta-form:visible").not(dateBookForm).addClass("hidden");
            dateBookForm.toggleClass("hidden");
        });
    }

    function activateGuestSelector(){
        dateBookForm.find("#book-guests").on("change", updatePrice);
    }

    function updatePrice(){

        var priceRoute = costDiv.data("price-route");
        var nrOfGuestsPar = "nrOfGuests";
        var nrOfGuests = dateBookForm.find("#book-guests").val();
        var eventUUIDPar = "eventUUID";
        var eventUUID = dateBookForm.find("#eventId").val();
        var $formGrp = costDiv.parents(".form-group");

        $formGrp.removeClass("flipInX").addClass("flipOutX");

        $.getJSON(priceRoute + '?' + eventUUIDPar + '=' + eventUUID + "&" + nrOfGuestsPar + '=' + nrOfGuests + '&tid=' + Date(), function(data){
            costDiv.text(data);
            $formGrp.removeClass("flipOutX").addClass("flipInX");
        });
    }

});
