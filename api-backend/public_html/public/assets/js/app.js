$.ajaxSetup({
    headers: {
        "X-CSRF-TOKEN": $('meta[name="csrf-token"]').attr("content"),
    },
});



$(".mediaGallery").hide();

$("#type").change(function () {
    const typeVal = $(this).val();
    const sourceURL = $(".sourceURL");
    const mediaGallery = $(".mediaGallery");
    const downloadableOrNot = $("#downloadableOrNot");
    const sourceURLLabel = $("#sourceURL_label");

    if (typeVal == "7") {
        sourceURL.hide().find("input").prop("required", false);
        mediaGallery.show().find("select").prop("required", true);
    } else {
        mediaGallery.hide().find("select").prop("required", false);
        sourceURL.show().find("input").prop("required", true);
    }

    if (typeVal == "1" || typeVal == "2" || typeVal == "8") {
        downloadableOrNot.hide();
    } else {
        downloadableOrNot.show();
    }

    sourceURLLabel.html(
        typeVal == "1" ? "Source ID (GUMDnD*****)" : "Source URL"
    );
});

$("#edit_type").change(function () {
    const typeVal = $(this).val();
    const sourceURL = $(".sourceURL");
    const mediaGallery = $(".mediaGallery");
    const downloadableOrNot = $("#edit_downloadableOrNot");
    const sourceURLLabel = $("#edit_sourceURL_label");

    if (typeVal == "7") {
        sourceURL.hide().find("input").prop("required", false);
        mediaGallery.show().find("select").prop("required", true);
    } else {
        mediaGallery.hide().find("select").prop("required", false);
        sourceURL.show().find("input").prop("required", true);
    }

    if (typeVal == "1" || typeVal == "2" || typeVal == "8") {
        downloadableOrNot.hide();
    } else {
        downloadableOrNot.show();
    }

    sourceURLLabel.html(
        typeVal == "1" ? "Source ID (GUMDnD*****)" : "Source URL"
    );
});

$("#is_download").on("change", function () {
    $("#is_download_hidden").val(this.checked ? 1 : 0);
});
$("#edit_is_download").on("change", function () {
    $("#edit_is_download_hidden").val(this.checked ? 1 : 0);
});

$(document).on("click", ".source_file_video", function (e) {
    e.preventDefault();
    var source_url = $(this).data("source_url");
    $("#showVideoUrl").attr("src", `${source_url}`);
    $("#videoPreviewModal").modal("show");
});

function showSuccessToast() {
    iziToast.show({
        title: "Success",
        message: "Action was successfully completed.",
        color: "green",
        position: "topRight",
        transitionIn: "fadeInDown",
        transitionOut: "fadeOutUp",
        timeout: 3000,
        animateInside: false,
        iconUrl: `${domainUrl}assets/img/check-circle.svg`,
    });
}

function showTesterToast() {
    iziToast.show({
        title: "Oops",
        message: "You are tester",
        color: "red",
        position: "topRight",
        transitionIn: "fadeInDown",
        transitionOut: "fadeOutUp",
        timeout: 3000,
        animateInside: false,
        iconUrl: `${domainUrl}assets/img/x.svg`,
    });
}

function somethingWentWrongToast() {
    iziToast.show({
        title: "Oops",
        message: "Something Went Wrong!",
        color: "red",
        position: "topRight",
        transitionIn: "fadeInDown",
        transitionOut: "fadeOutUp",
        timeout: 3000,
        animateInside: false,
        iconUrl: `${domainUrl}assets/img/x.svg`,
    });
}

var user_type = $("#user_type").val();

$("#content_type").change(function () {
    var selectedType = $(this).val();

    if (selectedType == "2") {
        $("#duration").closest(".col").hide();
        $("#duration").prop("required", false);

        $("#trailer_url").closest(".col").hide();
        $("#trailer_url").prop("required", false);
    } else {
        $("#duration").closest(".col").show();
        $("#duration").prop("required", true);

        $("#trailer_url").closest(".col").show();
        $("#trailer_url").prop("required", true);
    }
});

$("#content_type").trigger("change");
$(".saveButton").removeClass("spinning disabled");
$(document).on("hidden.bs.modal", function () {
    $(".saveButton").removeClass("spinning disabled");
    $(".importContentData").removeClass("spinning disabled");
    $(".importSeriesContentData").removeClass("disabled");
});

$(document).on("hidden.bs.modal", ".modal", function () {
    var modal = $(this);

    modal.find("form").trigger("reset");

    modal.find(".file-input").each(function () {
        var label = $(this).find("[data-js-label]");
        if (label.length > 0) {
            label.text("No File Selected"); // Reset the label text
            $(this).removeClass("-chosen");
        }
    });

    modal.find("#is_download_hidden").val(0);
    
    modal.find("#m3u8_video").attr("src", ``);
    modal.find(".progress").hide();

    modal
        .find(".modal_placeholder_image")
        .attr("src", `${domainUrl}/assets/img/placeholder-image.png`);
    modal.find(".selectric").selectric("destroy").selectric();
    modal.find("#edit_duration").closest(".col").show();
    modal.find("#edit_duration").prop("required", true);
    modal.find(".saveButton, .saveButton1").removeClass("spinning disabled");

    modal.find("#showVideoUrl").attr("src", ``);
    modal.removeData("bs.modal");
});

$("form").on("submit", function () {
    $(".saveButton").addClass("spinning");
    $(".saveButton").addClass("disabled");

    $(".saveButton1").addClass("spinning");
    $(".saveButton1").addClass("disabled");
});

$("#settingsForm").on("submit", function () {
    $(".saveButton2").addClass("spinning");
    $(".saveButton2").addClass("disabled");

    setTimeout(function () {
        $(".saveButton2").removeClass("spinning");
        $(".saveButton2").removeClass("disabled");
    }, 1000);
});

if ($(window).width() >= 1199) {
    $("table").removeClass("table-responsive");
}
if ($(window).width() <= 1199) {
    $("table").addClass("table-responsive");
}
if ($(window).width() <= 1450) {
    $("#moviesTable").addClass("table-responsive");
    $("#seriesTable").addClass("table-responsive");
    $("#tvChannelTable").addClass("table-responsive");
    $("#customAdTable").addClass("table-responsive");
}
// add class on responsive
$(window).on("resize", function () {
    if ($(window).width() >= 1199) {
        $("table").removeClass("table-responsive");
    }
    if ($(window).width() <= 1199) {
        $("table").addClass("table-responsive");
    }
    if ($(window).width() <= 1450) {
        $("table").addClass("table-responsive");
    }
});

$(".logo-name-small").each(function (index) {
    var characters = $(this).text().split("");
    $this = $(this);
    $this.empty();
    $.each(characters, function (i, el) {
        $this.append('<span class="letter-' + i + '">' + el + "</span>");
    });
});

// Custom input file
var inputs = document.querySelectorAll(".file-input");

for (var i = 0, len = inputs.length; i < len; i++) {
    customInput(inputs[i]);
}

function customInput(el) {
    const fileInput = el.querySelector('[type="file"]');
    const label = el.querySelector("[data-js-label]");

    fileInput.onchange = fileInput.onmouseout = function () {
        if (!fileInput.value) return;

        var value = fileInput.value.replace(/^.*[\\\/]/, "");
        el.className += " -chosen";
        label.innerText = value;
    };
} 

function checkUserType(callback) {
    if (user_type == 1) {
        callback();
    } else {
        $(".modal").modal("hide");
        showTesterToast();
    }
}
