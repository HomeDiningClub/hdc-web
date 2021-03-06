// Global vars
widget      = $(".step");
btnnext     = $(".next");
btnprev     = $(".prev");
btnsubmit   = $(".submit");
btndelete   = $(".delete");
btnabort    = $(".abort");
progressBar = $(".progress-bar");
progress    = $(".progress");

function initMultiForm(form, formValRules, dateRules, timeRules){
    var current = 1;

    // Init buttons and UI
    widget.not(':eq(0)').hide();
    hideButtons(current);
    setProgress(current);

    // Next button click action
    btnnext.click(function(){
        goForward = false;
        if(current < widget.length){

            if(formValRules)
                goForward = form.valid();

            if(goForward){
                widget.show();
                widget.not(':eq('+(current++)+')').hide();
                setProgress(current);
            }
        }
        hideButtons(current);
    });

    // Back button click action
    btnprev.click(function(){
        if(current > 1){
            current = current - 2;
            if(current < widget.length){
                widget.show();
                widget.not(':eq('+(current++)+')').hide();
                setProgress(current);
            }
        }
        hideButtons(current);
    });

    // Submit button click
/*    btnsubmit.click(function(){
        alert("Submit button clicked");
    });*/



}

function hideMultiFormItems(){
    btnnext.hide();
    btnprev.hide();
    progress.hide();
}

function activateValidation(form, formValRules, dateRules, timeRules){
    // Validation rules
    $.validator.addClassRules("date-input", dateRules);
    $.validator.addClassRules("time-input", timeRules);
    form.validate(formValRules);

}

// Change progress bar action
setProgress = function(currstep){
    var percent = parseFloat(100 / widget.length) * currstep;
    percent = percent.toFixed();
    progressBar.css("width",percent+"%").html(percent+"%");
};

// Hide buttons according to the current step
hideButtons = function(current){
    var limit = parseInt(widget.length);

    $(".action").hide();

    if(current < limit) btnnext.show();
    if(current > 1) btnprev.show();
    if (current == limit) {
        btnnext.hide();
        btnsubmit.show();
        btnabort.show();
        btndelete.show();
    }
};

