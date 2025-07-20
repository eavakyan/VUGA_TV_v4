    $(document).ready(function () {
        $(".sideBarli").removeClass("activeLi");
        $(".liveTvChannelSideA").addClass("activeLi");

        
    $("#type").change(function () {
        var typeVal = $(this).val();
        var sourceURLLabel = $(".source");
        
        sourceURLLabel.html(
            typeVal == "1" ? "Source ID (GUMDnD*****)" : "Source URL"
        );
    });

    $("#edit_type").change(function () {
        var typeVal = $(this).val();
        var sourceURLLabel = $(".source");

        sourceURLLabel.html(
            typeVal == "1" ? "Source ID (GUMDnD*****)" : "Source URL"
        );
    });



        $("#tvChannelTable").dataTable({
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
                url: `${domainUrl}fetchTvChannelList`,
                error: (error) => {
                    console.log(error);
                },
            },
        });

        $(document).on("submit", "#addTvChannelForm", function (e) {
            e.preventDefault();
            checkUserType(function (e) {
                let formData = new FormData($("#addTvChannelForm")[0]);
                $.ajax({
                    type: "POST",
                    url: `${domainUrl}addTvChannel`,
                    data: formData,
                    contentType: false,
                    processData: false,
                    success: function (response) {
                        if (response.status) {
                            showSuccessToast();
                            $("#tvChannelTable")
                                .DataTable()
                                .ajax.reload(null, false);
                            $("#addTvChannelModal").modal("hide");
                        }
                    },
                });
            });
        });

        $(document).on("click", ".edit", function (e) {
            e.preventDefault();

            var id = $(this).attr("rel");
            var title = $(this).data("title");
            var thumbnail = $(this).data("thumbnail");
            var access_type = $(this).data("access_type");
            var category_ids = $(this).data("category_ids");
            var type = $(this).data("type");
            var source = $(this).data("source");

            $("#tv_channel_id").val(id);
            $("#edit_title").val(title);

            if (thumbnail) {
                $("#edit_image").attr("src", `${thumbnail}`);
            } else {
                $("#edit_image").attr("src", "assets/img/placeholder-image.png");
            }
            $("#edit_access_type").val(access_type).selectric("refresh");

            var categoryArray = [];
            if (typeof category_ids === "string") {
                categoryArray = category_ids.split(",").map(function (item) {
                    return item.trim();
                });
            } else if (Array.isArray(category_ids)) {
                categoryArray = category_ids;
            } else if (typeof category_ids === "number") {
                categoryArray = [category_ids.toString()];
            }
            $("#edit_selectCategory").val(categoryArray).selectric("refresh");
            $("#edit_type").val(type).selectric("refresh");

            if (type == 1) {
                $(".source").html("Source ID (GUMDnD*****)");
            } else {
                $(".source").html("Source");
            }

            $("#edit_source").val(source);
            
            $("#editTvChannelModal").modal("show");
        });

        $(document).on("submit", "#editTvChannelForm", function (e) {
            e.preventDefault();
            var id = $("#tv_channel_id").val();
            checkUserType(function (e) {
                let editformData = new FormData($("#editTvChannelForm")[0]);
                editformData.append("tv_channel_id", id);
                $.ajax({
                    type: "POST",
                    url: `${domainUrl}updateTvChannel`,
                    data: editformData,
                    contentType: false,
                    processData: false,
                    success: function (response) {
                        if (response.status) {
                            showSuccessToast();
                            $("#tvChannelTable").DataTable().ajax.reload(null, false);
                            $("#editTvChannelModal").modal("hide");
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
                            url: `${domainUrl}deleteTvChannel`,
                            dataType: "json",
                            data: {
                                tv_channel_id: id,
                            },
                            success: function (response) {
                                if (response.status) {
                                    showSuccessToast();
                                    $("#tvChannelTable").DataTable().ajax.reload(null, false);
                                } else {
                                    somethingWentWrongToast();
                                }
                            },
                        });
                    }
                });
            });
        });

        $(document).on("click", ".m3u8_Url_Link", function (e) {
            e.preventDefault();

            var type = $(this).data("type");
            var source = $(this).data("source");

            console.log(type);
            console.log(source);

            if (type == 2) {
                var video = document.getElementById("m3u8_video");
                if (Hls.isSupported()) {
                    var hls = new Hls();
                    hls.loadSource("https://rt-glb.servicecdn.ru/dvr/rtnews/playlist.m3u8");
                    hls.attachMedia(video);
                    video.play();
                } else if (video.canPlayType("application/vnd.apple.mpegurl")) {
                    video.src = "https://rt-glb.servicecdn.ru/dvr/rtnews/playlist.m3u8";
                    video.addEventListener("loadedmetadata", function () {
                        video.play();
                    });
                }
                $("#m3u8Modal").modal("show");
            }

            else {
                console.log("Lol");
            }
    
        });
    });
