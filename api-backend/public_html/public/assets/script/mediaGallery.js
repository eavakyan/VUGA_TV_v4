$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".mediaGallerySideA").addClass("activeLi");

    $("#mediaGalleryTable").dataTable({
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
            url: `${domainUrl}mediaGalleryList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addMediaForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addMediaForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addMedia`,
                data: formData,
                contentType: false,
                processData: false,
                xhr: function () {
                    var xhr = new window.XMLHttpRequest();
                    xhr.upload.addEventListener(
                        "progress",
                        function (e) {
                            if (e.lengthComputable) {
                                var percentLoaded = Math.round(
                                    (e.loaded / e.total) * 100
                                );
                                $("#progress-bar")
                                    .css("width", percentLoaded + "%")
                                    .attr("aria-valuenow", percentLoaded)
                                    .text(percentLoaded + "%");
                                $("#progress").show();
                            }
                        },
                        false
                    );
                    return xhr;
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#mediaGalleryTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#addMediaModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#file").change(function () {
        $("#progress-bar")
            .css("width", "0%")
            .attr("aria-valuenow", 0)
            .text("0%");
        $("#progress").hide();
    });


    $(document).on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var title = $(this).data("title");

        $("#media_id").val(id);
        $("#edit_title").val(title);

        $("#editMediaModal").modal("show");
    });

    $(document).on("submit", "#editMediaForm", function (e) {
        e.preventDefault();
        var id = $("#media_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editMediaForm")[0]);
            editformData.append("media_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateMedia`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#mediaGalleryTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#editMediaModal").modal("hide");
                    } else {
                        somethingWentWrongToast();
                    }
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
                        url: `${domainUrl}deleteMedia`,
                        dataType: "json",
                        data: {
                            media_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#mediaGalleryTable").DataTable().ajax.reload(null, false);
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
