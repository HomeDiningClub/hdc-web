$(document).ready(function(){

    // Init suggestion
    var dateSugFormWrapper = $("#date-suggestion-form");
    var defaultSuggestedTimeIfNonSelected = "19:00:00";
    activateDateSuggestionButtons();
    validateSuggestionForm(dateSugFormWrapper);

    function activateDateSelector(){

        var datePickerOptions = getDatePickerOptions();
        var timePickerOptions = getTimePickerOptions();

        var sugDateInput = dateSugFormWrapper.find(".date input[type=date]");
        var sugTimeInput = dateSugFormWrapper.find(".time input[type=time]");
        var sugCommentInput = dateSugFormWrapper.find("#suggest-comment");

        sugDateInput.datetimepicker(datePickerOptions);
        sugTimeInput.datetimepicker(timePickerOptions);
        addValidationOnSuggestionForm(sugCommentInput, sugDateInput, sugTimeInput, dateSugFormWrapper.find("form"));
        addListenerForSettingDefaultTime(sugTimeInput, defaultSuggestedTimeIfNonSelected);
    }

    function validateSuggestionForm(formWrapper){
        formWrapper.find("button[type=submit]").click(function(e){
            if(dateSugFormWrapper.find("form").valid()){
                $(this).prop("disabled", true);
                $(this).text($(this).data("send-txt"));
                $(this).parents("form").submit();
            }else {
                e.preventDefault();
                return false;
            }
        });
    }

    function addValidationOnSuggestionForm(sugCommentInput, sugDateInput, sugTimeInput, form){

        addValidatorMethodRequired(
            "myRequired",
            $(sugDateInput).data("validation-req-txt")
        );
        addValidatorMethodTime(
            "myTime",
            $(sugTimeInput).data("validation-txt")
        );

        addValidatorMethodDateISO(
            "myDateISO",
            $(sugDateInput).data("validation-txt")
        );

        var formValRules = {
            ignore: ":not(:visible)",
            rules: {
                "suggest-comment": {
                    required: true
                },
                "suggest-date": {
                    myRequired: true,
                    myDateISO: true
                },
                "suggest-time": {
                    myRequired: true,
                    myTime: true
                }
            },
            messages: {
                 "suggest-comment": sugCommentInput.data("validation-req-txt")
            }
        };

        form.validate(formValRules);


    }

    function activateDateSuggestionButtons(){
        $("#event-date-list-suggest-date").on("click", function(){
            var uuid = $(this).data("event-uuid");
            dateSugFormWrapper.find(".suggest-event-uuid").val(uuid);
            activateDateSelector();
            $(".event-cta-form:visible").not(dateSugFormWrapper).addClass("hidden");
            dateSugFormWrapper.toggleClass("hidden");
        });
    }

});
