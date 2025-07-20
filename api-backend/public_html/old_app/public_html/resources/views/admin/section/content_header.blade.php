<?php  if($content_type == 1){
            $is_series = "display:none;";
            $is_movie = "display:inline-block;";
        }else{
            $is_series = "display:inline-block;";
            $is_movie = "display:none;";
        }
?>
@if($content_id)
<a class="btn {{ (request()->is('content/edit*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg" href="{{route('content/edit',['flag' => $content_type ,'id' => $content_id ])}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>{{$title}} Content</a>
@else
<a class="btn {{ (request()->is('content/add')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg" href="{{route('content/add')}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>{{$title}} Content</a>
@endif

@if($content_id)
<a class="btn {{ (request()->is('movie/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_movie_source is_movie" href="{{route('movie/source/list',['flag' => 1 ,'id' => $content_id ])}}" style="{{$is_movie}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@else
<a class="btn {{ (request()->is('movie/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_movie_source is_movie" href="#" style="{{$is_movie}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@endif
<!-- 
@if($content_id)
<a class="btn {{ ( request()->is('series/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_series_source is_series" href="{{route('series/source/list',['flag' => 2, 'id' => $content_id  ])}}" style="{{$is_series}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@else
<a class="btn {{ ( request()->is('series/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_series_source is_series" href="#" style="{{$is_series}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@endif -->


@if($content_id)
<a class="btn {{ (request()->is('movie/cast*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_cast is_movie" href="{{route('movie/cast/list',['flag' => 1 ,'id' => $content_id ])}}" style="{{$is_movie}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Cast</a>
@else
<a class="btn {{ (request()->is('movie/cast*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_cast is_movie" href="#"  style="{{$is_movie}}" @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Cast</a>
@endif


@if($content_id)
<a class="btn {{ (request()->is('movie/subtitle*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_movie_subtitles is_movie" href="{{route('movie/subtitle/list',['flag' => 1, 'id' => $content_id  ])}}" style="{{$is_movie}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Subtitle</a>
@else
<a class="btn {{ (request()->is('movie/subtitle*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg  is_movie_subtitles is_movie" href="#" style="{{$is_movie}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Subtitle</a>
@endif


<!-- @if($content_id)
<a class="btn {{ (request()->is('series/subtitle*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_series_subtitles is_series" href="{{route('series/subtitle/list',['flag' => 2, 'id' => $content_id  ])}}"  style="{{$is_series}}" @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Subtitle</a>
@else
<a class="btn {{ (request()->is('series/subtitle*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_series_subtitles is_series" href="#"  style="{{$is_series}}" @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Subtitle</a>
@endif -->


@if($content_id)
<a class="btn {{ (request()->is('series/season*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_season is_series" href="{{route('series/season/list',['flag' => 2 ,'id' => $content_id ])}}" style="{{$is_series}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Season</a>
@else
<a class="btn {{ (request()->is('series/season*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg  is_season is_series" href="#" style="{{$is_series}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Season</a>
@endif

<!-- @if($content_id)
<a class="btn {{ (request()->is('movie/comment*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_comment is_movie" href="{{route('movie/comment/list',['flag' => 1 ,'id' => $content_id ])}}" style="{{$is_movie}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Comment</a>

<a class="btn {{ (request()->is('series/comment*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_comment is_series" href="{{route('series/comment/list',['flag' => 2 ,'id' => $content_id ])}}" style="{{$is_series}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Comment</a>
@else
<a class="btn {{ (request()->is('movie/comment*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_comment is_movie" href="#" style="{{$is_movie}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Comment</a>

<a class="btn {{ (request()->is('series/comment*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_comment is_series" href="#" style="{{$is_series}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Comment</a>
@endif -->