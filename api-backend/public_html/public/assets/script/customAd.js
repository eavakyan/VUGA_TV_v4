$(document).ready(function () {
    // Remove and add classes
    $(".sideBarli").removeClass("activeLi");
    $(".customAdSideA").addClass("activeLi");

    var id = $("#customAd_id").val();

    $("#customAdTable").dataTable({
        autoWidth: false,
        processing: true,
        serverSide: true,
        serverMethod: "post",
        aaSorting: [[0, "desc"]],
        language: {
            paginate: {
                next: '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1"><polyline points="9 18 15 12 9 6"></polyline></svg>',
                previous:
                    '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1"><polyline points="15 18 9 12 15 6"></polyline></svg>',
            },
        },
        columnDefs: [
            {
                targets: "_all",
                orderable: false,
            },
        ],
        ajax: {
            url: `${domainUrl}fetchCustomAdList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("click", ".customAdOn", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            $.ajax({
                type: "POST",
                url: `${domainUrl}customAdOn`,
                dataType: "json",
                data: {
                    ad_id: id,
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdTable").DataTable().ajax.reload(null, false);
                    } else {
                       iziToast.show({
                           title: "Oops",
                           message: "Custom Ad cannot be turned on without any sources.",
                           color: "red",
                           position: "topRight",
                           transitionIn: "fadeInDown",
                           transitionOut: "fadeOutUp",
                           timeout: 4000,
                           animateInside: false,
                           iconUrl: `${domainUrl}assets/img/x.svg`,
                       });
                    }
                },
            });
        });
    });

    $(document).on("click", ".customAdOff", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            $.ajax({
                type: "POST",
                url: `${domainUrl}customAdOff`,
                dataType: "json",
                data: {
                    ad_id: id,
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdTable")
                            .DataTable()
                            .ajax.reload(null, false);
                    }
                },
            });
        });
    });

    $(document).on("submit", "#addCustomAdForm", function (e) {
        e.preventDefault();
        const androidLinkInput = document.getElementById("android_link");
        const iosLinkInput = document.getElementById("ios_link");

        if (!androidLinkInput.value && !iosLinkInput.value) {
            alert("Please provide at least one link (Android or iOS).");
            $(".saveButton").removeClass("spinning disabled");
            return;
        }

        checkUserType(function (e) {
            let formData = new FormData($("#addCustomAdForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addCustomAd`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#addCustomAdModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#customAdTable").on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var title = $(this).data("title");
        var brand_name = $(this).data("brand_name");
        var brand_logo = $(this).data("brand_logo");
        var button_text = $(this).data("button_text");
        var is_android = $(this).data("is_android");
        var android_link = $(this).data("android_link");
        var is_ios = $(this).data("is_ios");
        var ios_link = $(this).data("ios_link");
        var start_date = $(this).data("start_date");
        var end_date = $(this).data("end_date");

        $("#custom_ad_id").val(id);
        $("#edit_title").val(title);
        $("#edit_brand_name").val(brand_name);
        $("#edit_brand_icon").attr("src", brand_logo);
        $("#edit_button_text").val(button_text);
        $("#edit_start_date").val(start_date);
        $("#edit_end_date").val(end_date);

        if (is_android) {
            $("#edit_is_android_checkbox").prop("checked", true).change();
            $("#edit_android_link").val(android_link).prop("disabled", false);
            $("#edit_is_android_hidden").val("1");
        } else {
            $("#edit_is_android_checkbox").prop("checked", false).change();
            $("#edit_android_link").val("").prop("disabled", true);
            $("#edit_is_android_hidden").val("0");
        }

        if (is_ios) {
            $("#edit_is_ios_checkbox").prop("checked", true).change();
            $("#edit_ios_link").val(ios_link).prop("disabled", false);
            $("#edit_is_ios_hidden").val("1");
        } else {
            $("#edit_is_ios_checkbox").prop("checked", false).change();
            $("#edit_ios_link").val("").prop("disabled", true);
            $("#edit_is_ios_hidden").val("0");
        }

        $("#editCustomAdModal").modal("show");
    });

    $(document).on("click", ".editCustomAdModal", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var title = $(this).data("title");
        var brand_name = $(this).data("brand_name");
        var brand_logo = $(this).data("brand_logo");
        var button_text = $(this).data("button_text");
        var is_android = $(this).data("is_android");
        var android_link = $(this).data("android_link");
        var is_ios = $(this).data("is_ios");
        var ios_link = $(this).data("ios_link");
        var start_date = $(this).data("start_date");
        var end_date = $(this).data("end_date");

        $("#custom_ad_id").val(id);
        $("#edit_title").val(title);
        $("#edit_brand_name").val(brand_name);
        $("#edit_brand_icon").attr("src", brand_logo);
        $("#edit_button_text").val(button_text);
        $("#edit_start_date").val(start_date);
        $("#edit_end_date").val(end_date);

        if (is_android) {
            $("#edit_is_android_checkbox").prop("checked", true).change();
            $("#edit_android_link").val(android_link).prop("disabled", false);
            $("#edit_is_android_hidden").val("1");
        } else {
            $("#edit_is_android_checkbox").prop("checked", false).change();
            $("#edit_android_link").val("").prop("disabled", true);
            $("#edit_is_android_hidden").val("0");
        }

        if (is_ios) {
            $("#edit_is_ios_checkbox").prop("checked", true).change();
            $("#edit_ios_link").val(ios_link).prop("disabled", false);
            $("#edit_is_ios_hidden").val("1");
        } else {
            $("#edit_is_ios_checkbox").prop("checked", false).change();
            $("#edit_ios_link").val("").prop("disabled", true);
            $("#edit_is_ios_hidden").val("0");
        }

        $("#editCustomAdModal").modal("show");
    });

    $(document).on("submit", "#editCustomAdForm", function (e) {
        e.preventDefault();
        var id = $("#custom_ad_id").val();

        // Get toggle and link elements
        const androidCheckbox = document.getElementById("edit_is_android_checkbox");
        const iosCheckbox = document.getElementById("edit_is_ios_checkbox");
        const androidLinkInput = document.getElementById("edit_android_link");
        const iosLinkInput = document.getElementById("edit_ios_link");

        // Check if at least one toggle is enabled and corresponding link is provided
        if (
            (!androidCheckbox.checked && !iosCheckbox.checked) ||
            (androidCheckbox.checked && !androidLinkInput.value) ||
            (iosCheckbox.checked && !iosLinkInput.value)
        ) {
            alert(
                "Please enable at least one platform and provide the corresponding link."
            );
            $(".saveButton").removeClass("spinning disabled");
            return;
        }

        checkUserType(function (e) {
            let formData = new FormData($("#editCustomAdForm")[0]);
            formData.append("custom_ad_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateCustomAd`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdTable").DataTable().ajax.reload(null, false);
                        $("#detail_content").load(location.href + " #detail_content>*","");
                        $("#editCustomAdModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#customAdTable").on("click", ".delete", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            swal({
                title: "Are you sure?",
                icon: "error",
                buttons: true,
                dangerMode: true,
                buttons: ["Cancel", "Yes"],
            }).then((deleteValue) => {
                if (deleteValue) {
                    $.ajax({
                        type: "POST",
                        url: `${domainUrl}deleteCustomAd`,
                        dataType: "json",
                        data: {
                            custom_ad_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#customAdTable")
                                    .DataTable()
                                    .ajax.reload(null, false);
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            });
        });
    });

    $("#customAdImageSourceTable").dataTable({
        autoWidth: false,
        processing: true,
        serverSide: true,
        serverMethod: "post",
        aaSorting: [[0, "desc"]],
        language: {
            paginate: {
                next: '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1"><polyline points="9 18 15 12 9 6"></polyline></svg>',
                previous:
                    '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1"><polyline points="15 18 9 12 15 6"></polyline></svg>',
            },
        },
        columnDefs: [
            {
                targets: "_all",
                orderable: false,
            },
        ],
        ajax: {
            url: `${domainUrl}fetchCustomAdImageSourceList`,
            data: {
                custom_ad_id: id,
            },
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addCustomAdSourceImageForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addCustomAdSourceImageForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addCustomAdSourceImage`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdImageSourceTable").DataTable().ajax.reload(null, false);
                        $("#addCustomAdSourceImageModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#customAdImageSourceTable").on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var content = $(this).data("content");
        var headline = $(this).data("headline");
        var description = $(this).data("description");
        var show_time = $(this).data("show_time");
        
        $("#custom_ad_source_id").val(id);
        $("#edit_headline").val(headline);

         if (content) {
             $("#edit_content").attr("src", `${content}`);
         } else {
             $("#edit_content").attr("src", "assets/img/placeholder-image.png");
         }

        $("#edit_description").val(description);
        $("#edit_show_time").val(show_time);

        $("#editCustomAdSourceImageModal").modal("show");
    });

    $(document).on("submit", "#editCustomAdSourceImageForm", function (e) {
        e.preventDefault();
        var id = $("#custom_ad_source_id").val();
        checkUserType(function (e) {
            let formData = new FormData($("#editCustomAdSourceImageForm")[0]);
            formData.append("custom_ad_source_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateCustomAdSource`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdImageSourceTable").DataTable().ajax.reload(null, false);
                        $("#customAdVideoSourceTable").DataTable().ajax.reload(null, false);
                        $("#editCustomAdSourceImageModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#customAdImageSourceTable").on("click", ".delete", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            swal({
                title: "Are you sure?",
                icon: "error",
                buttons: true,
                dangerMode: true,
                buttons: ["Cancel", "Yes"],
            }).then((deleteValue) => {
                if (deleteValue) {
                    $.ajax({
                        type: "POST",
                        url: `${domainUrl}deleteCustomAdSource`,
                        dataType: "json",
                        data: {
                            custom_ad_source_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#customAdImageSourceTable")
                                    .DataTable()
                                    .ajax.reload(null, false);
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            });
        });
    });

    $("#customAdVideoSourceTable").dataTable({
        autoWidth: false,
        processing: true,
        serverSide: true,
        serverMethod: "post",
        aaSorting: [[0, "desc"]],
        language: {
            paginate: {
                next: '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1"><polyline points="9 18 15 12 9 6"></polyline></svg>',
                previous:
                    '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1"><polyline points="15 18 9 12 15 6"></polyline></svg>',
            },
        },
        columnDefs: [
            {
                targets: "_all",
                orderable: false,
            },
        ],
        ajax: {
            url: `${domainUrl}fetchCustomAdVideoSourceList`,
            data: {
                custom_ad_id: id,
            },
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addCustomAdSourceVideoForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addCustomAdSourceVideoForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addCustomAdSourceVideo`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdVideoSourceTable").DataTable().ajax.reload(null, false);
                        $("#addCustomAdSourceVideoModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#customAdVideoSourceTable").on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        // var content = $(this).data("content");
        var headline = $(this).data("headline");
        var description = $(this).data("description");
        var is_skippable = $(this).data("is_skippable");

        $("#custom_ad_source_video_id").val(id);
        // $("#video_content").val(content);
        
        $("#edit_headline_video").val(headline);
        $("#edit_descriptioon_video").val(description);
        $("#edit_is_skippable").val(is_skippable).selectric("refresh");

        $("#editCustomAdSourceVideoModal").modal("show");
    });

    $(document).on("submit", "#editCustomAdSourceVideoForm", function (e) {
        e.preventDefault();
        var id = $("#custom_ad_source_video_id").val();
        checkUserType(function (e) {
            let formData = new FormData($("#editCustomAdSourceVideoForm")[0]);
            formData.append("custom_ad_source_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateCustomAdSource`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#customAdVideoSourceTable").DataTable().ajax.reload(null, false);
                        $("#customAdImageSourceTable").DataTable().ajax.reload(null, false);
                        $("#editCustomAdSourceVideoModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#customAdVideoSourceTable").on("click", ".delete", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            swal({
                title: "Are you sure?",
                icon: "error",
                buttons: true,
                dangerMode: true,
                buttons: ["Cancel", "Yes"],
            }).then((deleteValue) => {
                if (deleteValue) {
                    $.ajax({
                        type: "POST",
                        url: `${domainUrl}deleteCustomAdSource`,
                        dataType: "json",
                        data: {
                            custom_ad_source_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#customAdImageSourceTable").DataTable().ajax.reload(null, false);
                                $("#customAdVideoSourceTable").DataTable().ajax.reload(null, false);
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            });
        });
    });

    $(document).on("click", ".deleteCustomAd", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            swal({
                title: "Are you sure?",
                icon: "error",
                buttons: true,
                dangerMode: true,
                buttons: ["Cancel", "Yes"],
            }).then((deleteValue) => {
                if (deleteValue) {
                    $.ajax({
                        type: "POST",
                        url: `${domainUrl}deleteCustomAd`,
                        dataType: "json",
                        data: {
                            custom_ad_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                // showSuccessToast();
                                // $("#customAdTable").DataTable().ajax.reload(null, false);
                                window.location.href = `${domainUrl}customAds`;
                                
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            });
        });
    });



});

 