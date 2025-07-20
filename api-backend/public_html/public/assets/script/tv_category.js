$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".liveTvCategorySideA").addClass("activeLi");

    $("#tvCategoryTable").dataTable({
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
            url: `${domainUrl}fetchTvCategoryList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addTvCategoryForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addTvCategoryForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addTvCategory`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#tvCategoryTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#addTvCategoryModal").modal("hide");
                    }
                },
            });
        });
    });

    $(document).on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var title = $(this).data("title");
        var image = $(this).data("image");

        $("#tv_category_id").val(id);
        $("#edit_title").val(title);

        if (image) {
            $("#edit_image").attr("src", `${image}`);
        } else {
            $("#edit_image").attr("src", "assets/img/placeholder-image.png");
        }

        $("#editTvCategoryModal").modal("show");
    });

    $(document).on("submit", "#editTvCategoryForm", function (e) {
        e.preventDefault();
        var id = $("#tv_category_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editTvCategoryForm")[0]);
            editformData.append("tv_category_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateTvCategory`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#tvCategoryTable").DataTable().ajax.reload(null, false);
                        $("#editTvCategoryModal").modal("hide");
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
                        url: `${domainUrl}deleteTvCategory`,
                        dataType: "json",
                        data: {
                            tv_category_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#tvCategoryTable").DataTable().ajax.reload(null, false);
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
