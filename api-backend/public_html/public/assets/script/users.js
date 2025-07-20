$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".userSideA").addClass("activeLi");

    
  $("#usersTable").dataTable({
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
          url: `${domainUrl}usersList`,
          error: (error) => {
              console.log(error);
          },
      },
  });
});