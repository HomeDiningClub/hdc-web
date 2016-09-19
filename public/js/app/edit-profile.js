$(document).ready(function () {

    var editProfileForm = $('form');
    var navbar = $('.header-navbar');

// listen for `invalid` events on all form inputs
    editProfileForm.find(':input').on('invalid', function (event) {
        var input = $(this);

        // the first invalid element in the form
        var first = editProfileForm.find(':invalid').first();

        // only handle if this is the first invalid input
        if (input[0] === first[0]) {

            // Mark the closest tab
            var id = $(this).parents(".tab-pane").attr("id");
            $(".nav.nav-tabs li a[href=#" + id + "]").trigger('click').closest('li').addClass('tab-has-error');

            // height of the nav bar plus some padding
            var navbarHeight = navbar.height() + 50;

            // the position to scroll to (accounting for the navbar)
            var elementOffset = input.offset().top - navbarHeight;

            // the current scroll position (accounting for the navbar)
            var pageOffset = window.pageYOffset - navbarHeight;

            // don't scroll if the element is already in view
            if (elementOffset > pageOffset && elementOffset < pageOffset + window.innerHeight) {
                return true
            }

            // note: avoid using animate, as it prevents the validation message displaying correctly
            $('html,body').scrollTop(elementOffset)
        }
    });

});