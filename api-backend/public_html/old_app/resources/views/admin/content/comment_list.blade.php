@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">
<style type="text/css">
.view {
    padding: 0.3rem 9px;
}


.switch {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 34px;
}

.switch input { 
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  -webkit-transition: .4s;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 26px;
  width: 26px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  -webkit-transition: .4s;
  transition: .4s;
}

input:checked + .slider {
  background-color: #2196F3;
}

input:focus + .slider {
  box-shadow: 0 0 1px #2196F3;
}

input:checked + .slider:before {
  -webkit-transform: translateX(26px);
  -ms-transform: translateX(26px);
  transform: translateX(26px);
}

/* Rounded sliders */
.slider.round {
  border-radius: 34px;
}

.slider.round:before {
  border-radius: 50%;
}
</style>
@stop
@section('content')
<section class="section">
  <div class="section-body">
      
    <div class="row">
        <div class="col-12 col-md-12 col-lg-12">
          @if($content_id) 
            <div class="card card-stats">
              <div class="card-content views-body  pull-right">
                <a href="#" class="btn btn-tab-movie"><i class="material-icons">remove_red_eye</i> {{ $data->total_view }} Views</a>
                <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">cloud_download</i> {{ $data->total_download }} Downloads</a>
                <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">share</i> {{ $data->total_share }} Shares</a>

              </div>
              <div class="card-body">
                <h4 class="text-center mb-0"> Edit {{$content_title}}</h4>
              </div>
            </div>
          @endif
          <div class="card">
            <div class="card-body">
            @include('admin.section.content_header')
            </div>
          </div> 
        </div> 
      </div>
    </div>
    <div class="row">
        <div class="col-12">
            <div class="card">
            <div class="card-header">
                <h4>{{$content_title}} : <span class="total_comment">{{$total_comment}}</span> Comments</h4>
            </div>
            <input id="content_id" name="content_id" type="hidden" class="form-control form-control-danger" value="{{$content_id}}">

            <div class="card-body">
                        
                <div class="table-responsive">
                    <table class="table table-striped" id="comment-listing" width="100%">
                    <thead>
                        <tr>
                        <th>Profile</th>
                        <th>User Name </th>
                        <th>Comment</th>
                        <th>Added Date</th>
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
  var dataTable = $('#comment-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [4], /* column index */
          'orderable': false, /* true or false */
        }], 
    'ajax': {
        'url':'{{ route("showContentCommentList") }}',
        'data': function(data){
            data.content_id = $("#content_id").val();
        }
    }
  });
  
  $('a[data-toggle="tab"]').on('shown.bs.tab', function(e){
      $($.fn.dataTable.tables(true)).DataTable()
         .columns.adjust();
   }); 

  $(document).on('click', '.showHideComment', function (e) {
      e.preventDefault();
      var content_id = $(this).attr('data-content_id');
      var comment_id = $(this).attr('data-id');
      var status = $(this).attr('data-status');
      if(status == 1){
        status = 0;
        var msg = "hide";
      }else{
        status = 1;
        var msg = "show";
      }
      var text = 'You will not be able to recover this data!';   
      var confirmButtonText = 'Yes, Change Status!';
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
                  url: '{{ route("changeCommentStatus") }}',
                  type: 'POST',
                  data: {"content_id":content_id,"comment_id":comment_id,"status":status},
                  dataType: "json",
                  cache: false,
                  success: function (data) {
                      $('.loader').hide();
                      $('#comment-listing').DataTable().ajax.reload(null, false);
                      $('.total_comment').text(data.total_comment);
                      swal("Confirm!", "Your Comment has been "+msg+"!", "success");
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
                swal("Confirm!", "Your Comment has been "+msg+"!", "success");
              }
          } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
    });

    $(document).on('click', '.DeleteComment', function (e) {
        e.preventDefault();
        var content_id = $(this).attr('data-content_id');
        var comment_id = $(this).attr('data-id');
        var text = 'You will not be able to recover Comment data!';   
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
                    url: '{{ route("deleteComment") }}',
                    type: 'POST',
                    data: {"content_id":content_id,"comment_id":comment_id},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        $('#comment-listing').DataTable().ajax.reload(null, false);
                        $('.total_comment').text(data.total_comment);
                        if (data.success == 1) {
                        swal("Confirm!", "Comment has been deleted!", "success");
                        } else {
                        swal("Confirm!", "Comment has not been deleted!", "error");
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
                swal("Confirm!", "Comment has been deleted!", "success");
              }
            } else {
            swal("Cancelled", "Your imaginary file is safe :)", "error");
            }
        });
    });
});
</script>

@endsection
