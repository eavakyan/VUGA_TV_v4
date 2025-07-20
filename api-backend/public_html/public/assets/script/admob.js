$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".admobSideA").addClass("activeLi");

       
    $(document).on("submit", "#admobAndroidForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#admobAndroidForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}admobAndroid`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $(document).on("submit", "#admobiOSForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#admobiOSForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}admobiOS`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

});
