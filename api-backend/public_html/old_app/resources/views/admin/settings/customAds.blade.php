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
            <h4>Custom Ads</h4>
            <a href="createCustomAd" class="btn btn-primary text-light ml-auto" data-whatever="@mdo">Create New Ad</a>
        </div>

        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped" id="customAdsTable">
                    <thead>
                        <tr>
                            <th>Brand Logo</th>
                            <th>Number</th>
                            <th>Title</th>
                            <th>Brand Name</th>
                            <th>Platform</th>
                            <th>Views</th>
                            <th>Clicks</th>
                            <th>Start Date</th>
                            <th>End Date</th>
                            <th>On/Off</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
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
            var dataTable = $('#customAdsTable').dataTable({
                'processing': true,
                'serverSide': true,
                'serverMethod': 'post',
                "order": [
                    [0, "desc"]
                ],
                'columnDefs': [{
                    'targets': [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
                    /* column index */
                    'orderable': false,
                    /* true or false */
                }],
                'ajax': {
                    'url': '{{ route('showCustomAdsList') }}',
                    'data': function(data) {}
                }
            });

            $("#customAdsTable").on("change", ".ad_status", function(event) {
                event.preventDefault();

                if (user_type == "1") {
                    if ($(this).prop("checked") == true) {
                        var value = 1;
                    } else {
                        value = 0;
                    }
                    var itemId = $(this).attr("rel");

                    var url = "{{ url('ads/changeAdStatus') }}" + "/" + itemId + "/" + value;

                    $.getJSON(url).done(function(data) {
                        // console.log(data);
                        $("#customAdsTable").DataTable().ajax.reload(null, false);

                        if (data.status) {
                            iziToast.success({
                                title: "Success!",
                                message: "Status Changed successfully.",
                                position: "topRight",
                                timeOut: 4000,
                            });
                        } else {
                            iziToast.error({
                                title: "Ad Has No Resources!",
                                message: "Please add resources to this Ad.",
                                position: "topRight",
                            });
                        }


                    });
                } else {
                    iziToast.error({
                        title: app.Error,
                        message: app.tester,
                        position: "topRight",
                    });
                }
            });


            $(document).on('click', '.deleteCustomAd', function(e) {
                e.preventDefault();
                var id = $(this).attr('data-id');
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
                                    url: '{{ route('deleteCustomAd') }}',
                                    type: 'POST',
                                    data: {
                                        "id": id
                                    },
                                    dataType: "json",
                                    cache: false,
                                    success: function(data) {
                                        $('.loader').hide();
                                        $('#customAdsTable').DataTable().ajax.reload(null,
                                            false);
                                        // $('.total_category').text(data.total_category);
                                        if (data.success == 1) {
                                            swal("Confirm!",
                                                "Item has been deleted!",
                                                "success");
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

        });
    </script>

@endsection
