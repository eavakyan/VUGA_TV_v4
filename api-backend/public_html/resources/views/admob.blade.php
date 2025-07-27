@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/admob.js') }}"></script>
@endsection

@section('content')


<section class="section">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-6 col-md-6 col-sm-12">
                <div class="card">
                    <div class="card-header">
                        <h1 class="modal-title fs-5 fw-semibold" id="exampleModalLabel">{{ __('admobAndroid') }}</h1>
                    </div>
                    <div class="card-body px-4">
                        <form id="admobAndroidForm" method="post">
                            <input type="hidden" name="type" value="1">
                            <div class="row">
                                <div class="col-lg-12 col-md-12">
                                    <div class="form-group">
                                        <label for="title" class="form-label">{{ __('bannerId') }}</label>
                                        <input type="text" class="form-control" name="banner_id" required="" value="{{ $admobAndroid->banner_id }}">
                                    </div>
                                </div>
                                <div class="col-lg-12 col-md-12">
                                    <div class="form-group">
                                        <label for="title" class="form-label">{{ __('intersialId') }}</label>
                                        <input type="text" class="form-control" name="intersial_id" required="" value="{{ $admobAndroid->intersial_id }}">
                                    </div>
                                </div>
                                <div class="col-lg-12 col-md-12">
                                    <div class="form-group">
                                        <label for="title" class="form-label">{{ __('rewardedId') }}</label>
                                        <input type="text" class="form-control" name="rewarded_id" required="" value="{{ $admobAndroid->rewarded_id }}">
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer px-0">
                                <button type="submit" class="btn theme-btn text-white">{{ __('save') }}</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="col-lg-6 col-md-6 col-sm-12">
                <div class="card">
                    <div class="card-header">
                        <h1 class="modal-title fs-5 fw-semibold" id="exampleModalLabel">{{ __('admobiOS') }}</h1>
                    </div>
                    <div class="card-body px-4">
                        <form id="admobiOSForm" method="post">
                            <input type="hidden" name="type" value="2">
                            <div class="row">
                                <div class="col-lg-12 col-md-12">
                                    <div class="form-group">
                                        <label for="title" class="form-label">{{ __('bannerId') }}</label>
                                        <input type="text" class="form-control" name="banner_id" required="" value="{{ $admobiOS->banner_id }}">
                                    </div>
                                </div>
                                <div class="col-lg-12 col-md-12">
                                    <div class="form-group">
                                        <label for="title" class="form-label">{{ __('intersialId') }}</label>
                                        <input type="text" class="form-control" name="intersial_id" required="" value="{{ $admobiOS->intersial_id }}">
                                    </div>
                                </div>
                                <div class="col-lg-12 col-md-12">
                                    <div class="form-group">
                                        <label for="title" class="form-label">{{ __('rewardedId') }}</label>
                                        <input type="text" class="form-control" name="rewarded_id" required="" value="{{ $admobiOS->rewarded_id }}">
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer px-0">
                                <button type="submit" class="btn theme-btn text-white">{{ __('save') }}</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

</section>
@endsection