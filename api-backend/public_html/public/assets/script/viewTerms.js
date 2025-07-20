$(document).ready(function () {
  $(".sideBarli").removeClass("activeLi");
  $(".termsSideA").addClass("activeLi");
 
  $(document).on("submit", "#terms", function (e) {
      e.preventDefault();
     checkUserType(function (e) {
          let formData = new FormData($("#terms")[0]);
          let quillContent = quill3.root.innerHTML;
          formData.append("content", quillContent); // Append the Quill editor's content
          $.ajax({
              type: "POST",
              url: `${domainUrl}updateTerms`,
              data: formData,
              contentType: false,
              processData: false,
              success: function (response) {
                  if (response.status) {
                      showSuccessToast();
                  }
              },
          });
      });
  });

  
});
