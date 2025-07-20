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
        .pointer {
            cursor: pointer;
        }

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
            <h4>View Custom Ad : {{ $customAd->campaign_number }}</h4>
            <h4 class="ml-auto">Views: {{ $customAd->views }} | Clicks : {{ $customAd->clicks }} | CTR :
                {{ $customAd->ctr }}</h4>
        </div>

        <div class="card-body">
            <form action="" method="post" enctype="multipart/form-data" class="create_ad" id="editAdForm"
                autocomplete="off">
                @csrf

                <input id="adId" type="hidden" value="{{ $customAd->id }}" name="id">

                <div class="form-row">
                    <div class="form-group col-md-12">
                        <label>{{ __('Ad Title (For Reference Only)') }}</label>
                        <input value="{{ $customAd->title }}" id="title" name="title" class="form-control"
                            value="" required disabled>
                    </div>
                </div>

                <div class="form-row ">
                    <div class="form-group col-md-6">
                        <label>{{ __('Brand Name') }}</label>
                        <input value="{{ $customAd->brand_name }}" id="brand_name" name="brand_name" class="form-control"
                            value="" required disabled>
                    </div>
                </div>

                <div class="form-row ">

                    <div class="form-group col-md-2">
                        <label class="form-label d-block">{{ __('Brand Logo') }}</label>
                        <img height="65" width="65" class="rounded" id="brand_logo_img"
                            src="{{ url(env('DEFAULT_IMAGE_URL') . $customAd->brand_logo) }}" alt="">

                    </div>

                    <div class="form-group col-md-3">
                        <label>Button Text</label>
                        <input value="{{ $customAd->button_text }}" name="button_text" class="form-control"
                            value="" required disabled>
                    </div>
                    <div class="form-group col-md-3">
                        <label>Start Date</label>
                        <input min="<?php echo date('Y-m-d'); ?>" type="date" name="start_date" class="form-control"
                            value="{{ $customAd->start_date }}" required disabled>
                    </div>
                    <div class="form-group col-md-3">
                        <label>End Date</label>
                        <input value="{{ $customAd->end_date }}" min="<?php echo date('Y-m-d'); ?>" type="date" name="end_date"
                            class="form-control" required disabled>
                    </div>
                </div>
                <div class="form-row mx-xl-1 ">
                    {{-- Android Link --}}
                    <div class="form-row bg-link mr-xl-1  col-xl">
                        <div class="form-group col-md-2">
                            <label class="d-block" for="">{{ __('For Android') }}</label>
                            <label class="switch ">
                                <input type="checkbox" name="is_android" id="androidSwitch"
                                    {{ $customAd->is_android == 1 ? 'checked' : '' }} disabled>
                                <span class="slider round"></span>
                            </label>
                        </div>

                        <div class="form-group col-md-10">
                            <label>{{ __('Android Link') }}</label>
                            <input value="{{ $customAd->android_link }}" id="android_link" type="text"
                                name="android_link" class="form-control" value="" value=""
                                {{ $customAd->is_android == 0 ? 'disabled' : '' }} disabled>
                        </div>
                    </div>

                    {{-- iOS Link --}}
                    <div class="form-row bg-link ml-xl-1 mt-2 mt-xl-0 col-xl">
                        <div class="form-group col-md-2">
                            <label class="d-block" for="">{{ __('For iOS') }}</label>
                            <label class="switch ">
                                <input type="checkbox" name="is_ios" id="iosSwitch"
                                    {{ $customAd->is_ios == 1 ? 'checked' : '' }} disabled>
                                <span class="slider round"></span>
                            </label>
                        </div>

                        <div class="form-group col-md-10">
                            <label>{{ __('iOS Link') }}</label>
                            <input value="{{ $customAd->ios_link }}" id="ios_link" name="ios_link" class="form-control"
                                value="" {{ $customAd->is_ios == 0 ? 'disabled' : '' }} disabled>
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

            </form>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h4>Image Resources</h4>
            <button class="btn btn-primary text-light ml-auto add-image">Add Image</button>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped" id="imageResourceTable">
                    <thead>
                        <tr>
                            <th>Image</th>
                            <th>Headline</th>
                            <th>Description</th>
                            <th>Show Time</th>
                            <th>Created At</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>
        </div>
    </div>

    {{-- Video Table --}}
    <div class="card">
        <div class="card-header">
            <h4>Video Resources</h4>
            <button class="btn btn-primary text-light ml-auto add-video">Add Video</button>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped" id="videoResourceTable">
                    <thead>
                        <tr>
                            <th>View</th>
                            <th>Headline</th>
                            <th>Description</th>
                            <th>Type</th>
                            <th>Created At</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>
        </div>
    </div>

    {{-- Add Video Modal --}}
    <div class="modal fade" id="addVideoModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="ModalLabel">Add Video Resource</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <form id="addVideoForm" method="post" enctype="multipart">
                    {{ csrf_field() }}
                    <input id="adId" type="hidden" value="{{ $customAd->id }}" name="ad_id">
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="image" class="form-label">Video</label>
                            <input accept="video/mp4,video/x-m4v,video/*" class="form-control" type="file"
                                id="video" name="video" required>
                        </div>
                        <div class="form-group">
                            <label for="headline">Headline</label>
                            <input id="headline" name="headline" type="text"
                                class="form-control form-control-danger" required>
                        </div>
                        <div class="form-group">
                            <label for="description">Description</label>
                            <textarea id="description" name="description" class="form-control" required></textarea>
                        </div>
                        <div class="form-group">
                            <select name="type" class="form-control form-control-sm"
                                aria-label="Default select example">
                                <option selected value="0">Must Watch</option>
                                <option value="1">Skippable</option>
                            </select>
                        </div>

                    </div>
                    <div class="modal-footer">
                        <input type="hidden" name="category_id" id="category_id" value="">
                        <button type="submit" class="btn btn-success">Submit</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    {{-- Edit Video Modal --}}
    <div class="modal fade" id="editVideoModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="ModalLabel">Edit Video Resource</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <form id="editVideoForm" method="post" enctype="multipart">
                    {{ csrf_field() }}
                    <input id="vidId" type="hidden" name="id">
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="image" class="form-label">Video (Select Only If you want to change)</label>
                            <input accept="video/mp4,video/x-m4v,video/*" class="form-control" type="file"
                                id="edit_video" name="video">
                        </div>
                        <div class="form-group">
                            <label for="edit_vid_headline">Headline</label>
                            <input id="edit_vid_headline" name="headline" type="text"
                                class="form-control form-control-danger" required>
                        </div>
                        <div class="form-group">
                            <label for="edit_vid_description">Description</label>
                            <textarea id="edit_vid_description" name="description" class="form-control" required></textarea>
                        </div>
                        <div class="form-group">
                            <select id="edit_vid_type" name="type" class="form-control form-control-sm"
                                aria-label="Default select example">
                            </select>
                        </div>

                    </div>
                    <div class="modal-footer">
                        <button type="submit" class="btn btn-success">Submit</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    {{-- Add Image Modal --}}
    <div class="modal fade" id="addImageModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="ModalLabel">Add Image Resource</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <form id="addImageForm" method="post" enctype="multipart">
                    {{ csrf_field() }}
                    <input id="adId" type="hidden" value="{{ $customAd->id }}" name="ad_id">
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="image" class="form-label">Image</label>
                            <input accept="image/png, image/gif, image/jpeg" class="form-control" type="file"
                                id="image" name="image" required>

                        </div>
                        <div class="form-group">
                            <label for="headline">Headline</label>
                            <input id="headline" name="headline" type="text"
                                class="form-control form-control-danger" required>
                        </div>
                        <div class="form-group">
                            <label for="description">Description</label>
                            <textarea id="description" name="description" class="form-control" required></textarea>
                        </div>
                        <div class="form-group">
                            <label for="show_time">Show Time (In Seconds)</label>
                            <input id="show_time" name="show_time" type="number"
                                class="form-control form-control-danger" required>
                        </div>

                    </div>
                    <div class="modal-footer">
                        <input type="hidden" name="category_id" id="category_id" value="">
                        <button type="submit" class="btn btn-success">Submit</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    {{-- Edit Image Modal --}}
    <div class="modal fade" id="editImageModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="ModalLabel">Edit Image Resource</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <form id="editsImageForm" method="post" enctype="multipart">
                    {{ csrf_field() }}
                    <div class="modal-body">

                        <img class="mb-3 rounded" id="edit_ad_img" width="100" height="100" src=""
                            alt="">

                        <input id="imgId" type="hidden" name="id">

                        <div class="form-group">
                            <label for="image" class="form-label">Image</label>
                            <input accept="image/png, image/gif, image/jpeg" class="form-control" type="file"
                                id="image" name="image">
                        </div>
                        <div class="form-group">
                            <label for="edit_headline">Headline</label>
                            <input id="edit_headline" name="headline" type="text"
                                class="form-control form-control-danger" required>
                        </div>
                        <div class="form-group">
                            <label for="edit_description">Description</label>
                            <textarea id="edit_description" name="description" class="form-control" required></textarea>
                        </div>
                        <div class="form-group">
                            <label for="edit_show_time">Show Time (In Seconds)</label>
                            <input id="edit_show_time" name="show_time" type="number"
                                class="form-control form-control-danger" required>
                        </div>

                    </div>
                    <div class="modal-footer">
                        <input type="hidden" name="category_id" id="category_id" value="">
                        <button type="submit" class="btn btn-success">Submit</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    {{-- Show Image Modal --}}
    <div class="modal fade" id="showImageModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="ModalLabel">Image Preview</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>

                <div class="modal-body">
                    <img id="show-image" class="rounded" src="" width="100%" alt="">

                    <h5 class="mt-3" id="img-headline"></h5>
                    <p id="img-desc"></p>
                </div>

            </div>
        </div>
    </div>

    {{-- Video Modal --}}
    <div class="modal fade" id="video_modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel"
        aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h4>Video Preview</h4>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <video rel="" id="video" width="450" height="450" controls>
                        <source src="" type="video/mp4">
                        Your browser does not support the video tag.
                    </video>
                    <h5 class="mt-3" id="vid-headline"></h5>
                    <p id="vid-desc"></p>
                </div>

            </div>
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
            var is_android = {!! $customAd->is_android !!};
            var is_iOS = {!! $customAd->is_ios !!};
            var adId = $("#adId").val();

            $('.add-image').on('click', function(event) {
                event.preventDefault();
                $('#addImageModal').modal().show();
            })
            $('.add-video').on('click', function(event) {
                event.preventDefault();
                $('#addVideoModal').modal().show();
            })


            $("#addVideoForm").on("submit", function(event) {
                event.preventDefault();
                if (user_type == "1") {
                    $(".loader").show();
                    var formdata = new FormData($("#addVideoForm")[0]);

                    $.ajax({
                        url: "{{ url('ads/addVideoToAd') }}",
                        type: "POST",
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(response) {
                            console.log(response);
                            if (response.status == true) {
                                $(".loader").hide();
                                $('#videoResourceTable').DataTable().ajax.reload(null, false);
                                $('#addVideoModal').modal("hide");
                                iziToast.success({
                                    title: "Successful!",
                                    message: "Item added Successfully !",
                                    position: "topRight",
                                });
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
            $("#editVideoForm").on("submit", function(event) {
                event.preventDefault();
                if (user_type == "1") {
                    $(".loader").show();
                    var formdata = new FormData($("#editVideoForm")[0]);

                    $.ajax({
                        url: "{{ url('ads/editAdVideo') }}",
                        type: "POST",
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(response) {
                            console.log(response);
                            if (response.status) {
                                $(".loader").hide();
                                $('#videoResourceTable').DataTable().ajax.reload(null, false);
                                $('#editVideoModal').modal("hide");
                                iziToast.success({
                                    title: "Successful!",
                                    message: "Item updated Successfully !",
                                    position: "topRight",
                                });
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
            $("#addImageForm").on("submit", function(event) {
                event.preventDefault();
                if (user_type == "1") {
                    $(".loader").show();
                    var formdata = new FormData($("#addImageForm")[0]);

                    $.ajax({
                        url: "{{ url('ads/addImageToAd') }}",
                        type: "POST",
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(response) {
                            console.log(response);
                            if (response.status == true) {
                                $(".loader").hide();
                                $('#imageResourceTable').DataTable().ajax.reload(null, false);
                                $('#addImageModal').modal("hide");
                                iziToast.success({
                                    title: "Successful!",
                                    message: "Item added Successfully !",
                                    position: "topRight",
                                });
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
            $("#editsImageForm").on("submit", function(event) {
                event.preventDefault();
                if (user_type == "1") {
                    $(".loader").show();
                    var formdata = new FormData($("#editsImageForm")[0]);

                    $.ajax({
                        url: "{{ url('ads/editAdImage') }}",
                        type: "POST",
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(response) {
                            console.log(response);
                            if (response.status == true) {
                                $(".loader").hide();
                                $('#imageResourceTable').DataTable().ajax.reload(null, false);
                                $('#editImageModal').modal("hide");
                                iziToast.success({
                                    title: "Successful!",
                                    message: "Item updated Successfully !",
                                    position: "topRight",
                                });
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

            var dataTable = $('#imageResourceTable').dataTable({
                'processing': true,
                'serverSide': true,
                'serverMethod': 'post',
                "order": [
                    [0, "desc"]
                ],
                'columnDefs': [{
                    'targets': [0, 1, 2, 3, 4, 5],
                    /* column index */
                    'orderable': false,
                    /* true or false */
                }],
                'ajax': {
                    'url': '{{ route('showAdImagesList') }}',
                    'data': function(data) {
                        data.adId = adId;
                    }
                }
            });
            var dataTable = $('#videoResourceTable').dataTable({
                'processing': true,
                'serverSide': true,
                'serverMethod': 'post',
                "order": [
                    [0, "desc"]
                ],
                'columnDefs': [{
                    'targets': [0, 1, 2, 3, 4, 5],
                    /* column index */
                    'orderable': false,
                    /* true or false */
                }],
                'ajax': {
                    'url': '{{ route('showAdVideoList') }}',
                    'data': function(data) {
                        data.adId = adId;
                    }
                }
            });

            $("#videoResourceTable").on('click', '.show-vid', function(e) {
                e.preventDefault();
                var vidUrl = $(this).attr('rel');
                console.log(vidUrl);
                $("#video source").attr("src", vidUrl);
                $("#video")[0].load();
                $('#vid-headline').text($(this).data('headline'))
                $('#vid-desc').text($(this).data('description'))
                $("#video_modal").modal("show");
                $("#video").trigger("play");

            });
            $('#videoResourceTable').on('click', '.delete-vid', function(e) {
                e.preventDefault();
                var id = $(this).attr('rel');
                var text = 'You will not be able to recover it!';
                var btn = 'btn-danger';
                swal({
                        title: "Are you sure?",
                        text: text,
                        type: "warning",
                        showCancelButton: true,
                        cancelButtonClass: btn,
                        confirmButtonText: "Confirm",
                        cancelButtonText: "Cancel",
                        closeOnConfirm: true,
                        closeOnCancel: true,
                        allowOutsideClick: true
                    },
                    function(isConfirm) {
                        if (isConfirm) {
                            if (user_type == 1) {
                                $('.loader').show();
                                $.ajax({
                                    url: '{{ route('deleteAdVideo') }}',
                                    type: 'POST',
                                    data: {
                                        "id": id
                                    },
                                    dataType: "json",
                                    cache: false,
                                    success: function(data) {
                                        $('.loader').hide();
                                        $('#videoResourceTable').DataTable().ajax.reload(
                                            null,
                                            false);
                                        if (data.status) {
                                            iziToast.success({
                                                title: "Successful!",
                                                message: "Item deleted Successfully !",
                                                position: "topRight",
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
                        }
                    });
            });

            $("#video_modal").on("hidden.bs.modal", function() {
                $("#video").trigger("pause");
            });

            $("#imageResourceTable").on('click', '.show-img', function(e) {
                e.preventDefault();
                var imgURL = $(this).attr('src');
                console.log(imgURL);
                $('#show-image').attr('src', imgURL);
                $('#img-headline').text($(this).data('headline'))
                $('#img-desc').text($(this).data('description'))
                $('#showImageModal').modal('show');

            });

            $(document).on('click', '.delete-img', function(e) {
                e.preventDefault();
                var id = $(this).attr('rel');
                var text = 'You will not be able to recover it!';
                var btn = 'btn-danger';
                swal({
                        title: "Are you sure?",
                        text: text,
                        type: "warning",
                        showCancelButton: true,
                        cancelButtonClass: btn,
                        confirmButtonText: "Confirm",
                        cancelButtonText: "Cancel",
                        closeOnConfirm: true,
                        closeOnCancel: true,
                        allowOutsideClick: true
                    },
                    function(isConfirm) {
                        if (isConfirm) {
                            if (user_type == 1) {
                                $('.loader').show();
                                $.ajax({
                                    url: '{{ route('deleteAdImage') }}',
                                    type: 'POST',
                                    data: {
                                        "id": id
                                    },
                                    dataType: "json",
                                    cache: false,
                                    success: function(data) {
                                        $('.loader').hide();
                                        $('#imageResourceTable').DataTable().ajax.reload(
                                            null,
                                            false);
                                        if (data.success) {
                                            iziToast.success({
                                                title: "Successful!",
                                                message: "Item deleted Successfully !",
                                                position: "topRight",
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
                        }
                    });
            });
            $(document).on('click', '.edit-img', function(e) {
                e.preventDefault();
                $('#imgId').val($(this).attr('rel'));
                $('#edit_description').val($(this).data('description'));
                $('#edit_headline').val($(this).data('headline'));
                $('#edit_show_time').val($(this).data('show-time'));
                $('#edit_ad_img').attr('src', $(this).data('image'));
                $('#editImageModal').modal('show');
            });

            $("#videoResourceTable").on('click', '.edit-vid', function(e) {
                e.preventDefault();
                $('#vidId').val($(this).attr('rel'));
                var type = $(this).data('type');
                $('#edit_vid_description').val($(this).data('description'));
                $('#edit_vid_headline').val($(this).data('headline'));
                $('#edit_vid_type').append(`
                    <option ${type == 0 ? "selected" : ""} value="0">Must Watch</option>
                    <option ${type == 1 ? "selected" : ""} value="1">Skippable</option>
                `);
                $('#editVideoModal').modal('show');

            });

            $("#editVideoModal").on("hidden.bs.modal", function() {
                $("#edit_vid_type").empty();
                $('#editVideoForm').trigger("reset");
            });
            $("#editImageModal").on("hidden.bs.modal", function() {
                $('#editsImageForm').trigger("reset");
            });

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

            $("#editAdForm").on("submit", function(event) {
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
                    var formdata = new FormData($("#editAdForm")[0]);
                    formdata.append("is_android", is_android);
                    formdata.append("is_ios", is_iOS);

                    $.ajax({
                        url: "{{ url('ads/editCustomAd') }}",
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
