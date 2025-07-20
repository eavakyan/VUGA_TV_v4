@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">

<style type="text/css">

label.error {
    color: red !important;
    position: absolute !important;
    width: 100% !important;
    /*top: 40px !important;*/
    /*line-height: 14px !important;*/
    /*left: 0px !important;*/
}

</style>
@stop
@section('content')

<section class="section">
    <div class="section-body">
        <div class="row mt-sm-4">
            <div class="col-12 col-md-12 col-lg-6">
                <div class="card ">
                    <div class="card-body">
                    
                        <form method="post" id="updateMonthlySubscription" class="needs-validation">
                            <div class="card-header">
                                <h4>Monthly Subscription</h4>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Price</label>
                                    <input type="text" class="form-control" name="price" value="@if($monthlyData){{$monthlyData['price']}}@endif">
                                    </div>
                                </div>
                                
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Currency</label>
                                    <input type="text" class="form-control" name="currency" value="@if($monthlyData){{$monthlyData['currency']}}@endif">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Days</label>
                                    <input type="text" class="form-control" name="days" value="@if($monthlyData){{$monthlyData['days']}}@endif">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Android Product Id</label>
                                    <input type="text" class="form-control" name="android_product_id" value="@if($monthlyData){{$monthlyData['android_product_id']}}@endif">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Ios Product Id</label>
                                    <input type="text" class="form-control" name="ios_product_id" value="@if($monthlyData){{$monthlyData['ios_product_id']}}@endif">
                                    </div>
                                </div>
                            </div>
                            <div class="card-footer pt-0">
                                <input type="hidden" class="form-control monthly_hidden_id" name="hidden_id" value="@if($monthlyData){{$monthlyData['package_id']}}@endif">
                                <input type="hidden" class="form-control" name="duration" value="1">
                                <button class="btn btn-primary" type="submit" >Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="col-12 col-md-12 col-lg-6">
                <div class="card">
                    <div class="card-body">
                        
                        <form method="post" id="updateYearlySubscription" class="needs-validation">
                        <div class="card-header">
                            <h4>Yearly Subscription</h4>
                        </div>
                        <div class="card-body">
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Price</label>
                                    <input type="text" class="form-control" name="price" value="@if($yearlyData){{$yearlyData['price']}}@endif">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Currency</label>
                                    <input type="text" class="form-control" name="currency" value="@if($yearlyData){{$yearlyData['currency']}}@endif">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Days</label>
                                    <input type="text" class="form-control" name="days" value="@if($yearlyData){{$yearlyData['days']}}@endif">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Android Product Id</label>
                                    <input type="text" class="form-control" name="android_product_id" value="@if($yearlyData){{$yearlyData['android_product_id']}}@endif">
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-12">
                                    <label>Ios Product Id</label>
                                    <input type="text" class="form-control" name="ios_product_id" value="@if($yearlyData){{$yearlyData['ios_product_id']}}@endif">
                                    </div>
                                </div>
                            </div>
                            <div class="card-footer pt-0">
                                <input type="hidden" class="form-control yearly_hidden_id" name="hidden_id" value="@if($yearlyData){{$yearlyData['package_id']}}@endif">
                                <input type="hidden" class="form-control" name="duration" value="2">
                                <button class="btn btn-primary" type="submit" >Save</button>
                            </div>
                        </form>
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
   
    $('#updateMonthlySubscription').validate({
        rules: {
            price: {
                required: true,
            },
            currency: {
                required: true,
            },
            android_product_id: {
                required: true,
            },
            ios_product_id: {
                required: true,
            },
        },
        messages: {
            price: {
                required: "Please enter Price",
            },
            currency: {
                required: "Please enter Currency",
            },
            android_product_id: {
                required: "Please enter Android Product Id",
            },
            ios_product_id: {
                required: "Please enter Ios Product Id",
            },
        },
    });

    $(document).on('submit', '#updateMonthlySubscription', function(e) {
      e.preventDefault();
      if (user_type == 1) {
        var formdata = new FormData($("#updateMonthlySubscription")[0]);
        $('.loader').show();
        $.ajax({
            url: '{{ route("addUpdateSubscriptionPackage") }}',
            type: "post",
            data: formdata,
            cache: false,
            processData: false,
            contentType: false,
            dataType: "json",
            success: function(response) {
                $('.loader').hide();
                if (response.success == 1) {
                    $(".monthly_hidden_id").val(response.id);
                    iziToast.success({
                        title: 'Success!',
                        message: response.message,
                        position: 'topRight'
                    });
                } else {
                    iziToast.error({
                        title: 'Error!',
                        message: response.message,
                        position: 'topRight'
                    });
                }
            },
        });
    }else{
      iziToast.error({
          title: 'Error!',
          message: ' you are Tester ',
          position: 'topRight'
      });
    }
        return false;
    });

    $('#updateYearlySubscription').validate({
        rules: {
            price: {
                required: true,
            },
            currency: {
                required: true,
            },
            android_product_id: {
                required: true,
            },
            ios_product_id: {
                required: true,
            },
        },
        messages: {
            price: {
                required: "Please enter Price",
            },
            currency: {
                required: "Please enter Currency",
            },
            android_product_id: {
                required: "Please enter Android Product Id",
            },
            ios_product_id: {
                required: "Please enter Ios Product Id",
            },
        },
    });

    $(document).on('submit', '#updateYearlySubscription', function(e) {
      e.preventDefault();
      if (user_type == 1) {
        var formdata = new FormData($("#updateYearlySubscription")[0]);
        $('.loader').show();
        $.ajax({
            url: '{{ route("addUpdateSubscriptionPackage") }}',
            type: "post",
            data: formdata,
            cache: false,
            processData: false,
            contentType: false,
            dataType: "json",
            success: function(response) {
                $('.loader').hide();
                if (response.success == 1) {
                    $(".yearly_hidden_id").val(response.id);
                    iziToast.success({
                        title: 'Success!',
                        message: response.message,
                        position: 'topRight'
                    });
                } else {
                    iziToast.error({
                        title: 'Error!',
                        message: response.message,
                        position: 'topRight'
                    });
                }
            },
        });
    }else{
      iziToast.error({
          title: 'Error!',
          message: ' you are Tester ',
          position: 'topRight'
      });
    }
        return false;
    });
    
});
</script>
@stop