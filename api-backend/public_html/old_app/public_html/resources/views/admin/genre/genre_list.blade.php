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
                        <h4>Genre List (<span class="total_genre">{{$total_genre}}</span>)</h4>
                    </div>
                   
                    <div class="card-body">	
                        <div class="pull-right">
                            <div class="buttons"> 
                                <button class="btn btn-primary text-light" data-toggle="modal" data-target="#genreModal" data-whatever="@mdo">Add Genre</button>
                            </div>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-striped" id="genre-listing">
                                <thead>
                                <tr>
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

<div class="modal fade" id="genreModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="ModalLabel"> Add Genre </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateGenre" method="post" enctype="multipart">
        {{ csrf_field() }}
          <div class="modal-body">
            <div class="form-group">
              <label for="genre_name">Genre Name</label>
              <input id="genre_name" name="genre_name" type="text" class="form-control form-control-danger" placeholder="Enter Genre Name">
            </div>
          </div>
          <div class="modal-footer">
              <input type="hidden" name="genre_id" id="genre_id" value="">
            <button type="submit" class="btn btn-success">Save</button>
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
  var dataTable = $('#genre-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "asc" ]],
    'columnDefs': [ {
          'targets': [1], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showGenreList") }}',
        'data': function(data){
        }
    }
  });

  $('#genreModal').on('hidden.bs.modal', function(e) {
      $("#addUpdateGenre")[0].reset();
      $('.modal-title').text('Add Genre');
      $('#genre_id').val("");
      var validator = $("#addUpdateGenre").validate();
      validator.resetForm();
  });

  $("#genre-listing").on("click", ".UpdateGenre", function() {
      $('.modal-title').text('Edit Genre');
      $('#genre_id').val($(this).attr('data-id'));
      $('#genre_name').val($(this).attr('data-genre_name'));
  });

  $("#addUpdateGenre").validate({
      rules: {
        genre_name:{
            required: true,
              remote: {
                  url: '{{ route("CheckExistGenre") }}',
                  type: "post",
                  data: {
                      genre_name: function () { return $("#genre_name").val(); },
                      genre_id: function () { return $("#genre_id").val(); },
                  }
              }
          }, 
      },
      messages: {
        genre_name: {
            required: "Please Enter Genre Name",
            remote: "Genre Name Already Exist.",
        },
      }
  });

  $(document).on('submit', '#addUpdateGenre', function (e) {
    e.preventDefault();
    if (user_type == 1) {
      var formdata = new FormData($("#addUpdateGenre")[0]);
      $('.loader').show();

      $.ajax({
          url: '{{ route("addUpdateGenre") }}',
          type: 'POST',
          data: formdata,
          dataType: "json",
          contentType: false,
          cache: false,
          processData: false,
          success: function (data) {
              $('.loader').hide();
              $('#genreModal').modal('hide');
              if (data.success == 1) {

                $('#genre-listing').DataTable().ajax.reload(null, false);
                $('.total_genre').text(data.total_genre);
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

  $(document).on('click', '.DeleteGenre', function (e) {
    e.preventDefault();
    var genre_id = $(this).attr('data-id');
    var text = 'You will not be able to recover Genre data!';   
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
                url: '{{ route("deleteGenre") }}',
                type: 'POST',
                data: {"genre_id":genre_id},
                dataType: "json",
                cache: false,
                success: function (data) {
                    $('.loader').hide();
                    $('#genre-listing').DataTable().ajax.reload(null, false);
                    $('.total_genre').text(data.total_genre);
                    if (data.success == 1) {
                      swal("Confirm!", "Genre has been deleted!", "success");
                    } else {
                      swal("Confirm!", "Genre has not been deleted!", "error");
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
                swal("Confirm!", "Genre has been deleted!", "success");
              }
          } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
  });

});
</script>

@endsection
