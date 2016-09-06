// Presets the time if value is not set by listening to the "open" even of the DateTimePicker, Runs only once
function getDatePickerOptions() {
    return {
        locale: "sv",
        minDate: "now",
        useCurrent: false,
        format: "YYYY-MM-DD"
    };
}
function getTimePickerOptions() {
    return {
        locale: "sv",
        useCurrent: false,
        stepping: 15,
        format: "LT"
    };
}

function addListenerForSettingDefaultTime(selector, defaultTime){
    $(selector).one( "dp.show", function() {
        var obj = $(this).data("DateTimePicker");
        var timeOfDay = 0;
        if(obj.date() != null) {
            timeOfDay = obj.date().minutes() + obj.date().hours() * 60;
        }
        if(timeOfDay == 0){
            obj.date(defaultTime);
        }
    });

}

function addValidatorMethodRequired(name, message){
    $.validator.addMethod(name, $.validator.methods.required, message);
}

function addValidatorMethodTime(name, message){
    $.validator.addMethod(name, function(value, element) {
        return this.optional(element) || /^(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?$/i.test(value);
    }, message);
}

function addValidatorMethodDateISO(name, message){
    $.validator.addMethod(name, $.validator.methods.dateISO, message);
}