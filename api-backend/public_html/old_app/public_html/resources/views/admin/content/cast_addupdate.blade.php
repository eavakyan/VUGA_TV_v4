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
                <h4 class="text-center mb-0"> Edit {{$content_title}} Cast</h4>
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
                <h4>Movie Cast List (<span class="total_cast">{{$total_cast}}</span>)</h4>
            </div>

            <div class="card-body">
                <div class="pull-right">
                    <div class="buttons"> 
                    <button class="btn btn-primary text-light" data-toggle="modal" data-target="#MovieCastModal" data-whatever="@mdo" >Add Movie Cast</button>
                    </div>
                </div>
                        
                <div class="table-responsive">
                    <table class="table table-striped" id="cast-listing" width="100%">
                    <thead>
                        <tr>
                        <th>Image</th>
                        <th>Actor Name </th>
                        <th>Role</th>
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


<div class="modal fade" id="MovieCastModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="ModalLabel"> Add Movie Cast </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateMovieCast" method="post" enctype="multipart">
        {{ csrf_field() }}
            <div class="modal-body">
                <input id="content_id" name="content_id" type="hidden" class="form-control form-control-danger" value="{{$content_id}}">

                <div class="form-group">
                    <label for="actor_id">Select Actor</label>
                    <select id="actor_id" name="actor_id" class="form-control form-control-danger">
                        <option value="">Select</option>
                        @foreach($actorData as $value)
                            <option value="{{$value['actor_id']}}">{{$value['actor_name']}}</option>
                        @endforeach                        
                    </select>
                </div>  

                <div class="form-group">
                    <label for="charactor_name">Actor Role</label>
                    <input id="charactor_name" name="charactor_name" type="text" class="form-control form-control-danger" placeholder="Enter Actor Role">
                </div>

            </div>
            <div class="modal-footer">
                <input type="hidden" name="movie_cast_id" id="movie_cast_id" value="">
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
  var dataTable = $('#cast-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [3], /* column index */
          'orderable': false, /* true or false */
        }], 
    'ajax': {
        'url':'{{ route("showMovieCastList") }}',
        'data': function(data){
          data.content_id = $("#content_id").val();
        }
    }
  });


    $('#MovieCastModal').on('hidden.bs.modal', function(e) {
        $("#addUpdateMovieCast")[0].reset();
        $('.modal-title').text('Add Movie Cast');
        $('#movie_cast_id').val("");
        $('#actor_id').val("");
        var validator = $("#addUpdateMovieCast").validate();
        validator.resetForm();
    });

    $("#cast-listing").on("click", ".updateMovieCast", function() {
        var content_type = $("#content_type").val();
        $('.modal-title').text('Edit Movie Cast');
        $('#movie_cast_id').val($(this).attr('data-id'));
        $('#charactor_name').val($(this).attr('data-charactor_name'));
        $('#actor_id').val($(this).attr('data-actor_id'));
     
       
    });
    
    
    $("#addUpdateMovieCast").validate({
      rules: {
        actor_id: {
          required: true,
            remote: {
                url: '{{ route("CheckExistMCastActor") }}',
                type: "post",
                data: {
                    actor_id: function () { return $("#actor_id").val(); },
                    content_id: function () { return $("#content_id").val(); },
                    movie_cast_id: function () { return $("#movie_cast_id").val(); },
                }
            }
        },
        charactor_name:{
          required: true,
        },
      },
      messages: {
        actor_id: {
          required: "Please Select Actor",
          remote: "Actor Already Exist.",
        },
        charactor_name: {
          required: "Please Enter Role",
        },
      },

    });

  $(document).on('submit', '#addUpdateMovieCast', function (e) {
      e.preventDefault();
      if (user_type == 1) {
        var formdata = new FormData($("#addUpdateMovieCast")[0]);
        $('.loader').show();
        $.ajax({
            url: '{{ route("addUpdateMovieCast") }}',
            type: 'POST',
            data: formdata,
            dataType: "json",
            contentType: false,
            cache: false,
            processData: false,
            success: function (data) {
                $('.loader').hide();
                $('#MovieCastModal').modal('hide');
                if (data.success == 1) {
                  $('#cast-listing').DataTable().ajax.reload(null, false);
                  $('.total_cast').text(data.total_cast);
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

    $(document).on('click', '.DeleteMovieCast', function (e) {
        e.preventDefault();
        var movie_cast_id = $(this).attr('data-id');
        var content_id = $("#content_id").val();
        var text = 'You will not be able to recover Movie Cast data!';   
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
                    url: '{{ route("deleteMovieCast") }}',
                    type: 'POST',
                    data: {"movie_cast_id":movie_cast_id,"content_id":content_id},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        if (data.success == 1) {
                        $('#cast-listing').DataTable().ajax.reload(null, false);
                        $('.total_cast').text(data.total_cast);
                        swal("Confirm!", "Movie Cast has been deleted!", "success");
                        } else {
                        swal("Confirm!", "Movie Cast has not been deleted!", "error");
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
                swal("Confirm!", "Movie Cast has been deleted!", "success");
              }
            } else {
            swal("Cancelled", "Your imaginary file is safe :)", "error");
            }
        });
    });
});
</script>

@endsection
