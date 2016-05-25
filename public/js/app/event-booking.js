$(document).ready(function(){

    // Init booking
    var $dateBookForm = $("#booking-form");
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
                        activateBookingButtons();
                    });
                });

                $datePicker.slideDown();
            }else{
                $("#event-date-list-suggest-date").hide().trigger("click");
            }

        });



    }


    function activateBookingButtons(){
        $(".select-date-button").on("click", function(){
            var uuid = $(this).data("selected-uuid");
            var date = $(this).data("selected-date");
            var time = $(this).data("selected-time");

            updatePrice();

            $dateBookForm.find(".event-uuid").val(uuid);
            $dateBookForm.find("#booking-chosen-date").text(date);
            $dateBookForm.find("#booking-chosen-time").text(time);
            $(".event-cta-form:visible").not($dateBookForm).addClass("hidden");
            $dateBookForm.toggleClass("hidden");
        });
    }

    function activateGuestSelector(){
        $dateBookForm.find("#guests").on("change", updatePrice);
    }

    function updatePrice(){
        var $costDiv = $("#booking-total-cost");
        var priceRoute = $costDiv.data("price-route");
        var nrOfGuestsPar = "nrOfGuests";
        var nrOfGuests = $dateBookForm.find("#guests").val();
        var eventUUIDPar = "eventUUID";
        var eventUUID = $dateBookForm.find("#eventId").val();
        var $formGrp = $costDiv.parents(".form-group");

        $formGrp.removeClass("flipInX").addClass("flipOutX");

        $.getJSON(priceRoute + '?' + eventUUIDPar + '=' + eventUUID + "&" + nrOfGuestsPar + '=' + nrOfGuests + '&tid=' + Date(), function(data){
            $costDiv.text(data);
            $formGrp.removeClass("flipOutX").addClass("flipInX");
        });
    }

});
