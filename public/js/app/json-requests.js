
// Example config:
//var config = Object.create(null);
//config.map = 0;
//config.resultsNextBtn = "#resultEventNext";
//config.resultsPrevBtn = "#resultEventPrev";
//config.resultsTotal = "#resultsEventTotal";
//config.resultsPagination = "#pagination-event";
//config.resultsTarget = "#eventList";
//config.jsonRequest = '@{routes.EventPageController.viewEventByNameAndProfilePageJSON()}';
//config.queryParam = 'profileName';
//config.queryValue = '@profileName';
//config.htmlTemplate = htmlTemplate;

function getPagedJSON(config, pageDirection){

    $.getJSON(config.jsonRequest + '?' + config.queryParam + '=' + config.queryValue + '&page=' + (config.map + pageDirection) + '&tid=' + Date(), function(data){
        var nrOfItems = 0;
        var myArr = [];
        var hasNext = false;
        var hasPrev = false;
        var totalCount = 0;
        var totalPages = 0;

        $.each(data,function(key,value) {
            if(nrOfItems == 0){
                hasNext = value.hasNext;
                hasPrev = value.hasPrevious;
                totalCount = value.eventBoxCount;
                totalPages = value.totalPages;
            }

            nrOfItems = nrOfItems + 1;
            myArr.push(config.htmlTemplate(value));
        });

        if(nrOfItems > 0) {
            if(hasNext)
                $(config.resultsNextBtn).show();
            else
                $(config.resultsNextBtn).hide();

            if(hasPrev)
                $(config.resultsPrevBtn).show();
            else
                $(config.resultsPrevBtn).hide();

            config.map = config.map + pageDirection;

            if(totalPages > 1)
                $(config.resultsTotal).html((config.map+1) + " / " + totalPages);

            $(config.resultsTarget).html(myArr.join(''));
        }else{
            $(config.resultsPagination).hide();
        }

    });
}

function attachEventsToButtons(config){
    $(config.resultsNextBtn).off("click").on("click", function(){getPagedJSON(config, 1)});
    $(config.resultsPrevBtn).off("click").on("click", function(){getPagedJSON(config, -1)});
}
