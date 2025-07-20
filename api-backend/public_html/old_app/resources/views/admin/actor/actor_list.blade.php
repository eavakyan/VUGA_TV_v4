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
                        <h4>Actor List (<span class="total_actor">{{$total_actor}}</span>)</h4>
                    </div>
                   
                    <div class="card-body">	
                        <div class="pull-right">
                            <div class="buttons"> 
                                <button class="btn btn-primary text-light" data-toggle="modal" data-target="#actorModal" data-whatever="@mdo" >Add Actor</button>
                            </div>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-striped" id="actor-listing">
                                <thead>
                                <tr>
                                    <th>Profile</th>
                                    <th>Name</th>
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

<div class="modal fade" id="actorModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="ModalLabel"> Add Actor </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateActor" method="post" enctype="multipart">
        {{ csrf_field() }}
          <div class="modal-body">
            <div class="form-group">
                <label for="actor_name">Actor Name</label>
                <input id="actor_name" name="actor_name" type="text" class="form-control form-control-danger" placeholder="Enter Actor Name">
              </div>

              <div class="form-group">
                  <label for="actorprofile">Profile Image</label>
                  <input type="file" class="form-control-file file-upload custom_image valid" id="actor_image" name="actor_image" aria-required="true" aria-invalid="false">
                  <label id="actor_image-error" class="error image_error" for="actor_image" style="display: none;"></label>
                  <div class="preview_actorimg mt-4"></div>
              </div>

          </div>
          <div class="modal-footer">
              <input type="hidden" name="actor_id" id="actor_id" value="">
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
<script src="{{asset('assets/bundles/summernote/summernote-bs4.js')}}"></script>

<script>
$(document).ready(function (){
  var dataTable = $('#actor-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [2], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showActorList") }}',
        'data': function(data){
        }
    }
  });


  $(document).on('change', '#actor_image', function () {
    CheckFileExtention(this,'preview_actorimg');
  });

  var CheckFileExtention = function (input, cl) {

    if (input.files) {
        var allowedExtensions = /(\.jpg|\.jpeg|\.png)$/i;
        if (!allowedExtensions.exec(input.value)) {
            iziToast.error({
                title: 'Error!',
                message: 'Please upload file having extensions .jpeg/.jpg/.png only.',
                position: 'topRight'
              });
            input.value = '';
            return false;
        } else {
            if(cl.length > 0){
                var reader = new FileReader();

                reader.onload = function (e) {
                    $('.' + cl).html('<div class=""><img src="'+e.target.result+'" width="150" height="150"/> </div>');
                }

                reader.readAsDataURL(input.files[0]);
            }
        }
    }
  };

  $('#actorModal').on('hidden.bs.modal', function(e) {
      $("#addUpdateActor")[0].reset();
      $('.modal-title').text('Add Actor');
      $('#actor_id').val("");
      $('#about_actor').summernote("code", "");
      $('.preview_actorimg').html("");
      var validator = $("#addUpdateActor").validate();
      validator.resetForm();
  });

  $("#actor-listing").on("click", ".UpdateActor", function() {
      $('.modal-title').text('Edit Actor');
      $('#actor_id').val($(this).attr('data-id'));
      $('#actor_name').val($(this).attr('data-actor_name'));
      var image = $(this).attr('data-image');
      $('.preview_actorimg').html('<div class=""><img src="'+image+'" width="150" height="150"/> </div>');
  });

  $("#addUpdateActor").validate({
      rules: {
        actor_name:{
            required: true,
              remote: {
                  url: '{{ route("CheckExistActor") }}',
                  type: "post",
                  data: {
                      actor_name: function () { return $("#actor_name").val(); },
                      actor_id: function () { return $("#actor_id").val(); },
                  }
              }
          },
      },
      messages: {
        actor_name: {
            required: "Please Enter Actor Name",
            remote: "Actor Name Already Exist.",
        },
      }
  });

  $(document).on('submit', '#addUpdateActor', function (e) {
    e.preventDefault();
    if (user_type == 1) {
      var formdata = new FormData($("#addUpdateActor")[0]);
      $('.loader').show();
      $.ajax({
          url: '{{ route("addUpdateActor") }}',
          type: 'POST',
          data: formdata,
          dataType: "json",
          contentType: false,
          cache: false,
          processData: false,
          success: function (data) {
              $('.loader').hide();
              $('#actorModal').modal('hide');
              if (data.success == 1) {

                $('#actor-listing').DataTable().ajax.reload(null, false);
                $('.total_actor').text(data.total_actor);
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

  $(document).on('click', '.DeleteActor', function (e) {
    e.preventDefault();
    var actor_id = $(this).attr('data-id');
    var text = 'You will not be able to recover Actor data!';   
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
                url: '{{ route("deleteActor") }}',
                type: 'POST',
                data: {"actor_id":actor_id},
                dataType: "json",
                cache: false,
                success: function (data) {
                    $('.loader').hide();
                    $('#actor-listing').DataTable().ajax.reload(null, false);
                    $('.total_actor').text(data.total_actor);
                    if (data.success == 1) {
                      swal("Confirm!", "Actor has been deleted!", "success");
                    } else {
                      swal("Confirm!", "Actor has not been deleted!", "error");
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
            swal("Confirm!", "Actor has been deleted!", "success");
          }
        } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
  });

});
</script>

@endsection
