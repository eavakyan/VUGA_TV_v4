@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/summernote/summernote-bs4.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">

@stop

@section('content')

<section class="section">
    <div class="section-body">
        <div class="row mt-sm-4">
            <div class="col-12 col-md-12 col-lg-12">
                <div class="card ">
                <div class="card-header">
                            <h4>Privacy Policy</h4>
                        </div>
                    <div class="card-body">
                
                    <form method="POST" id="privacypolicyForm" novalidate>
                        @csrf
                        <div class="form-row">
                            <div class="form-group col-12">
                                <textarea id="policy" name="policy" placeholder="Privacy"
                                    class="form-control summernote">@if($privacyPolicy){{$privacyPolicy->policy}}@endif</textarea>
                            </div>
                        </div>
                        <input type="hidden" value="@if($privacyPolicy){{$privacyPolicy->id}}@endif" name="id">
                        <button class="btn btn-primary">Save</button>
                    </form>
                    </div>
                </div>
            </div>
  
        </div>
    </div>
</section>
@stop

@section('pageSpecificJs')
<script src="{{asset('assets/bundles/summernote/summernote-bs4.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>

    <script>
        $('.summernote').summernote({
            height: 400,
            toolbar: [
                ["style", ["bold", "italic", "underline", "clear"]],
                // ["font", ["strikethrough"]],
                ['fontsize', ['fontsize']],
                ['color', ['color']],
                ["para", ["paragraph"]]
            ]
        });
        $(document).ready(function() {
            $(document).on('submit', '#privacypolicyForm', function(e) {
                e.preventDefault();
                if (user_type == 1) {
                    var formdata = new FormData($("#privacypolicyForm")[0]);
                    $('.loader').show();
                    $.ajax({
                        url: '{{ route("UpdatePrivacypolicy") }}',
                        type: "post",
                        data: formdata,
                        cache: false,
                        processData: false,
                        contentType: false,
                        dataType: "json",
                        success: function(response) {
                            $('.loader').hide();
                            if (response.success == 1) {
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
@endsection