$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".settingSideA").addClass("activeLi");


    const eye = document.querySelector(".feather-eye");
    const eyeoff = document.querySelector(".feather-eye-off");
    const passwordField = document.querySelector("input[type=password]");

    eye.addEventListener("click", () => {
        eye.style.display = "none";
        eyeoff.style.display = "block";
        passwordField.type = "text";
    });

    eyeoff.addEventListener("click", () => {
        eyeoff.style.display = "none";
        eye.style.display = "block";
        passwordField.type = "password";
    });

    const eye1 = document.querySelector(".eye1");
    const eyeoff1 = document.querySelector(".eye-off1");
    const passwordField1 = document.querySelector(
        "input#newPassword[type=password]"
    );

    eye1.addEventListener("click", () => {
        eye1.style.display = "none";
        eyeoff1.style.display = "block";
        passwordField1.type = "text";
    });

    eyeoff1.addEventListener("click", () => {
        eyeoff1.style.display = "none";
        eye1.style.display = "block";
        passwordField1.type = "password";
    });
    
    $(document).on("submit", "#settingsForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#settingsForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}saveSettings`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                    showSuccessToast();
                    $("#reloadContent").load(location.href + " #reloadContent>*","");
                    }
                },
            });
        });
    });

    $(document).on("submit", "#storageSettingForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#storageSettingForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}saveSettings`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#reloadContent").load(location.href + " #reloadContent>*", "");
                    }
                },
            });
        });
    });

    
    $(document).on("submit", "#changePasswordForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#changePasswordForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}changePassword`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#changePasswordForm")[0].reset();
                    } else if(response.status == false) {
                        iziToast.show({
                            title: "Oops",
                            message: response.message,
                            color: "red",
                            position: "topRight",
                            transitionIn: "fadeInDown",
                            transitionOut: "fadeOutUp",
                            timeout: 3000,
                            animateInside: false,
                            iconUrl: `${domainUrl}assets/img/x.svg`,
                        });
                    }
                },
            });
        });
    });

    $(".enableDisableLiveTvTab").change(function () {
        let isChecked = $(this).prop("checked") ? 1 : 0;

        $.ajax({
            type: "POST",
            url: `${domainUrl}saveSettings`,
            data: {
                is_live_tv_enable: isChecked,
            },
            success: function (response) {
                if (response.status) {
                    showSuccessToast();
                } else {
                    somethingWentWrongToast();
                }
            },
            error: function () {
                somethingWentWrongToast();
            },
        });
    });


    $(".is_admob_android").change(function () {
        let isChecked = $(this).prop("checked") ? 1 : 0;

        $.ajax({
            type: "POST",
            url: `${domainUrl}saveSettings`,
            data: {
                is_admob_android: isChecked,
            },
            success: function (response) {
                if (response.status) {
                    showSuccessToast();
                } else {
                    somethingWentWrongToast();
                }
            },
            error: function () {
                somethingWentWrongToast();
            },
        });
    });


    $(".is_admob_ios").change(function () {
        let isChecked = $(this).prop("checked") ? 1 : 0;

        $.ajax({
            type: "POST",
            url: `${domainUrl}saveSettings`,
            data: {
                is_admob_ios: isChecked,
            },
            success: function (response) {
                if (response.status) {
                    showSuccessToast();
                } else {
                    somethingWentWrongToast();
                }
            },
            error: function () {
                somethingWentWrongToast();
            },
        });
    });

    $(".is_custom_android").change(function () {
        let isChecked = $(this).prop("checked") ? 1 : 0;

        $.ajax({
            type: "POST",
            url: `${domainUrl}saveSettings`,
            data: {
                is_custom_android: isChecked,
            },
            success: function (response) {
                if (response.status) {
                    showSuccessToast();
                } else {
                    somethingWentWrongToast();
                }
            },
            error: function () {
                somethingWentWrongToast();
            },
        });
    });
    $(".is_custom_ios").change(function () {
        let isChecked = $(this).prop("checked") ? 1 : 0;

        $.ajax({
            type: "POST",
            url: `${domainUrl}saveSettings`,
            data: {
                is_custom_ios: isChecked,
            },
            success: function (response) {
                if (response.status) {
                    showSuccessToast();
                } else {
                    somethingWentWrongToast();
                }
            },
            error: function () {
                somethingWentWrongToast();
            },
        });
    });

    

});
