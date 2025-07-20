@extends('admin_layouts/main')
@section('pageSpecificCss')
    <link href="{{ asset('assets/bundles/datatables/datatables.min.css') }}" rel="stylesheet">
    <link href="{{ asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css') }}"
        rel="stylesheet">
    <link href="{{ asset('assets/bundles/izitoast/css/iziToast.min.css') }}" rel="stylesheet">

@stop
@section('content')
    <section class="section">
        <div class="section-body">

            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            <h4 class="box-title">Notify Users</h4>
                        </div>
                        <div class="card-body">
                            <form class="forms-sample" id="sendNotification">
                                {{ csrf_field() }}
                                <div class="form-row">
                                    <div class="form-group col-md-4">
                                        <label for="notification_topic">Select Topic</label>
                                        <select name="notification_topic" class="form-control" id="notification_topic">
                                            <option value="flixy">flixy</option>
                                        </select>
                                    </div>
                                    <div class="form-group col-md-4">
                                        <label for="notification_title">Title</label>
                                        <input type="text" name="notification_title" class="form-control"
                                            id="notification_title" placeholder="Enter Title">
                                    </div>
                                </div>
                                <div class="form-row">
                                    <div class="form-group col-md-4">
                                        <label for="notification_message">Message</label>
                                        <textarea type="text" name="notification_message" class="form-control" id="notification_message"
                                            placeholder="Enter Message"></textarea>
                                    </div>
                                    <div class="form-group col-md-4">
                                        <label> Image</label>
                                        <input type="file" name="notify_image" class="form-control" id="notify_image">

                                    </div>
                                    <div class="form-group col-md-4">
                                        <div id="photo_notify" class="col-md-10 mt-4"> </div>
                                    </div>
                                </div>

                                <button type="submit" class="btn btn-primary mr-2">Send</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
            <!-- <div class="row">
                                <div class="col-12">
                                  <div class="card">
                                    <div class="card-header">
                                      <h4>Notification List (<span class="total_notification">{{ $total_notification }}</span>)</h4>
                                    </div>
                                    <div class="card-body">
                                      <div class="table-responsive">
                                        <table class="table table-striped" id="notification-listing">
                                          <thead>
                                            <tr>
                                              <th>Title</th>
                                              <th>Message</th>
                                              <th>Image</th>
                                              <th>Actions</th>
                                            </tr>
                                          </thead>
                                          <tbody>

                                          </tbody>
                                        </table>
                                    </div>
                                </div>
                              </div> -->
        </div>
        </div>
    </section>

    <div class="modal fade" id="notificationModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="ModalLabel"> Update Notification </h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <form id="UpdateNotification" method="post" enctype="multipart">
                    {{ csrf_field() }}
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="notification_title">Title</label>
                            <input type="text" name="notification_title" class="form-control" id="notification_title"
                                placeholder="Enter Title">
                        </div>
                        <div class="form-group">
                            <label for="notification_message">Message</label>
                            <textarea type="text" name="notification_message" class="form-control" id="notification_message"
                                placeholder="Enter Message"></textarea>
                        </div>
                        <div class="form-group">
                            <label for="notification_img">Image</label>
                            <input type="file" id="notification_img" name="notification_img" class="form-control">
                            <div id="photo_gallery" class="col-md-10 mt-4">
                            </div>
                            <input type="hidden" name="hidden_notification_img" id="hidden_notification_img"
                                value="">
                        </div>
                    </div>
                    <div class="modal-footer">
                        <input type="hidden" name="notification_id" id="notification_id" value="">
                        <button type="submit" class="btn btn-success">Save</button>
                        <button type="button" class="btn btn-light" data-dismiss="modal">Close</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

@endsection

@section('pageSpecificJs')

    <script src="{{ asset('assets/bundles/datatables/datatables.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/jquery-ui/jquery-ui.min.js') }}"></script>
    <script src="{{ asset('assets/js/page/datatables.js') }}"></script>
    <script src="{{ asset('assets/js/fnStandingRedraw.js') }}"></script>
    <script src="{{ asset('assets/bundles/izitoast/js/iziToast.min.js') }}"></script>

    <script>
        $(document).ready(function() {

            var dataTable = $('#notification-listing').dataTable({
                'processing': true,
                'serverSide': true,
                'serverMethod': 'post',
                "order": [
                    [0, "desc"]
                ],
                'columnDefs': [{
                    'targets': [0, 1],
                    /* column index */
                    'orderable': false,
                    /* true or false */
                }],
                'ajax': {
                    'url': '{{ route('showNotificationList') }}',
                    'data': function(data) {
                        // Read values
                        // var user_id = $('#user_id').val();

                        // Append to data
                        // data.user_id = user_id;
                    }
                }
            });


            $('#notification_img').on('change', function() {
                imagesPreview(this, '#photo_gallery');
            });

            var imagesPreview = function(input, placeToInsertImagePreview) {

                if (input.files) {
                    var allowedExtensions = /(\.jpg|\.jpeg|\.png|\.jfif)$/i;
                    if (!allowedExtensions.exec(input.value)) {
                        iziToast.error({
                            title: 'Error!',
                            message: 'Please upload file having extensions .jpeg/.jpg/.png only.',
                            position: 'topRight'
                        });
                        input.value = '';
                        return false;
                    } else {

                        var reader = new FileReader();

                        reader.onload = function(event) {
                            $(placeToInsertImagePreview).html('<div class="borderwrap" data-href="' + event
                                .target.result + '"><div class="filenameupload"><img src="' + event
                                .target.result + '" width="130" height="130"> </div></div>');
                        }

                        reader.readAsDataURL(input.files[0]);
                    }
                }
            };

            $(document).on('change', '#notify_image', function() {
                imagesPreview(this, '#photo_notify');
            });

            $("#sendNotification").validate({
                rules: {
                    notification_topic: {
                        required: true,
                    },
                    notification_message: {
                        required: true,
                    },
                },
                messages: {
                    notification_topic: {
                        required: "Please Select Topic",
                    },
                    notification_message: {
                        required: "Please Enter Message",
                    },
                },

            });

            $(document).on('submit', '#sendNotification', function(e) {
                e.preventDefault();
                if (user_type == 1) {
                    var formdata = new FormData($("#sendNotification")[0]);
                    $('.loader').show();
                    $.ajax({
                        url: '{{ route('sendNotification') }}',
                        type: 'POST',
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(data) {
                            $('.loader').hide();
                            $('#sendNotification').trigger("reset");
                            $("#photo_notify").hide()
                            if (data.success == 1) {
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

            $('#notificationModal').on('hidden.bs.modal', function(e) {
                $("#UpdateNotification")[0].reset();
                $('.modal-title').text('Edit Notification');
                $('#notification_id').val("");
                $('#notification_title').val("");
                $('#notification_message').val("");
                $('#photo_gallery').html("");
                $('#hidden_notification_img').val("");
                var validator = $("#UpdateNotification").validate();
                validator.resetForm();
            });

            $("#notification-listing").on("click", ".UpdateNotification", function() {
                $('.loader').show();
                $('.modal-title').text('Edit Notification');
                var notification_img = $(this).attr('data-img');
                $('#notification_id').val($(this).attr('data-id'));
                $('#notification_title').val($(this).attr('data-title'));
                $('#notification_message').val($(this).attr('data-message'));
                var html =
                    '<div class="borderwrap"><div class="filenameupload"><img src="{{ env('DEFAULT_IMAGE_URL') }}' +
                    notification_img + '" width="130" height="130"> </div>  </div>';
                $('#photo_gallery').html(html);
                $('#hidden_notification_img').val(notification_img);
                $('.loader').hide();
            });

            $("#UpdateNotification").validate({
                rules: {
                    notification_img: {
                        required: {
                            depends: function(element) {
                                return ($('#notification_id').val() == 0)
                            }
                        }
                    }
                },
                messages: {
                    notification_img: {
                        required: "Please Select Notification Image",
                    }
                }
            });

            $(document).on('submit', '#UpdateNotification', function(e) {
                e.preventDefault();
                if (user_type == 1) {
                    var formdata = new FormData($("#UpdateNotification")[0]);
                    $('.loader').show();
                    $.ajax({
                        url: '{{ route('UpdateNotification') }}',
                        type: 'POST',
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(data) {
                            $('.loader').hide();
                            $('#notificationModal').modal('hide');
                            if (data.success == 1) {

                                $('#notification-listing').DataTable().ajax.reload(null, false);
                                $('.total_notification').text(data.total_notification);
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

            $(document).on('click', '#DeleteNotification', function(e) {
                e.preventDefault();
                var notification_id = $(this).attr('data-id');
                var text = 'You will not be able to recover this data!';
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
                    function(isConfirm) {
                        if (isConfirm) {
                            if (user_type == 1) {
                                $('.loader').show();
                                $.ajax({
                                    url: '{{ route('deleteNotification') }}',
                                    type: 'POST',
                                    data: {
                                        "notification_id": notification_id
                                    },
                                    dataType: "json",
                                    cache: false,
                                    success: function(data) {
                                        $('.loader').hide();
                                        $('#notification-listing').DataTable().ajax.reload(
                                            null, false);
                                        $('.total_notification').text(data
                                            .total_notification);
                                        if (data.success == 1) {
                                            swal("Confirm!",
                                                "Notification has been deleted!",
                                                "success");
                                        } else {
                                            swal("Confirm!",
                                                "Notification has not been deleted!",
                                                "error");
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
                                swal("Confirm!", "Notification has been deleted!", "success");
                            }
                        } else {
                            swal("Cancelled", "Your imaginary file is safe :)", "error");
                        }
                    });
            });

        });
    </script>

@endsection
