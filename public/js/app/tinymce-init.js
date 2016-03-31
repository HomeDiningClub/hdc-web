$(document).ready(function () {

    // TinyMCE
    tinymce.init({
        mode : "specific_textareas",
        editor_selector : "richtext",
        theme : "modern",
        skin: "lightgray",
        statusbar : true,
        height: 200,
        content_css : "/assets/stylesheets/richtext.min.css",
        plugins: [
            "advlist autolink link image lists charmap print preview hr anchor pagebreak spellchecker",
            "searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking",
            "table contextmenu directionality emoticons template paste textcolor"
        ],
        toolbar: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | print preview media fullpage | forecolor backcolor emoticons"
    });

    tinymce.init({
        mode : "specific_textareas",
        editor_selector : "richtextuser",
        theme : "modern",
        skin: "light",
        content_css : "/assets/stylesheets/richtext.min.css",
        menubar: false,
        plugins: [ "paste" ],
        paste_as_text: true,
        extended_valid_elements: "b,i,em,strong",
        statusbar : false,
        toolbar: "bold italic"
    });

    tinymce.init({
        mode : "specific_textareas",
        editor_selector : "richtext-extra",
        theme : "modern",
        skin: "light",
        height: 200,
        content_css : "/assets/stylesheets/richtext.min.css",
        menubar: false,
        plugins: [ "paste" ],
        paste_as_text: true,
        extended_valid_elements: "b,i,em,strong,li,ul,ol",
        statusbar : false,
        toolbar: "bullist numlist | bold italic | undo redo"
    });

});