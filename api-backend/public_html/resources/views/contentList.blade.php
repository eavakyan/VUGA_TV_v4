@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/content.js') }}"></script>
<script>
  document.addEventListener('DOMContentLoaded', function() {

    const TMDBAPI = @json($TMDBAPI);

    const isTMDBAPIComplete = Object.values(TMDBAPI).every(value => value !== null && value !== '');
    const TMDB_Button = document.getElementById('TMDB_Button');
    if (!isTMDBAPIComplete) {
      $('#TMDB_Button').addClass('d-none');
      $('#TMDB_Note').removeClass('d-none');
    } else {
      $('#TMDB_Button').removeClass('d-none');
      $('#TMDB_Note').addClass('d-none');
    }

  });
</script>
@endsection

@section('content')
<div class="row">
  <div class="col-xl-3 col-lg-3 col-md-4 col-sm-12 col-xs-12">
    <div class="card">
      <div class="card-header border-0 d-flex align-items-center justify-content-between">
        <h4 class="mb-0 fw-semibold">{{ __('all') }} </h4>
        <h4 class="badge badge-primary mb-0 fw-semibold px-4">{{ $movieCount + $seriesCount }} </h4>
      </div>
    </div>
  </div>
  <div class="col-xl-3 col-lg-3 col-md-4 col-sm-12 col-xs-12">
    <div class="card">
      <div class="card-header border-0 d-flex align-items-center justify-content-between">
        <h4 class="mb-0 fw-semibold">{{ __('movies') }} </h4>
        <h4 class="badge badge-primary mb-0 fw-semibold px-4">{{ $movieCount }} </h4>
      </div>
    </div>
  </div>
  <div class="col-xl-3 col-lg-3 col-md-4 col-sm-12 col-xs-12">
    <div class="card">
      <div class="card-header border-0 d-flex align-items-center justify-content-between">
        <h4 class="mb-0 fw-semibold">{{ __('series') }} </h4>
        <h4 class="badge badge-primary mb-0 fw-semibold px-4">{{ $seriesCount }} </h4>
      </div>
    </div>
  </div>
</div>


<section class="section">
  <p class="text-end text-danger" id="TMDB_Note">
    *If you want to use TMDB Service Then add <b> TMDB_API_KEY </b> in <b> .env </b> file
  </p>
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-block d-xl-flex align-items-center justify-content-between gap-2">
          <div class="d-block d-xl-flex align-items-center justify-content-between">
            <h4 class="mb-0 fw-semibold">{{ __('contentList') }} </h4>
            <nav class="card-tab mt-2 mt-xl-0">
              <div class="nav nav-tabs" id="nav-tab" role="tablist">
                <button class="nav-link active" id="nav-movies-tab" data-bs-toggle="tab" data-bs-target="#nav-movies" type="button" role="tab" aria-controls="nav-movies" aria-selected="false">
                  {{ __('movies') }}
                </button>
                <button class="nav-link" id="nav-series-tab" data-bs-toggle="tab" data-bs-target="#nav-series" type="button" role="tab" aria-controls="nav-series" aria-selected="false">
                  {{ __('series') }}
                </button>
              </div>
            </nav>
          </div>
          <div class="d-block d-sm-flex align-items-center justify-content-end">
            <button type="button" class="btn btn-primary text-light px-4 me-2 mt-3 mb-xl-0" data-bs-toggle="modal" data-bs-target="#addContentModal">
              {{ __('addContent') }}
            </button>
            <a href="{{ route('fetchContentFromTMDB') }}" id="TMDB_Button" class="btn btn-tmdb shadow-none text-light px-4 mt-3 mb-xl-0">
              {{ __('fetchContentFromTMDB') }}
            </a>
          </div>
        </div>
      </div>
    </div>
    <div class="tab-content" id="nav-tabContent">
      <div class="tab-pane show active" id="nav-movies" role="tabpanel" aria-labelledby="nav-movies-tab" tabindex="0">
        <div class="card-body">
          <table class="table table-striped w-100" id="moviesTable">
            <thead>
              <tr>
                <th class="content-poster"> {{ __('poster') }}</th>
                <th> {{ __('title') }}</th>
                <th> {{ __('ratings') }}</th>
                <th> {{ __('releaseYear') }}</th>
                <th> {{ __('language') }}</th>
                <th> {{ __('featured') }}</th>
                <th> {{ __('hideShow') }}</th>
                <th class="text-end" width="200px"> {{ __('action') }} </th>
              </tr>
            </thead>
          </table>
        </div>
      </div>

      <div class="tab-pane" id="nav-series" role="tabpanel" aria-labelledby="nav-series-tab" tabindex="0">
        <div class="card-body">
          <table class="table table-striped w-100" id="seriesTable">
            <thead>
              <tr>
                <th class="content-poster"> {{ __('poster') }}</th>
                <th> {{ __('title') }}</th>
                <th> {{ __('ratings') }}</th>
                <th> {{ __('releaseYear') }}</th>
                <th> {{ __('language') }}</th>
                <th> {{ __('featured') }}</th>
                <th> {{ __('hideShow') }}</th>
                <th class="text-end" width="200px"> {{ __('action') }} </th>
              </tr>
            </thead>
          </table>
        </div>
      </div>
    </div>
  </div>
</section>


<!-- Add Content Modal -->
<div class="modal fade" id="addContentModal" tabindex="-1" aria-labelledby="addContentModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addNewContent') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addNewContentForm" method="POST">
        <div class="modal-body">
          <div class="row">
            <div class="col-xl-6 col-lg-6 col-md-6 col-sm-12">
              <div class="form-group">
                <label for="content_type" class="form-label">{{ __('selectType') }}</label>
                <select name="type" id="content_type" class="form-control selectric" required>
                  <option value="" disabled selected class="d-none">{{ __('selectOption') }}</option>
                  <option value="1">{{ __('movie') }}</option>
                  <option value="2">{{ __('series') }}</option>
                </select>
              </div>
              <div class="form-group">
                <label for="title" class="form-label">{{ __('title') }}</label>
                <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
              </div>
              <div class="form-group">
                <label for="description" class="form-label">{{ __('description') }}</label>
                <textarea name="description" id="description" rows="4" class="form-control" required></textarea>
              </div>
              <div class="form-group">
                <label for="selectGenre" class="form-label">{{ __('selectGenre') }} ({{ __('multiple') }})</label>
                <select name="genre_ids[]" class="form-control selectric" id="selectGenre" multiple="multiple" required tabindex="-1" aria-hidden="true">
                  <option value="0" disabled class="d-none">{{ __('selectGenre') }}</option>
                  @foreach ($genres as $genre)
                  <option value="{{ $genre->id }}">{{ $genre->title }}</option>
                  @endforeach
                </select>
              </div>
              <div class="form-group">
                <label for="language_id" class="form-label">{{ __('selectLanguage') }}</label>
                <select name="language_id" id="language_id" class="form-control selectric" required>
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
                    <input name="release_year" type="number" class="form-control" id="release_year" aria-describedby="release_year" required>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-group">
                    <label for="ratings" class="form-label">{{ __('ratings') }}</label>
                    <div class="rating_star">
                      <img src="{{ asset('assets/img/rating_star.svg') }}" alt="rating_star">
                      <input name="ratings" type="number" step=any class="form-control" id="ratings" aria-describedby="rating" required>
                    </div>
                  </div>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <div class="form-group">
                    <label for="duration" class="form-label">{{ __('duration') }}</label>
                    <input name="duration" type="text" class="form-control" id="duration" aria-describedby="duration" required>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-group">
                    <label for="trailer_url" class="form-label">{{ __('youtubeID') }} (GUMDnD*****)</label>
                    <input name="trailer_url" type="text" class="form-control" id="trailer_url" aria-describedby="trailer_url" required>
                  </div>
                </div>
              </div>
              <div class="d-block d-md-flex">
                <div class="form-group">
                  <label for="vertical_poster" class="form-label">{{ __('verticalPoster') }}</label>
                  <div class="posterImg position-relative">
                    <div class="upload-options">
                      <label for="poster">
                        <input name="vertical_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'vertical_poster')" id="poster" required>
                      </label>
                    </div>
                    <img id="vertical_poster" class="custom_img img-fluid vertical_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                  </div>
                </div>
                <div class="form-group mb-0 ms-md-3">
                  <label for="horizontalPoster" class="form-label">{{ __('horizontalPoster') }}</label>
                  <div class="horizontalPosterImg position-relative">
                    <div class="upload-options">
                      <label for="horizontalPoster">
                        <input name="horizontal_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'horizontalPosterImg')" id="horizontalPoster" required>
                      </label>
                    </div>
                    <img id="horizontalPosterImg" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
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

<!-- Edit Content Modal -->
<div class="modal fade" id="editContentModal" tabindex="-1" aria-labelledby="editContentModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editContent') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editContentForm" method="POST" class="editform">
        <input type="hidden" id="content_id">
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
                <input name="title" type="text" class="form-control" id="edit_title" aria-describedby="title" required="">
              </div>
              <div class="form-group">
                <label for="description" class="form-label">{{ __('description') }}</label>
                <textarea name="description" id="edit_description" rows="4" class="form-control" required=""></textarea>
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