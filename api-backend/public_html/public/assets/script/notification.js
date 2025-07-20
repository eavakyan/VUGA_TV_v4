$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".notificationSideA").addClass("activeLi");

    $("#notificationTable").dataTable({
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
            url: `${domainUrl}notificationList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addNotificationForm", function (e) {
        e.preventDefault();
       checkUserType(function (e) {
            let formData = new FormData($("#addNotificationForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addNotification`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#notificationTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#addNotificationModal").modal("hide");
                    }
                },
            });
        });
    });

    $(document).on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var title = $(this).data("title");
        var description = $(this).data("description");

        $("#notification_id").val(id);
        $("#edit_title").val(title);
        $("#edit_description").val(description);

        $("#editNotificationModal").modal("show");
    });

    $(document).on("submit", "#editNotificationForm", function (e) {
        e.preventDefault();
        var id = $("#notification_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editNotificationForm")[0]);
            editformData.append("notification_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateNotification`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#notificationTable").DataTable().ajax.reload(null, false);
                        $("#editNotificationModal").modal("hide");
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $(document).on("click", ".repeat", function (e) {
        e.preventDefault();

        $(this).addClass("spinning disabled");
        
        var title = $(this).data("title");
        var description = $(this).data("description");

        checkUserType(function (e) {
            let formData = new FormData();
            formData.append("title", title);
            formData.append("description", description);

            $.ajax({
                type: "POST",
                url: `${domainUrl}repeatNotification`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $(".repeat").removeClass("spinning disabled");
                    } else {
                        somethingWentWrongToast();
                    }
                    $(this).removeClass("spinning disabled");
                },
                error: function () {
                    somethingWentWrongToast();
                    $(this).removeClass("spinning disabled");
                },
            });
        });
    });

    $(document).on("click", ".delete", function (e) {
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
                        url: `${domainUrl}deleteNotification`,
                        dataType: "json",
                        data: {
                            notification_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#notificationTable").DataTable().ajax.reload(null, false);
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
