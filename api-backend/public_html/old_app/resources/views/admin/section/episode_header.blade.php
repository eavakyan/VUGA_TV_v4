@if($episode_id)
<a class="btn {{ (request()->is('series/season/episode/edit*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg" href="{{route('series/season/episode/edit',['season_id' => $season_id,'id' => $episode_id ])}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>{{$title}} Content</a>
@else
<a class="btn {{ (request()->is('series/season/episode/add*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg" href="{{route('series/season/episode/add',['season_id' => $season_id])}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>{{$title}} Content</a>
@endif

@if($episode_id)
<a class="btn {{ ( request()->is('series/season/episode/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_source" href="{{route('series/season/episode/source/list',['season_id' => $season_id ,'id' => $episode_id ])}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@else
<a class="btn {{ ( request()->is('series/season/episode/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg  is_source" href="#"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@endif


@if($episode_id)
<a class="btn {{ (request()->is('series/season/episode/subtitle*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_subtitles" href="{{route('series/season/episode/subtitle/list',['season_id' => $season_id ,'id' => $episode_id ])}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Subtitle</a>
@else
<a class="btn {{ (request()->is('series/season/episode/subtitle*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_subtitles" href="#"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Subtitle</a>
@endif

