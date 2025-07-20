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
          <!--<div class="card-content views-body  pull-right">-->
          <!--  <a href="#" class="btn btn-tab-movie"><i class="material-icons">remove_red_eye</i> {{ $data->total_view }} Views</a>-->
          <!--  <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">share</i> {{ $data->total_share }} Shares</a>-->
          <!--</div>-->
        @endif   
        <div class="card-body">
        @if($title == 'Add')
            <h4 class="text-center mb-0"> New TV Channel </h4>
          @else
            <h4 class="text-center mb-0"> {{$title}} {{$channel_title}}</h4>
          @endif  
        </div>
      </div>
    <div class="card">
      <div class="card-body">
      <!-- @include('admin.section.tv_channel_header') -->
        <div class="card-body">
            <form class="forms-sample" id="addUpdateTVChannel">
            {{ csrf_field() }}
 

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
            <div class="form-row">
                <div class="form-group col-md-4">
                  <label for="channel_title">TV Channel Title</label>
                  <input type="text"  name="channel_title" class="form-control" id="channel_title" placeholder="TV Channel Title" value="@if($data){{$data['channel_title']}}@endif">
                </div>

                <div class="form-group col-md-4">
                <label for="access_type">Select Access Type</label>
                <select id="access_type" name="access_type" class="form-control form-control-danger">
                      <option value="1" {{$selected}}>Free</option>
                      <option value="2" {{$selected1}}>Paid</option>
                      <option value="3" {{$selected2}}>Unlock With Video Ads</option>
                    </select>
              </div>      
              
              <div class="form-group col-md-4">
                <label for="category_id">Select Category(multiple)</label>
                <select id="category_id" name="category_id[]" class="form-control form-control-danger selectric" multiple="">
                <option value="" disabled>-- Select --</option>
                @foreach($categoryData as $value)
                  <?php $selected=""; if($data && in_array($value['category_id'],explode(',',$data['category_id']))){ $selected="selected"; }?>
                    <option value="{{$value['category_id']}}" {{$selected}}>{{$value['category_name']}}</option>
                  @endforeach
                </select>
              </div>
            

            </div>

            <?php $selected3 = $selected4 = '';
                if($data && $data['source_type']){ 
                  if($data['source_type'] == 1){
                    $selected3 = 'selected';
                  }
                  if($data['source_type'] == 2){
                    $selected4 = 'selected';
                  }
                }
              ?>
            <div class="form-row">
              <div class="form-group col-md-6">
                    <label for="source_type">Select Source Type</label>
                    <select name="source_type" class="form-control form-control-danger" id="source_type">
                    <option value="1" {{$selected3}}>Youtube Id</option>
                    <option value="2" {{$selected4}}>M3u8 Url</option>
                    </select>
                </div> 

                <div class="form-group col-md-6">
                    <label for="source">Source</label>
                    <input type="text"  name="source" class="form-control" id="source" value="@if($data){{$data['source']}}@endif" placeholder="Source URL">
                </div>
              </div>



            <div class="form-row">
           
           <div class="form-row">
             <div class="form-group col-md-6">
                 <label for="channel_img">Add TV Channel Images</label>
                 <input type="file" name="channel_thumb" id="channel_thumb" class="form-control channel_thumb"/>
             </div>
             <div class="form-group col-md-6">
               <div id="photo_gallery">
                   @if($data && !empty($data['channel_thumb']))
                   <div class="borderwrap" data-href="@if(!empty($data['channel_thumb'])){{$data['channel_thumb']}}@endif">
                       <div class="filenameupload"><img src="@if($data && !empty($data['channel_thumb']) ){{url(env('DEFAULT_IMAGE_URL').$data['channel_thumb'])}}@endif" width="400" height="200">
                       </div>
                   </div>
                   @endif
                 </div>
             </div>
           </div>
         </div>
         
            <input type="hidden" name="channel_id" id="channel_id" value="@if($data){{$data['channel_id']}}@endif">
            <input type="hidden" name="action" id="action" value="@if($data){{'update'}}@else{{'add'}}@endif">
            <button type="submit" class="btn btn-primary mr-2 channel_add" >Submit</button>
            <a class="btn btn-light" href="{{route('channel/list')}}">Cancel</a>
          </form>
        </div>
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
<script>

  $(document).ready(function() {

    $(document).on('change', '#channel_thumb', function() {
      imagesPreview(this, '#photo_gallery');
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
                    $(placeToInsertImagePreview).html('<div class="borderwrap" data-href="'+event.target.result+'"><div class="filenameupload"><img src="'+event.target.result+'" width="400" height="200">  </div></div>');
                }

                reader.readAsDataURL(input.files[i]);
                }
            }
        }
    };
    $('select[name="category_id[]"]').on('change', function() { // fires when the value changes
        $(this).valid(); // trigger validation on hidden select
    });
    $("#addUpdateTVChannel").validate({
      ignore: ':hidden:not(select)',
      rules: {
        channel_title: {
          required: true,
          remote: {
              url: '{{ route("CheckExistTVChannel") }}',
              type: "post",
              data: {
                  channel_title: function () { return $("#channel_title").val(); },
                  channel_id: function () { return $("#channel_id").val(); },
              }
          }
        },
        "category_id[]": {
          required: true,
        },
        channel_thumb:{
          required: {
            depends: function(element) {
              return ($('#channel_id').val() == "")
            }
          },
        },
        source_type:{
          required: true,
        },
        source:{
          required: true,
        },
      },
      messages: {
        channel_title: {
          required: "Please Enter TV Channel Title",
          remote: "TV Channel Already Exist.",
        },
        "category_id[]": {
          required: "Please Select Category",
        },
        channel_thumb: {
          required: "Please Upload Image",
        },
        source_type:{
          required: "Please Select Source type",
        },
        source: {
          required: "Please Enter Source",
        },
      },
      errorPlacement: function(error, element) {
        // check if element has Selectric initialized on it
        var data = element.data('selectric');
        error.appendTo( data ? element.closest( '.' + data.classes.wrapper ).parent() : element.parent() );
      }

    });

  $(document).on('submit', '#addUpdateTVChannel', function (e) {
      e.preventDefault();
      if (user_type == 1) {
      var formdata = new FormData($("#addUpdateTVChannel")[0]);
      $('.loader').show();
      $.ajax({
          url: '{{ route("addUpdateTVChannel") }}',
          type: 'POST',
          data: formdata,
          dataType: "json",
          contentType: false,
          cache: false,
          processData: false,
          success: function (data) {
              $('.loader').hide();
              if (data.success == 1) {
                // window.location.href = '{{ route("channel/list") }}';
                if(data.flag == 1){
                  window.location.href ='{{ url("tv/channel/edit") }}'+"/"+data.channel_id;
                }else{
                  $('.is_source').attr('href','{{ url("tv/channel/source/list") }}'+"/"+data.channel_id);
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