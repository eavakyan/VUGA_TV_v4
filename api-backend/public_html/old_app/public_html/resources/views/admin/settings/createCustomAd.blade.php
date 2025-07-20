@extends('admin_layouts/main')
@section('pageSpecificCss')
    <link href="{{ asset('assets/bundles/datatables/datatables.min.css') }}" rel="stylesheet">
    <link href="{{ asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css') }}"
        rel="stylesheet">
    <link href="{{ asset('assets/bundles/summernote/summernote-bs4.css') }}" rel="stylesheet">
    <link href="{{ asset('assets/bundles/izitoast/css/iziToast.min.css') }}" rel="stylesheet">

@stop
@section('content')

    <style>
        .switch {
            position: relative;
            display: inline-block;
            width: 55px;
            height: 28px;
        }

        .switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }

        .slider {
            position: absolute;
            cursor: pointer;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: #ccc;
            -webkit-transition: .4s;
            transition: .4s;
        }

        .slider:before {
            position: absolute;
            content: "";
            height: 20px;
            width: 20px;
            left: 4px;
            bottom: 4px;
            background-color: white;
            -webkit-transition: .4s;
            transition: .4s;
        }

        input:checked+.slider {
            background-color: #2196F3;
        }

        input:focus+.slider {
            box-shadow: 0 0 1px #2515b6;
        }

        input:checked+.slider:before {
            -webkit-transform: translateX(26px);
            -ms-transform: translateX(26px);
            transform: translateX(26px);
        }

        /* Rounded sliders */
        .slider.round {
            border-radius: 34px;
        }

        .slider.round:before {
            border-radius: 50%;
        }
    </style>

    <div class="card">
        <div class="card-header">
            <h4>Create Custom Ad</h4>
        </div>

        <div class="card-body">
            <form action="" method="post" enctype="multipart/form-data" class="create_ad" id="createAdForm"
                autocomplete="off">
                @csrf

                <div class="form-row">
                    <div class="form-group col-md-12">
                        <label>{{ __('Ad Title (For Reference Only)') }}</label>
                        <input id="title" name="title" class="form-control" value="" required>
                    </div>
                </div>

                <div class="form-row ">
                    <div class="form-group col-md-6">
                        <label>{{ __('Brand Name') }}</label>
                        <input id="brand_name" name="brand_name" class="form-control" value="" required>
                    </div>
                </div>

                <div class="form-row ">

                    <div class="form-group col-md-3 d-flex">
                        <img height="65" width="65" class="rounded" id="brand_logo_img"
                            src="https://placehold.jp/150x150.png" alt="">

                        <div class="form-group col-md-9">
                            <label for="brand_logo" class="form-label">{{ __('Brand Logo') }}</label>
                            <input class="form-control" type="file" id="brand_logo" name="brand_logo"
                                accept="image/png, image/gif, image/jpeg" required>
                        </div>
                    </div>

                    <div class="form-group col-md-3">
                        <label>Button Text</label>
                        <input name="button_text" class="form-control" value="" required>
                    </div>
                    <div class="form-group col-md-3">
                        <label>Start Date</label>
                        <input min="<?php echo date('Y-m-d'); ?>" type="date" name="start_date" class="form-control"
                            value="" required>
                    </div>
                    <div class="form-group col-md-3">
                        <label>End Date</label>
                        <input min="<?php echo date('Y-m-d'); ?>" type="date" name="end_date" class="form-control" value=""
                            required>
                    </div>
                </div>
                <div class="form-row mx-xl-1 ">
                    {{-- Android Link --}}
                    <div class="form-row bg-link mr-xl-1  col-xl">
                        <div class="form-group col-md-2">
                            <label class="d-block" for="">{{ __('For Android') }}</label>
                            <label class="switch ">
                                <input type="checkbox" name="is_android" id="androidSwitch">
                                <span class="slider round"></span>
                            </label>
                        </div>

                        <div class="form-group col-md-10">
                            <label>{{ __('Android Link') }}</label>
                            <input id="android_link" type="text" name="android_link" class="form-control" value=""
                                disabled>
                        </div>
                    </div>

                    {{-- iOS Link --}}
                    <div class="form-row bg-link ml-xl-1 mt-2 mt-xl-0 col-xl">
                        <div class="form-group col-md-2">
                            <label class="d-block" for="">{{ __('For iOS') }}</label>
                            <label class="switch ">
                                <input type="checkbox" name="is_ios" id="iosSwitch">
                                <span class="slider round"></span>
                            </label>
                        </div>

                        <div class="form-group col-md-10">
                            <label>{{ __('iOS Link') }}</label>
                            <input id="ios_link" name="ios_link" class="form-control" value="" disabled>
                        </div>
                    </div>
                </div>
                <div class="mt-3">
                    <p>
                        * The ads will be <strong> OFF</strong> by default. You need to turn it <strong> ON</strong> after
                        creating.
                        <br>
                        * Ads can be turned <strong> ON</strong> only once it has minimum 1 Image/Video resource.
                        <br>
                        * Resources can be added by clicking on Edit Ad.
                    </p>
                </div>

                <div class="form-group d-flex justify-content-end">
                    <button id="btn_cancel" class="btn btn-danger  mr-2">{{ __('Cancel') }}</button>
                    <input class="btn btn-success" type="submit" value=" {{ __('Submit') }}">
                </div>

            </form>
        </div>
    </div>


@endsection

@section('pageSpecificJs')

    <script src="{{ asset('assets/bundles/datatables/datatables.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/jquery-ui/jquery-ui.min.js') }}"></script>
    <script src="{{ asset('assets/js/page/datatables.js') }}"></script>
    <script src="{{ asset('assets/bundles/izitoast/js/iziToast.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/summernote/summernote-bs4.js') }}"></script>

    <script>
        $(document).ready(function() {
            var is_android = 0;
            var is_iOS = 0;

            // On brand logo change
            var brandLogoInput = $("#brand_logo");
            brandLogoInput.change(function() {
                if (brandLogoInput[0].files && brandLogoInput[0].files[0]) {
                    var reader = new FileReader();

                    reader.onload = function(e) {
                        $("#brand_logo_img").attr("src", e.target.result);
                    };
                    reader.readAsDataURL(brandLogoInput[0].files[0]);
                    console.log(brandLogoInput[0].files[0]);
                }
            });

            $(document).on("change", "#androidSwitch", function(event) {
                event.preventDefault();

                if ($(this).prop("checked") == true) {
                    is_android = 1;
                    $("#android_link").prop("disabled", false);
                } else {
                    is_android = 0;
                    $("#android_link").val("");
                    $("#android_link").prop("disabled", true);
                }
            });
            $(document).on("change", "#iosSwitch", function(event) {
                event.preventDefault();

                if ($(this).prop("checked") == true) {
                    is_iOS = 1;
                    $("#ios_link").prop("disabled", false);
                } else {
                    is_iOS = 0;
                    $("#ios_link").val("");
                    $("#ios_link").prop("disabled", true);
                }
            });

            $("#createAdForm").on("submit", function(event) {
                event.preventDefault();
                if (is_android == 0 && is_iOS == 0) {
                    iziToast.error({
                        title: "Attention!",
                        message: "Please select campaign type! (Android, iOS)!",
                        position: "topRight",
                    });
                    return;
                }
                if (is_android == 1 && $("#android_link").val() == "") {
                    iziToast.error({
                        title: "Attention!",
                        message: "Please add value for android link!",
                        position: "topRight",
                    });
                    return;
                }
                if (is_iOS == 1 && $("#ios_link").val() == "") {
                    iziToast.error({
                        title: "Attention!",
                        message: "Please add value for iOS link!",
                        position: "topRight",
                    });
                    return;
                }

                if (user_type == "1") {
                    $(".loader").show();
                    var formdata = new FormData($("#createAdForm")[0]);
                    formdata.append("is_android", is_android);
                    formdata.append("is_ios", is_iOS);

                    $.ajax({
                        url: "{{ url('ads/createNewAd') }}",
                        type: "POST",
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(response) {
                            console.log(response);

                            if (response.status == true) {
                                window.history.back();
                            } else {
                                $(".loader").hide();
                                iziToast.error({
                                    title: app.Error,
                                    message: response.message,
                                    position: "topRight",
                                });
                            }
                        },
                        error: function(err) {
                            console.log(err.message);
                        },
                    });
                } else {
                    iziToast.error({
                        title: app.Error,
                        message: app.tester,
                        position: "topRight",
                    });
                }
            });


        });
    </script>

@endsection
