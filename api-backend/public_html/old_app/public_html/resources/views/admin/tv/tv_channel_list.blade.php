@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/summernote/summernote-bs4.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">

@stop
@section('content')
<section class="section">
  <div class="section-body">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-header">
            <h4>TV Channel List (<span class="total_categorychannel">{{$total_channel}}</span>)</h4>
          </div>
          
          <div class="card-body">	
            <div class="pull-right">
              <div class="buttons"> 
                  <a class="btn btn-primary text-light" href="{{route('channel/add')}}" >Add TV Channel</a>
              </div>
            </div>
            <div class="table-responsive">
              <table class="table table-striped" id="channel-listing">
                <thead>
                  <tr>
                    <th>Icon</th>
                    <th>Name</th>
                    <th>Category</th>
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
  </div>
</section>

@endsection

@section('pageSpecificJs')

<script src="{{asset('assets/bundles/datatables/datatables.min.js')}}"></script>
<script src="{{asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/js/page/datatables.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>
<script src="{{asset('assets/bundles/summernote/summernote-bs4.js')}}"></script>

<script>
$(document).ready(function (){
  var dataTable = $('#channel-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [3], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showTVChannelList") }}',
        'data': function(data){
        }
    }
  });

  $(document).on('click', '.DeleteTVChannel', function (e) {
    e.preventDefault();
    var channel_id = $(this).attr('data-id');
    var text = 'You will not be able to recover TV Channel data!';   
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
                url: '{{ route("deleteTVChannel") }}',
                type: 'POST',
                data: {"channel_id":channel_id},
                dataType: "json",
                cache: false,
                success: function (data) {
                    $('.loader').hide();
                    $('#channel-listing').DataTable().ajax.reload(null, false);
                    $('.total_channel').text(data.total_channel);
                    if (data.success == 1) {
                      swal("Confirm!", "TV Channel has been deleted!", "success");
                    } else {
                      swal("Confirm!", "TV Channel has not been deleted!", "error");
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
            swal("Confirm!", "TV Channel has been deleted!", "success");
          }
          } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
  });

});
</script>

@endsection
