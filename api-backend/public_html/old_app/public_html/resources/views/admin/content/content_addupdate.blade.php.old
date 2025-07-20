@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/jquery-selectric/selectric.css')}}" rel="stylesheet">

<style type="text/css">
.custom-switch-input:checked ~ .custom-switch-indicator:before {
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

input:checked + .slider {
  background-color: #2196F3;
}

input:focus + .slider {
  box-shadow: 0 0 1px #2196F3;
}

input:checked + .slider:before {
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
  <div class="section-body">

<div class="row">
  <div class="col-12 col-md-12 col-lg-12">
      <div class="card card-stats">
        @if($title == 'Edit')
          <div class="card-content views-body  pull-right">
            <a href="#" class="btn btn-tab-movie"><i class="material-icons">remove_red_eye</i> {{ $data->total_view }} Views</a>
            <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">cloud_download</i> {{ $data->total_download }} Downloads</a>
            <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">share</i> {{ $data->total_share }} Shares</a>
          </div>
        @endif     
        <div class="card-body">
          @if($title == 'Add')
            <h4 class="text-center mb-0"> {{$title}} New Content</h4>
          @else
            <h4 class="text-center mb-0"> {{$title}} {{$content_title}} </h4>
          @endif          
        </div>
      </div>
    <div class="card">
      <div class="card-body">
      @include('admin.section.content_header')
      @if($title == 'Add')
      <div style="float: right;"><label class="switch"> <input type="checkbox" id="is_notify" > <span class="slider round"></span> </label></div>
      @endif 
        <div class="card-body">
          <form class="forms-sample" id="addUpdateContent">
            {{ csrf_field() }}

            <div class="form-row">
              <div class="form-group col-md-6">
                <label for="content_title">Content Title</label>
                <input type="text"  name="content_title" class="form-control" id="content_title" placeholder="Content Title" value="@if($data){{$data['content_title']}}@endif">
              </div>
              <?php $selected = $selected1 = $selected2 = $style = '';
                if($data && $data['content_type']){ 
                  if($data['content_type'] == 1){
                    $selected = 'selected';
                     $style = "display:block;";
                  }
                  if($data['content_type'] == 2){
                    $selected1 = 'selected';
                    $style = "display:none;";
                  }
                }
              ?>
              <div class="form-group col-md-6">
                <label for="content_type">Select Content Type</label>
                <select id="content_type" name="content_type" class="form-control form-control-danger">
                  <option value="1" {{$selected}}>Movie</option>
                  <option value="2" {{$selected1}}>Series</option>
                </select>
              </div>  
            </div>
            <div class="form-row">
              <div class="form-group col-md-6">
                <label for="description">Description</label>
                <textarea  name="description" class="form-control" id="description" placeholder="Description" >@if($data){{$data['description']}}@endif</textarea>
              </div>
              <div class="form-group col-md-3">
                <label for="duration">Duration</label>
                <input type="text"  name="duration" class="form-control" id="duration" placeholder="Duration" value="@if($data){{$data['duration']}}@endif">
              </div>
              <div class="form-group col-md-3">
                <label for="release_year">Release Year</label>
                <input type="text"  name="release_year" class="form-control" id="release_year" placeholder="2021" value="@if($data){{$data['release_year']}}@endif">
              </div>
              
            </div>
      
            <div class="form-row">
             <div class="form-group col-md-6">
                <label for="ratings">Ratings</label>
                <input type="text"  name="ratings" class="form-control" id="ratings" placeholder="0" value="@if($data){{$data['ratings']}}@endif">
              </div>
              <div class="form-group col-md-6">
                <label for="language_id">Select Language</label>
                <select id="language_id" name="language_id" class="form-control form-control-danger">
                  @foreach($languagedata as $value)
                  <?php $selected=""; if($data && $data['language_id'] == $value['language_id']){ $selected="selected"; }?>
                    <option value="{{$value['language_id']}}" {{$selected}}>{{$value['language_name']}}</option>
                  @endforeach
                </select>
              </div>

              
            </div>
            <div class="form-row">
              <!-- <div class="form-group col-md-6">
                <label for="download_link">Download Link</label>
                <input type="text"  name="download_link" class="form-control" id="download_link" placeholder="Download Link" value="@if($data){{$data['download_link']}}@endif">
              </div> -->
              <div class="form-group col-md-6">
                <label for="genre_id">Select Genre(multiple)</label>
                <select id="genre_id" name="genre_id[]" class="form-control form-control-danger selectric" multiple="">
                <option value="" disabled>-- Select --</option>
                  @foreach($genredata as $value)
                  <?php $selected=""; if($data && in_array($value['genre_id'],explode(',',$data['genre_id']))){ $selected="selected"; }?>
                    <option value="{{$value['genre_id']}}" {{$selected}}>{{$value['genre_name']}}</option>
                  @endforeach
                </select>
              </div>
              <div class="form-group col-md-6 trailer_div" style="{{$style}}">
                <label for="trailer_url">Youtube Trailer Id</label>
                <input type="text"  name="trailer_url" class="form-control" id="trailer_url" placeholder="Youtube Trailer Id" value="@if($data){{$data['trailer_url']}}@endif">
              </div>
            </div>

            
            <div class="form-row">
                <div class="form-group col-md-4">
                    <label for="verticle_poster">Verticle Poster</label>
                    <input type="file" name="verticle_poster" class="form-control" id="verticle_poster"/>
                    <input type="hidden" name="hidden_verticle_poster" class="hidden_verticle_poster" value="@if($data && $data['verticle_poster']){{$data['verticle_poster']}}@endif">
                </div>

                <div class="form-group col-md-8">
                    <label for="horizontal_poster">Horizontal Poster</label>
                    <input type="file" name="horizontal_poster" class="form-control" id="horizontal_poster"/>
                    <input type="hidden" name="hidden_horizontal_poster" class="hidden_horizontal_poster" value="@if($data && $data['horizontal_poster']){{$data['horizontal_poster']}}@endif">
                </div>

            </div>

            <div class="form-row">
                <div class="col-md-4" id="verticle_poster_preview">
                  @if($data && $data['verticle_poster'])
                    <div class="borderwrap" data-href="@if($data && $data['verticle_poster']){{$data['verticle_poster']}}@endif"><div class="filenameupload"><img src="@if($data && $data['verticle_poster']){{url(env('DEFAULT_IMAGE_URL').$data['verticle_poster'])}}@endif" width="300" height="500">  </div></div>
                  @endif
                </div>

                <div class="col-md-8" id="horizontal_poster_preview">
                  @if($data && $data['horizontal_poster'])
                    <div class="borderwrap" data-href="@if($data && $data['horizontal_poster']){{$data['horizontal_poster']}}@endif"><div class="filenameupload"><img src="@if($data && $data['horizontal_poster']){{url(env('DEFAULT_IMAGE_URL').$data['horizontal_poster'])}}@endif" width="750" height="500">  </div></div>
                  @endif
                </div>               
               
            </div>

          </div>

          <input type="hidden" name="content_id" id="content_id" value="@if($data){{$data['content_id']}}@endif">
          <input type="hidden" name="action" id="action" value="@if($data){{'update'}}@else{{'add'}}@endif">
          <button type="submit" class="btn btn-primary mr-2 content_add" >Submit</button>
          <a class="btn btn-light" href="{{route('content/list')}}">Cancel</a>

        </form>
      </div>
    </div>
  </div>

  </div>
</section>

@endsection
@section('pageSpecificJs')
<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-selectric/jquery.selectric.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-steps/jquery.steps.min.js')}}"></script>
<script src="{{asset('assets/js/page/form-wizard.js')}}"></script>

<script>

  $(document).ready(function() {
  
    $(document).on('change', '#content_type', function() {
      var value = $(this).val();
      if(value == 2){
        $(".is_series").show();
        $(".is_movie").hide();
        $(".trailer_div").attr('style','display:none;');
      }else{
        $(".is_movie").show();
        $(".is_series").hide();
        $(".trailer_div").attr('style','display:block;');
      }
      
    });

    $(document).on('change', '#verticle_poster', function() {
      imagesPreview(this, '#verticle_poster_preview',width="300",height="500");
    });
    
    $(document).on('change', '#horizontal_poster', function() {
      imagesPreview(this, '#horizontal_poster_preview',width="730",height="500");
    });

    var imagesPreview = function(input, placeToInsertImagePreview,width,height) {

        if (input.files) {
            var filesAmount = input.files.length;
            var allowedExtensions = /(\.jpg|\.jpeg|\.png|\.jfif|\.webp)$/i;
            for (i = 0; i < filesAmount; i++) {

                if(!allowedExtensions.exec(input.value)){
                iziToast.error({
                    title: 'Error!',
                    message: 'Please upload file having extensions .jpeg/.jpg/.png only.',
                    position: 'topRight'
                });
                input.value = '';
                return false;
                }else{

                var reader = new FileReader();

                reader.onload = function(event) {
                    $(placeToInsertImagePreview).html('<div class="borderwrap" data-href="'+event.target.result+'"><div class="filenameupload"><img src="'+event.target.result+'" width="'+width+'" height="'+height+'">  </div></div>');
                }

                reader.readAsDataURL(input.files[i]);
                }
            }
        }
    };
    $('select[name="genre_id[]"]').on('change', function() { // fires when the value changes
        $(this).valid(); // trigger validation on hidden select
    });
  $("#addUpdateContent").validate({
    ignore: ':hidden:not(select)',
    rules: {
        content_title: {
          required: true,
        },
        description:{
          required: true,
        },
        duration:{
          required: true,
        },
        ratings:{
          required: true,
        },
        release_year:{
          required: true,
        },
        language_id:{
          required: true,
        },
        trailer_url:{
          required: true,
        },
        "genre_id[]":{
          required: true,
        },
        verticle_poster:{
          required: {
            depends: function(element) {
              return ($('#content_id').val() == "")
            }
          },
        },
        horizontal_poster:{
          required: {
            depends: function(element) {
              return ($('#content_id').val() == "")
            }
          },
        },
      },
      messages: {
        content_title: {
          required: "Please Enter Content Title",
        },
        description: {
          required: "Please Enter Description",
        },
        duration: {
          required: "Please Enter Duration",
        },
        ratings: {
          required: "Please Enter Ratings",
        },
        release_year: {
          required: "Please Enter Release Year",
        },
        language_id: {
          required: "Please Select Language",
        },
        trailer_url: {
          required: "Please Enter Youtube Trailer Id",
        },
        "genre_id[]":{
          required: "Please Select Genre",
        },
        verticle_poster: {
          required: "Please Upload Verticle Poster",
        },
        horizontal_poster: {
          required: "Please Upload Horizontal Poster",
        },
      },
      errorPlacement: function(error, element) {
        // check if element has Selectric initialized on it
        var data = element.data('selectric');
        error.appendTo( data ? element.closest( '.' + data.classes.wrapper ).parent() : element.parent() );
      }

  });

  $(document).on('submit', '#addUpdateContent', function (e) {
    e.preventDefault();
    if (user_type == 1) {
      var formdata = new FormData($("#addUpdateContent")[0]);
      formdata.append('is_notify',$("#is_notify").val());
      $('.loader').show();
      $.ajax({
          url: '{{ route("addUpdateContent") }}',
          type: 'POST',
          data: formdata,
          dataType: "json",
          contentType: false,
          cache: false,
          processData: false,
          success: function (data) {
              $('.loader').hide();
              if (data.success == 1) {
                if(data.flag == 1){
                    window.location.href ='{{ url("content/edit") }}'+"/"+data.content_type+"/"+data.content_id;
                }else{
                  var url = '{{ url("movie/source/list/1") }}'+"/"+data.content_id;
                  // window.location.href = '{{ route("content/list") }}';
                  $('.is_movie_source').attr('href','{{ url("movie/source/list/1") }}'+"/"+data.content_id);
                  $('.is_series_source').attr('href','{{ url("series/source/list/2") }}'+"/"+data.content_id);
                  $('.is_cast').attr('href','{{ url("movie/cast/list") }}'+"/"+data.content_id);
                  $('.is_movie_subtitles').attr('href','{{url("movie/subtitle/list/1") }}'+"/"+data.content_id);
                  $('.is_series_subtitles').attr('href','{{url("series/subtitle/list/2") }}'+"/"+data.content_id);
                  $('.is_season').attr('href','{{url("series/season/list") }}'+"/"+data.content_id);
                }
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
          error: function (jqXHR, textStatus, errorThrown) {
              alert(errorThrown);
          }
      });
    } else{
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