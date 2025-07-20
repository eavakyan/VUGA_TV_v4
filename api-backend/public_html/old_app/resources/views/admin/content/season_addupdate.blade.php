@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">
<style type="text/css">
.card1{
  display: inline-block;
  position: relative;
  width: 100%;
  margin: 25px 0;
  box-shadow: 0 1px 4px 0 rgb(0 0 0 / 14%);
  border-radius: 6px;
  color: rgba(0,0,0, 0.87);
  background: #fff;
}
.btn-tab-season{
    background-color: white !important;
    border: 2px solid black !important;
    color: black !important;
    padding: 10px;
    position: absolute;
    top: -20px;
    left: 23px;
    border-radius: 1px !important;
}

.btn-tab-season-action {   
    position: relative;
    top: -20px;
    margin-right: 10px;
    border-radius: 1px !important;
    background-color: black !important;
    border: 2px solid black !important;
    color: white !important;
}
</style>
@stop
@section('content')
<section class="section">
  <div class="section-body">
      
    <div class="row">
        <div class="col-12 col-md-12 col-lg-12">
          @if($content_id) 
            <div class="card card-stats">
              <div class="card-content views-body  pull-right">
                <a href="#" class="btn btn-tab-movie"><i class="material-icons">remove_red_eye</i> {{ $data->total_view }} Views</a>
                <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">cloud_download</i> {{ $data->total_download }} Downloads</a>
                <a href="#" class="btn btn-tab-movie pull-right"><i class="material-icons">share</i> {{ $data->total_share }} Shares</a>
              </div>
            <div class="card-body">
              <h4 class="text-center mb-0"> Edit {{$content_title}} Season</h4>
            </div>
          </div>
          @endif
          <div class="card">
            <div class="card-body">
            @include('admin.section.content_header')
            </div>
          </div>
        </div> 
      </div>
    </div>
    <div class="row">
        <div class="col-12">
            <div class="card">
              <div class="card-header">
                  <h4>Series Season List (<span class="total_season">{{$total_season}}</span>)</h4>
              </div>

              <div class="card-body">

                <div class="pull-right">
                  <div class="buttons"> 
                  <button class="btn btn-primary text-light" data-toggle="modal" data-target="#seasonModal" data-whatever="@mdo" >Add Series Season</button>
                  </div>
                </div>

                <div class="form-group col-md-6 p-0">
                  <select id="s_season_id" class="form-control form-control-danger">
                    @foreach($seasondata as $value)
                      <option value="{{$value['season_id']}}" data-season_title="{{$value['season_title']}}" data-trailer_url="{{$value['trailer_url']}}">{{$value['season_title']}}</option>
                    @endforeach
                  </select>
                </div>

              </div>
              <div class="card-body">
              <input type="hidden" class="is_session_data" value="@if(count($seasondata) <= 0){{1}}@endif">

                <div class="col-md-12">
                  <div class="card1" style="@if(count($seasondata) <= 0){{'display:none;'}}@endif">
                    <h6 class="btn-tab-season"><i class="fas fa-box"></i> <span class="season_title">@if(count($seasondata) > 0){{$seasondata[0]['season_title']}}@endif</span></h6>
                    <a data-toggle="modal" data-target="#seasonModal" data-whatever="@mdo"  class="btn  btn-xs btn-tab-season-action pull-right updateSeason" data-id="@if(count($seasondata) > 0){{$seasondata[0]['season_id']}}@endif" data-season_title="@if(count($seasondata) > 0){{$seasondata[0]['season_title']}}@endif" data-trailer_url="@if(count($seasondata) > 0){{$seasondata[0]['trailer_url']}}@endif"><i class="fas fa-edit"></i></a>
                    <a class="btn  btn-xs btn-tab-season-action pull-right DeleteSeriesSeason" data-id="@if(count($seasondata) > 0){{$seasondata[0]['season_id']}}@endif"><i class="fas fa-trash"></i></a>
                    <?php if(count($seasondata) > 0){
                        $season_id = $seasondata[0]['season_id'];
                    }else{
                      $season_id = 0;
                    }?>
                    <a href="{{route('series/season/episode/add',['season_id'=>$season_id])}}"  class="btn  btn-xs btn-tab-season-action pull-right addEpisode" style="@if(count($seasondata) <= 0){{'display:none;'}}@endif"><i class="fas fa-plus"></i> New Episode</a>
                    <br>
                    <div class="episodes-contrainer ">
                      <div class="table-responsive">
                        <table class="table table-striped" id="episode-listing" width="100%">
                          <thead>
                              <tr>
                                <th>Episode Thumb</th>
                                <th>Episode Title</th>
                                <th>Description</th>
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
        </div>
    </div>
  </div>
</section>


<div class="modal fade" id="seasonModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="ModalLabel"> Add Series Season </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <form id="addUpdateSeriesSeason" method="post" enctype="multipart">
        {{ csrf_field() }}
            <div class="modal-body">
                <input id="content_id" name="content_id" type="hidden" class="form-control form-control-danger" value="{{$content_id}}">
                <input id="content_type" name="content_type" type="hidden" class="form-control form-control-danger" value="{{$content_type}}">

                <div class="form-group">
                    <label for="season_title">Season Title</label>
                    <input type="text" class="form-control valid" id="season_title" name="season_title" aria-required="true" aria-invalid="false" placeholder="Season Title">
                </div>
                
              <div class="form-group">
                <label for="trailer_url">Youtube Trailer Id</label>
                <input type="text"  name="trailer_url" class="form-control" id="trailer_url" placeholder="Youtube Trailer Id" >
              </div>

            </div>
            <div class="modal-footer">
                <input type="hidden" name="season_id" id="season_id" value="">
                <button type="submit" class="btn btn-success" >Save</button>
                <button type="button" class="btn btn-light" data-dismiss="modal">Close</button>
            </div>
        </form>
      </div>
    </div>
  </div>
@endsection

@section('pageSpecificJs')

<script src="{{asset('assets/bundles/datatables/datatables.min.js')}}"></script>
<script src="{{asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/js/page/datatables.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>

<script>
$(document).ready(function (){
  var dataTable = $('#episode-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [1], /* column index */
          'orderable': false, /* true or false */
        }], 
    'ajax': {
        'url':'{{ route("showSeasonEpisodeList") }}',
        'data': function(data){
            data.season_id = $('#s_season_id').val();
            data.content_type = $("#content_type").val();
            data.content_id = $("#content_id").val();
        }
    }
  });

    $(document).on("change", "#s_season_id", function() {
      var season_id = $(this).val();
      var season_title = $(this).find(':selected').attr('data-season_title');
      var trailer_url = $(this).find(':selected').attr('data-trailer_url');
      $(".season_title").text(season_title);
      $(".trailer_url").text(trailer_url);

      $(".updateSeason").attr('data-id',season_id);
      $(".updateSeason").attr('data-season_title',season_title);
      $(".updateSeason").attr('data-trailer_url',trailer_url);
      $(".DeleteSeriesSeason").attr('data-id',season_id);
      $(".addEpisode").attr('href','{{ url("series/season/episode/add") }}'+"/"+season_id);
      $('#episode-listing').DataTable().ajax.reload(null, false);
    });

    $('#seasonModal').on('hidden.bs.modal', function(e) {
        $("#addUpdateSeriesSeason")[0].reset();
        $('.modal-title').text('Add Season');
        $('#season_id').val("");
        $('#season_title').val("");
        $('#trailer_url').val("");
        var validator = $("#addUpdateSeriesSeason").validate();
        validator.resetForm();
    });

    $(document).on("click", ".updateSeason", function() {
        $('.modal-title').text('Edit Season');
        $('#season_id').val($(this).attr('data-id'));
        $('#season_title').val($(this).attr('data-season_title'));
        $('#trailer_url').val($(this).attr('data-trailer_url'));
    });
    
    
    $("#addUpdateSeriesSeason").validate({
      rules: {
        season_title: {
          required: true,
          remote: {
                url: '{{ route("CheckExistSeason") }}',
                type: "post",
                data: {
                    season_title: function () { return $("#season_title").val(); },
                    content_id: function () { return $("#content_id").val(); },
                    season_id: function () { return $("#season_id").val(); },
                }
            }
        },
        trailer_url: {
          required: true,
        }
      },
      messages: {
        season_title: {
          required: "Please Enter Title",
          remote : "Season already exist"
        },
        trailer_url: {
         required: "Please Enter Trailer ID",
        }
      },

    });

  $(document).on('submit', '#addUpdateSeriesSeason', function (e) {
      e.preventDefault();
      if (user_type == 1) {
        var formdata = new FormData($("#addUpdateSeriesSeason")[0]);
        var is_session_data = $('.is_session_data').val();
        var season_id = $('#season_id').val();
        $('.loader').show();
        $.ajax({
            url: '{{ route("addUpdateSeriesSeason") }}',
            type: 'POST',
            data: formdata,
            dataType: "json",
            contentType: false,
            cache: false,
            processData: false,
            success: function (data) {
                $('.loader').hide();
                $('#seasonModal').modal('hide');
                if (data.success == 1) {
                  $('.total_season').text(data.total_season);
                  var season_id = $('#s_season_id').val();
                  html = "";
                  $( data.data ).each(function( index, value) {
                    if(season_id == value.season_id){
                      var selected = 'selected';
                    }else{
                      var selected = '';
                    }
                    html += '<option value="'+value.season_id+'"  data-season_title="'+value.season_title+'" data-trailer_url="'+value.trailer_url+'" '+selected+'>'+value.season_title+'</option>';
                  });
                  $('#s_season_id').html(html);
                  if(is_session_data == 1 || season_id){
                    $('.card1').removeAttr("style");
                    $(".season_id").attr('data-id',data.new_data.season_id);
                    $('.season_title').text(data.new_data.season_title);
                    $('.trailer_url').text(data.new_data.trailer_url);
                    $(".updateSeason").attr('data-id',data.new_data.season_id);
                    $(".updateSeason").attr('data-season_title',data.new_data.season_title);
                     $(".updateSeason").attr('data-trailer_url',data.new_data.trailer_url);
                    $(".DeleteSeriesSeason").attr('data-id',data.new_data.season_id);
                    $(".addEpisode").removeAttr("style");
                    $(".addEpisode").attr('href','{{ url("series/season/episode/add") }}'+"/"+data.new_data.season_id);
                    $('#episode-listing').DataTable().ajax.reload(null, false);
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

    $(document).on('click', '.DeleteSeriesSeason', function (e) {
        e.preventDefault();
        var content_type = $("#content_type").val();
        var content_id = $("#content_id").val();
        var season_id = $(this).attr('data-id');
        var text = 'You will not be able to recover Content Season data!';   
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
        function(isConfirm){
            if (isConfirm){
              if (user_type == 1) {
                $('.loader').show();
                $.ajax({
                    url: '{{ route("deleteSeriesSeason") }}',
                    type: 'POST',
                    data: {"season_id":season_id,"content_id":content_id,"content_type":content_type},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        if (data.success == 1) {
                        $('.total_season').text(data.total_season);
                        html = "";

                        if((data.data).length > 0){
                          var season_id = data.data[0].season_id;
                          $( data.data ).each(function( index, value) {
                            if(season_id == value.season_id){
                              var selected = 'selected';
                            }else{
                              var selected = '';
                            }
                            html += '<option value="'+value.season_id+'"  data-season_title="'+value.season_title+'" data-trailer_url="'+value.trailer_url+'" '+selected+'>'+value.season_title+'</option>';
                          });
                        }
                        $('#s_season_id').html(html);

                        if((data.data).length > 0){
                          $('.card1').removeAttr("style");
                          $(".season_id").attr('data-id',data.data[0].season_id);
                          $('.season_title').text(data.data[0].season_title);
                          $('.trailer_url').text(data.data[0].trailer_url);
                          $(".updateSeason").attr('data-id',data.data[0].season_id);
                          $(".updateSeason").attr('data-season_title',data.data[0].season_title);
                            $(".updateSeason").attr('data-trailer_url',data.data[0].trailer_url);
                          $(".DeleteSeriesSeason").attr('data-id',data.data[0].season_id);
                          $(".addEpisode").removeAttr("style");
                          $(".addEpisode").attr('href','{{ url("series/season/episode/add") }}'+"/"+data.data[0].season_id);
                          $('#episode-listing').DataTable().ajax.reload(null, false);
                        }else{
                          $('.card1').attr("style","display:none;");
                        }
                        swal("Confirm!", "Series Season has been deleted!", "success");
                        } else {
                        swal("Confirm!", "Series Season has not been deleted!", "error");
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
                swal("Confirm!", "Series Season has been deleted!", "success");
              }
            } else {
            swal("Cancelled", "Your imaginary file is safe :)", "error");
            }
        });
    });
    
    $(document).on('click', '.DeleteSeasonEpisode', function (e) {
        e.preventDefault();
        var season_id = $("#season_id").val();
        var episode_id = $(this).attr('data-id');
        var text = 'You will not be able to recover Content Season data!';   
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
        function(isConfirm){
            if (isConfirm){
              if (user_type == 1) {
                $('.loader').show();
                $.ajax({
                    url: '{{ route("deleteSeasonEpisode") }}',
                    type: 'POST',
                    data: {"season_id":season_id,"episode_id":episode_id},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        if (data.success == 1) {
                          $('#episode-listing').DataTable().ajax.reload(null, false);
                            swal("Confirm!", "Episode has been deleted!", "success");
                        } else {
                            swal("Confirm!", "Episode has not been deleted!", "error");
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
                swal("Confirm!", "Episode has been deleted!", "success");
              }
            } else {
            swal("Cancelled", "Your imaginary file is safe :)", "error");
            }
        });
    });
});
</script>

@endsection
