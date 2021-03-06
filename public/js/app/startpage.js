$(document).ready(function() {

    var wizardOptions = {
        tabClass: 'nav nav-pills nav-justified',
        onTabShow: function(tab, navigation, index) {

            var $current = index + 1;
            var $total = navigation.find('li').length;

            if($current < $total) {
                navigation.prevObject.find('.pager .next').show();
            } else {
                navigation.prevObject.find('.pager .next').hide();
            }

            if($current > 1) {
                navigation.prevObject.find('.pager .previous').show();
            } else {
                navigation.prevObject.find('.pager .previous').hide();
            }

        }
    };

    $('#guest-wizard').bootstrapWizard(wizardOptions);
    $('#host-wizard').bootstrapWizard(wizardOptions);
});
