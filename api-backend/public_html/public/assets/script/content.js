$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".contentSideA").addClass("activeLi");

    function handleContentTypeFields(type) {
        if (type == "2") {
            $("#edit_duration").closest(".col").hide();
            $("#edit_duration").prop("required", false);
        } else {
            $("#edit_duration").closest(".col").show();
            $("#edit_duration").prop("required", true);
        }
    }

    // Trailer management functions
    var trailerIndex = 0;
    
    function addTrailerItem(container, title, url, isPrimary, isEdit) {
        // Set default values
        title = title || '';
        url = url || '';
        isPrimary = isPrimary || false;
        isEdit = isEdit || false;
        
        var index = trailerIndex++;
        var prefix = isEdit ? 'edit_' : '';
        var requiredAttr = (!isEdit && index === 0) ? 'required' : '';
        var checkedAttr = isPrimary ? 'checked' : '';
        var removeButton = index > 0 ? '<button type="button" class="btn btn-sm btn-danger remove-trailer-btn">&times;</button>' : '';
        
        var trailerHtml = 
            '<div class="trailer-item mb-2" data-index="' + index + '">' +
                '<div class="row align-items-end">' +
                    '<div class="col-md-4">' +
                        '<input type="text" class="form-control" name="' + prefix + 'trailer_titles[]" ' +
                               'placeholder="Trailer Title" value="' + title + '">' +
                    '</div>' +
                    '<div class="col-md-6">' +
                        '<input type="text" class="form-control trailer-url" name="' + prefix + 'trailer_urls[]" ' +
                               'placeholder="YouTube URL, MP4, HLS or other video URL" value="' + url + '" ' + requiredAttr + '>' +
                    '</div>' +
                    '<div class="col-md-2">' +
                        '<div class="d-flex align-items-center">' +
                            '<div class="form-check me-2">' +
                                '<input class="form-check-input primary-trailer" type="radio" ' +
                                       'name="' + prefix + 'primary_trailer" value="' + index + '" ' + checkedAttr + '>' +
                                '<label class="form-check-label">Primary</label>' +
                            '</div>' +
                            removeButton +
                        '</div>' +
                    '</div>' +
                '</div>' +
            '</div>';
        
        container.append(trailerHtml);
    }
    
    // Add trailer button handlers
    $(document).on('click', '.add-trailer-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var container = $('#trailers-container');
        if (container.length) {
            addTrailerItem(container, '', '', false, false);
        }
        return false;
    });
    
    $(document).on('click', '.add-trailer-btn-edit', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var container = $('#edit-trailers-container');
        if (container.length) {
            addTrailerItem(container, '', '', false, true);
        }
        return false;
    });
    
    // Remove trailer button handler
    $(document).on('click', '.remove-trailer-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var $trailerItem = $(this).closest('.trailer-item');
        var wasPrimary = $trailerItem.find('.primary-trailer').is(':checked');
        $trailerItem.remove();
        
        // If this was the primary trailer, make the first remaining one primary
        if (wasPrimary) {
            $('.trailer-item:first .primary-trailer').prop('checked', true);
        }
        return false;
    });
    
    // Load trailers for edit modal
    function loadTrailersForEdit(contentId) {
        $.ajax({
            type: 'GET',
            url: `${domainUrl}api/v2/content-trailers/${contentId}`,
            success: function(response) {
                const container = $('#edit-trailers-container');
                container.empty();
                
                if (response.data && response.data.length > 0) {
                    response.data.forEach((trailer, index) => {
                        addTrailerItem(container, trailer.title, trailer.trailer_url, 
                                     trailer.is_primary == 1, true);
                    });
                } else {
                    // Add default empty trailer if none exist
                    addTrailerItem(container, 'Trailer', '', true, true);
                }
            },
            error: function() {
                // Add default empty trailer on error
                addTrailerItem($('#edit-trailers-container'), 'Trailer', '', true, true);
            }
        });
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
        // trailer_url removed - will be loaded via AJAX
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

        // Load trailers for this content
        loadTrailersForEdit(id);
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
            
            // Collect trailer data
            const trailerData = [];
            $('.trailer-item').each(function(index) {
                const title = $(this).find('input[name="edit_trailer_titles[]"]').val() || 
                             $(this).find('input[name="trailer_titles[]"]').val();
                const url = $(this).find('input[name="edit_trailer_urls[]"]').val() || 
                           $(this).find('input[name="trailer_urls[]"]').val();
                const isPrimary = $(this).find('.primary-trailer').is(':checked');
                
                if (url.trim()) {
                    trailerData.push({
                        title: title || 'Trailer',
                        url: url.trim(),
                        is_primary: isPrimary
                    });
                }
            });
            
            editformData.append('trailers', JSON.stringify(trailerData));
            
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
    
    // Handle add content form submission (for new content)
    $(document).on("submit", "#addContentForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addContentForm")[0]);
            
            // Collect trailer data for new content
            const trailerData = [];
            $('#trailers-container .trailer-item').each(function(index) {
                const title = $(this).find('input[name="trailer_titles[]"]').val();
                const url = $(this).find('input[name="trailer_urls[]"]').val();
                const isPrimary = $(this).find('.primary-trailer').is(':checked');
                
                if (url.trim()) {
                    trailerData.push({
                        title: title || 'Trailer',
                        url: url.trim(),
                        is_primary: isPrimary
                    });
                }
            });
            
            formData.append('trailers', JSON.stringify(trailerData));
            
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
                        $("#addContentForm")[0].reset();
                        // Reset trailer container
                        $('#trailers-container').empty();
                        addTrailerItem($('#trailers-container'), 'Trailer', '', true);
                    } else {
                        somethingWentWrongToast();
                    }
                },
            });
        });
    });
    
    // Initialize trailer container on page load
    if ($('#trailers-container').length) {
        addTrailerItem($('#trailers-container'), 'Trailer', '', true);
    }
    
    // Additional button handlers for safety (in case event delegation doesn't work)
    $('#addContentModal').on('shown.bs.modal', function() {
        $('.add-trailer-btn').off('click.trailer').on('click.trailer', function(e) {
            e.preventDefault();
            e.stopPropagation();
            var container = $('#trailers-container');
            if (container.length) {
                addTrailerItem(container, '', '', false, false);
            }
            return false;
        });
    });
    
    $('#editContentModal').on('shown.bs.modal', function() {
        $('.add-trailer-btn-edit').off('click.trailer').on('click.trailer', function(e) {
            e.preventDefault();
            e.stopPropagation();
            var container = $('#edit-trailers-container');
            if (container.length) {
                addTrailerItem(container, '', '', false, true);
            }
            return false;
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
