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
                        <h4>Language List (<span class="total_language">{{$total_language}}</span>)</h4>
                    </div>
                   
                    <div class="card-body">	
                        <div class="pull-right">
                            <div class="buttons"> 
                                <button class="btn btn-primary text-light" data-toggle="modal" data-target="#languageModal" data-whatever="@mdo" >Add Language</button>
                            </div>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-striped" id="language-listing">
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

<div class="modal fade" id="languageModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="ModalLabel"> Add Language </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateLanguage" method="post" enctype="multipart">
        {{ csrf_field() }}
          <div class="modal-body">
            <div class="form-group">
              <label for="language_name">Language Name</label>
              <input id="language_name" name="language_name" type="text" class="form-control form-control-danger" placeholder="Enter Language Name">
            </div>
          </div>
          <div class="modal-footer">
              <input type="hidden" name="language_id" id="language_id" value="">
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
  var dataTable = $('#language-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "asc" ]],
    'columnDefs': [ {
          'targets': [1], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showLanguageList") }}',
        'data': function(data){
        }
    }
  });

  $('#languageModal').on('hidden.bs.modal', function(e) {
      $("#addUpdateLanguage")[0].reset();
      $('.modal-title').text('Add Language');
      $('#language_id').val("");
      var validator = $("#addUpdateLanguage").validate();
      validator.resetForm();
  });

  $("#language-listing").on("click", ".UpdateLanguage", function() {
      $('.modal-title').text('Edit Language');
      $('#language_id').val($(this).attr('data-id'));
      $('#language_name').val($(this).attr('data-language_name'));
  });

  $("#addUpdateLanguage").validate({
      rules: {
        language_name:{
            required: true,
              remote: {
                  url: '{{ route("CheckExistLanguage") }}',
                  type: "post",
                  data: {
                      language_name: function () { return $("#language_name").val(); },
                      language_id: function () { return $("#language_id").val(); },
                  }
              }
          }, 
      },
      messages: {
        language_name: {
            required: "Please Enter Language Name",
            remote: "Language Name Already Exist.",
        },
      }
  });

  $(document).on('submit', '#addUpdateLanguage', function (e) {
    e.preventDefault();
    if (user_type == 1) {
      var formdata = new FormData($("#addUpdateLanguage")[0]);
      $('.loader').show();
      $.ajax({
          url: '{{ route("addUpdateLanguage") }}',
          type: 'POST',
          data: formdata,
          dataType: "json",
          contentType: false,
          cache: false,
          processData: false,
          success: function (data) {
              $('.loader').hide();
              $('#languageModal').modal('hide');
              if (data.success == 1) {

                $('#language-listing').DataTable().ajax.reload(null, false);
                $('.total_language').text(data.total_language);
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

  $(document).on('click', '.DeleteLanguage', function (e) {
    e.preventDefault();
    var language_id = $(this).attr('data-id');
    var text = 'You will not be able to recover Language data!';   
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
                url: '{{ route("deleteLanguage") }}',
                type: 'POST',
                data: {"language_id":language_id},
                dataType: "json",
                cache: false,
                success: function (data) {
                    $('.loader').hide();
                    $('#language-listing').DataTable().ajax.reload(null, false);
                    $('.total_language').text(data.total_language);
                    if (data.success == 1) {
                      swal("Confirm!", "Language has been deleted!", "success");
                    } else {
                      swal("Confirm!", "Language has not been deleted!", "error");
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
                swal("Confirm!", "Language has been deleted!", "success");
              }
          } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
  });

});
</script>

@endsection
