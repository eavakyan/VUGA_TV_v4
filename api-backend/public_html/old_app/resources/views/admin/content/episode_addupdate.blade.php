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
          </div>
        @endif    
        <div class="card-body">
          @if($title == 'New')
            <h4 class="text-center mb-0"> {{$title}} {{$content_title}} Episode in {{$season_title}}</h4>
          @else
            <h4 class="text-center mb-0"> {{$title}} {{$data['episode_title']}}</h4>
          @endif          
        </div>
      </div>
    <div class="card">
      <div class="card-body">
      @include('admin.section.episode_header')
        <div class="card-body">
          <form class="forms-sample" id="addUpdateSeasonEpisode">
            {{ csrf_field() }}

                <input id="episode_season_id" name="episode_season_id" type="hidden" class="form-control form-control-danger" value="{{$season_id}}">

                <div class="form-row">
                  <div class="col-md-6">
                      <div class="form-group col-md-12 p-0">
                          <label for="episode_title">Episode Title</label>
                          <input type="text" class="form-control valid" id="episode_title" name="episode_title" aria-required="true" aria-invalid="false" placeholder="Episode Title" value="@if($data){{$data['episode_title']}}@endif">
                      </div>
                      <div class="form-row">
                        <div class="form-group col-md-6">
                        <label for="duration">Duration</label>
                        <input type="text"  name="duration" class="form-control" id="duration" placeholder="Duration" value="@if($data){{$data['episode_duration']}}@endif">
                        </div>
                        <?php $selected = $selected1 = $selected2 = '';
                            if($data && $data['access_type']){ 
                                if($data['access_type'] == 1){
                                    $selected = 'selected';
                                }
                                if($data['access_type'] == 2){
                                    $selected1 = 'selected';
                                }
                                if($data['access_type'] == 3){
                                    $selected2 = 'selected';
                                }
                            }
                        ?>
                        <div class="form-group col-md-6">
                            <label for="access_type">Select Access Type</label>
                            <select id="access_type" name="access_type" class="form-control form-control-danger">
                                <option value="1" {{$selected}}>Free</option>
                                <option value="2" {{$selected1}}>Paid</option>
                                <option value="3" {{$selected2}}>Unlock With Video Ads</option>
                            </select>
                        </div> 
                        </div> 
                        <div class="form-group">
                              <label for="description">Description</label>
                              <textarea  name="description" class="form-control" id="description" placeholder="Description" >@if($data){{$data['episode_description']}}@endif</textarea>
                      </div> 
                      </div> 
                      
                    <div class="form-group col-md-2">
                        <label for="episodeprofile">Episode Thumb</label>
                        <input type="file" class="form-control-file file-upload custom_image valid" id="episode_thumb" name="episode_thumb" aria-required="true" aria-invalid="false">
                        <label id="episode_thumb-error" class="error image_error" for="episode_thumb" style="display: none;"></label>
                    </div>
                    <div class="col-md-4">
                        <div class="episode_thumb_preview">
                        @if($data && $data['episode_thumb'])
                            <div class="borderwrap" data-href="@if($data && $data['episode_thumb']){{$data['episode_thumb']}}@endif"><div class="filenameupload"><img src="@if($data && $data['episode_thumb']){{url(env('DEFAULT_IMAGE_URL').$data['episode_thumb'])}}@endif" width="300" height="300">  </div></div>
                        @endif
                        </div>
                    </div>
                <!-- </div>
                <div class="form-row"> -->

                 
                    
                </div> 
                
          <input type="hidden" name="episode_id" id="episode_id" value="@if($data){{$data['episode_id']}}@endif">
          <input type="hidden" name="action" id="action" value="@if($data){{'update'}}@else{{'add'}}@endif">
          <button type="submit" class="btn btn-primary mr-2 channel_add" >Submit</button>
          <a class="btn btn-light" href="{{route('series/season/list',['flag'=>2,'id'=>$seasonData['content_id'] ])}}">Cancel</a>

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
  
    $(document).on('change', '#episode_thumb', function() {
      imagesPreview(this, '.episode_thumb_preview');
    });

    var imagesPreview = function(input, placeToInsertImagePreview) {

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
                    $(placeToInsertImagePreview).html('<div class="borderwrap" data-href="'+event.target.result+'"><div class="filenameupload"><img src="'+event.target.result+'" width="300" height="300">  </div></div>');
                }

                reader.readAsDataURL(input.files[i]);
                }
            }
        }
    };

    $("#addUpdateSeasonEpisode").validate({
      rules: {
        episode_thumb:{
          required: {
            depends: function(element) {
              return ($('#episode_id').val() == "")
            }
          },
        },
        episode_title: {
          required: true,
        },
        description: {
          required: true,
        },
        duration: {
          required: true,
        },
        episode_title: {
          required: true,
        },
        access_type:{
          required: true,
        },
      },
      messages: {
        episode_thumb: {
          required: "Please Upload Thumb",
        },
        episode_title: {
          required: "Please Enter Title",
        },
        description: {
          required: "Please Enter Description",
        },
        duration: {
          required: "Please Enter Duration",
        },
        access_type:{
          required: "Please Select Access type",
        },
      },

    });

  $(document).on('submit', '#addUpdateSeasonEpisode', function (e) {
      e.preventDefault();
      if (user_type == 1) {
        var formdata = new FormData($("#addUpdateSeasonEpisode")[0]);
        $('.loader').show();
        $.ajax({
            url: '{{ route("addUpdateSeasonEpisode") }}',
            type: 'POST',
            data: formdata,
            dataType: "json",
            contentType: false,
            cache: false,
            processData: false,
            success: function (data) {
                $('.loader').hide();
                $('#seasonEpisodeModal').modal('hide');
                if (data.success == 1) {
                  if(data.flag == 1){
                    window.location.href ='{{ url("series/season/episode/edit") }}'+"/"+data.season_id+"/"+data.episode_id;
                  }else{
                    $('.is_source').attr('href','{{ url("series/season/episode/source/list") }}'+"/"+data.season_id+"/"+data.episode_id);
                    $('.is_subtitles').attr('href','{{url("series/season/episode/subtitle/list") }}'+"/"+data.season_id+"/"+data.episode_id);
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
      }else{
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