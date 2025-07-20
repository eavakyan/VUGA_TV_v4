$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".topContentsSideA").addClass("activeLi");

    $("#topContentsTable").DataTable({
        autoWidth: false,
        processing: true,
        serverSide: true,
        bLengthChange: false,
        bInfo: false,
        bFilter: false,
        bPaginate: false,
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
            url: `${domainUrl}topContentsList`,
            error: (error) => {
                console.log(error);
            },
        },
        drawCallback: function () {
            makeSortable();
        },
    });

    function makeSortable() {
        $("#topContentsTable tbody")
            .sortable({
                update: function (event, ui) {
                    var order = $(this).sortable("toArray", {
                        attribute: "id",
                    });
                    saveOrder(order);
                },
            })
            .disableSelection();
    }

    function saveOrder(order) {
        $.ajax({
            url: `${domainUrl}saveOrder`,
            method: "POST",
            data: {
                order: order,
            },
            success: function (response) {
                console.log(response);
                showSuccessToast();
                $("#topContentsTable").DataTable().ajax.reload(null, false);
                console.log("Order saved successfully.");
            },
            error: function (error) {
                console.error("Error saving order:", error);
            },
        });
    }

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
                        url: `${domainUrl}removeFromTopContent`,
                        dataType: "json",
                        data: {
                            top_content_id: id,
                        },
                        success: function (response) {
                            if (response.status) {
                                window.location.reload();
                            } else {
                                somethingWentWrongToast();
                            }
                        },
                    });
                }
            });
        });
    });

    $(document).on("click", ".content_checkbox", function (e) {
        e.preventDefault();
        var id = $(this).attr("rel");
        var checked = $(this).is(":checked");
        checkUserType(function (e) {
            if (checked) {
                $.ajax({
                    type: "POST",
                    url: `${domainUrl}addToTopContent`,
                    dataType: "json",
                    data: {content_id: id},
                    success: function (response) {
                        if (response.status) {
                           location.reload();
                        } else {
                            somethingWentWrongToast();
                        }
                    },
                });
            } else {
                showTesterToast();
            }
        });
    });
});
