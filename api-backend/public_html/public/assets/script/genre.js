$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".genreSideA").addClass("activeLi");

    $("#genresTable").dataTable({
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
            url: `${domainUrl}genresList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addGenreForm", function (e) {
        e.preventDefault();
        checkUserType(function () {
            let formData = new FormData($("#addGenreForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addGenre`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#genresTable").DataTable().ajax.reload(null, false);
                        $("#addGenreModal").modal("hide");
                    }
                },
            });
        });
    });

    
    $(document).on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var title = $(this).data("title");

        $("#genre_id").val(id);
        $("#edit_title").val(title);

        $("#editGenreModal").modal("show");
    });

    $(document).on("submit", "#editGenreForm", function (e) {
        e.preventDefault();
        var id = $("#genre_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editGenreForm")[0]);
            editformData.append("genre_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateGenre`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#genresTable").DataTable().ajax.reload(null, false);
                        $("#editGenreModal").modal("hide");
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
                            url: `${domainUrl}deleteGenre`,
                            dataType: "json",
                            data: {
                                genre_id: id,
                            },
                            success: function (response) {
                                if (response.status) {
                                    showSuccessToast();
                                    $("#genresTable")
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

});
