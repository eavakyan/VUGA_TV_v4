$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".languageSideA").addClass("activeLi");

    $("#languagesTable").dataTable({
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
            url: `${domainUrl}languagesList`,
            error: (error) => {
                console.log(error);
            },
        },
    });

    $(document).on("submit", "#addLanguageForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addLanguageForm")[0]);
            $.ajax({
                type: "POST",
                url: `${domainUrl}addLanguage`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#languagesTable").DataTable().ajax.reload(null, false);
                        $("#addLanguageModal").modal("hide");
                    }
                },
            });
        });
    });

    $(document).on("click", ".edit", function (e) {
        e.preventDefault();

        var id = $(this).attr("rel");
        var title = $(this).data("title");
        var code = $(this).data("code");

        $("#language_id").val(id);
        $("#edit_title").val(title);
        $("#edit_code").val(code);

        $("#editLanguageModal").modal("show");
    });

    $(document).on("submit", "#editLanguageForm", function (e) {
        e.preventDefault();
        var id = $("#language_id").val();
        checkUserType(function (e) {
            let editformData = new FormData($("#editLanguageForm")[0]);
            editformData.append("language_id", id);
            $.ajax({
                type: "POST",
                url: `${domainUrl}updateLanguage`,
                data: editformData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        showSuccessToast();
                        $("#languagesTable")
                            .DataTable()
                            .ajax.reload(null, false);
                        $("#editLanguageModal").modal("hide");
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
                        url: `${domainUrl}deleteLanguage`,
                        dataType: "json",
                        data: {
                            language_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                showSuccessToast();
                                $("#languagesTable")
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
