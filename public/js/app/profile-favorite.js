$(document).ready(function(){

    var oid = $("#afrf-wrapper").data("owner-id");

    if(oid.length > 0){
        $("#rf").click(function(){

            $.get("/profile/favorite/remove?userCredentialObjectId=" + oid + "&tid=" + new Date().toString(),function(data,status){
                $( "#af" ).show();
                $( "#rf" ).hide();
                alert('Favorit borttagen!');
            });
        });


        $("#af").click(function(){
            //  var oid = $(this).data("owner-id");
            $.get("/profile/favorite/add?userCredentialObjectId=" + oid + "&tid=" + new Date().toString(),function(data,status){
                $( "#af" ).hide();
                $( "#rf" ).show();
                alert('Favorit tillagd!')
            });
        });

        $.get("/profile/favorite/isFav?userCredentialObjectId=" + oid + "&tid=" + new Date().toString(),function(data,status){
            if(data =="YES") {
                $("#af").hide();
                $( "#rf" ).show();
            } else if(data == "NO") {
                $("#af").show();
                $("#rf").hide();

            } else {
                $("#af,#rf,#afrf-wrapper").hide();
            }
        });
    }

});