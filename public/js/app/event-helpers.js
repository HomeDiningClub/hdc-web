// Presets the time if value is not set by listening to the "open" even of the DateTimePicker, Runs only once
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