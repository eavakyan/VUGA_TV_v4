@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/setting.js') }}"></script>

<script>
    document.addEventListener('DOMContentLoaded', function() {

        const awsConfig = @json($awsConfig);
        const doConfig = @json($doConfig);

        const isAwsConfigComplete = Object.values(awsConfig).every(value => value !== null && value !== '');
        const awsRadio = document.getElementById('awsRadio');
        const awsNote = document.getElementById('awsNote');
        if (!isAwsConfigComplete) {
            awsRadio.disabled = true;
            $('#awsNote').removeClass('d-none');
        } else {
            $('#localRadio').attr('checked');
            $('#awsNote').addClass('d-none');
        }

        const isDoConfigComplete = Object.values(doConfig).every(value => value !== null && value !== '');
        const doRadio = document.getElementById('doRadio');
        if (!isDoConfigComplete) {
            doRadio.disabled = true;
            $('#doNote').removeClass('d-none');
        } else {
            $('#localRadio').attr('checked');
            $('#doNote').addClass('d-none');
        }
    });
</script>

@endsection

@section('content')

<div class="row same-height-card">
    <div class="col-lg-6 col-md-12 col-sm-12">
        <div class="card">
            <div class="card-header">
                <div class="page-title w-100">
                    <div class="d-flex align-items-center justify-content-between">
                        <h4 class="mb-0 fw-semibold">{{ __('settings') }}</h4>
                    </div>
                </div>
            </div>
            <div class="card-body px-4">
                <form id="settingsForm" method="post" enctype="multipart/form-data" class="form-border" autocomplete="off">
                    @csrf
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="currency" class="form-label">{{ __('appNameTitle') }}</label>
                                <input value="{{ $setting->app_name }}" type="text" name="app_name" class="form-control" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="currency" class="form-label">{{ __('skipTime') }}</label>
                                <input value="{{ $setting->videoad_skip_time }}" type="text" name="videoad_skip_time" class="form-control" required>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer p-0">
                        <button type="button" class="btn"></button>
                        <button type="submit" class="btn theme-btn text-white">{{ __('save') }}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div class="col-lg-6 col-md-12 col-sm-12">
        <div class="card">
            <div class="card-header">
                <div class="page-title w-100">
                    <div class="d-flex align-items-center justify-content-between">
                        <h4 class="mb-0 fw-semibold">{{ __('changePassword') }}</h4>
                    </div>
                </div>
            </div>
            <div class="card-body px-4">
                <form id="changePasswordForm" method="POST">
                    <div class="row">
                        <div class="col-lg-6 col-md-6 col-sm-12 position-relative">
                            <div class="form-group">
                                <label for="appName" class="form-label">{{ __('oldPassword') }}</label>
                                <input type="password" class="form-control" name="user_password" id="userPassword" required="">
                                <div class="password-icon">
                                    <i data-feather="eye"></i>
                                    <i data-feather="eye-off"></i>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-6 col-md-6 col-sm-12 position-relative">
                            <div class="form-group">
                                <label for="appName" class="form-label">{{ __('newPassword') }}</label>
                                <input type="password" class="form-control" name="new_password" id="newPassword" required="">
                                <div class="password-icon">
                                    <i data-feather="eye" class="eye1"></i>
                                    <i data-feather="eye-off" class="eye-off1"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer p-0">
                        <button type="button" class="btn"></button>
                        <button type="submit" class="btn theme-btn text-white">{{ __('changePassword') }}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-md-3">
        <div class="card">
            <div class="card-header">
                <div class="page-title w-100">
                    <div class="d-flex align-items-center justify-content-between">
                        <h4 class="mb-0 fw-semibold">{{ __('storageSetting') }}</h4>
                    </div>
                </div>
            </div>
            <div class="card-body px-4">
                <form id="storageSettingForm" method="POST">
                    <div class="card w-auto p-3">
                        <div class="checkbox-slider d-flex align-items-center justify-content-between">
                            <span class="me-3">
                                {{ __('local') }}
                            </span>
                            <label>
                                <input type="radio" class="d-none" name="storage_type" value="0" id="localRadio" {{ $setting->storage_type == 0 ? 'checked' : '' }}>
                                <span class="toggle_background">
                                    <div class="circle-icon"></div>
                                    <div class="vertical_line"></div>
                                </span>
                            </label>
                        </div>
                    </div>
                    <div class="card w-auto p-3">
                        <div class="checkbox-slider d-flex align-items-center justify-content-between">
                            <span class="me-3">
                                {{ __('AWSS3') }}
                                <br>
                                <span id="awsNote" class="text-danger"> {{ __('pleaseAddValuesInEnvFile') }} </span>
                            </span>
                            <label>
                                <input type="radio" class="d-none" name="storage_type" value="1" id="awsRadio" {{ $setting->storage_type == 1 ? 'checked' : '' }}>
                                <span class="toggle_background">
                                    <div class="circle-icon"></div>
                                    <div class="vertical_line"></div>
                                </span>
                            </label>
                        </div>
                    </div>
                    <div class="card w-auto p-3">
                        <div class="checkbox-slider d-flex align-items-center justify-content-between">
                            <span class="me-3">
                                {{ __('digitalOceanSpace') }}
                                <br>
                                <span id="doNote" class="text-danger"> {{ __('pleaseAddValuesInEnvFile')}} </span>
                            </span>
                            <label>
                                <input type="radio" class="d-none" name="storage_type" value="2" id="doRadio" {{ $setting->storage_type == 2 ? 'checked' : '' }}>
                                <span class="toggle_background">
                                    <div class="circle-icon"></div>
                                    <div class="vertical_line"></div>
                                </span>
                            </label>
                        </div>
                    </div>
                    <div class="modal-footer p-0">
                        <button type="submit" class="btn theme-btn text-white">{{ __('save') }}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="col-md-6">
        <div class="card">
            <div class="card-header">
                <div class="page-title w-100">
                    <div class="d-flex align-items-center justify-content-between">
                        <h4 class="mb-0 fw-semibold">{{ __('adsSettings') }}</h4>
                    </div>
                </div>
            </div>
            <div class="card-body px-4">
                <div class="row">
                    <div class="col-lg-6">
                        <div class="row">
                            <div class="col-xl-12 col-lg-6 mb-3">
                                <div class="card w-auto p-3 m-0">
                                    <div class="checkbox-slider d-flex align-items-center justify-content-between">
                                        <span class="me-3">
                                            {{ __('admobAndroidAds') }}
                                        </span>
                                        <label>
                                            <input type="checkbox" class="d-none is_admob_android" {{ $setting->is_admob_android == 1 ? 'checked' : '' }} name="is_admob_android">
                                            <span class="toggle_background">
                                                <div class="circle-icon"></div>
                                                <div class="vertical_line"></div>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xl-12 col-lg-6">
                                <div class="card w-auto p-3 m-0">
                                    <div class="checkbox-slider d-flex align-items-center justify-content-between">
                                        <span class="me-3">
                                            {{ __('admobiOSAds') }}
                                        </span>
                                        <label>
                                            <input type="checkbox" class="d-none is_admob_ios" {{ $setting->is_admob_ios == 1 ? 'checked' : '' }} name="is_admob_ios">
                                            <span class="toggle_background">
                                                <div class="circle-icon"></div>
                                                <div class="vertical_line"></div>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="row">
                            <div class="col-xl-12 col-lg-6 mb-3">
                                <div class="card w-auto p-3 m-0">
                                    <div class="checkbox-slider d-flex align-items-center justify-content-between">
                                        <span class="me-3">
                                            {{ __('customAndroidAds') }}
                                        </span>
                                        <label>
                                            <input type="checkbox" class="d-none is_custom_android" {{ $setting->is_custom_android == 1 ? 'checked' : '' }} name="is_custom_android">
                                            <span class="toggle_background">
                                                <div class="circle-icon"></div>
                                                <div class="vertical_line"></div>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xl-12 col-lg-6">
                                <div class="card w-auto p-3 m-0">
                                    <div class="checkbox-slider d-flex align-items-center justify-content-between">
                                        <span class="me-3">
                                            {{ __('customiOSAds') }}
                                        </span>
                                        <label>
                                            <input type="checkbox" class="d-none is_custom_ios" {{ $setting->is_custom_ios == 1 ? 'checked' : '' }} name="is_custom_ios">
                                            <span class="toggle_background">
                                                <div class="circle-icon"></div>
                                                <div class="vertical_line"></div>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card w-auto p-3">
            <div class="checkbox-slider d-flex align-items-center justify-content-between">
                <span class="me-3">
                    {{ __('enableDisableLiveTvTab') }}
                </span>
                <label>
                    <input type="checkbox" class="d-none enableDisableLiveTvTab" {{ $setting->is_live_tv_enable == 1 ? 'checked' : '' }} name="is_live_tv_enable">
                    <span class="toggle_background">
                        <div class="circle-icon"></div>
                        <div class="vertical_line"></div>
                    </span>
                </label>
            </div>
        </div>
    </div>
</div>

@endsection