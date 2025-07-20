@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">
@stop
@section('content')
<section class="section">
  <div class="section-body">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header">
              <h4>User List (<span class="total_user">{{$total_user}}</span>)</h4>
            </div>
            <div class="card-body">	
              <div class="table-responsive">
                  <table class="table table-striped" id="user-listing">
                    <thead>
                      <tr>
                        <th>Image </th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Total Purchase</th>
                        <!-- <th>Is Premium ?</th> -->
                        <th>Status</th>
                        <th>View Details</th>
                      </tr>
                    </thead>
                    <tbody>

                    </tbody>
                  </table>
              </div>
            </div>

        </div>
      </div>
    </div>
  </div>
</section>
@endsection

@section('pageSpecificJs')

<script src="{{asset('assets/bundles/datatables/datatables.min.js')}}"></script>
<script src="{{asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/js/page/datatables.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>

<script>
$(document).ready(function (){
  var dataTable = $('#user-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [3,4,5], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showUserList") }}',
        'data': function(data){
          data.is_premium = 2;
        }
    }
  });

  var dataTable = $('#premium-user-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [0], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showUserList") }}',
        'data': function(data){
          data.is_premium = 1;
        }
    }
  });

  var dataTable = $('#normal-user-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [0], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showUserList") }}',
        'data': function(data){
            // Read values
            data.is_premium = 0;
        }
    }
  });

  $(document).on('click', '#userDelete', function (e) {
      e.preventDefault();
      var user_id = $(this).attr('data-id');
      var text = 'You will not be able to recover this data!';   
      var confirmButtonText = 'Yes, Delete it!';
      var btn = 'btn-danger';
      swal({
        title: "Are you sure?",
        text: text,
        type: "warning",
        showCancelButton: true,
        confirmButtonClass: btn,
        confirmButtonText: confirmButtonText,
        cancelButtonText: "No, cancel please!",
        closeOnConfirm: false,
        closeOnCancel: false
      },
      function(isConfirm){
          if (isConfirm){
            $('.preloader').show();
            $.ajax({
                url: '{{ route("deleteUser") }}',
                type: 'POST',
                data: {"user_id":user_id},
                dataType: "json",
                cache: false,
                success: function (data) {
                    $('.preloader').hide();
                    $('#user-listing').DataTable().ajax.reload(null, false);
                    $('.total_user').text(data.total_user);
                    if (data.success == 1) {
                      swal("Confirm!", "User has been deleted!", "success");
                    } else {
                      swal("Confirm!", "User has not been deleted!", "error");
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(errorThrown);
                }
            });
          } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
    });
    
});
</script>

@endsection
