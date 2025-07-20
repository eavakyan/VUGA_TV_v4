@extends('admin_layouts/main')
@section('pageSpecificCss')
    <link href="{{ asset('assets/bundles/datatables/datatables.min.css') }}" rel="stylesheet">
    <link href="{{ asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css') }}"
        rel="stylesheet">
    <link href="{{ asset('assets/bundles/summernote/summernote-bs4.css') }}" rel="stylesheet">
    <link href="{{ asset('assets/bundles/izitoast/css/iziToast.min.css') }}" rel="stylesheet">

@stop
@section('content')
    <section class="section">
        <div class="section-body">
            <div class="row">
                <div class="col-12">
                    <div class="card">
                        <div class="card-header">
                            <h4>TV Category List (<span class="total_category">{{ $total_category }}</span>)</h4>
                        </div>

                        <div class="card-body">
                            <div class="pull-right">
                                <div class="buttons">
                                    <button class="btn btn-primary text-light" data-toggle="modal"
                                        data-target="#categoryModal" data-whatever="@mdo">Add TV Category</button>
                                </div>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-striped" id="category-listing">
                                    <thead>
                                        <tr>
                                            <th>Icon</th>
                                            <th>Name</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>

                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="modal fade" id="categoryModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="ModalLabel"> Add TV Category </h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <form id="addUpdateTVCategory" method="post" enctype="multipart">
                    {{ csrf_field() }}
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="category_name">Category Name</label>
                            <input id="category_name" name="category_name" type="text"
                                class="form-control form-control-danger" placeholder="Enter TV Category Name">
                        </div>

                        <div class="form-group">
                            <label for="categoryprofile">Category Icon</label>
                            <input type="file" class="form-control-file file-upload custom_image valid"
                                id="category_image" name="category_image" aria-required="true" aria-invalid="false">
                            <label id="category_image-error" class="error image_error" for="category_image"
                                style="display: none;"></label>
                            <div class="preview_categoryimg mt-4"></div>
                        </div>

                    </div>
                    <div class="modal-footer">
                        <input type="hidden" name="category_id" id="category_id" value="">
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
    <script src="{{ asset('assets/bundles/izitoast/js/iziToast.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/summernote/summernote-bs4.js') }}"></script>

    <script>
        $(document).ready(function() {
            var dataTable = $('#category-listing').dataTable({
                'processing': true,
                'serverSide': true,
                'serverMethod': 'post',
                "order": [
                    [0, "desc"]
                ],
                'columnDefs': [{
                    'targets': [2],
                    /* column index */
                    'orderable': false,
                    /* true or false */
                }],
                'ajax': {
                    'url': '{{ route('showTVCategoryList') }}',
                    'data': function(data) {}
                }
            });


            $(document).on('change', '#category_image', function() {
                CheckFileExtention(this, 'preview_categoryimg');
            });

            var CheckFileExtention = function(input, cl) {

                if (input.files) {
                    var allowedExtensions = /(\.jpg|\.jpeg|\.png)$/i;
                    if (!allowedExtensions.exec(input.value)) {
                        iziToast.error({
                            title: 'Error!',
                            message: 'Please upload file having extensions .jpeg/.jpg/.png only.',
                            position: 'topRight'
                        });
                        input.value = '';
                        return false;
                    } else {
                        if (cl.length > 0) {
                            var reader = new FileReader();

                            reader.onload = function(e) {
                                $('.' + cl).html('<div class=""><img src="' + e.target.result +
                                    '" width="150" height="150"/> </div>');
                            }

                            reader.readAsDataURL(input.files[0]);
                        }
                    }
                }
            };

            $('#categoryModal').on('hidden.bs.modal', function(e) {
                $("#addUpdateTVCategory")[0].reset();
                $('.modal-title').text('Add TV Category');
                $('#category_id').val("");
                $('#about_category').summernote("code", "");
                $('.preview_categoryimg').html("");
                var validator = $("#addUpdateTVCategory").validate();
                validator.resetForm();
            });

            $("#category-listing").on("click", ".UpdateTVCategory", function() {
                $('.modal-title').text('Edit TV Category');
                $('#category_id').val($(this).attr('data-id'));
                $('#category_name').val($(this).attr('data-category_name'));
                var image = $(this).attr('data-image');
                $('.preview_categoryimg').html('<div class=""><img src="' + image +
                    '" width="150" height="150"/> </div>');
            });

            $("#addUpdateTVCategory").validate({
                rules: {
                    category_name: {
                        required: true,
                        remote: {
                            url: '{{ route('CheckExistTVCategory') }}',
                            type: "post",
                            data: {
                                category_name: function() {
                                    return $("#category_name").val();
                                },
                                category_id: function() {
                                    return $("#category_id").val();
                                },
                            }
                        }
                    },
                },
                messages: {
                    category_name: {
                        required: "Please Enter Category Name",
                        remote: "Category Name Already Exist.",
                    },
                }
            });

            $(document).on('submit', '#addUpdateTVCategory', function(e) {
                e.preventDefault();
                if (user_type == 1) {
                    var formdata = new FormData($("#addUpdateTVCategory")[0]);
                    $('.loader').show();
                    $.ajax({
                        url: '{{ route('addUpdateTVCategory') }}',
                        type: 'POST',
                        data: formdata,
                        dataType: "json",
                        contentType: false,
                        cache: false,
                        processData: false,
                        success: function(data) {
                            $('.loader').hide();
                            $('#categoryModal').modal('hide');
                            if (data.success == 1) {

                                $('#category-listing').DataTable().ajax.reload(null, false);
                                $('.total_category').text(data.total_category);
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

            $(document).on('click', '.DeleteTVCategory', function(e) {
                e.preventDefault();
                var category_id = $(this).attr('data-id');
                var text = 'You will not be able to recover TV Category data!';
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
                                    url: '{{ route('deleteTVCategory') }}',
                                    type: 'POST',
                                    data: {
                                        "category_id": category_id
                                    },
                                    dataType: "json",
                                    cache: false,
                                    success: function(data) {
                                        $('.loader').hide();
                                        $('#category-listing').DataTable().ajax.reload(null,
                                            false);
                                        $('.total_category').text(data.total_category);
                                        if (data.success == 1) {
                                            swal("Confirm!",
                                                "TV Category has been deleted!",
                                                "success");
                                        } else {
                                            swal("Confirm!",
                                                "TV Category has not been deleted!",
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
                                swal("Confirm!", "TV Category has been deleted!", "success");
                            }
                        } else {
                            swal("Cancelled", "Your imaginary file is safe :)", "error");
                        }
                    });
            });

        });
    </script>

@endsection
