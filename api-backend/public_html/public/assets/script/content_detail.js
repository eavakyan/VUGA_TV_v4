$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".contentSideA").addClass("activeLi");
    
    $("#source_file").change(function () {
        var file = this.files[0];
        var progressBar = $("#progress-bar");
        var progressContainer = $("#progress");

        if (file) {
            var reader = new FileReader();

            reader.onprogress = function (e) {
                if (e.lengthComputable) {
                    var percentLoaded = Math.round((e.loaded / e.total) * 100);
                    progressBar.css("width", percentLoaded + "%");
                    progressBar.attr("aria-valuenow", percentLoaded);
                    progressBar.text(percentLoaded + "%");
                    progressContainer.show();
                }
            };

            reader.onload = function (e) {
                // You can optionally preview the file here if needed
            };

            reader.readAsDataURL(file);
        }
    });

    

    $("#edit_source_file").change(function () {
        var file = this.files[0];
        var progressBar = $("#edit-progress-bar");
        var progressContainer = $("#edit-progress");

        if (file) {
            var reader = new FileReader();

            reader.onprogress = function (e) {
                if (e.lengthComputable) {
                    var percentLoaded = Math.round((e.loaded / e.total) * 100);
                    progressBar.css("width", percentLoaded + "%");
                    progressBar.attr("aria-valuenow", percentLoaded);
                    progressBar.text(percentLoaded + "%");
                    progressContainer.show();
                }
            };

            reader.onload = function (e) {
                // You can optionally preview the file here if needed
            };

            reader.readAsDataURL(file);
        }
    });

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

    var id = $("#content_id1").val();
    var episode_id_1 = $("#episode_id_for_source_subtitle").val();

    // let season_id = $("#season_list").val();
    var currentSeasonId = $("#season_list").val();


    $(document).on("click", ".editContentModal", function (e) {
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
        $("#edit_content_title").val(title);
        $("#edit_content_description").val(description);
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
                        $("#detail_content").load(
                            location.href + " #detail_content>*",
                            ""
                        );
                        $("#editContentModal").modal("hide");
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $(document).on("click", ".deleteContent", function (e) {
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
                                window.location.href = `${domainUrl}contentList`;
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            });
        });
    });

    $(document).on("click", ".editEpisodeModal", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var number = $(this).data("number");
        var thumbnail = $(this).data("thumbnail");
        var title = $(this).data("title");
        var description = $(this).data("description");
        var duration = $(this).data("duration");
        var access_type = $(this).data("access_type");

        $("#edit_episode_id").val(id);
        $("#edit_number").val(number);
        $("#edit_episode_title").val(title);
        $("#edit_description").val(description);
        $("#edit_duration").val(duration);
        $("#edit_episode_access_type").val(access_type).selectric("refresh");

        $("#edit_thumbnailPoster").attr("src", `${thumbnail}`);

        $("#editEpisodeModal").modal("show");
    });

    $(document).on("click", ".deleteEpisode", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        swal({
            title: "Are you sure You want to delete!",
            icon: "warning",
            buttons: true,
            dangerMode: true,
        }).then((deleteValue) => {
            if (deleteValue) {
                if (deleteValue == true) {
                    $.ajax({
                        type: "POST",
                        url: `${domainUrl}deleteEpisode`,
                        data: {
                            episode_id: id,
                        },
                        dataType: "json",
                        success: function (response) {
                            if (response.status) {
                                window.location.href =
                                    `${domainUrl}series/` + response.content_id;
                            } else {
                                console.log(response.message);
                            }
                        },
                    });
                }
            }
        });
    });

    // sourceTable
    $("#sourceTable").dataTable({
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
            url: `${domainUrl}fetchSourceList`,
            data: {
                content_id: id,
            },
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addSourceForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addSourceForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addSource`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#sourceTable").DataTable().ajax.reload(null, false);
                        $("#addSourceModal").modal("hide");
                    }
                },
            });
        });
    });
    
    $("#sourceTable").on("click", ".edit", function (e) {
        e.preventDefault();

        const data = $(this).data();

        $("#edit_source_id").val($(this).attr("rel"));
        $("#edit_title").val(data.title);
        $("#edit_quality").val(data.quality);
        $("#edit_size").val(data.size);
        $("#edit_is_download").prop("checked", data.download == 1);
        $("#edit_is_download_hidden").val(data.download == 1 ? 1 : 0);
        $("#edit_type").val(data.type).selectric("refresh");
        $("#edit_media").val(data.source).selectric("refresh");

        const sourceURL = $(".sourceURL");
        const mediaGallery = $(".mediaGallery");
        const downloadableOrNot = $("#edit_downloadableOrNot");
        const sourceURLLabel = $("#edit_sourceURL_label");

        if (data.type == "7") {
            sourceURL.hide();
            mediaGallery.show();
        } else {
            mediaGallery.hide();
            sourceURL.show();
        }

        if (data.type == "1" || data.type == "2") {
            downloadableOrNot.hide();
        } else {
            downloadableOrNot.show();
        }

        sourceURLLabel.html(
            data.type == "1" ? "Source ID (GUMDnD*****)" : "Source URL"
        );
        $("#edit_sourceURL").val(data.source);
        $("#edit_access_type").val(data.accesstype).selectric("refresh");

        $("#editSourceModal").modal("show");
    });

    $(document).on("submit", "#editSourceForm", function (e) {
        e.preventDefault();
        var id = $("#edit_source_id").val();
       checkUserType(function (e) {
            let editformData = new FormData($("#editSourceForm")[0]);
            editformData.append("source_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateContentSource`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#sourceTable").DataTable().ajax.reload(null, false);
                        $("#editSourceModal").modal("hide");
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $("#sourceTable").on("click", ".delete", function (e) {
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
                        url: `${domainUrl}deleteSource`,
                        dataType: "json",
                        data: {
                            source_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#sourceTable")
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

    // castTable
    $("#castTable").dataTable({
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
            url: `${domainUrl}fetchCastList`,
            data: {
                content_id: id,
            },
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addCastForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addCastForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addCast`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#castTable").DataTable().ajax.reload(null, false);
                        $("#addCastModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#castTable").on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var actor_id = $(this).data("actor_id");
        var character_name = $(this).data("character_name");

        $("#edit_cast_id").val(id);
        $("#edit_actor_id").val(actor_id).selectric("refresh");
        $("#edit_character_name").val(character_name);

        $("#editCastModal").modal("show");
    });

    $(document).on("submit", "#editCastForm", function (e) {
        e.preventDefault();
        var id = $("#edit_cast_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editCastForm")[0]);
            editformData.append("cast_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateCast`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#castTable").DataTable().ajax.reload(null, false);
                        $("#editCastModal").modal("hide");
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $("#castTable").on("click", ".delete", function (e) {
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
                        url: `${domainUrl}deleteCast`,
                        dataType: "json",
                        data: {
                            cast_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#castTable")
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

    // subtitleTable
    $("#subtitleTable").dataTable({
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
            url: `${domainUrl}fetchSubtitleList`,
            data: {
                content_id: id,
            },
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addSubtitleForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addSubtitleForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addSubtitle`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#subtitleTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#addSubtitleModal").modal("hide");
                        $("#addSubtitleFOrm")[0].reset();
                    }
                },
            });
        });
    });

    $("#subtitleTable").on("click", ".delete", function (e) {
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
                        url: `${domainUrl}deleteSubtitle`,
                        dataType: "json",
                        data: {
                            subtitle_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#subtitleTable")
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

    // series
    $(document).on("submit", "#addSeasonForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addSeasonForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addSeason`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        // showSuccessToast();
                        // $("#addSeasonModal").modal("hide");
                        location.reload();
                    }
                },
            });
        });
    });

    $("#season_list").each(function () {
        if ($("option", this).length > 1) {
            $("#is_season").addClass("season_added");
        }
    });

    $("#season_list").change(function () {
        var season_id = $(this).val(); // Update the season_id dynamically
        var selectedOption = $(this).find("option:selected");
        var season_title = selectedOption.data("title");
        var trailer_url = selectedOption.data("trailerurl");

        currentSeasonId = season_id;

        $("#edit_season")
            .attr("rel", season_id)
            .data("title", season_title)
            .data("trailerurl", trailer_url);

        $("#delete_season").attr("rel", season_id);

        $("#youtube_id").attr("href", "https://youtu.be/" + trailer_url);

        $("#seasonBadge").html(season_title);

        $("#season_id_for_episode").val(season_id);

        $("#episodeTable").DataTable().ajax.reload(null, false);
    });

    $("#episodeTable").DataTable({
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
            url: `${domainUrl}fetchEpisodeList`,
            data: function (d) {
                d.season_id = currentSeasonId; // Use the dynamic season_id
            },
            error: function (error) {
                console.log(error);
            },
        },
    });

   


    $("#season_list").trigger("change");

    $("#edit_season").on("click", function (e) {
        var id = $(this).attr("rel");
        var season_title = $(this).data("title");
        var trailer_url = $(this).data("trailerurl");

        $("#season_id").val(id);
        $("#edit_title").val(season_title);
        $("#edit_trailer_url").val(trailer_url);
        $("#editSeasonModal").modal("show");
    });

    $(document).on("submit", "#editSeasonForm", function (e) {
        e.preventDefault();
        var id = $("#season_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editSeasonForm")[0]);
            editformData.append("season_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateSeason`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        location.reload();
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $("#season_action").on("click", ".delete", function (e) {
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
                        url: `${domainUrl}deleteSeason`,
                        dataType: "json",
                        data: {
                            season_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                location.reload();
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            });
        });
    });

    // Episode
    $("#episodeSourceTable").dataTable({
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
            url: `${domainUrl}fetchEpisodeSourceList`,
            data: {
                episode_id: episode_id_1,
            },
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addEpisodeSourceForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addEpisodeSourceForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addEpisodeSource`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#episodeSourceTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#addEpisodeSourceModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#episodeSourceTable").on("click", ".edit", function (e) {
        e.preventDefault();

        const data = $(this).data();

        $("#episode_source_id").val($(this).attr("rel"));
        $("#edit_title").val(data.title);
        $("#edit_quality").val(data.quality);
        $("#edit_size").val(data.size);
        $("#edit_is_download").prop("checked", data.download == 1);
        $("#edit_is_download_hidden").val(data.download == 1 ? 1 : 0);
        $("#edit_type").val(data.type).selectric("refresh");
        $("#edit_media").val(data.source).selectric("refresh");

        const sourceURL = $(".sourceURL");
        const mediaGallery = $(".mediaGallery");
        const downloadableOrNot = $("#edit_downloadableOrNot");
        const sourceURLLabel = $("#edit_sourceURL_label");

        if (data.type == "7") {
            sourceURL.hide().find("input").prop("required", false);
            mediaGallery.show().find("select").prop("required", true);
        } else {
            mediaGallery.hide().find("select").prop("required", false);
            sourceURL.show().find("input").prop("required", true);
        }

        if (data.type == "1" || data.type == "2") {
            downloadableOrNot.hide();
        } else {
            downloadableOrNot.show();
        }

        sourceURLLabel.html(
            data.type == "1" ? "Source ID (GUMDnD*****)" : "Source URL"
        );

        $("#edit_sourceURL").val(data.source);
        $("#edit_access_type").val(data.accesstype).selectric("refresh");

        $("#editEpisodeSourceModal").modal("show");
    });

    $(document).on("submit", "#editEpisodeSourceForm", function (e) {
        e.preventDefault();
        var id = $("#episode_source_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editEpisodeSourceForm")[0]);
            editformData.append("episode_source_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateEpisodeSource`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#episodeSourceTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#editEpisodeSourceModal").modal("hide");
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $("#episodeSourceTable").on("click", ".delete", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        swal({
            title: "Are you sure You want to delete!",
            icon: "warning",
            buttons: true,
            dangerMode: true,
        }).then((deleteValue) => {
            if (deleteValue) {
                if (deleteValue == true) {
                    $.ajax({
                        type: "POST",
                        url: `${domainUrl}deleteEpisodeSource`,
                        data: {
                            episode_source_id: id,
                        },
                        dataType: "json",
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#episodeSourceTable")
                                    .DataTable()
                                    .ajax.reload(null, false);
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            }
        });
    });

    $(document).on("submit", "#addEpisodeForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addEpisodeForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addEpisode`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#episodeTable").DataTable().ajax.reload(null, false);
                        $("#addEpisodeModal").modal("hide");
                    }
                },
            });
        });
    });

    $("#episodeTable").on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var number = $(this).data("number");
        var thumbnail = $(this).data("thumbnail");
        var title = $(this).data("title");
        var description = $(this).data("description");
        var duration = $(this).data("duration");
        var access_type = $(this).data("access_type");

        $("#episode_id").val(id);
        $("#edit_number").val(number);
        $("#edit_episode_title").val(title);
        $("#edit_description").val(description);
        $("#edit_duration").val(duration);
        $("#edit_access_type").val(access_type).selectric("refresh");

        $("#edit_thumbnailPoster").attr("src", `${thumbnail}`);

        $("#editEpisodeModal").modal("show");
    });

    $(document).on("submit", "#editEpisodeForm", function (e) {
        e.preventDefault();
        var id = $("#episode_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editEpisodeForm")[0]);
            editformData.append("episode_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateEpisode`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#episodeTable").DataTable().ajax.reload(null, false);
                        $("#detail_content").load(
                            location.href + " #detail_content>*",
                            ""
                        );
                        $("#editEpisodeModal").modal("hide");
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });

    $("#episodeTable").on("click", ".delete", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        checkUserType(function (e) {
            swal({
                title: "Are you sure You want to delete!",
                icon: "warning",
                buttons: true,
                dangerMode: true,
            }).then((deleteValue) => {
                if (deleteValue) {
                    if (deleteValue == true) {
                        $.ajax({
                            type: "POST",
                            url: `${domainUrl}deleteEpisode`,
                            data: {
                                episode_id: id,
                            },
                            dataType: "json",
                            success: function (response) {
                                if (response.status) {
                                    showSuccessToast();
                                    $("#episodeTable").DataTable().ajax.reload(null, false);
                                } else {
                                    console.log(response.message);
                                }
                            },
                        });
                    }
                }
            });
        });
    });

    // Episode Subtitle Table
    $("#episodeSubtitleTable").dataTable({
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
            url: `${domainUrl}fetchEpisodeSubtitleList`,
            data: {
                episode_id: episode_id_1,
            },
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addEpisodeSubtitleForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addEpisodeSubtitleForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addEpisodeSubtitle`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#episodeSubtitleTable").DataTable().ajax.reload(null, false);
                        $("#addEpisodeSubtitleModal").modal("hide");
                    } else if (response.status == 422) {
                        console.log(response.message);
                    }
                },
            });
        });
    });

    $("#episodeSubtitleTable").on("click", ".delete", function (e) {
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
                        url: `${domainUrl}deleteEpisodeSubtitle`,
                        dataType: "json",
                        data: {
                            episode_subtitle_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#episodeSubtitleTable")
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
