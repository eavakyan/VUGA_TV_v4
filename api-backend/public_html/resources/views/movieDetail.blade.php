@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/content_detail.js') }}"></script>
@endsection

@section('content')
<input type="hidden" id="content_id1" value="{{ $content->id }}">
<div class="page-content">
  <section class="detail_list">
    <div class="card" id="detail_content">
      <div class="card-header border-0 d-block d-md-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center">
          <img data-fancybox src='{{ $content->horizontal_poster ? $content->horizontal_poster : asset("assets/img/default.png") }}' alt="" class="rounded border-muted object-fit-cover" style="background: rgb(118 118 128 / 12%);" width="100px" height="60px" class="">
          <div class="ms-3">
            <h4 class="fw-normal mb-0"> {{ $content->title }} </h4>
          </div>
        </div>
        <div class="d-flex align-items-center justify-content-end">
          <a class='me-2 btn btn-success px-4 py-2 text-white editContentModal' rel='{{$content->id}}' data-type='{{$content->type}}' data-title='{{$content->title}}' data-description='{{$content->description}}' data-duration='{{$content->duration}}' data-release_year='{{$content->release_year}}' data-ratings='{{$content->ratings}}' data-language_id='{{$content->language_id}}' data-genre_ids='{{$content->genre_ids}}' data-vposter='{{ $content->vertical_poster }}' data-hposter='{{ $content->horizontal_poster }}'>{{ __('edit') }}</a>
          <a class='btn btn-danger px-4 py-2 text-white deleteContent' rel='{{$content->id}}'>{{ __('delete') }}</a>
        </div>
      </div>
    </div>
    <nav class="card-tab mb-4 d-block d-lg-flex align-items-center justify-content-between">
      <div class="nav nav-tabs" id="nav-tab" role="tablist">
        <button class="nav-link active" id="nav-source-tab" data-bs-toggle="tab" data-bs-target="#nav-source" type="button" role="tab" aria-controls="nav-source" aria-selected="false">
          {{ __('source') }}
        </button>
        <button class="nav-link" id="nav-cast-tab" data-bs-toggle="tab" data-bs-target="#nav-cast" type="button" role="tab" aria-controls="nav-cast" aria-selected="false">
          {{ __('cast') }}
        </button>
        <button class="nav-link" id="nav-subtitle-tab" data-bs-toggle="tab" data-bs-target="#nav-subtitle" type="button" role="tab" aria-controls="nav-subtitle" aria-selected="false">
          {{ __('subtitle') }}
        </button>
      </div>
      <div class="d-block d-md-flex align-items-center justify-content-between">
        <p class="btn badge badge-secondary text-light px-4 m-0 ms-2 mt-2 mt-md-0">
          <i data-feather="eye"></i>
          <span class="ms-2"> {{ $content->total_view }} Views </span>
        </p>
        <p class="btn badge badge-secondary text-light px-4 mb-0 ms-2 mt-2 mt-md-0">
          <i data-feather="share-2"></i>
          <span class="ms-2"> {{ $content->total_share }} Shares </span>
        </p>
        <p class="btn badge badge-secondary text-light px-4 mb-0 ms-2 mt-2 mt-md-0">
          <i data-feather="download"></i>
          <span class="ms-2"> {{ $content->total_download }} Downloads </span>
        </p>
      </div>
    </nav>
    <div class="tab-content" id="nav-tabContent">
      <div class="tab-pane show active" id="nav-source" role="tabpanel" aria-labelledby="nav-source-tab" tabindex="0">
        <div class="card">
          <div class="card-header d-block d-md-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center">
              <h4 class="fw-normal m-0"> {{ __('sourceList') }} </h4>
              @if($content->trailers && $content->trailers->count() > 0)
                @php $primaryTrailer = $content->trailers->where('is_primary', 1)->first() ?? $content->trailers->first(); @endphp
                <a href="{{ $primaryTrailer->trailer_url }}" target="_blank" rel="noopener noreferrer" class="bg-white theme-color px-3 fw-semibold rounded"> {{ $primaryTrailer->title ?? __('trailer') }} </a>
                @if($content->trailers->count() > 1)
                  <div class="dropdown d-inline-block ms-2">
                    <button class="btn btn-sm btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                      {{ __('moreTrailers') }} ({{ $content->trailers->count() - 1 }})
                    </button>
                    <ul class="dropdown-menu">
                      @foreach($content->trailers->where('content_trailer_id', '!=', $primaryTrailer->content_trailer_id) as $trailer)
                        <li><a class="dropdown-item" href="{{ $trailer->trailer_url }}" target="_blank">{{ $trailer->title }}</a></li>
                      @endforeach
                    </ul>
                  </div>
                @endif
              @else
                <span class="text-muted">{{ __('noTrailer') }}</span>
              @endif
            </div>
            <button type="button" class="btn btn-primary text-light px-4 mt-2 mt-md-2" data-bs-toggle="modal" data-bs-target="#addSourceModal">
              {{ __('addMovieSource') }}
            </button>
          </div>
          <div class="card-body">
            <table class="table table-striped w-100" id="sourceTable">
              <thead>
                <tr>
                  <th> {{ __('title') }}</th>
                  <th> {{ __('type') }}</th>
                  <th> {{ __('source') }}</th>
                  <th> {{ __('quality') }}</th>
                  <th class="text-end" width="200px"> {{ __('action') }} </th>
                </tr>
              </thead>
            </table>
          </div>
        </div>
      </div>
      <div class="tab-pane" id="nav-cast" role="tabpanel" aria-labelledby="nav-cast-tab" tabindex="0">
        <div class="card">
          <div class="card-header d-flex align-items-center justify-content-between">
            <h4 class="fw-normal m-0"> {{ __('castList') }} </h4>
            <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addCastModal">
              {{ __('addCast') }}
            </button>
          </div>
          <div class="card-body">
            <table class="table table-striped w-100" id="castTable">
              <thead>
                <tr>
                  <th> {{ __('actor') }}</th>
                  <th> {{ __('characterName') }}</th>
                  <th class="text-end" width="200px"> {{ __('action') }} </th>
                </tr>
              </thead>
            </table>
          </div>
        </div>
      </div>
      <div class="tab-pane" id="nav-subtitle" role="tabpanel" aria-labelledby="nav-subtitle-tab" tabindex="0">
        <div class="card">
          <div class="card-header d-flex align-items-center justify-content-between">
            <h4 class="fw-normal m-0"> {{ __('subtitleList') }} </h4>
            <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addSubtitleModal">
              {{ __('addSubtitle') }}
            </button>
          </div>
          <div class="card-body">
            <table class="table table-striped w-100" id="subtitleTable">
              <thead>
                <tr>
                  <th> {{ __('language') }}</th>
                  <th class="text-end" width="200px"> {{ __('action') }} </th>
                </tr>
              </thead>
            </table>
          </div>
        </div>
      </div>
    </div>
  </section>
</div>

<!-- Source Modal -->
<div class="modal fade" id="addSourceModal" tabindex="-1" aria-labelledby="addSourceModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addMovieSource')}}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addSourceForm" method="POST">
        <div class="modal-body">
          <input type="hidden" name="content_id" id="content_id" value="{{ $content->id }}">
          <div class="form-group">
            <label for="title" class="form-label">{{ __('sourceTitle')}}</label>
            <input name="title" id="title" type="text" class="form-control" required>
          </div>
          <div class="form-group">
            <label for="quality" class="form-label">{{ __('sourceQuality')}}</label>
            <input name="quality" id="quality" type="text" class="form-control" required>
          </div>
          <div class="form-group">
            <label for="size" class="form-label">{{ __('sourceSize')}}</label>
            <input name="size" id="size" type="text" class="form-control" required>
          </div>
          <div class="form-group w-100">
            <label for="type" class="form-label">{{ __('selectSourceType')}}</label>
            <select name="type" id="type" class="form-control selectric" aria-invalid="false" required>
              <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
              <option class="youtube_id" value="1">Youtube ID</option>
              <option class="m3u8_url" value="2">M3u8 Url</option>
              <option value="3">Mov Url</option>
              <option value="4">Mp4 Url</option>
              <option value="5">Mkv Url</option>
              <option value="6">Webm Url</option>
              <option value="8">embedded Url</option>
              <option class="otherSelect" value="7">{{ __('fileUpload')}} (Mp4, Mov, Mkv, Webm)</option>
            </select>
          </div>
          <div class="form-group">
            <div class="mediaGallery">
              <label for="media" class="form-label">{{ __('selectMedia')}}  </label>
              <select name="media" id="media" class="form-control selectric">
                <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                @foreach($mediaGalleries as $mediaGallery)
                <option value="{{ $mediaGallery->id }}"> {{ $mediaGallery->title }} </option>
                @endforeach
              </select>
            </div>
            <div class="sourceURL">
              <label for="sourceURL" class="form-label" id="sourceURL_label">{{ __('sourceURL')}}</label>
              <input name="source_url" id="sourceURL" type="text" class="form-control" aria-describedby="sourceURL">
            </div>
          </div>
          <div class="progress mb-2" id="progress" style="height: 25px; display: none;">
            <div class="progress-bar" id="progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">0%</div>
          </div>
          <div class="d-flex align-items-center">
            <div class="form-group mb-0 w-100">
              <label for="access_type" class="form-label">{{ __('selectAccessType')}}</label>
              <select name="access_type" id="access_type" class="form-control selectric" required>
                <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                <option value="1">{{ __('free')}}</option>
                <option value="2">{{ __('premium')}}</option>
                <option value="3">{{ __('unlockWithVideoAds')}}</option>
              </select>
            </div>
            <div class="form-group ms-2 w-50" id="downloadableOrNot">
              <div class="checkbox-slider">
                <label class="form-label w-100" for="is_download">{{ __('downloadableOrNot') }}</label>
                <div class="d-flex align-items-center h-50">
                  <label>
                    <input type="checkbox" id="is_download" class="opacity-0 position-absolute">
                    <input type="hidden" id="is_download_hidden" name="is_download" value="0" required>
                    <span class="toggle_background">
                      <div class="circle-icon"></div>
                      <div class="vertical_line"></div>
                    </span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('close') }}</button>
          <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('save') }}</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Edit Source Modal -->
<div class="modal fade" id="editSourceModal" tabindex="-1" aria-labelledby="editSourceModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editMovieSource')}}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editSourceForm" method="POST">
        <div class="modal-body">
          <input type="hidden" id="edit_source_id">
          <div class="form-group">
            <label for="title" class="form-label">{{ __('sourceTitle')}}</label>
            <input name="title" id="edit_title" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label for="quality" class="form-label">{{ __('sourceQuality')}}</label>
            <input name="quality" id="edit_quality" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label for="size" class="form-label">{{ __('sourceSize')}}</label>
            <input name="size" id="edit_size" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label for="type" class="form-label">{{ __('selectSourceType')}}</label>
            <select name="type" id="edit_type" class="form-control selectric" aria-invalid="false" required>
              <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
              <option class="youtube_id" value="1">Youtube ID</option>
              <option class="m3u8_url" value="2">M3u8 Url</option>
              <option value="3">Mov Url</option>
              <option value="4">Mp4 Url</option>
              <option value="5">Mkv Url</option>
              <option value="6">Webm Url</option>
              <option value="8">embedded Url</option>
              <option class="otherSelect" value="7">{{ __('fileUpload')}} (Mp4, Mov, Mkv, Webm)</option>
            </select>
          </div>
          <div class="form-group">
            <!-- <div class="sourceFile">
              <label for="source_file" class="form-label">{{ __('sourceFile')}}</label>
              <div class="file-input form-control w-100">
                <input name="source_file" id="edit_source_file" type="file" class="form-control" aria-describedby="sourceFile" accept="video/mp4,video/x-m4v,video/*">
                <span class='button'>{{ __('choose')}}</span>
                <span class='label' data-js-label>{{ __('changeFile')}}</label>
              </div>
            </div> -->
            <div class="mediaGallery">
              <label for="media" class="form-label">{{ __('selectMedia')}}</label>
              <select name="media" id="edit_media" class="form-control selectric">
                <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                @foreach($mediaGalleries as $mediaGallery)
                <option value="{{ $mediaGallery->id }}"> {{ $mediaGallery->title }} </option>
                @endforeach
              </select>
            </div>
            <div class="sourceURL">
              <label for="sourceURL" class="form-label" id="edit_sourceURL_label">{{ __('sourceURL')}}</label>
              <input name="source_url" id="edit_sourceURL" type="text" class="form-control" aria-describedby="sourceURL">
            </div>
          </div>
          <div class="progress mb-2" id="edit-progress" style=" height: 25px; display: none;">
            <div class="progress-bar" id="edit-progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">0%</div>
          </div>
          <div class="d-flex align-items-center">
            <div class="form-group mb-0 w-100">
              <label for="access_type" class="form-label">{{ __('selectAccessType')}}</label>
              <select name="access_type" id="edit_access_type" class="form-control selectric">
                <option value="1">{{ __('free')}}</option>
                <option value="2">{{ __('premium')}}</option>
                <option value="3">{{ __('unlockWithVideoAds')}}</option>
              </select>
            </div>
            <div class="form-group ms-2 w-50" id="edit_downloadableOrNot">
              <div class="checkbox-slider">
                <label class="form-label w-100" for="is_download">{{ __('downloadableOrNot') }}</label>
                <div class="d-flex align-items-center h-50">
                  <label>
                    <input type="checkbox" id="edit_is_download" class="opacity-0 position-absolute">
                    <input type="hidden" id="edit_is_download_hidden" name="is_download" value="0">
                    <span class="toggle_background">
                      <div class="circle-icon"></div>
                      <div class="vertical_line"></div>
                    </span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('close') }}</button>
          <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('save') }}</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Add Cast Modal -->
<div class="modal fade" id="addCastModal" tabindex="-1" aria-labelledby="addCastModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addCast') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addCastForm" method="POST">
        <input type="hidden" name="content_id" value="{{ $content->id }}">
        <div class="modal-body">
          <div class="row">
            <div class="form-group">
              <label for="actor_id" class="form-label">{{ __('selectActor') }}</label>
              <select name="actor_id" id="actor_id" class="form-control selectric" required>
                <option value="" disabled selected class="d-none">{{ __('selectActor') }}</option>
                @foreach ($actors as $actor)
                <option value="{{ $actor->id }}">{{ $actor->fullname }}</option>
                @endforeach
              </select>
            </div>
            <div class="form-group">
              <label for="character_name" class="form-label">{{ __('characterName') }}</label>
              <input name="character_name" type="text" class="form-control" id="character_name" aria-describedby="character_name" required>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('close') }}</button>
          <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('save') }}</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Edit Cast Modal -->
<div class="modal fade" id="editCastModal" tabindex="-1" aria-labelledby="editCastModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editCast') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editCastForm" method="POST">
        <input type="hidden" name="cast_id" id="edit_cast_id">
        <div class="modal-body">
          <div class="row">
            <div class="form-group">
              <label for="edit_actor_id" class="form-label">{{ __('selectActor') }}</label>
              <select name="actor_id" id="edit_actor_id" class="form-control selectric" required="">
                <option value="" disabled selected class="d-none">{{ __('selectActor') }}</option>
                @foreach ($actors as $actor)
                <option value="{{ $actor->id }}">{{ $actor->fullname }}</option>
                @endforeach
              </select>
            </div>
            <div class="form-group">
              <label for="edit_character_name" class="form-label">{{ __('characterName') }}</label>
              <input name="character_name" id="edit_character_name" type="text" class="form-control" aria-describedby="character_name" required="">
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('close') }}</button>
          <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('save') }}</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Add Subtitle Modal -->
<div class="modal fade" id="addSubtitleModal" tabindex="-1" aria-labelledby="addSubtitleModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addSubtitle') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addSubtitleForm" method="POST">
        <input type="hidden" name="content_id" value="{{ $content->id }}">
        <div class="modal-body">
          <div class="row">
            <div class="form-group">
              <label for="language_id" class="form-label">{{ __('selectLanguage') }}</label>
              <select name="language_id" id="language_id" class="form-control selectric" required>
                <option value="" disabled selected class="d-none">{{ __('selectLanguage') }}</option>
                @foreach ($languages as $language)
                <option value="{{ $language->id }}">{{ $language->title }}</option>
                @endforeach
              </select>
            </div>
            <div class="form-group">
              <label for="file" class="form-label">{{ __('subtitleFile')}}</label>
              <div class="file-input form-control w-100">
                <input name="file" id="file" type="file" class="form-control" aria-describedby="file" accept=".srt" required>
                <span class='button'>{{ __('choose')}}</span>
                <span class='label' data-js-label>{{ __('noFileSelected')}}</label>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('close') }}</button>
          <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('save') }}</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Edit Content Modal -->
<div class="modal fade" id="editContentModal" tabindex="-1" aria-labelledby="editContentModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editContent') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editContentForm" method="POST" class="editform">
        <input type="hidden" id="edit_content_id">
        <div class="modal-body">
          <div class="row">
            <div class="col-xl-6 col-lg-6 col-md-6 col-sm-12">
              <div class="form-group">
                <label for="content_type" class="form-label">{{ __('selectType') }}</label>
                <select name="type" id="edit_content_type" class="form-control selectric" required="" disabled>
                  <option value="" disabled selected class="d-none">{{ __('selectOption') }}</option>
                  <option value="1">{{ __('movie') }}</option>
                  <option value="2">{{ __('series') }}</option>
                </select>
              </div>
              <div class="form-group">
                <label for="title" class="form-label">{{ __('title') }}</label>
                <input name="title" type="text" class="form-control" id="edit_content_title" aria-describedby="title" required="">
              </div>
              <div class="form-group">
                <label for="description" class="form-label">{{ __('description') }}</label>
                <textarea name="description" id="edit_content_description" rows="4" class="form-control" required=""></textarea>
              </div>
              <div class="form-group">
                <label for="selectGenre" class="form-label">{{ __('selectGenre') }} ({{ __('multiple') }})</label>
                <select name="genre_ids[]" class="form-control selectric" id="edit_selectGenre" multiple="multiple" required="" tabindex="-1" aria-hidden="true">
                  <option value disabled class="d-none">{{ __('selectGenre') }}</option>
                  @foreach ($genres as $genre)
                  <option value="{{ $genre->id }}">{{ $genre->title }}</option>
                  @endforeach
                </select>
              </div>

              <div class="form-group">
                <label for="language_id" class="form-label">{{ __('selectLanguage') }}</label>
                <select name="language_id" id="edit_language_id" class="form-control selectric" required="">
                  <option value="" disabled selected class="d-none">{{ __('selectLanguage') }}</option>
                  @foreach ($languages as $language)
                  <option value="{{ $language->id }}">{{ $language->title }}</option>
                  @endforeach
                </select>
              </div>
            </div>
            <div class="col-xl-6 col-lg-6 col-md-6 col-sm-12">
              <div class="row">
                <div class="col-md-6">
                  <div class="form-group">
                    <label for="release_year" class="form-label">{{ __('releaseYear') }}</label>
                    <input name="release_year" type="number" class="form-control" id="edit_release_year" aria-describedby="release_year" required="">
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-group">
                    <label for="ratings" class="form-label">{{ __('ratings') }}</label>
                    <div class="rating_star">
                      <img src="{{ asset('assets/img/rating_star.svg') }}" alt="rating_star">
                      <input name="ratings" type="number" step=any class="form-control" id="edit_ratings" aria-describedby="rating" required="">
                    </div>
                  </div>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <div class="form-group">
                    <label for="duration" class="form-label">{{ __('duration') }}</label>
                    <input name="duration" type="text" class="form-control" id="edit_duration" aria-describedby="duration" required="">
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-group">
                    <label for="trailer_url" class="form-label">{{ __('youtubeID') }} (GUMDnD*****)</label>
                    <input name="trailer_url" type="text" class="form-control" id="edit_trailer_url" aria-describedby="trailer_url" required="">
                  </div>
                </div>
              </div>
              <div class="d-block d-md-flex">
                <div class="form-group">
                  <label for="vertical_poster" class="form-label">{{ __('verticalPoster') }}</label>
                  <div class="posterImg position-relative">
                    <div class="upload-options">
                      <label for="edit_poster">
                        <input name="vertical_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_vertical_poster')" id="edit_poster">
                      </label>
                    </div>
                    <img id="edit_vertical_poster" class="custom_img img-fluid vertical_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                  </div>
                </div>
                <div class="form-group mb-0 ms-md-3">
                  <label for="horizontal_poster" class="form-label">{{ __('horizontalPoster') }}</label>
                  <div class="horizontalPosterImg position-relative">
                    <div class="upload-options">
                      <label for="edit_horizontalPoster">
                        <input name="horizontal_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_horizontalPosterImg')" id="edit_horizontalPoster">
                      </label>
                    </div>
                    <img id="edit_horizontalPosterImg" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('close') }}</button>
          <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('save') }}</button>
        </div>
      </form>
    </div>
  </div>
</div>


<script>
  function loadFile(event, targetId) {
    var output = document.getElementById(targetId);
    if (event.target.files.length > 0) {
      output.src = URL.createObjectURL(event.target.files[0]);
      output.onload = function() {
        URL.revokeObjectURL(output.src);
      };
    } else {
      output.src = `{{ asset('assets/img/placeholder-image.png')}}`;
    }
  }
</script>

@endsection