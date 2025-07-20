@if($channel_id)
<a class="btn {{ (request()->is('tv/channel/edit*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg" href="{{route('channel/edit',['id' => $channel_id ])}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>{{$title}}</a>
@else
<a class="btn {{ (request()->is('tv/channel/add*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg" href="{{route('channel/add')}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>{{$title}}</a>
@endif

@if($channel_id)
<a class="btn {{ ( request()->is('tv/channel/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg is_source" href="{{route('tv/channel/source/list',['channel_id' => $channel_id])}}"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@else
<a class="btn {{ ( request()->is('tv/channel/source*')) ? 'btn-primary text-light' : 'btn-secondary text-dark' }} btn-lg  is_source" href="#"  @if(Session::get('admin_id') == 2){{"disabled"}}@endif>Source</a>
@endif
