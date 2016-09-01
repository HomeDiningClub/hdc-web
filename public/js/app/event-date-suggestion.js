$(document).ready(function(){

    // Init suggestion
    var dateSugForm = $("#date-suggestion-form");
    var defaultSuggestedTimeIfNonSelected = "19:00:00";
    activateDateSuggestionButtons();


    function activateDateSelector(){

        var datePickerOptions = {
            locale: "sv",
            minDate: "now",
            useCurrent: false,
            format: "YYYY-MM-DD"
        };

        var timePickerOptions = {
            locale: "sv",
            useCurrent: false,
            stepping: 15,
            format: "LT"
        };
        dateSugForm.find(".date input[type=date]").datetimepicker(datePickerOptions);
        var sugTimeInput = dateSugForm.find(".time input[type=time]");
        sugTimeInput.datetimepicker(timePickerOptions);
        addListenerForSettingDefaultTime(sugTimeInput, defaultSuggestedTimeIfNonSelected);
    }


    function activateDateSuggestionButtons(){
        $("#event-date-list-suggest-date").on("click", function(){
            var uuid = $(this).data("event-uuid");
            dateSugForm.find(".suggest-event-uuid").val(uuid);
            activateDateSelector();
            $(".event-cta-form:visible").not(dateSugForm).addClass("hidden");
            dateSugForm.toggleClass("hidden");
        });
    }

});
