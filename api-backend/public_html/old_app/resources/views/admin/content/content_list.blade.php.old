@extends('admin_layouts/main')
@section('pageSpecificCss')
<link href="{{asset('assets/bundles/datatables/datatables.min.css')}}" rel="stylesheet">
<link href="{{asset('assets/bundles/datatables/DataTables-1.10.16/css/dataTables.bootstrap4.min.css')}}" rel="stylesheet">

<link rel="stylesheet" href="{{asset('assets/bundles/izitoast/css/iziToast.min.css')}}">

<style type="text/css">
.view {
    padding: 0.3rem 9px;
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

    <div class="row ">
      
        <div class="col-xl-3 col-lg-3 col-md-3 col-sm-3 col-xs-12">
            <div class="card">
                <div class="card-statistic-4">
                    <div class="align-items-center justify-content-between">
                        <div class="row ">
                            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 pr-0 ">
                                <div class="card-content text-center">
                                    <h3 class="font-20 total_content" style="color: dodgerblue;"> {{$total_content}} </h3>
                                    <span class="badge badge-success text-center">All</span>
                                </div>
                            </div>                            
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-lg-3 col-md-3 col-sm-3 col-xs-12">
            <div class="card">
                <div class="card-statistic-4">
                    <div class="align-items-center justify-content-between">
                        <div class="row ">
                            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 pr-0 ">
                                <div class="card-content text-center">
                                    <h3 class="font-20 total_movie" style="color: dodgerblue;"> {{$total_movie}} </h3>
                                    <span class="badge badge-danger text-center">Movie</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-lg-3 col-md-3 col-sm-3 col-xs-12">
            <div class="card">
                <div class="card-statistic-4">
                    <div class="align-items-center justify-content-between">
                        <div class="row ">
                            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 pr-0 ">
                                <div class="card-content text-center">
                                    <h3 class="font-20 total_series" style="color: dodgerblue;"> {{$total_series}} </h3>
                                    <span class="badge badge-primary text-center">Series</span>
                                </div>
                            </div>                            
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
    </div>
  <div class="section-body">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header">
              <h4>Content List </h4>
            </div>

            <div class="card-body">
                <div class="pull-right">
                    <div class="buttons"> 
                    <a class="btn btn-primary text-light" href="{{route('content/add')}}" >Add Content</a>
                    </div>
                </div>
              <div class="tab" role="tabpanel">
                <ul class="nav nav-pills border-b mb-0 p-3">
                    <li role="presentation" class="nav-item"><a class="nav-link pointer active" href="#Section0" aria-controls="home" role="tab" data-toggle="tab">Movie </a></li>
                    <li role="presentation" class="nav-item"><a class="nav-link pointer" href="#Section1" aria-controls="home" role="tab" data-toggle="tab">Series </a></li>
                </ul>
                <div class="tab-content tabs" id="home">
                
                  <div role="tabpanel" class="tab-pane active" id="Section0">
              
                    <div class="card-body">	
                    
                      <div class="table-responsive">
                          <table class="table table-striped" id="movie-listing" width="100%">
                            <thead>
                              <tr>
                                <th>Verticle Poster</th>
                                <th>Horizontal Poster</th>
                                <th>Title</th>
                                <th>Ratings</th>
                                <th>Release Year</th>
                                <th>Language</th>
                                <th>Is Featured</th>
                                <th>Action</th>
                              </tr>
                            </thead>
                            <tbody>

                            </tbody>
                          </table>
                      </div>
                    </div>
                  </div>

                  <div role="tabpanel" class="tab-pane" id="Section1">
              
                    <div class="card-body">	
                      
                      <div class="table-responsive">
                          <table class="table table-striped" id="series-listing" width="100%">
                            <thead>
                              <tr>
                              <th>Verticle Poster</th>
                                <th>Horizontal Poster</th>
                                <th>Title</th>
                                <th>Ratings</th>
                                <th>Release Year</th>
                                <th>Language</th>
                                <th>Is Featured</th>
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
</section>
@endsection

@section('pageSpecificJs')

<script src="{{asset('assets/bundles/datatables/datatables.min.js')}}"></script>
<script src="{{asset('assets/bundles/datatables/DataTables-1.10.16/js/dataTables.bootstrap4.min.js')}}"></script>
<script src="{{asset('assets/bundles/jquery-ui/jquery-ui.min.js')}}"></script>
<script src="{{asset('assets/js/page/datatables.js')}}"></script>
<script src="{{asset('assets/bundles/izitoast/js/iziToast.min.js')}}"></script>

<script>
$(document).ready(function (){
  var dataTable = $('#movie-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [7], /* column index */
          'orderable': false, /* true or false */
        }], 
    'ajax': {
        'url':'{{ route("showContentList") }}',
        'data': function(data){
          data.content_type = 1;
        }
    }
  });

  var dataTable4 = $('#series-listing').dataTable({
    'processing': true,
    'serverSide': true,
    'serverMethod': 'post',
    "order": [[ 0, "desc" ]],
    'columnDefs': [ {
          'targets': [7], /* column index */
          'orderable': false, /* true or false */
        }],
   
    'ajax': {
        'url':'{{ route("showContentList") }}',
        'data': function(data){
          data.content_type = 2;
        }
    }
  });
  
  $('a[data-toggle="tab"]').on('shown.bs.tab', function(e){
      $($.fn.dataTable.tables(true)).DataTable()
         .columns.adjust();
   }); 

  $(document).on('click', '#changeFeatureStatus', function (e) {
      e.preventDefault();
      var content_id = $(this).attr('data-id');
      var status = $(this).attr('data-status');
      if(status == 1){
        status = 0;
      }else{
        status = 1;
      }
      var text = 'You will not be able to recover this data!';   
      var confirmButtonText = 'Yes, Change Status!';
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
                  url: '{{ route("changeFeatureStatus") }}',
                  type: 'POST',
                  data: {"content_id":content_id,"status":status},
                  dataType: "json",
                  cache: false,
                  success: function (data) {
                      $('.loader').hide();
                      $('#movie-listing').DataTable().ajax.reload(null, false);
                      $('#series-listing').DataTable().ajax.reload(null, false);
                      $('.total_content').text(data.total_content);
                      $('.total_movie').text(data.total_movie);
                      $('.total_series').text(data.total_series);
                      swal("Confirm!", "Your Content has been changed!", "success");
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
                swal("Confirm!", "Your Content has been changed!", "success");
            }
          } else {
          swal("Cancelled", "Your imaginary file is safe :)", "error");
        }
      });
    });

    $(document).on('click', '.DeleteContent', function (e) {
        e.preventDefault();
        var content_id = $(this).attr('data-id');
        var text = 'You will not be able to recover Content data!';   
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
                    url: '{{ route("deleteContent") }}',
                    type: 'POST',
                    data: {"content_id":content_id},
                    dataType: "json",
                    cache: false,
                    success: function (data) {
                        $('.loader').hide();
                        $('#movie-listing').DataTable().ajax.reload(null, false);
                        $('#series-listing').DataTable().ajax.reload(null, false);
                        $('.total_content').text(data.total_content);
                        $('.total_movie').text(data.total_movie);
                        $('.total_series').text(data.total_series);
                        if (data.success == 1) {
                        swal("Confirm!", "Content has been deleted!", "success");
                        } else{
                          swal("Confirm!", "Content has not been deleted!", "error");
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
                swal("Confirm!", "Content has been deleted!", "success");
              }
            } else {
            swal("Cancelled", "Your imaginary file is safe :)", "error");
            }
        });
    });
});
</script>

@endsection
