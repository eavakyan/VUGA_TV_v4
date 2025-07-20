@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">
<style type="text/css">
.view {
    padding: 0.3rem 9px;
}
</style>
@stop
@section('content')
<section class="section">
  <div class="section-body">
      
    <div class="row">
        <div class="col-12 col-md-12 col-lg-12">
          @if($channel_id) 
          <div class="card card-stats">
            <div class="card-content views-body  pull-right">
              <a href="#" class="btn btn-tab-movie"><i class="material-icons">remove_red_eye</i> {{ $data->total_view }} Views</a>
              <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">share</i> {{ $data->total_share }} Shares</a>
            </div>
            <div class="card-body">
            <h4 class="text-center mb-0"> Edit {{$channel_title}} Source</h4>
            </div>
          </div>
          @endif
          <div class="card">
            <div class="card-body">
            @include('admin.section.tv_channel_header')
            </div>
          </div> 
        </div>
    </div>
    <div class="row">
        <div class="col-12">
            <div class="card">
            <div class="card-header">
                <h4>Source List (<span class="total_source">{{$total_source}}</span>)</h4>
            </div>

            <div class="card-body">
              @if($total_source <= 0)
                <div class="pull-right">
                    <div class="buttons"> 
                    <button class="btn btn-primary text-light" data-toggle="modal" data-target="#SourceModal" data-whatever="@mdo" @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Add Source</button>
                    </div>
                </div>
                @endif
                        
                <div class="table-responsive">
                    <table class="table table-striped" id="source-content-listing" width="100%">
                    <thead>
                        <tr>
                        <th>Type</th>
                        <!-- <th>Title </th> -->
                        <th>URL</th>
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


<div class="modal fade" id="SourceModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="ModalLabel"> Add Source </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateTVChannelSource" method="post" enctype="multipart">
        {{ csrf_field() }}
            <div class="modal-body">

                <input id="channel_id" name="channel_id" type="hidden" class="form-control form-control-danger" value="{{$channel_id}}">

                <!-- <div class="form-group">
                    <label for="source_title">Source Title</label>
                    <input id="source_title" name="source_title" type="text" class="form-control form-control-danger" placeholder="Enter Source Title">
                </div>

                <div class="form-group">
                    <label for="source_quality">Source Quality</label>
                    <input id="source_quality" name="source_quality" type="text" class="form-control form-control-danger" placeholder="Enter Source Quality">
                </div>

                <div class="form-group">
                    <label for="source_size">Source Size</label>
                    <input id="source_size" name="source_size" type="text" class="form-control form-control-danger" placeholder="Enter Source Size">
                </div>

                <div class="form-group">
                    <div class="custom-control custom-checkbox">
                        <input type="checkbox" class="custom-control-input"  name="downloadable"  id="downloadable">
                        <label class="custom-control-label" for="downloadable">Downloadable Or not</label>
                    </div>
                </div>      
                

                <div class="form-group">
                    <label for="access_type">Select Access Type</label>
                    <select id="access_type" name="access_type" class="form-control form-control-danger">
                        <option value="1">Free</option>
                        <option value="2">Paid</option>
                        <option value="3">Unlock With Video Ads</option>
                    </select>
                </div>   -->

                <div class="form-group">
                    <label for="source_type">Select Source Type</label>
                    <select name="source_type" class="form-control form-control-danger" id="source_type">
                    <option value="1">Youtube Id</option>
                    <option value="2">M3u8 Url</option>
                    </select>
                </div> 

                <div class="form-group">
                    <label for="source">Source</label>
                    <input type="text"  name="source" class="form-control" id="source" placeholder="Source URL">
                </div>

            </div>
            <div class="modal-footer">
                <input type="hidden" name="source_id" id="source_id" value="">
                <button type="submit" class="btn btn-success source_add" @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Save</button>
                <button type="button" class="btn btn-light" data-dismiss="modal">Close</button>
            </div>
        </form>
      </div>
    </div>
  </div>
@endsection

@section('pageSpecificJs')

<script src="{{asset('assets/bundles/datatables/datatables.min.js')}}"></script>
<script src="{{asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/js/page/datatables.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>

<script>
$(document).ready(function (){
  var dataTable = $('#source-content-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [2], /* column index */
          'orderable': false, /* true or false */
        }], 
    'ajax': {
        'url':'{{ route("showTVChannelSourceList") }}',
        'data': function(data){
            data.channel_id = $("#channel_id").val();
        }
    }
  });

    $('#SourceModal').on('hidden.bs.modal', function(e) {
        $("#addUpdateTVChannelSource")[0].reset();
        $('.modal-title').text('Add Source');
        $('#source_id').val("");
        var validator = $("#addUpdateTVChannelSource").validate();
        validator.resetForm();
    });

    $("#source-content-listing").on("click", ".updateTVChannelSource", function() {
        $('.modal-title').text('Edit Source');
        $('#source_id').val($(this).attr('data-id'));
        $('#source_title').val($(this).attr('data-source_title'));
        $('#source_quality').val($(this).attr('data-source_quality'));
        $('#source_size').val($(this).attr('data-source_size'));
        $('#access_type').val($(this).attr('data-access_type'));
        var is_downladable  = $(this).attr('data-downloadable')
        if(is_downladable ==  1){
            $('#downloadable').prop('checked', true);
        }

        $('#source').val($(this).attr('data-source'));
       
    });
    
    
    $("#addUpdateTVChannelSource").validate({
      rules: {
        source_title: {
          required: true,
        },
        source_quality:{
          required: true,
        },
        source_size:{
          required: true,
        },
        access_type:{
          required: true,
        },
        source:{
          required: true,
        },
      },
      messages: {
        source_title: {
          required: "Please Enter Source Title",
        },
        source_quality: {
          required: "Please Enter Source Quality",
        },
        source_size:{
          required: "Please Enter Source Size",
        },
        access_type:{
          required: "Please Select Access type",
        },
        source: {
          required: "Please Enter Source",
        },
      },

    });

  $(document).on('submit', '#addUpdateTVChannelSource', function (e) {
      e.preventDefault();
      var formdata = new FormData($("#addUpdateTVChannelSource")[0]);
      $('.loader').show();
      $.ajax({
          url: '{{ route("addUpdateTVChannelSource") }}',
          type: 'POST',
          data: formdata,
          dataType: "json",
          contentType: false,
          cache: false,
          processData: false,
          success: function (data) {
              $('.loader').hide();
              $('#SourceModal').modal('hide');
              if (data.success == 1) {
                $('#source-content-listing').DataTable().ajax.reload(null, false);
                $('.total_source').text(data.total_source);
                iziToast.success({
                  title: 'Success!',
                  message: data.message,
                  position: 'topRight'
                });
              } else {
                iziToast.error({
                  title: 'Error!',
                  message: data.message,
                  position: 'topRight'
                });
              }
          },
          error: function (jqXHR, textStatus, errorThrown) {
              alert(errorThrown);
          }
      });
    });

    $(document).on('click', '.DeleteTVChannelSource', function (e) {
        e.preventDefault();
        var channel_id = $("#channel_id").val();
        var source_id = $(this).attr('data-id');
        var text = 'You will not be able to recover Tv Channel Source data!';   
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
                $('.loader').show();
                $.ajax({
                    url: '{{ route("deleteTVChannelSource") }}',
                    type: 'POST',
                    data: {"source_id":source_id,"channel_id":channel_id},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        if (data.success == 1) {
                        $('#source-content-listing').DataTable().ajax.reload(null, false);
                        $('.total_source').text(data.total_source);
                        swal("Confirm!", "Tv Channel Source has been deleted!", "success");
                        } else {
                        swal("Confirm!", "Tv Channel Source has not been deleted!", "error");
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
