$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".contentSideA").addClass("activeLi");

    function handleContentTypeFields(type) {
        if (type == "2") {
            $("#edit_duration").closest(".col").hide();
            $("#edit_duration").prop("required", false);

            $("#edit_trailer_url").closest(".col").hide();
            $("#edit_trailer_url").prop("required", false);
        } else {
            $("#edit_duration").closest(".col").show();
            $("#edit_duration").prop("required", true);

            $("#edit_trailer_url").closest(".col").show();
            $("#edit_trailer_url").prop("required", true);
        }
    }

    $("#moviesTable").dataTable({
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
            url: `${domainUrl}fetchMoviesList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("click", ".unfeatured", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            $.ajax({
                type: "POST",
                url: `${domainUrl}unfeatured`,
                dataType: "json",
                data: {
                    content_id: id,
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#moviesTable").DataTable().ajax.reload(null, false);
                        $("#seriesTable").DataTable().ajax.reload(null, false);
                    }
                },
            });
        });
    });

    $(document).on("click", ".featured", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            $.ajax({
                type: "POST",
                url: `${domainUrl}featured`,
                dataType: "json",
                data: {
                    content_id: id,
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#moviesTable").DataTable().ajax.reload(null, false);
                        $("#seriesTable").DataTable().ajax.reload(null, false);
                    }
                },
            });
        });
    });

    $(document).on("click", ".hideContent", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            $.ajax({
                type: "POST",
                url: `${domainUrl}hideContent`,
                dataType: "json",
                data: {
                    content_id: id,
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#moviesTable").DataTable().ajax.reload(null, false);
                        $("#seriesTable").DataTable().ajax.reload(null, false);
                    }
                },
            });
        });
    });

    $(document).on("click", ".showContent", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            $.ajax({
                type: "POST",
                url: `${domainUrl}showContent`,
                dataType: "json",
                data: {
                    content_id: id,
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#moviesTable").DataTable().ajax.reload(null, false);
                        $("#seriesTable").DataTable().ajax.reload(null, false);
                    }
                },
            });
        });
    });

    $(document).on("submit", "#addNewContentForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addNewContentForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addNewContent`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#moviesTable").DataTable().ajax.reload(null, false);
                        $("#seriesTable").DataTable().ajax.reload(null, false);
                        $("#addContentModal").modal("hide");
                        $(".selectric").selectric("destroy").selectric();
                    }
                },
            });
        });
    });

    $(document).on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var type = $(this).data("type");
        var title = $(this).data("title");
        var description = $(this).data("description");
        var duration = $(this).data("duration");
        var release_year = $(this).data("release_year");
        var ratings = $(this).data("ratings");
        var language_id = $(this).data("language_id");
        var genre_ids = $(this).data("genre_ids");
        var trailer_url = $(this).data("trailer_url");
        var vposter = $(this).data("vposter");
        var hposter = $(this).data("hposter");

        $("#content_id").val(id);
        $("#edit_content_type").val(type).selectric("refresh");
        $("#edit_title").val(title);
        $("#edit_description").val(description);
        $("#edit_duration").val(duration);
        $("#edit_release_year").val(release_year);
        $("#edit_ratings").val(ratings);
        $("#edit_language_id").val(language_id).selectric("refresh");

        var genreArray = [];
        if (typeof genre_ids === "string") {
            genreArray = genre_ids.split(",").map(function (item) {
                return item.trim();
            });
        } else if (Array.isArray(genre_ids)) {
            genreArray = genre_ids;
        } else if (typeof genre_ids === "number") {
            genreArray = [genre_ids.toString()];
        }

        $("#edit_selectGenre").val(genreArray).selectric("refresh");

        $("#edit_trailer_url").val(trailer_url);
        $("#edit_vertical_poster").attr("src", `${vposter}`);
        $("#edit_horizontalPosterImg").attr("src", `${hposter}`);

        handleContentTypeFields(type);

        $("#editContentModal").modal("show");
    });

    $(document).on("submit", "#editContentForm", function (e) {
        e.preventDefault();
        var id = $("#content_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editContentForm")[0]);
            editformData.append("content_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateContent`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#moviesTable").DataTable().ajax.reload(null, false);
                        $("#seriesTable").DataTable().ajax.reload(null, false);
                        $("#editContentModal").modal("hide");
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
                        url: `${domainUrl}deleteContent`,
                        dataType: "json",
                        data: {
                            content_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#moviesTable").DataTable().ajax.reload(null, false);
                                $("#seriesTable").DataTable().ajax.reload(null, false);
                            } else {
                                somethingWentWrongToast();
                                console.log(response.data);
                            }
                        },
                    });
                }
            });
        });
    });

    // seriesTable
    $("#seriesTable").dataTable({
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
            url: `${domainUrl}fetchSeriesList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    // Get the URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get("tab");

    // Check if the tab parameter is set to 'series'
    if (tab === "series") {
        $("#nav-series-tab").tab("show"); // Activate the series tab
    } else {
        $("#nav-movies-tab").tab("show"); // Activate the movies tab by default
    }

    $(document).on("click", ".notifyContent", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        var title = $(this).data("title");
        var description = $(this).data("description");
        $(this).addClass("spinning disabled");

        checkUserType(function (e) {
            $.ajax({
                type: "POST",
                url: `${domainUrl}notifyContent`,
                dataType: "json",
                data: {
                    content_id: id,
                    title: title,
                    description: description,
                },
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $(".notifyContent").removeClass("spinning disabled");
                    }
                },
            });
        });
    });

    
});
