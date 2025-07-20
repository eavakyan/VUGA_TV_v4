@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">

@stop
@section('content')
<section class="section">
  <div class="section-body">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header">
              <h4>Subscription List </h4>
            </div>

            <div class="card-body">
         
            <div class="card-body">	
                 
                 <div class="table-responsive">
                     <table class="table table-striped" id="subscription-listing">
                       <thead>
                         <tr>
                            <!-- <th>Purchse ID </th> -->
                            <th>UserName</th>
                            <th>Method</th>
                            <th>Pcak</th>
                            <th>Duration</th>
                            <th>Start Date</th>
                            <th>End Date</th>
                            <th>Price</th>
                            <th>Action</th>        
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
    var dataTable = $('#subscription-listing').dataTable({
        'processing': true,
        'serverSide': true,
        'serverMethod': 'post',
        "order": [[ 0, "desc" ]],
        'columnDefs': [ {
            'targets': [7], /* column index */
            'orderable': false, /* true or false */
            }],
        'ajax': {
            'url':'{{ route("showSubscriptionList") }}',
            'data': function(data){
              data.status = 0;
            }
        }
    });
    $(document).on('click', '.DeleteSubscription', function (e) {
    e.preventDefault();
    var subscription_id = $(this).attr('data-id');
    var text = 'You will not be able to recover Subscription data!';   
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
          if (user_type == 1) {
            $('.loader').show();
            $.ajax({
                url: '{{ route("deleteSubscription") }}',
                type: 'POST',
                data: {"subscription_id":subscription_id},
                dataType: "json",
                cache: false,
                success: function (data) {
                    $('.loader').hide();
                    $('#subscription-listing').DataTable().ajax.reload(null, false);
                    if (data.success == 1) {
                      swal("Confirm!", "Subscription  has been deleted!", "success");
                    } else {
                      swal("Confirm!", "Subscription  has not been deleted!", "error");
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(errorThrown);
                }
            });
          }else{
            iziToast.error({
                title: 'Error!',
                message: ' you are Tester ',
                position: 'topRight'
            });
            swal("Confirm!", "Subscription  has been deleted!", "success");
          }
          } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
  });

});
</script>

@endsection
