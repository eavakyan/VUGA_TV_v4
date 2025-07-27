@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/content_detail.js') }}"></script>
@endsection
@section('content')

<input type="hidden" id="content_id1" value="{{ $content->id }}">
<div class="page-content">
  <section class="detail_list">
    <div class="card" id="detail_content">
      <div class="card-header border-0 d-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center">
          <img src='{{ $content->horizontal_poster ? $content->horizontal_poster : asset("assets/img/default.png") }}' alt="" class="rounded border-muted object-fit-cover" data-fancybox style="background: rgb(118 118 128 / 12%);" width="100px" height="60px" class="">
          <h4 class="fw-normal ms-3 mb-0"> {{ $content->title }} </h4>
        </div>
        <div class="d-flex align-items-center justify-content-between">
          <a class='me-2 btn btn-success px-4 py-2 text-white editContentModal' rel='{{$content->id}}' data-type='{{$content->type}}' data-title='{{$content->title}}' data-description='{{$content->description}}' data-duration='{{$content->duration}}' data-release_year='{{$content->release_year}}' data-ratings='{{$content->ratings}}' data-language_id='{{$content->language_id}}' data-genre_ids='{{$content->genre_ids}}' data-trailer_url='{{$content->trailer_url}}' data-vposter='{{ $content->vertical_poster }}' data-hposter='{{ $content->horizontal_poster }}'>{{ __('edit') }}</a>
          <a class='btn btn-danger px-4 py-2 text-white deleteContent' rel='{{$content->id}}'>{{ __('delete') }}</a>
        </div>
      </div>
    </div>

    <nav class="card-tab mb-4 d-flex align-items-center justify-content-between">
      <div class="nav nav-tabs" id="nav-tab" role="tablist">
        <button class="nav-link active" id="nav-season-tab" data-bs-toggle="tab" data-bs-target="#nav-season" type="button" role="tab" aria-controls="nav-season" aria-selected="false">
          {{ __('season') }}
        </button>
        <button class="nav-link" id="nav-cast-tab" data-bs-toggle="tab" data-bs-target="#nav-cast" type="button" role="tab" aria-controls="nav-cast" aria-selected="false">
          {{ __('cast') }}
        </button>
      </div>
      <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addSeasonModal">
        {{ __('addSeason') }}
      </button>
    </nav>

    <div class="tab-content" id="nav-tabContent">
      <div class="tab-pane show active" id="nav-season" role="tabpanel" aria-labelledby="nav-season-tab" tabindex="0">
        <div id="is_season">
          <div class="row align-items-center">
            <div class="col-md-4 mb-4">
              <div class="selectSeason d-flex">
                <div class="form-group w-100 m-0 me-2 season-select">
                  <select name="season_list" id="season_list" class="form-control selectric" required="">
                    <option value="0" disabled class="d-none"> {{ __('seasonList')}}</option>
                    @foreach ($seasons as $season)
                    <option value="{{ $season->id }}" data-trailerurl="{{ $season->trailer_url }}" data-title="{{ $season->title }}">
                      {{ $season->title }}
                    </option>
                    @endforeach
                  </select>
                  <span id="selectedValue"></span>
                </div>
                <div id="season_action" class="w-auto">
                  <div class="text-end d-flex">
                    <div class="me-2 btn btn-success px-4 text-white edit" id="edit_season" rel='{{ $seasons->first() ? $seasons->first()->id : "" }}' data-title='{{ $seasons->first() ? $seasons->first()->title : ""  }}' data-trailerurl='{{ $seasons->first() ? $seasons->first()->trailer_url : ""  }}'>
                      {{ __('edit') }}
                    </div>
                    <div class="btn btn-danger px-4 text-white delete" id="delete_season" rel='{{ $seasons->first() ? $seasons->first()->id : "" }}'>
                      {{ __('delete') }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card">
            <div class="card-header d-flex align-items-center justify-content-between">
              <div class="d-flex align-items-center justify-content-between">
                <h4 class="fw-normal m-0">
                  <span id="seasonBadge"> {{ $seasons->first() ? $seasons->first()->title : ""  }} </span>
                </h4>
                <a href="https://youtu.be/{{ $seasons->first() ? $seasons->first()->trailer_url : ''  }}" target="_blank" rel="noopener noreferrer" id="youtube_id" class="bg-white theme-color px-3 fw-semibold rounded"> {{ __('trailer') }} </a>
              </div>
              <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addEpisodeModal">
                {{ __('addEpisode') }}
              </button>
            </div>
            <div class="card-body">
              <table class="table table-striped w-100" id="episodeTable">
                <thead>
                  <tr>
                    <th> {{ __('thumbnail') }}</th>
                    <th> {{ __('title') }}</th>
                    <th> {{ __('description') }}</th>
                    <th class="text-end" width="200px"> {{ __('action') }} </th>
                  </tr>
                </thead>
              </table>
            </div>
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

    </div>

  </section>
</div>

<!-- Add Season Modal -->
<div class="modal fade" id="addSeasonModal" tabindex="-1" aria-labelledby="addSeasonModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addSeason') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addSeasonForm" method="POST">
        <input type="hidden" name="content_id" id="content_id" value="{{ $content->id }}">
        <div class="modal-body">
          <div class="form-group">
            <label for="title" class="form-label">{{ __('title') }}</label>
            <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
          </div>
          <div class="form-group m-0">
            <label for="trailer_url" class="form-label">{{ __('youtubeID') }} (GUMDnD*****)</label>
            <input name="trailer_url" type="text" class="form-control" id="trailer_url" aria-describedby="trailer_url" required>
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

<!-- Edit Season Modal -->
<div class="modal fade" id="editSeasonModal" class="editSeasonModal" tabindex="-1" aria-labelledby="editSeasonModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editSeason') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editSeasonForm" method="POST">
        <input type="hidden" name="season_id" id="season_id" value="">
        <div class="modal-body">
          <div class="form-group">
            <label for="edit_title" class="form-label">{{ __('title') }}</label>
            <input name="title" type="text" class="form-control" id="edit_title" aria-describedby="title" required value="">
          </div>
          <div class="form-group m-0">
            <label for="edit_trailer_url" class="form-label">{{ __('youtubeID') }} (GUMDnD*****)</label>
            <input name="trailer_url" type="text" class="form-control" id="edit_trailer_url" aria-describedby="trailer_url" required value="">
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

<!-- Add Episode Modal -->
<div class="modal fade" id="addEpisodeModal" tabindex="-1" aria-labelledby="addEpisodeModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addEpisode') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addEpisodeForm" method="POST">
        <input type="hidden" name="season_id" id="season_id_for_episode">
        <div class="modal-body">
          <div class="form-group">
            <label for="number" class="form-label">{{ __('episodeNumber') }}</label>
            <input name="number" type="number" class="form-control" id="number" required min="0" max="99" oninput="this.value = this.value.slice(0, 2);">
          </div>
          <div class="form-group">
            <label for="title" class="form-label">{{ __('title') }}</label>
            <input name="title" type="text" class="form-control" id="episode_title" aria-describedby="title" required>
          </div>
          <div class="form-group">
            <label for="description" class="form-label">{{ __('description') }}</label>
            <textarea name="description" id="description" rows="4" class="form-control" required=""></textarea>
          </div>
          <div class="row">
            <div class="col-auto">
              <div class="form-group">
                <label for="horizontal_poster" class="form-label">{{ __('horizontalPoster') }}</label>
                <div class="horizontalPosterImg position-relative">
                  <div class="upload-options">
                    <label for="thumbnail">
                      <input name="thumbnail" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'thumbnailPoster')" id="thumbnail">
                    </label>
                  </div>
                  <img id="thumbnailPoster" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('./assets/img/placeholder-image.png')}}">
                </div>
              </div>
            </div>
            <div class="col">
              <div class="form-group">
                <label for="duration" class="form-label">{{ __('duration') }}</label>
                <input name="duration" type="text" class="form-control" id="duration" aria-describedby="duration" required>
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

<!-- Edit Episode Modal -->
<div class="modal fade" id="editEpisodeModal" tabindex="-1" aria-labelledby="editEpisodeModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editEpisode') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editEpisodeForm" method="POST">
        <input type="hidden" name="episode_id" id="episode_id">
        <div class="modal-body">
          <div class="form-group">
            <label for="number" class="form-label">{{ __('episodeNumber') }}</label>
            <input name="number" type="number" class="form-control" id="edit_number" required min="0" max="99" oninput="this.value = this.value.slice(0, 2);">
          </div>
          <div class="form-group">
            <label for="title" class="form-label">{{ __('title') }}</label>
            <input name="title" type="text" class="form-control" id="edit_episode_title" aria-describedby="title" required>
          </div>
          <div class="form-group">
            <label for="description" class="form-label">{{ __('description') }}</label>
            <textarea name="description" id="edit_description" rows="4" class="form-control" required=""></textarea>
          </div>
          <div class="row">
            <div class="col-auto">
              <div class="form-group">
                <label for="horizontal_poster" class="form-label">{{ __('horizontalPoster') }}</label>
                <div class="horizontalPosterImg position-relative">
                  <div class="upload-options">
                    <label for="edit_thumbnail">
                      <input name="thumbnail" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_thumbnailPoster')" id="edit_thumbnail">
                    </label>
                  </div>
                  <img id="edit_thumbnailPoster" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                </div>
              </div>
            </div>
            <div class="col">
              <div class="form-group">
                <label for="duration" class="form-label">{{ __('duration') }}</label>
                <input name="duration" type="text" class="form-control" id="edit_duration" aria-describedby="duration" required>
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
              <div class="form-group m-0">
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
                <div class="col">
                  <div class="form-group">
                    <label for="release_year" class="form-label">{{ __('releaseYear') }}</label>
                    <input name="release_year" type="number" class="form-control" id="edit_release_year" aria-describedby="release_year" required="">
                  </div>
                </div>
                <div class="col">
                  <div class="form-group">
                    <label for="ratings" class="form-label">{{ __('ratings') }}</label>
                    <div class="rating_star">
                      <img src="{{ asset('assets/img/rating_star.svg') }}" alt="rating_star">
                      <input name="ratings" type="number" step=any class="form-control" id="edit_ratings" aria-describedby="rating" required="">
                    </div>
                  </div>
                </div>
              </div>

              <div class="d-flex">
                <div class="form-group m-0">
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
                <div class="form-group mb-0 ms-3">
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