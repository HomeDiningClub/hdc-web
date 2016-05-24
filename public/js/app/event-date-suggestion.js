$(document).ready(function(){

    // Init suggestion
    activateDateSuggestionButtons();
    var $dateSugForm = $("#date-suggestion-form");

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
        $dateSugForm.find(".date input[type=date]").datetimepicker(datePickerOptions);
        $dateSugForm.find(".time input[type=time]").datetimepicker(timePickerOptions);
    }


    function activateDateSuggestionButtons(){
        $("#event-date-list-suggest-date").on("click", function(){
            var uuid = $(this).data("event-uuid");
            $dateSugForm.find(".event-uuid").val(uuid);
            activateDateSelector();
            $dateSugForm.removeClass("hidden");
        });
    }

});
