@extends('include.app')
@section('script')

<script src="{{ asset('assets/script/tmdb.js') }}"></script>
<script>
    const localization = {
        importData: "{{ __('importData') }}",
    };
</script>
@endsection

@section('content')
<section class="section">
    <div class="card">
        <div class="card-header">
            <div class="page-title w-100">
                <div class="d-block d-md-flex align-items-center justify-content-between">
                    <h4 class="mb-0 fw-semibold">{{ __('fetchContentFromTMDB') }}</h4>
                    <nav class="card-tab mt-2 mt-xl-0">
                        <div class="nav nav-tabs" id="nav-tab" role="tablist">
                            <button class="nav-link active" id="nav-movies-tab" data-bs-toggle="tab" data-bs-target="#nav-movies" type="button" role="tab" aria-controls="nav-movies" aria-selected="false">
                                {{ __('movie') }}
                            </button>
                            <button class="nav-link" id="nav-series-tab" data-bs-toggle="tab" data-bs-target="#nav-series" type="button" role="tab" aria-controls="nav-series" aria-selected="false">
                                {{ __('series') }}
                            </button>
                        </div>
                    </nav>
                </div>
            </div>
        </div>
        <div class="card-body pb-0">
            <div class="tab-content" id="nav-tabContent">
                <div class="tab-pane show active" id="nav-movies" role="tabpanel" aria-labelledby="nav-movies-tab" tabindex="0">
                    <form id="searchMovieTMDBForm" method="post">
                        <div class="row justify-content-center">
                            <div class="col-xl-4 col-lg-4 col-md-4 col-sm-12">
                                <div id="parameters" class="searchInput">
                                    <div class="form-group">
                                        <input type="text" class="select-selected form-control" id="movieQuery" name="query" maxlength="64" placeholder="{{ __('searchMovie') }}" required>
                                    </div>
                                    <button type="submit" id="add_parameters" class="btn btn-primary px-3 searchIcon">
                                        <i data-feather="search"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </form>

                    <table class="table table-striped w-100" id="resultOfMovieTable">
                        <thead>
                            <tr>
                                <th width="200px"> {{ __('image') }}</th>
                                <th> {{ __('title') }}</th>
                                <th class="w-100"> {{ __('overview') }}</th>
                                <th class="text-end" width="250px"> {{ __('action') }} </th>
                            </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>

                    <ul class="pagination-movie pagination-controls">
                        <li class="paginate_button page-item previous">
                            <a href="#" id="prev-page-movie" disabled class="pagination-ui">
                                <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1">
                                    <polyline points="15 18 9 12 15 6"></polyline>
                                </svg>
                            </a>
                        </li>
                        <li id="movie-page-buttons"></li>
                        <li class="paginate_button page-item next">
                            <a href="#" id="next-page-movie" disabled class="pagination-ui">
                                <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1">
                                    <polyline points="9 18 15 12 9 6"></polyline>
                                </svg>
                            </a>
                        </li>
                    </ul>
                </div>

                <div class="tab-pane" id="nav-series" role="tabpanel" aria-labelledby="nav-series-tab" tabindex="0">
                    <form id="searchSeriesTMDBForm" method="post">
                        <div class="row justify-content-center">
                            <div class="col-xl-4 col-lg-4 col-md-4 col-sm-12">
                                <div id="parameters" class="searchInput">
                                    <div class="form-group">
                                        <input type="text" class="select-selected form-control" id="seriesQuery" name="query" maxlength="64" placeholder="{{ __('searchSeries') }}" required>
                                    </div>
                                    <button type="submit" id="add_parameters" class="btn btn-primary px-3 searchIcon">
                                        <i data-feather="search"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </form>

                    <table class="table table-striped w-100" id="resultOfSeriesTable">
                        <thead>
                            <tr>
                                <th width="200px"> {{ __('image') }}</th>
                                <th> {{ __('title') }}</th>
                                <th class="w-100"> {{ __('overview') }}</th>
                                <th class="text-end" width="250px"> {{ __('action') }} </th>
                            </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>

                    <ul class="pagination-series pagination-controls">
                        <li class="paginate_button page-item previous">
                            <a href="#" id="prev-page-series" disabled class="pagination-ui">
                                <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1">
                                    <polyline points="15 18 9 12 15 6"></polyline>
                                </svg>
                            </a>
                        </li>
                        <li id="series-page-buttons"></li>
                        <li class="paginate_button page-item next">
                            <a href="#" id="next-page-series" disabled class="pagination-ui">
                                <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1">
                                    <polyline points="9 18 15 12 9 6"></polyline>
                                </svg>
                            </a>
                        </li>
                    </ul>

                </div>
            </div>

        </div>
    </div>
</section>


<!-- Set Content Modal -->
<div class="modal fade" id="setMovieContentModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="setMovieContentModalLabel" aria-hidden="true">
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
                                    <!-- <option value="" disabled class="d-none">{{ __('selectOption') }}</option> -->
                                    <option value="1" selected>{{ __('movie') }}</option>
                                    <!-- <option value="2">{{ __('series') }}</option> -->
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="title" class="form-label">{{ __('title') }}</label>
                                <input name="title" type="text" class="form-control" id="set_title" aria-describedby="title" required>
                            </div>
                            <div class="form-group">
                                <label for="description" class="form-label">{{ __('description') }}</label>
                                <textarea name="description" id="set_description" rows="4" class="form-control" required></textarea>
                            </div>
                            <div class="form-group" id="genres_input">
                                <label for="selectGenre" class="form-label">{{ __('selectGenre') }} ({{ __('multiple') }})</label>
                                <select name="genre_ids[]" class="form-control selectric" id="set_selectGenre" multiple="multiple" required tabindex="-1" aria-hidden="true">
                                    <option value="0" disabled class="d-none">{{ __('selectGenre') }}</option>
                                    @foreach ($genres as $genre)
                                    <option value="{{ $genre->id }}">{{ $genre->title }}</option>
                                    @endforeach
                                </select>
                            </div>
                            <div class="form-group m-0">
                                <label for="language_id" class="form-label">{{ __('selectLanguage') }}</label>
                                <select name="language_id" id="set_language_id" class="form-control selectric" required>
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
                                        <input name="release_year" type="number" class="form-control" id="set_release_year" aria-describedby="release_year" required>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="form-group">
                                        <label for="ratings" class="form-label">{{ __('ratings') }}</label>
                                        <div class="rating_star">
                                            <img src="{{ asset('assets/img/rating_star.svg') }}" alt="rating_star">
                                            <input name="ratings" type="number" step=any class="form-control" id="set_ratings" aria-describedby="rating" required>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col">
                                    <div class="form-group">
                                        <label for="duration" class="form-label">{{ __('duration') }}</label>
                                        <input name="duration" type="text" class="form-control" id="set_duration" aria-describedby="duration" required>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="form-group">
                                        <label for="trailer_url" class="form-label">{{ __('youtubeID') }} (GUMDnD*****)</label>
                                        <input name="trailer_url" type="text" class="form-control" id="set_trailer_url" aria-describedby="trailer_url" required>
                                    </div>
                                </div>
                            </div>
                            <div class="d-flex">
                                <input type="hidden" id="vertical_poster_url" name="vertical_poster_url">
                                <input type="hidden" id="horizontal_poster_url" name="horizontal_poster_url">
                                <div class="form-group m-0">
                                    <label for="vertical_poster" class="form-label">{{ __('verticalPoster') }}</label>
                                    <div class="posterImg position-relative">
                                        <div class="upload-options">
                                            <label for="poster">
                                                <input name="vertical_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'set_vertical_poster')" id="poster">
                                            </label>
                                        </div>
                                        <img id="set_vertical_poster" class="custom_img img-fluid vertical_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                                    </div>
                                </div>
                                <div class="form-group mb-0 ms-3">
                                    <label for="horizontalPoster" class="form-label">{{ __('horizontalPoster') }}</label>
                                    <div class="horizontalPosterImg position-relative">
                                        <div class="upload-options">
                                            <label for="horizontalPoster">
                                                <input name="horizontal_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'set_horizontal_poster')" id="horizontalPoster">
                                            </label>
                                        </div>
                                        <img id="set_horizontal_poster" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
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


<!-- Set Content Modal -->
<div class="modal fade" id="setSeriesContentModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="setSeriesContentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addNewContent') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addNewContentFormSeries" method="POST">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xl-6 col-lg-6 col-md-6 col-sm-12">
                            <div class="form-group">
                                <label for="content_type" class="form-label">{{ __('selectType') }}</label>
                                <select name="type" id="series_content_type" class="form-control selectric" required>
                                    <!-- <option value="" disabled class="d-none">{{ __('selectOption') }}</option> -->
                                    <!-- <option value="1" selected>{{ __('movie') }}</option> -->
                                    <option value="2" selected>{{ __('series') }}</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="title" class="form-label">{{ __('title') }}</label>
                                <input name="title" type="text" class="form-control" id="series_set_title" aria-describedby="title" required>
                            </div>
                            <div class="form-group">
                                <label for="description" class="form-label">{{ __('description') }}</label>
                                <textarea name="description" id="series_set_description" rows="4" class="form-control" required></textarea>
                            </div>
                            <div class="form-group" id="genres_input">
                                <label for="selectGenre" class="form-label">{{ __('selectGenre') }} ({{ __('multiple') }})</label>
                                <select name="genre_ids[]" class="form-control selectric" id="series_set_selectGenre" multiple="multiple" required tabindex="-1" aria-hidden="true">
                                    <option value="0" disabled class="d-none">{{ __('selectGenre') }}</option>
                                    @foreach ($genres as $genre)
                                    <option value="{{ $genre->id }}">{{ $genre->title }}</option>
                                    @endforeach
                                </select>
                            </div>
                            <div class="form-group m-0">
                                <label for="language_id" class="form-label">{{ __('selectLanguage') }}</label>
                                <select name="language_id" id="series_set_language_id" class="form-control selectric" required>
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
                                        <input name="release_year" type="number" class="form-control" id="series_set_release_year" aria-describedby="release_year" required>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="form-group">
                                        <label for="ratings" class="form-label">{{ __('ratings') }}</label>
                                        <div class="rating_star">
                                            <img src="{{ asset('assets/img/rating_star.svg') }}" alt="rating_star">
                                            <input name="ratings" type="number" step=any class="form-control" id="series_set_ratings" aria-describedby="rating" required>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- <div class="row">
                                <div class="col">
                                    <div class="form-group">
                                        <label for="duration" class="form-label">{{ __('duration') }}</label>
                                        <input name="duration" type="text" class="form-control" id="set_duration" aria-describedby="duration" required>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="form-group">
                                        <label for="trailer_url" class="form-label">{{ __('youtubeID') }} (GUMDnD*****)</label>
                                        <input name="trailer_url" type="text" class="form-control" id="set_trailer_url" aria-describedby="trailer_url" required>
                                    </div>
                                </div>
                            </div> -->
                            <div class="d-flex">
                                <input type="hidden" id="series_vertical_poster_url" name="vertical_poster_url">
                                <input type="hidden" id="series_horizontal_poster_url" name="horizontal_poster_url">
                                <div class="form-group m-0">
                                    <label for="vertical_poster" class="form-label">{{ __('verticalPoster') }}</label>
                                    <div class="posterImg position-relative">
                                        <div class="upload-options">
                                            <label for="poster">
                                                <input name="vertical_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'series_set_vertical_poster')" id="poster">
                                            </label>
                                        </div>
                                        <img id="series_set_vertical_poster" class="custom_img img-fluid vertical_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                                    </div>
                                </div>
                                <div class="form-group mb-0 ms-3">
                                    <label for="horizontalPoster" class="form-label">{{ __('horizontalPoster') }}</label>
                                    <div class="horizontalPosterImg position-relative">
                                        <div class="upload-options">
                                            <label for="horizontalPoster">
                                                <input name="horizontal_poster" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'series_set_horizontal_poster')" id="horizontalPoster">
                                            </label>
                                        </div>
                                        <img id="series_set_horizontal_poster" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
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