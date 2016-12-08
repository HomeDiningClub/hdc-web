$(document).ready(function(){

    // Init booking
    var dateBookForm = $("#booking-form");
    var costDiv = $("#booking-total-cost");
    activateGuestSelector();
    activateDateSelector();
    setupBookingSubmitButton();

    function activateDateSelector(){
        var $datePickerChoice = $("#event-date-list-choice");
        var $datePicker = $("#event-date-list-picker");
        var $datePickerList = $("#event-date-list-picker-list");
        var $datePickerResults = $("#event-date-list-result");
        var $btnShowDatePicker = $(".btn-show-datepicker");
        var $btnShowDateList = $(".btn-show-datelist");
        var eventTimeRoute = $datePicker.data("get-time-route");
        var availableDatesRoute = $datePicker.data("get-all-available-dates-route");
        var datePar = "date";
        var eventUUIDPar = "eventUUID";
        var eventUUID = $datePicker.data("event-uuid");
        var dateFormat = "YYYY-MM-DD";

        $.getJSON(availableDatesRoute + '?' + eventUUIDPar + '=' + eventUUID, function(data) {
            var availableDates = [];
            if(data) {
                // Hide or show the results-list
                $datePickerResults.toggleClass("hidden");

                // Store the selectable dates
                $.each(data, function(key, val) {
                    var addThisDate = true;

                    // Don't add duplicates dates (since times are fetched separately)
                    if($.inArray(val, availableDates) > -1){
                        addThisDate = false;
                    }

                    if(addThisDate){
                        availableDates.push(val);
                    }
                });

                // Common setup
                function onClickShowDateList() {
                    //$(this).prop("checked", true);
                    $datePicker.hide();
                    $datePickerResults.empty();
                    // Don't show list if only one date, just render the default
                    if($datePickerList.find("button").length > 1){
                        $datePickerList.show();
                    }
                    setDefaultDateForList();
                }
                function onClickShowDatePicker() {
                    //$(this).prop("checked", true);
                    $datePickerList.hide();
                    $datePickerResults.empty();
                    $datePicker.show();
                    setDefaultDateForPicker();
                }

                function setDefaultDateForPicker(){
                    $datePicker.data("DateTimePicker").date(null);
                    $datePicker.data("DateTimePicker").date(availableDates[0]);
                }
                function setDefaultDateForList(){
                    $datePickerList.find(".btn").first().focus().trigger("click");
                }

                function onClickDateEvent(e, t) {
                    var selDate;
                    if(e.date){
                        selDate = e.date.format(dateFormat);
                    }else if(t){
                        selDate = t.text();
                    }else{
                        // No date selected, just a reset of the picker
                        return false;
                    }
                    $datePickerResults.load(eventTimeRoute + '?' + eventUUIDPar + '=' + eventUUID + "&" + datePar + '=' + selDate, function(){
                        $(this).removeClass("flipOutX").addClass("flipInX");
                        dateBookForm.addClass("hidden");
                        activateBookingButtons();
                    });
                }
                $btnShowDateList.on("click", onClickShowDateList);
                $btnShowDatePicker.on("click", onClickShowDatePicker);

                // DateList setup
                $.each(availableDates, function(key,val) {
                    var btn = $('<button/>')
                        .text(val)
                        .addClass("btn btn-lg btn-orange-outline btn-block")
                        .click(function (e) {
                            var t = $(this);
                            onClickDateEvent(e, t);
                    });
                    $datePickerList.append(btn);
                });

                // DatePicker setup
                var datePickerOptions = {
                    locale: "sv",
                    minDate: "now",
                    useCurrent: false,
                    format: dateFormat,
                    inline: true,
                    enabledDates: availableDates
                };
                // Attach event to dates in picket
                $datePicker.datetimepicker(datePickerOptions).on("dp.change", function(e){onClickDateEvent(e)});

                if(availableDates.length > 2){
                    // Move the switch
                    $btnShowDatePicker.prop("checked", true);
                    setDefaultDateForPicker();
                    // Show the date-picker
                    $datePicker.show();
                }else{
                    // Move the switch
                    $btnShowDateList.prop("checked", true);
                    setDefaultDateForList();
                    // Don't show list if only one date, just render the default
                    if($datePickerList.find("button").length > 1){
                        $datePickerList.show();
                    }
                }

                // Show the date-choice-switcher
                $datePickerChoice.toggleClass("hidden");
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

    function setupBookingSubmitButton(){
        dateBookForm.find("button[type=submit]").click(function(e){
            $(this).prop("disabled", true);
            $(this).text($(this).data("send-txt"));
            $(this).parents("form").submit();
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
