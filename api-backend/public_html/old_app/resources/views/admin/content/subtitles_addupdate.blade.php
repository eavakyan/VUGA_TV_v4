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
          @if($content_id) 
          <div class="card card-stats">
              <div class="card-content views-body  pull-right">
                <a href="#" class="btn btn-tab-movie"><i class="material-icons">remove_red_eye</i> {{ $data->total_view }} Views</a>
                <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">cloud_download</i> {{ $data->total_download }} Downloads</a>
                <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">share</i> {{ $data->total_share }} Shares</a>
              </div>
            <div class="card-body">
              <h4 class="text-center mb-0"> Edit {{$content_title}} Subtitles</h4> 
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
                <h4>@if($content_type == 1){{"Movie"}}@else{{"Series"}}@endif Subtitles List (<span class="total_subtitles">{{$total_subtitles}}</span>)</h4>
            </div>

            <div class="card-body">
                <div class="pull-right">
                    <div class="buttons"> 
                    <button class="btn btn-primary text-light" data-toggle="modal" data-target="#subtitlesModal" data-whatever="@mdo" >Add @if($content_type == 1){{"Movie"}}@else{{"Series"}}@endif Subtitles</button>
                    </div>
                </div>
                        
                <div class="table-responsive">
                    <table class="table table-striped" id="subtitles-listing" width="100%">
                    <thead>
                        <tr>
                        <th>Language Name </th>
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


<div class="modal fade" id="subtitlesModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="ModalLabel"> Add @if($content_type == 1){{"Movie"}}@else{{"Series"}}@endif Subtitles </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateContentSubtitles" method="post" enctype="multipart">
        {{ csrf_field() }}
            <div class="modal-body">
                <input id="content_id" name="content_id" type="hidden" class="form-control form-control-danger" value="{{$content_id}}">
                <input id="content_type" name="content_type" type="hidden" class="form-control form-control-danger" value="{{$content_type}}">

                <div class="form-group">
                    <label for="language_id">Select Language</label>
                    <select id="language_id" name="language_id" class="form-control form-control-danger">
                        <option value="">Select</option>
                        @foreach($languagedata as $value)
                            <option value="{{$value['language_id']}}">{{$value['language_name']}}</option>
                        @endforeach                        
                    </select>
                </div>  

                <div class="form-group">
                    <label for="subtitle_file">Subtitle File</label>
                    <input type="file" class="form-control-file file-upload custom_image valid" id="subtitle_file" name="subtitle_file" aria-required="true" aria-invalid="false">
                </div>

            </div>
            <div class="modal-footer">
                <input type="hidden" name="subtitles_id" id="subtitles_id" value="">
                <button type="submit" class="btn btn-success" >Save</button>
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
  var dataTable = $('#subtitles-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [1], /* column index */
          'orderable': false, /* true or false */
        }], 
    'ajax': {
        'url':'{{ route("showContentSubtitleList") }}',
        'data': function(data){
            data.content_type = $("#content_type").val();
            data.content_id = $("#content_id").val();
        }
    }
  });

    $('#subtitlesModal').on('hidden.bs.modal', function(e) {
        $("#addUpdateContentSubtitles")[0].reset();
        var content_type = $("#content_type").val();
        if(content_type == 1){
          content_type = 'Movie';
        }else{
          content_type = 'Series';
        }
        $('.modal-title').text('Add '+content_type+' Subtitles');
        $('#subtitles_id').val("");
        $('#language_id').val("");
        var validator = $("#addUpdateContentSubtitles").validate();
        validator.resetForm();
    });

    $("#subtitles-listing").on("click", ".updateSubtitles", function() {
        var content_type = $("#content_type").val();
        if(content_type == 1){
          content_type = 'Movie';
        }else{
          content_type = 'Series';
        }
        $('.modal-title').text('Edit '+content_type+' Subtitles');
        $('#subtitles_id').val($(this).attr('data-id'));
        $('#language_id').val($(this).attr('data-language_id'));
    });
    
    
    $("#addUpdateContentSubtitles").validate({
      rules: {
        language_id: {
          required: true,
        },
        subtitle_file:{
            required: true,
        },
      },
      messages: {
        language_id: {
          required: "Please Select Actor",
        },
        subtitle_file: {
          required: "Please Select File",
        },
      },

    });

  $(document).on('submit', '#addUpdateContentSubtitles', function (e) {
      e.preventDefault();
      if (user_type == 1) {
        var formdata = new FormData($("#addUpdateContentSubtitles")[0]);
        $('.loader').show();
        $.ajax({
            url: '{{ route("addUpdateContentSubtitles") }}',
            type: 'POST',
            data: formdata,
            dataType: "json",
            contentType: false,
            cache: false,
            processData: false,
            success: function (data) {
                $('.loader').hide();
                $('#subtitlesModal').modal('hide');
                if (data.success == 1) {
                  $('#subtitles-listing').DataTable().ajax.reload(null, false);
                  $('.total_subtitles').text(data.total_subtitles);
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
      }else{
        iziToast.error({
            title: 'Error!',
            message: ' you are Tester ',
            position: 'topRight'
        });
      }
    });

    $(document).on('click', '.DeleteContentSubtitles', function (e) {
        e.preventDefault();
        var content_type = $("#content_type").val();
        var content_id = $("#content_id").val();
        var subtitles_id = $(this).attr('data-id');
        var text = 'You will not be able to recover Content Subtitles data!';   
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
                    url: '{{ route("deleteContentSubtitles") }}',
                    type: 'POST',
                    data: {"subtitles_id":subtitles_id,"content_id":content_id,"content_type":content_type},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        if (data.success == 1) {
                        $('#subtitles-listing').DataTable().ajax.reload(null, false);
                        $('.total_subtitles').text(data.total_subtitles);
                        swal("Confirm!", "Movie Subtitles has been deleted!", "success");
                        } else {
                        swal("Confirm!", "Movie Subtitles has not been deleted!", "error");
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
                swal("Confirm!", "Movie Subtitles has been deleted!", "success");
              }
            } else {
            swal("Cancelled", "Your imaginary file is safe :)", "error");
            }
        });
    });
});
</script>

@endsection
