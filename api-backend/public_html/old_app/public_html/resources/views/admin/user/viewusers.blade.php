@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">
@stop
@section('content')

<section class="section">
  <div class="section-body">
    <div class="row mt-sm-4">
      <div class="col-12 col-md-12 col-lg-4">
        <div class="card author-box">
          <div class="card-body">
            <div class="author-box-center">
              <img alt="image" style="    width: 120px; height: 120px;" src="@if($data->profile_image){{url(env('DEFAULT_IMAGE_URL').$data->profile_image)}}@else{{asset('assets/dist/img/default.png')}}@endif" class="rounded-circle author-box-picture user-profile">
              <div class="clearfix" style="margin-top: 20px;"> <button type="button" data-toggle="modal" data-target="#editModal" class="btn btn-primary btn-lg" >Edit Profile </button> </div>
              <div class="author-box-name" style="margin-top: 10px;">
                <h5>{{$data->first_name}} {{$data->last_name}}</h5>
              </div>
              <div class="author-box-job"></div>
            </div>
            
          </div>
        </div>
      </div>
      <input type="hidden" name="hidden_user_id" id="hidden_user_id" value="{{$data->user_id}}">

      <div class="col-12 col-md-12 col-lg-8">
        <div class="card">
          <div class="padding-20">
            <ul class="nav nav-tabs" id="myTab2" role="tablist">
              <li class="nav-item">
                <a class="nav-link active" id="home-tab2" data-toggle="tab" href="#about" role="tab" aria-selected="true">About</a>
              </li>
            </ul>
            <div class="tab-content tab-bordered" id="myTab3Content">
              <div class="tab-pane fade show active" id="about" role="tabpanel" aria-labelledby="home-tab2">
                <div class="row">
                  <div class="col-md-3 col-6 b-r">
                    <strong>Full Name</strong>
                    <br>
                    <p class="text-muted">{{$data->fullname}}</p>
                  </div>
                  <div class="col-md-6 col-6 b-r">
                    <strong>Email</strong>
                    <br>
                    <p class="text-muted">{{$data->email}}</p>
                  </div>
                  <div class="col-md-3 col-6">
                    
                  </div>
                </div>
                
                
              </div>
              
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-12 col-lg-12">
        <div class="card">
          <div class="padding-20">
            <ul class="nav nav-tabs" id="myTab2" role="tablist">
              <li class="nav-item">
                <a class="nav-link active" id="home-tab2" data-toggle="tab" href="#about" role="tab" aria-selected="true">Subscription </a>
              </li>
            </ul>
            <br/>
            <div class="table-responsive">
              <table class="table table-striped" id="subscription-listing" style="width:100%;">
                <thead>
                  <tr>
                    <!-- <th> ID </th> -->
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
  </div>
</section>


<div class="modal fade" id="editModal" tabindex="-1" role="dialog" aria-labelledby="formModal"
          aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="formModal">Edit Profile Image</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
      <form name="edit_profile"  id="edit_profile">
      
          <input type="hidden" name="user_id" id="user_id" value="{{$data->user_id}}">
          <div class="form-group">
            <label>Profile Picture</label>
            <div class="input-group">
              <div class="input-group-prepend">
                <div class="input-group-text">
                  <i class="fas fa-list"></i>
                </div>
              </div>
                <input type="file" class="form-control" name="profile_image" id="profile_image">
              <br/>
            </div>
          </div>
          <input type="hidden" name="hdn_profile_image" id="hdn_profile_image" value="{{$data->profile_image}}">
          <div class="form-group">
            <p id="preview1" style="text-align: center;"><img src="@if($data->profile_image){{url(env('DEFAULT_IMAGE_URL').$data->profile_image)}}@else{{asset('assets/dist/img/default.png')}}@endif" width="100" height="100" id="preview_img_1" style=" margin-right: 5px;"/></p>
          </div>
  
          <input type="submit" class="btn btn-primary m-t-15 waves-effect" value="Save" id="language_from_btn" >
        </form>
      </div>
    </div>
  </div>
</div>

@stop

@section('pageSpecificJs')

<script src="{{asset('assets/bundles/datatables/datatables.min.js')}}"></script>
<script src="{{asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/js/page/datatables.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>

<script>
$(document).ready(function (){
  var dataTable = $('#user-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [0], /* column index */
          'orderable': false, /* true or false */
        }],
    'ajax': {
        'url':'{{ route("showUserList") }}',
        'data': function(data){
          data.is_premium = 2;
        }
    }
  });

  $(document).on('change', '#profile_image', function () {
    CheckFileExtention(this,'preview1');
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
                    $('#' + cl).html('<img src="'+e.target.result+'" width="100" height="100" id="preview_img_1" style=" margin-right: 5px;"/>');
                }

                reader.readAsDataURL(input.files[0]);
            }
        }
    }
  };

  if($("form[name='edit_profile']").length > 0){
    

      $("form[name='edit_profile']").validate({
        submitHandler: function (form) {
          if (user_type == 1) {
          $('.loader').show();
          $.ajax({
                url: '{{ route("updateUserProfile") }}',
                data: new FormData($('#edit_profile')[0]),
                type: 'POST',
                contentType: false,
                cache: false,
                processData: false,
                dataType : "json",
                success: function ( data ) {
                    $('.loader').hide();
                  if(data.status == 1)
                  {
                        if(data.user_profile_url){
                            $('.user-profile').attr('src',data.user_profile_url);
                        }
                        $('#editModal').modal('hide');

                    successmessage('Success', 'Admin profile update successfully');
                  }
                  else
                  {
                    errormessage('Error', 'Admin profile update failed');
                  }
                }
            });
          } else{
        iziToast.error({
              title: 'Error!',
              message: ' you are Tester ',
              position: 'topRight'
          });
      }
      }
    });
  
  }


    var dataTable = $('#subscription-listing').dataTable({
        'processing': true,
        'serverSide': true,
        'serverMethod': 'post',
        "order": [[ 0, "desc" ]],
        'columnDefs': [ {
            'targets': [0], /* column index */
            'orderable': false, /* true or false */
            }],
        'ajax': {
            'url':'{{ route("showSubscriptionList") }}',
            'data': function(data){
            data.status = 1;
            data.user_id = $('#hidden_user_id').val();
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
          } else{
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
