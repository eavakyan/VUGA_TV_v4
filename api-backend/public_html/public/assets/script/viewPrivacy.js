$(document).ready(function () {
  $(".sideBarli").removeClass("activeLi");
  $(".privacySideA").addClass("activeLi");

  $(document).on("submit", "#privacy", function (e) {
      e.preventDefault();
      checkUserType(function (e) {
          var myEditor = document.querySelector("#privacyContent .ql-editor");
          var htmlContent = myEditor.innerHTML;

          var formData = new FormData($("#privacy")[0]);
          formData.append("content", htmlContent);

          $.ajax({
              type: "POST",
              url: `${domainUrl}updatePrivacy`,
              data: formData,
              contentType: false,
              processData: false,
              success: function (response) {
                  if (response.status) {
                      showSuccessToast();
                  } else {
                      showErrorToast();
                  }
              },
              error: function (error) {
                  showErrorToast();
              },
          });
      });
  });
  
});
