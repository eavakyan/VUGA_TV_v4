@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">

@stop
@section('content')
<section class="section">
  <div class="row">
    <div class="col-12 col-md-12 col-lg-12">
      <div class="card">
        <div class="card-header">
          <h4>Ads</h4>
        </div>

        <div class="tab" role="tabpanel">
                <ul class="nav nav-pills border-b mb-0 p-3">
                    <li role="presentation" class="nav-item"><a class="nav-link pointer active" href="#Section0" aria-controls="home" role="tab" data-toggle="tab">Android </a></li>
                    <li role="presentation" class="nav-item"><a class="nav-link pointer" href="#Section1" aria-controls="home" role="tab" data-toggle="tab">Ios </a></li>
                </ul>
                <div class="tab-content tabs" id="home">
                
                  <div role="tabpanel" class="tab-pane active" id="Section0">
              
                    <div class="card-body">	
                    
                      <form class="forms-sample" id="addUpdateAndriodAds"> 
                        <div class="card-body">
                          <div class="form-row">
                            <div class="form-group col-md-6">
                              <label for="android_admob_banner_id">Banner Id</label>
                              <input type="text"  name="android_admob_banner_id" class="form-control" id="android_admob_banner_id" placeholder="Banner Id" value="@if(isset($AdsData)){{$AdsData['android_admob_banner_id']}}@endif">
                            </div>    
                            <div class="form-group col-md-6">
                              <label for="android_admob_interestitial_id">Interestitial Id</label>
                              <input type="text"  name="android_admob_interestitial_id" class="form-control" id="android_admob_interestitial_id" placeholder="Interestitial Id" value="@if(isset($AdsData)){{$AdsData['android_admob_interestitial_id']}}@endif">
                            </div>            
                          </div>

                          <div class="form-row">
                            <div class="form-group col-md-6">
                              <label for="android_admob_native_id">Native Id</label>
                              <input type="text"  name="android_admob_native_id" class="form-control" id="android_admob_native_id" placeholder="Native Id" value="@if(isset($AdsData)){{$AdsData['android_admob_native_id']}}@endif">
                            </div>    
                            <div class="form-group col-md-6">
                              <label for="android_admob_rewarded_id">Rewarded ID</label>
                              <input type="text"  name="android_admob_rewarded_id" class="form-control" id="android_admob_rewarded_id" placeholder="Rewarded ID" value="@if(isset($AdsData)){{$AdsData['android_admob_rewarded_id']}}@endif">
                            </div>            
                          </div>

                        </div>
                        <div class="card-footer">
                          <input type="hidden" name="hidden_id" class="hidden_id" value="@if(isset($AdsData)){{$AdsData['ads_id']}}@endif">
                          <button class="btn btn-primary" >Submit</button>
                        </div>
                      </form>
                    </div>
                  </div>    

                  <div role="tabpanel" class="tab-pane" id="Section1">
              
                    <div class="card-body">	
                      
                      <form class="forms-sample" id="addUpdateIosAds"> 
                        <div class="card-body">
                          <div class="form-row">
                            <div class="form-group col-md-6">
                              <label for="ios_admob_banner_id">Banner Id</label>
                              <input type="text"  name="ios_admob_banner_id" class="form-control" id="ios_admob_banner_id" placeholder="Banner Id" value="@if(isset($AdsData)){{$AdsData['ios_admob_banner_id']}}@endif">
                            </div>    
                            <div class="form-group col-md-6">
                              <label for="ios_admob_interestitial_id">Interestitial Id</label>
                              <input type="text"  name="ios_admob_interestitial_id" class="form-control" id="ios_admob_interestitial_id" placeholder="Interestitial Id" value="@if(isset($AdsData)){{$AdsData['ios_admob_interestitial_id']}}@endif">
                            </div>            
                          </div>

                          <div class="form-row">
                            <div class="form-group col-md-6">
                              <label for="ios_admob_native_id">Native Id</label>
                              <input type="text"  name="ios_admob_native_id" class="form-control" id="ios_admob_native_id" placeholder="Native Id" value="@if(isset($AdsData)){{$AdsData['ios_admob_native_id']}}@endif">
                            </div>    
                            <div class="form-group col-md-6">
                              <label for="ios_admob_rewarded_id">Rewarded ID</label>
                              <input type="text"  name="ios_admob_rewarded_id" class="form-control" id="ios_admob_rewarded_id" placeholder="Rewarded ID" value="@if(isset($AdsData)){{$AdsData['ios_admob_rewarded_id']}}@endif">
                            </div>            
                          </div>

                        </div>
                        <div class="card-footer">
                          <input type="hidden" name="hidden_id" class="hidden_id" value="@if(isset($AdsData)){{$AdsData['ads_id']}}@endif">
                          <button class="btn btn-primary" >Submit</button>
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


@endsection

@section('pageSpecificJs')

<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>
<script>
$(document).ready(function (){

  $(document).on('submit', '#addUpdateAndriodAds', function (e) {
    e.preventDefault();
    if (user_type == 1) {
    var formdata = new FormData($("#addUpdateAndriodAds")[0]);
    $('.loader').show();
    $.ajax({
        url: '{{ route("addUpdateAndriodAds") }}',
        type: 'POST',
        data: formdata,
        dataType: "json",
        contentType: false,
        cache: false,
        processData: false,
        success: function (data) {
            $('.loader').hide();
            if (data.success == 1) {
              $(".hidden_id").val(data.data);
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

  $(document).on('submit', '#addUpdateIosAds', function (e) {
    e.preventDefault();
    if (user_type == 1) {
    var formdata = new FormData($("#addUpdateIosAds")[0]);
    $('.loader').show();
    $.ajax({
        url: '{{ route("addUpdateIosAds") }}',
        type: 'POST',
        data: formdata,
        dataType: "json",
        contentType: false,
        cache: false,
        processData: false,
        success: function (data) {
            $('.loader').hide();
            if (data.success == 1) {
              $(".hidden_id").val(data.data);
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
  

});
</script>

@endsection
