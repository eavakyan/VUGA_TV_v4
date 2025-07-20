@extends('admin_layouts/main')
@section('pageSpecificCss')
    <link href="{{ asset('assets/bundles/izitoast/css/iziToast.min.css') }}" rel="stylesheet">

    <style type="text/css">
        .borderwrap {
            float: left;
            /* border: 1px dashed #000; */
            margin-right: 10px;
            border-radius: 6px;
            position: relative;
        }

        .middle {
            transition: .5s ease;
            opacity: 1;
            position: absolute;
            top: 4px;
            right: -22px;
            transform: translate(-50%, -50%);
            -ms-transform: translate(-50%, -50%)
        }

        .remove_img {
            color: #ffa117 !important;
            cursor: pointer;
        }

        .custom-switch-input:checked~.custom-switch-indicator:before {
            left: calc(2rem + 1px);
        }

        .custom-switch-indicator:before {
            height: calc(2.25rem - 4px);
            width: calc(2.25rem - 4px);
        }

        .custom-switch-indicator {
            height: 2.25rem;
            width: 4.25rem;
        }


        .switch {
            position: relative;
            display: inline-block;
            width: 60px;
            height: 34px;
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
            height: 26px;
            width: 26px;
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
            box-shadow: 0 0 1px #2196F3;
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
@stop
@section('content')
    <section class="section">
        <div class="row">
            <div class="col-12 col-md-12 col-lg-12">
                <div class="card">
                    <div class="card-header">
                        <h4>Settings</h4>
                    </div>
                    <form class="forms-sample" id="addUpdateSetting">
                        <div class="card-body">
                            <div class="form-row">
                                <div class="form-group col-md-6">
                                    <label for="privacy_url">Privacy Url</label>
                                    <!-- <a href="{{ url('privacy-policy') }}" target="_blank">{{ url('privacy-policy') }}</a> -->
                                    <input type="text" name="privacy_url" class="form-control" id="privacy_url"
                                        placeholder="Privacy Url" value="{{ url('privacy-policy') }}" readonly>
                                </div>
                                <div class="form-group col-md-6">
                                    <label for="terms_url">Terms Url</label>
                                    <!-- <a href="{{ url('terms-condition') }}" target="_blank">{{ url('terms-condition') }}</a> -->
                                    <input type="text" name="terms_url" class="form-control" id="terms_url"
                                        placeholder="Terms Url" value="{{ url('terms-condition') }}" readonly>
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group col-md-6">
                                    <label for="more_apps_url">More apps url</label>
                                    <input type="text" name="more_apps_url" class="form-control" id="more_apps_url"
                                        placeholder="More apps url"
                                        value="@if (isset($SettingData)) {{ $SettingData['more_apps_url'] }} @endif">
                                </div>

                            </div>

                        </div>
                        <div class="card-footer">
                            <input type="hidden" name="flag" value="1">
                            <input type="hidden" name="hidden_id" class="hidden_id"
                                value="@if (isset($SettingData)) {{ $SettingData['id'] }} @endif">
                            <button class="btn btn-primary">Submit</button>
                        </div>
                    </form>
                </div>


                <div class="card">
                    <div class="card-header">
                        <h4>App Setting</h4>
                    </div>
                    <form class="forms-sample" id="addUpdateAppName">
                        <div class="card-body">
                            <div class="form-row">
                                <div class="form-group col-md-4">
                                    <label for="app_name">App Name</label>
                                    <input type="text" name="app_name" class="form-control" id="app_name"
                                        placeholder="App Name"
                                        value="@if (isset($SettingData)) {{ $SettingData['app_name'] }} @endif">
                                </div>
                                <div class="form-group col-md-4">
                                    <label for="app_name">Skip Time (For Video Custom Ads Only)</label>
                                    <input type="number" name="videoad_skip_time" class="form-control" id="videoad_skip_time"
                                        value="{{$SettingData->videoad_skip_time}}">
                                </div>

                                <div class="form-group col-md-3 ml-5">
                                    <label for="app_name">Enable/Disable Live Tv Tab</label></br>
                                    <label class="switch"> <input type="checkbox" name="is_live_tv_enable"
                                            id="is_live_tv_enable"
                                            {{ $SettingData->is_live_tv_enable == 1 ? 'checked' : '' }}> <span
                                            class="slider round"></span> </label>
                                </div>
                            </div>
                            <h5>Ads Settings</h5>
                            <div class="form-row mt-4">
                                <div class="form-group col-md-3">
                                    <label for="app_name">Admob Android Ads</label></br>
                                    <label class="switch"> <input type="checkbox" name="is_admob_and" id="is_admob_and"
                                            {{ $SettingData->is_admob_and == 1 ? 'checked' : '' }}> <span
                                            class="slider round"></span> </label>
                                </div>
                                <div class="form-group col-md-3">
                                    <label for="app_name">Admob iOS Ads</label></br>
                                    <label class="switch"> <input type="checkbox" name="is_admob_ios" id="is_admob_ios"
                                            {{ $SettingData->is_admob_ios == 1 ? 'checked' : '' }}> <span
                                            class="slider round"></span> </label>
                                </div>
                                <div class="form-group col-md-3">
                                    <label for="app_name">Custom Android Ads</label></br>
                                    <label class="switch"> <input type="checkbox" name="is_custom_and" id="is_custom_and"
                                            {{ $SettingData->is_custom_and == 1 ? 'checked' : '' }}> <span
                                            class="slider round"></span> </label>
                                </div>
                                <div class="form-group col-md-3">
                                    <label for="app_name">Custom iOS Ads</label></br>
                                    <label class="switch"> <input type="checkbox" name="is_custom_ios"
                                            id="is_custom_ios" {{ $SettingData->is_custom_ios == 1 ? 'checked' : '' }}>
                                        <span class="slider round"></span> </label>
                                </div>
                            </div>


                        </div>
                        <div class="card-footer">
                            <input type="hidden" name="flag" value="2">
                            <input type="hidden" name="hidden_id" class="hidden_id"
                                value="@if (isset($SettingData)) {{ $SettingData['id'] }} @endif">
                            <button class="btn btn-primary">Submit</button>
                        </div>
                    </form>
                </div>

            </div>
        </div>


    </section>


@endsection

@section('pageSpecificJs')

    <script src="{{ asset('assets/bundles/jquery-ui/jquery-ui.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/izitoast/js/iziToast.min.js') }}"></script>
    <script>
        $(document).ready(function() {

            $(document).on('submit', '#addUpdateSetting', function(e) {
                e.preventDefault();
                if (user_type == 1) {
                    var formdata = new FormData($("#addUpdateSetting")[0]);
                    $('.loader').show();
                    $.ajax({
                        url: '{{ route('addUpdateSetting') }}',
                        type: 'POST',
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(data) {
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
                        error: function(jqXHR, textStatus, errorThrown) {
                            alert(errorThrown);
                        }
                    });
                } else {
                    iziToast.error({
                        title: 'Error!',
                        message: ' you are Tester ',
                        position: 'topRight'
                    });
                }
            });

            $(document).on('submit', '#addUpdateAppName', function(e) {
                e.preventDefault();
                if (user_type == 1) {
                    var formdata = new FormData($("#addUpdateAppName")[0]);
                    $('.loader').show();
                    $.ajax({
                        url: '{{ route('addUpdateSetting') }}',
                        type: 'POST',
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(data) {
                            $('.loader').hide();
                            if (data.success == 1) {
                                $(".hidden_id").val(data.data);
                                $(".logo_name").text($("#app_name").val());
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
                        error: function(jqXHR, textStatus, errorThrown) {
                            alert(errorThrown);
                        }
                    });
                } else {
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
