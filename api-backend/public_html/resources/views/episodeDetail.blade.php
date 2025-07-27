@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/content_detail.js') }}"></script>
@endsection

@section('content')
<input type="hidden" id="episode_id_for_source_subtitle" value="{{ $episode->id }}">
<div class="page-content">
    <section class="detail_list">
        <div class="card" id="detail_content">
            <div class="card-header border-0 d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center">
                    <img src='{{ $episode->thumbnail ? $episode->thumbnail : asset("assets/img/default.png") }}' alt="" class="rounded border-muted object-fit-cover" data-fancybox style="background: rgb(118 118 128 / 12%);" width="100px" height="60px" class="">
                    <h4 class="fw-normal ms-3 mb-0"> {{ $episode->title }} </h4>
                </div>
                <div class="d-flex align-items-center justify-content-between">
                    <a class='me-2 btn btn-success px-4 py-2 text-white editEpisodeModal' rel='{{$episode->id}}' data-number='{{$episode->number}}' data-thumbnail='{{ $episode->thumbnail }}' data-title='{{$episode->title}}' data-description='{{$episode->description}}' data-duration='{{$episode->duration}}' data-access_type='{{$episode->access_type}}'>{{ __('edit') }}</a>
                    <a class='btn btn-danger px-4 py-2 text-white deleteEpisode' rel='{{$episode->id}}'>{{ __('delete') }}</a>
                </div>
            </div>
        </div>
        <nav class="card-tab mb-4 d-flex align-items-center justify-content-between">
            <div class="nav nav-tabs" id="nav-tab" role="tablist">
                <button class="nav-link active" id="nav-source-tab" data-bs-toggle="tab" data-bs-target="#nav-source" type="button" role="tab" aria-controls="nav-source" aria-selected="false">
                    {{ __('source') }}
                </button>
                <button class="nav-link " id="nav-subtitle-tab" data-bs-toggle="tab" data-bs-target="#nav-subtitle" type="button" role="tab" aria-controls="nav-subtitle" aria-selected="false">
                    {{ __('subtitle') }}
                </button>
            </div>
            <div class="d-flex align-items-center justify-content-between">
                <p class="btn badge badge-secondary text-light px-4 m-0 ms-2">
                    <i data-feather="eye"></i>
                    <span class="ms-2"> {{ $episode->total_view }} Views </span>
                </p>
                <p class="btn badge badge-secondary text-light px-4 mb-0 ms-2">
                    <i data-feather="download"></i>
                    <span class="ms-2"> {{ $episode->total_download }} Downloads </span>
                </p>
            </div>
        </nav>

        <div class="tab-content" id="nav-tabContent">
            <div class="tab-pane show active" id="nav-source" role="tabpanel" aria-labelledby="nav-source-tab" tabindex="0">
                <div class="card">
                    <div class="card-header d-flex align-items-center justify-content-between">
                        <h4 class="fw-normal m-0"> {{ __('sourceList') }} </h4>
                        <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addEpisodeSourceModal">
                            {{ __('addEpisodeSource') }}
                        </button>
                    </div>
                    <div class="card-body">
                        <table class="table table-striped w-100" id="episodeSourceTable">
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
            <div class="tab-pane " id="nav-subtitle" role="tabpanel" aria-labelledby="nav-subtitle-tab" tabindex="0">
                <div class="card">
                    <div class="card-header d-flex align-items-center justify-content-between">
                        <h4 class="fw-normal m-0"> {{ __('subtitleList') }} </h4>
                        <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addEpisodeSubtitleModal">
                            {{ __('addSubtitle') }}
                        </button>
                    </div>
                    <div class="card-body">
                        <table class="table table-striped w-100" id="episodeSubtitleTable">
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

<!-- Add Episode Source Modal -->
<div class="modal fade" id="addEpisodeSourceModal" tabindex="-1" aria-labelledby="addEpisodeSourceModalLabel" aria-hidden="true">
    <div class="modal-dialog  modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addEpisodeSource') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addEpisodeSourceForm" method="POST">
                <div class="modal-body">
                    <input type="hidden" name="episode_id" id="episode_id" value="{{ $episode->id }}">
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
                            <label for="media" class="form-label">{{ __('selectMedia')}}</label>
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
                        <div class="form-group mb-0  w-100">
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
                                        <input type="hidden" id="is_download_hidden" name="is_download" value="0">
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

<!-- Edit Episode Source Modal -->
<div class="modal fade" id="editEpisodeSourceModal" tabindex="-1" aria-labelledby="editEpisodeSourceModalLabel" aria-hidden="true">
    <div class="modal-dialog  modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editEpisodeSource') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editEpisodeSourceForm" method="POST">
                <div class="modal-body">
                    <input type="hidden" id="episode_source_id">
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
                        <select name="type" id="edit_type" class="form-control selectric" aria-invalid="false">
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
                    <div class="progress mb-2" id="edit-progress" style="height: 25px; display: none;">
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

<!-- Add Subtitle Modal -->
<div class="modal fade" id="addEpisodeSubtitleModal" tabindex="-1" aria-labelledby="addEpisodeSubtitleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addEpisodeSubtitle') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addEpisodeSubtitleForm" method="POST">
                <input type="hidden" name="episode_id" id="episode_id_for_subtitle" value="{{ $episode->id }}">
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group">
                            <label for="language_id" class="form-label">{{ __('selectLanguage') }}</label>
                            <select name="language_id" id="language_id" class="form-control selectric" required="">
                                <option value="" disabled selected class="d-none">{{ __('selectLanguage') }}</option>
                                @foreach ($languages as $language)
                                <option value="{{ $language->id }}">{{ $language->title }}</option>
                                @endforeach
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="file" class="form-label">{{ __('subtitleFile')}}</label>
                            <div class="file-input form-control w-100">
                                <input name="file" id="file" type="file" class="form-control" aria-describedby="file" accept=".srt">
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

<!-- Edit Episode Modal -->
<div class="modal fade" id="editEpisodeModal" tabindex="-1" aria-labelledby="editEpisodeModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editEpisode') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editEpisodeForm" method="POST">
                <input type="hidden" name="episode_id" id="edit_episode_id">
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
                        <div class="col-lg-5">
                            <div class="form-group">
                                <label for="duration" class="form-label">{{ __('duration') }}</label>
                                <input name="duration" type="text" class="form-control" id="edit_duration" aria-describedby="duration" required>
                            </div>
                        </div>
                        <div class="col-lg-7">
                            <div class="form-group">
                                <label for="access_type" class="form-label">{{ __('selectAccessType')}}</label>
                                <select name="access_type" id="edit_episode_access_type" class="form-control selectric" required>
                                    <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                                    <option value="1">{{ __('free')}}</option>
                                    <option value="2">{{ __('premium')}}</option>
                                    <option value="3">{{ __('unlockWithVideoAds')}}</option>
                                </select>
                            </div>
                        </div>
                    </div>
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