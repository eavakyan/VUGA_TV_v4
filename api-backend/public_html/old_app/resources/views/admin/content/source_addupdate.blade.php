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
            <h4 class="text-center mb-0"> Edit {{$content_title}} Source</h4>
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
    <div class="row">
        <div class="col-12">
            <div class="card">
            <div class="card-header">
                <h4>@if($content_type == 1){{"Movie"}}@else{{"Series"}}@endif Source List (<span class="total_source">{{$total_source}}</span>)</h4>
            </div>

            <div class="card-body">
                <div class="pull-right">
                    <div class="buttons"> 
                    <button class="btn btn-primary text-light" data-toggle="modal" data-target="#SourceModal" data-whatever="@mdo" >Add @if($content_type == 1){{"Movie"}}@else{{"Series"}}@endif Source</button>
                    </div>
                </div>
                        
                <div class="table-responsive">
                    <table class="table table-striped" id="source-content-listing" width="100%">
                    <thead>
                        <tr>
                        <th>Type</th>
                        <th>Title </th>
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
          <h5 class="modal-title" id="ModalLabel"> Add @if($content_type == 1){{"Movie"}}@else{{"Series"}}@endif Source </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateContentSource" method="post" enctype="multipart/form-data">
        {{ csrf_field() }}
            <div class="modal-body">

                <input id="content_type" name="content_type" type="hidden" class="form-control form-control-danger" value="{{$content_type}}">
                <input id="content_id" name="content_id" type="hidden" class="form-control form-control-danger" value="{{$content_id}}">

                <div class="form-group">
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
                </div>  

                <div class="form-group">
                    <label for="source_type">Select Source Type</label>
                    <select name="source_type" class="form-control form-control-danger" id="source_type">
                    <option value="1">Youtube Id</option>
                    <option value="2">M3u8 Url</option>
                    <option value="3">Mov Url</option>
                    <option value="4">Mp4 Url</option>
                    <option value="5">Mkv Url</option>
                    <option value="6">Webm Url</option>
                    <option value="7">File Upload (Mp4, Mov, Mkv, Webm)</option>
                    </select>
                </div> 

                <div class="form-group SourceURLDiv">
                    <label for="source">Source</label>
                    <input type="text"  name="source" class="form-control" id="source" placeholder="Source URL">
                </div>

                <div class="form-group SourceFileDiv" style="display:none;">
                    <div class="form-row">
                        <div class="form-group col-md-8">
                            <label for="source_video">Source File</label>
                            <input type="file" name="source_video" class="form-control source_video"/>
                            <input type="hidden" name="source_file" class="hidden_source_video">
                            <div class="progress progress" style="height: 25px;margin:10px 0px;">
                            <div class="progress-bar" width="">0%</div>
                            </div>
                        </div>
                        <div class="col-md-4 source_preview">
                        </div>
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <input type="hidden" name="source_id" id="source_id" value="">
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
  var dataTable = $('#source-content-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [3], /* column index */
          'orderable': false, /* true or false */
        }], 
    'ajax': {
        'url':'{{ route("showContentSourceList") }}',
        'data': function(data){
            data.content_type = $("#content_type").val();
            data.content_id = $("#content_id").val();
        }
    }
  });

  
  $(document).on('change', '#source_type', function() {
      var value = $(this).val();
      if(value == 7){
        $(this).parent().parent().find(".SourceFileDiv").show();
        $(this).parent().parent().find(".SourceURLDiv").hide();
      }else{
        $(this).parent().parent().find(".SourceURLDiv").show();
        $(this).parent().parent().find(".SourceFileDiv").hide();
      }
      
    });

    $(document).on('change', '.source_video', function() {
      videoPreview(this);
    });

    var videoPreview = function(input) {

        if (input.files) {
        var filesAmount = input.files.length;
        var allowedExtensions = /(\.mp4|\.mov|\.mkv|\.webm)$/i;
        for (i = 0; i < filesAmount; i++) {

            if(!allowedExtensions.exec(input.value)){
            iziToast.error({
                title: 'Error!',
                message: 'Please upload correct file.',
                position: 'topRight'
            });
            input.value = '';
            return false;
            }else{

            $('.source_add').attr('disabled',true);
            var formdata = new FormData($("#addUpdateContentSource")[0]);

            $.ajax({
                xhr: function() {
                var xhr = new window.XMLHttpRequest();
                xhr.upload.addEventListener("progress", function(evt) {
                    if (evt.lengthComputable) {
                    var percentComplete = ((evt.loaded / evt.total) * 100);
                    percentComplete = percentComplete.toFixed(2);
                    $(input).parent().find(".progress-bar").width(percentComplete + '%');
                    $(input).parent().find(".progress-bar").html(percentComplete + '%');
                    }
                }, false);
                    return xhr;
                },
                type: 'POST',
                url: '{{ route("UploadContentSourceMedia") }}',
                data: formdata,
                contentType: false,
                cache: false,
                processData: false,
                dataType: "json",
                beforeSend: function() {
                    $(input).parent().find(".progress-bar").width('0%');
                    $(input).parent().parent().find('.source_preview').html("");
                },
                error: function() {
                    $(input).parent().parent().find('.source_preview').html('<p style="color:#EA4335;">File upload failed, please try again.</p>');
                },
                success: function(data) {
                    if (data.success == 1) {
                        $('.source_add').attr('disabled',false);
                        $(input).parent().find('.hidden_source_video').val(data.source_video);
                        $('.source_preview').html('<div class=""><video width="150" height="150" class="displayimg1" controls=""> <source src="'+data.default_path+'/'+data.source_video+'" type="video/mp4"> </video> </div>');
                        $(".source_video").val("");
                    } 
                }
            });
            }
        }
        }
    };


    $('#SourceModal').on('hidden.bs.modal', function(e) {
        $("#addUpdateContentSource")[0].reset();
        var content_type = $("#content_type").val();
        if(content_type == 1){
          content_type = 'Movie';
        }else{
          content_type = 'Series';
        }
        $('.modal-title').text('Add '+content_type+' Source');
        $('#source_id').val("");
        $('.source_preview').html("");
        $(".SourceURLDiv").show();
        $(".SourceFileDiv").hide();
        $(".progress-bar").width('0%');
        var validator = $("#addUpdateContentSource").validate();
        validator.resetForm();
    });

    $("#source-content-listing").on("click", ".updateContentSource", function() {
        var content_type = $("#content_type").val();
        if(content_type == 1){
          content_type = 'Movie';
        }else{
          content_type = 'Series';
        }
        $('.modal-title').text('Edit '+content_type+' Source');
        $('#source_id').val($(this).attr('data-id'));
        $('#source_title').val($(this).attr('data-source_title'));
        $('#source_quality').val($(this).attr('data-source_quality'));
        $('#source_size').val($(this).attr('data-source_size'));
        $('#access_type').val($(this).attr('data-access_type'));
        var is_downladable  = $(this).attr('data-downloadable')
        if(is_downladable ==  1){
            $('#downloadable').prop('checked', true);
        }

        var source_type = $(this).attr('data-source_type');
        $('#source_type').val(source_type);
        if(source_type == 7){
            $(".SourceFileDiv").show();
            $(".SourceURLDiv").hide();
            var source_video = $(this).attr('data-source');
            $('.source_preview').html('<div class=""><video width="150" height="150" class="displayimg1" controls=""> <source src="'+source_video+'" type="video/mp4"> </video> </div>');
             $('.hidden_source_video').val($(this).attr('data-source_video'));
        }else{
            $('#source').val($(this).attr('data-source'));
            $(".SourceFileDiv").hide();
            $(".SourceURLDiv").show();
        }
       
    });
    
    
    $("#addUpdateContentSource").validate({
      rules: {
        source_title: {
          required: true,
        },
        source_quality:{
          required: true,
        },
        // source_size:{
        //   required: true,
        // },
        access_type:{
          required: true,
        },
        source:{
          required: true,
        },
        source_video:{
          required: {
            depends: function(element) {
               return ($('.hidden_source_video').val() == '' && $('#source_type').val() == 7)
            }
          },
        },
      },
      messages: {
        source_title: {
          required: "Please Enter Source Title",
        },
        source_quality: {
          required: "Please Enter Source Quality",
        },
        // source_size:{
        //   required: "Please Enter Source Size",
        // },
        access_type:{
          required: "Please Select Access type",
        },
        source: {
          required: "Please Enter Source",
        },
        source_video: {
          required: "Please Upload Source",
        },
      },

    });

  $(document).on('submit', '#addUpdateContentSource', function (e) {
      e.preventDefault();
      if (user_type == 1) {
        var formdata = new FormData($("#addUpdateContentSource")[0]);
        $('.loader').show();
        $.ajax({
            url: '{{ route("addUpdateContentSource") }}',
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
      }else{
        iziToast.error({
            title: 'Error!',
            message: ' you are Tester ',
            position: 'topRight'
        });
      }
    });

    $(document).on('click', '.DeleteContentSource', function (e) {
        e.preventDefault();
        var content_type = $("#content_type").val();
        var content_id = $("#content_id").val();
        var source_id = $(this).attr('data-id');
        var text = 'You will not be able to recover Content Source data!';   
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
                    url: '{{ route("deleteContentSource") }}',
                    type: 'POST',
                    data: {"source_id":source_id,"content_id":content_id,"content_type":content_type},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        if (data.success == 1) {
                        $('#source-content-listing').DataTable().ajax.reload(null, false);
                        $('.total_source').text(data.total_source);
                        swal("Confirm!", "Content Source has been deleted!", "success");
                        } else {
                        swal("Confirm!", "Content Source has not been deleted!", "error");
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
                swal("Confirm!", "Content Source has been deleted!", "success");
              }
            } else {
            swal("Cancelled", "Your imaginary file is safe :)", "error");
            }
        });
    });
});
</script>

@endsection
