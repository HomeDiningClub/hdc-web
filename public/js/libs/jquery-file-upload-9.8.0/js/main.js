/*
 * jQuery File Upload Plugin JS Example 8.9.1
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/* global $, window */

$(function () {
    'use strict';
    var url = "/uploadimage.json";
    /*
    var uploadButton = $('<button/>')
        .addClass('btn btn-primary upload-file-submit')
        .prop('disabled', true)
        .text('Processing...')
        .on('click', function (event) {
            event.preventDefault();
            var $this = $(this),
                data = $this.data();
            $this
                .off('click')
                .text('Abort')
                .on('click', function () {
                    //data.submit();
                });
            data.submit().always(function () {
                $this.remove();
            });
        });

    var deleteButton = $('<button/>')
        .addClass('btn btn-danger btn-close')
        //.prop('disabled', true)
        //.text('Processing...  ')
        .on('click', function (event) {
            event.preventDefault();
            var $this = $(this),
                data = $this.data();
            data.files.slice(1);
            data.context.remove();
            //$this
                //.off('click')
                //.text('Abort')
                //.on('click', function () {

                    //data.files.remove();
                    //data.abort();
                    //$this.remove();
                //});
            //data.submit().always(function () {
            //    $this.remove();
            //});
            $this.remove();
        });
     */

    $('#fileupload').fileupload({
        //url: url,
        dataType: 'json',
        autoUpload: false,
        singleFileUploads:true,
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
        maxFileSize: 10000000, // 10 MB
        // Enable image resizing, except for Android and Opera,
        // which actually support image resizing, but fail to
        // send Blob objects via XHR requests:
        disableImageResize: /Android(?!.*Chrome)|Opera/
            .test(window.navigator.userAgent),
        previewMaxWidth: 100,
        previewMaxHeight: 100,
        previewCrop: false,
        limitConcurrentUploads: 3,
        stop: function (e, data) {

            $('#files').empty();
            $("#success-files .done-message").fadeIn(250);
            $("clear-queue-button").trigger("click");
            console.log('Uploads finished');
        }

    }).on('fileuploadadd', function (e, data) {
        $("#progress .progress-bar").css('width','0%');
        $("#success-files").fadeOut(100);
        $("#success-files .files").empty();

        data.context = $("#clear-queue-button").fadeIn(100)
          .click(function () {
               if (data.files.length > 0) {
                   $('#files').empty();
                   $("#upload-button").off("click");
                   //data.files.length = 0;
                   $("#clear-queue-button").fadeOut(100);
                   $("#upload-button").fadeOut(100);
               }
            });
        data.context = $("#upload-button").fadeIn(100)
                .click(function () {
                    //data.context = $('<p/>').text('Uploading...').replaceAll($(this));
                    $("#upload-button").fadeOut(100);
                    $("#clear-queue-button").fadeOut(100);
                    $("#success-files .files").empty();
                    $("#success-files .done-message").fadeOut(100);
                    data.submit();
                    //$(".upload-file-submit").click();

                });
        data.context = $('<div class="col-lg-2 col-md-2 col-xs-4 col-xs-6 thumb"/>').appendTo('#files');
        $.each(data.files, function (index, file) {
            var node = $('<p class="thumbnail thumbnail-centered" />');
                //.append($('<span/>').text(file.name));
            if (!index) {
                node
                    //.append(deleteButton.clone(true).data(data))
                    //.append(uploadButton.clone(true).data(data));

            }
            node.appendTo(data.context);
        });
    }).on('fileuploadprocessalways', function (e, data) {
        var index = data.index,
            file = data.files[index],
            node = $(data.context.children()[index]);
        if (file.preview) {
            node
                .prepend(file.preview);
        }
        if(file.name) {
            node
                .append('<br>')
                .append('<span class="text-small">' + file.name + '</span>');
        }
        if (file.error) {
            node
                .append('<br>')
                .append($('<span class="text-danger"/>').text(file.error));
        }
        if (index + 1 === data.files.length) {
            data.context.find('button')
                //.text(' ')
                .append('<span class="glyphicon glyphicon-remove"><span> ')
                .prop('disabled', !!data.files.error);
        }
    }).on('fileuploadprogressall', function (e, data) {
        var progress = parseInt(data.loaded / data.total * 100, 10);
        $('#progress .progress-bar').css(
            'width',
                progress + '%'
        );
    }).on('fileuploaddone', function (e, data) {
        $("#success-files").fadeIn(100);
        $.each(data.result.files, function (index, file) {

            if(file.name){
                var fileName = $('<li>').text(file.name).appendTo('#success-files .files');
            }
            if (file.url){
                var link = $('<a>')
                    .attr('target', '_blank')
                    .prop('href', file.url);
                $(data.context.children()[index])
                    .wrap(link);
            } else if (file.error){
                var error = $('<span class="text-danger"/>').text(file.error);
                $(data.context.children()[index])
                    .append('<br>')
                    .append(error);
            }
            if(file.action == "delete"){
                data.context.remove();
            }
            // Add to main image list, with a new icon
            if(file.objectId){
                $("#images-grid").prepend(imgTemplate(file, true));
            }
        });
        // Clear the upload events
        $("#upload-button").off("click");

    }).on('fileuploadfail', function (e, data) {
        $.each(data.files, function (index) {
            var error = $('<span class="text-danger"/>').text('File upload failed.');
            $(data.context.children()[index])
                .append('<br>')
                .append(error);
        });
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');

});


    // Basic
//    $('#fileupload').fileupload({
//        dataType: 'json',
//        add: function (e, data) {
//            $.each(data.files, function (index, file) {
//                $('<p/>').text(file.name).appendTo(document.body);
//            });
//            data.context = $("#upload-button").fadeIn(100)
//                .click(function () {
//                    data.context = $('<p/>').text('Uploading...').replaceAll($(this));
//                    data.submit();
//                });
//        },
//        progressall: function (e, data) {
//            var progress = parseInt(data.loaded / data.total * 100, 10);
//            $('#progress .bar').css(
//                'width',
//                    progress + '%'
//            );
//        },
//        done: function (e, data) {
//            $.each(data.result.files, function (index, file) {
//                $('<p/>').text(file.name).appendTo(document.body);
//            });
//            data.context.text('Upload finished.');
//        }
//    });

    // Initialize the jQuery File Upload widget:
    //$('#fileupload').fileupload({
        // Uncomment the following to send cross-domain cookies:
        //xhrFields: {withCredentials: true},
        //url: 'server/php/'
    //});

//    $('#fileupload').fileupload({
//        autoUpload: true,
//        dataType: 'json',
//        done: function (e, data) {
//            $.each(data.result.files, function (index, file) {
//                $('<p/>').text(file.name).appendTo('#files');
//            });
//        },
//        progressall: function (e, data) {
//            var progress = parseInt(data.loaded / data.total * 100, 10);
//            $('#progress .progress-bar').css(
//                'width',
//                    progress + '%'
//            );
//        }
//    }).prop('disabled', !$.support.fileInput)
//        .parent().addClass($.support.fileInput ? undefined : 'disabled');


    // Enable iframe cross-domain access via redirect option:
//    $('#fileupload').fileupload(
//        'option',
//        'redirect',
//        window.location.href.replace(
//            /\/[^\/]*$/,
//            '/cors/result.html?%s'
//        )
//    );
//
//    if (window.location.hostname === 'blueimp.github.io') {
//        // Demo settings:
//        $('#fileupload').fileupload('option', {
//            url: '//jquery-file-upload.appspot.com/',
//            // Enable image resizing, except for Android and Opera,
//            // which actually support image resizing, but fail to
//            // send Blob objects via XHR requests:
//            disableImageResize: /Android(?!.*Chrome)|Opera/
//                .test(window.navigator.userAgent),
//            maxFileSize: 5000000,
//            acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i
//        });
//        // Upload server status check for browsers with CORS support:
//        if ($.support.cors) {
//            $.ajax({
//                url: '//jquery-file-upload.appspot.com/',
//                type: 'HEAD'
//            }).fail(function () {
//                $('<div class="alert alert-danger"/>')
//                    .text('Upload server currently unavailable - ' +
//                            new Date())
//                    .appendTo('#fileupload');
//            });
//        }
//    } else {
//        // Load existing files:
//        $('#fileupload').addClass('fileupload-processing');
//        $.ajax({
//            // Uncomment the following to send cross-domain cookies:
//            //xhrFields: {withCredentials: true},
//            url: $('#fileupload').fileupload('option', 'url'),
//            dataType: 'json',
//            context: $('#fileupload')[0]
//        }).always(function () {
//            $(this).removeClass('fileupload-processing');
//        }).done(function (result) {
//            $(this).fileupload('option', 'done')
//                .call(this, $.Event('done'), {result: result});
//        });
//    }

