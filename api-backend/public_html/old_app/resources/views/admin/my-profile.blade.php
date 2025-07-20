@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">

<style type="text/css">

label.error {
    color: red !important;
    position: absolute !important;
    width: 100% !important;
    top: 40px !important;
    line-height: 14px !important;
    left: 0px !important;
}

</style>
@stop
@section('content')

<section class="section">
          <div class="section-body">
            <div class="row mt-sm-4">
              <div class="col-12 col-md-12 col-lg-4">
                <div class="card author-box">
                  <div class="card-body">
                    <div class="author-box-center">
                      <img alt="image" src="{{url(env('DEFAULT_IMAGE_URL').$data['profile_image'])}}" class="author-box-picture author-box-profile">
                      <div class="clearfix"></div>
                      <div class="author-box-name">{{$data['username']}}
                      </div>
                      <div class="author-box-email">{{$data['email']}}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="col-12 col-md-12 col-lg-8">
                <div class="card">
                  <div class="padding-20">
                    <ul class="nav nav-tabs" id="myTab2" role="tablist">
                      <li class="nav-item">
                        <a class="nav-link active" id="profile-tab2" data-toggle="tab" href="#edit_profile" role="tab"
                          aria-selected="false">Edit Profile</a>
                      </li>
                    </ul>
                    <div class="tab-content tab-bordered" id="myTab3Content">
                      <div class="tab-pane fade show active" id="edit_profile" role="tabpanel" aria-labelledby="profile-tab2">
                        <form method="post" id="updateAdminProfile" class="needs-validation">
                          <div class="card-header">
                            <h4>Edit Profile</h4>
                          </div>
                          <div class="card-body">
                            <div class="row">
                              <div class="form-group col-md-6 col-12">
                                <label>User Name</label>
                                <input type="text" class="form-control" name="admin_name" value="{{$data['username']}}">
                              </div>
                            </div>
                            <div class="row">
                              <div class="form-group col-md-6 col-12">
                                <label>Email</label>
                                <input type="email" class="form-control" name="email" value="{{$data['email']}}">
                              </div>
                            </div>
                            <div class="row">
                              <div class="form-group col-md-6 col-12">
                                <label>Pssword</label>
                                <input type="password" class="form-control" name="password" value="">
                              </div>
                            </div>
                            <div class="row">
                              <div class="form-group">
                                <label>Profile Image</label>
                                <input type="file" name="admin_profile" class="form-control">
                                <input type="hidden" class="form-control hdn_profile_image" name="hdn_profile_image" value="{{$data['profile_image']}}">
                              </div>
                            </div>
                          </div>
                          <div class="card-footer text-right">
                          <input type="hidden" class="form-control" name="admin_id" value="{{$data['id']}}">
                            <button class="btn btn-primary" type="submit">Save</button>
                          </div>
                        </form>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
@stop
@section('pageSpecificJs')
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>
<script type="text/javascript">
$(document).ready(function() {
   
    $('#updateAdminProfile').validate({
        rules: {
          admin_name: {
                required: true,
            },
            email: {
                required: true,
            },
        },
        messages: {
          admin_name: {
                required: "Please enter userame",
            },
            email: {
                required: "Please enter email",
            },
        },
    });

    $(document).on('submit', '#updateAdminProfile', function(e) {
      e.preventDefault();
      if (user_type == 1) {
      var formdata = new FormData($("#updateAdminProfile")[0]);
            $('.loader').show();
        $.ajax({
            url: '{{ route("updateAdminProfile") }}',
            type: "post",
            data: formdata,
            cache: false,
            processData: false,
            contentType: false,
            dataType: "json",
            success: function(response) {
                $('.loader').hide();
                if (response.status == 1) {
                  $('.author-box-name').text(response.admin_name);
                  $('.author-box-email').text(response.admin_email);
                  if(response.admin_profile){
                      $('.author-box-profile').attr('src',response.admin_profile_url);
                      $('.hdn_profile_image').val(response.admin_profile);
                  }
                  $("#profile_image").val('');

                  iziToast.success({
                    title: 'Success!',
                    message: 'Profile Updated Successfully',
                    position: 'topRight'
                  });
                } else {
                  iziToast.error({
                    title: 'Error!',
                    message: 'Profile Not Updated',
                    position: 'topRight'
                  });
                }
            },
        });
        return false;
      } else{
        iziToast.error({
              title: 'Error!',
              message: ' you are Tester ',
              position: 'topRight'
          });
      }
    });
    

});
</script>
@stop