@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/tv_channel.js') }}"></script>
@endsection
@section('content')
<section class="section">
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-block d-sm-flex align-items-center justify-content-between">
          <h4 class="mb-0 fw-semibold">{{ __('liveTvChannels') }} </h4>
          <button type="button" class="btn btn-primary text-light px-4 mt-2 mt-sm-0" data-bs-toggle="modal" data-bs-target="#addTvChannelModal">
            {{ __('addTvChannel') }}
          </button>
        </div>
      </div>
    </div>
    <div class="card-body">
      <table class="table table-striped w-100" id="tvChannelTable">
        <thead>
          <tr>
            <th width="200px"> {{ __('image') }}</th>
            <th> {{ __('category') }}</th>
            <th> {{ __('type') }}</th>
            <th> {{ __('source') }}</th>
            <th class="text-end" width="200px"> {{ __('action') }} </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</section>

<!-- Add Tv Channel Modal -->
<div class="modal fade" id="addTvChannelModal" tabindex="-1" aria-labelledby="addTvChannelModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addTvChannel') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addTvChannelForm" method="POST">
        <div class="modal-body">
          <div class="row">
            <div class="col-lg-6">
              <div class="form-group">
                <label for="title" class="form-label">{{ __('title') }}</label>
                <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group">
                <label for="access_type" class="form-label">{{ __('selectAccessType')}}</label>
                <select name="access_type" id="access_type" class="form-control selectric" required>
                  <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                  <option value="1">{{ __('free')}}</option>
                  <option value="2">{{ __('premium')}}</option>
                  <option value="3">{{ __('unlockWithVideoAds')}}</option>
                </select>
              </div>
            </div>
            <div class="col-lg-12">
              <div class="form-group">
                <label for="selectCategory" class="form-label">{{ __('selectCategory') }} ({{ __('multiple') }})</label>
                <select name="category_ids[]" class="form-control selectric" id="selectCategory" multiple="multiple" required tabindex="-1" aria-hidden="true">
                  <option value="0" disabled class="d-none">{{ __('selectCategory') }}</option>
                  @foreach ($tvCategories as $tvCategory)
                  <option value="{{ $tvCategory->id }}">{{ $tvCategory->title }}</option>
                  @endforeach
                </select>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group">
                <label for="type" class="form-label">{{ __('selectSourceType')}}</label>
                <select name="type" id="type" class="form-control selectric" aria-invalid="false" required="required">
                  <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                  <option value="1">{{ __('youtubeID')}}</option>
                  <option value="2">M3u8 Url</option>
                </select>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group">
                <label for="source" class="form-label source">{{ __('source') }}</label>
                <input name="source" type="text" class="form-control" id="source" aria-describedby="source" required>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group m-0">
                <label for="image" class="form-label">{{ __('image') }}</label>
                <div class="position-relative" style="width: 150px;">
                  <div class="upload-options">
                    <label for="poster">
                      <input name="thumbnail" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'image')" id="poster" required>
                    </label>
                  </div>
                  <img id="image" class="custom_img img-fluid modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
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

<!-- Edit Tv Channel Modal -->
<div class="modal fade" id="editTvChannelModal" tabindex="-1" aria-labelledby="editTvChannelModal" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editTvChannel') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editTvChannelForm" method="POST">
        <input type="hidden" id="tv_channel_id">
        <div class="modal-body">
          <div class="row">
            <div class="col-lg-6">
              <div class="form-group">
                <label for="title" class="form-label">{{ __('title') }}</label>
                <input name="title" type="text" class="form-control" id="edit_title" aria-describedby="title" required>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group">
                <label for="access_type" class="form-label">{{ __('selectAccessType')}}</label>
                <select name="access_type" id="edit_access_type" class="form-control selectric" required>
                  <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                  <option value="1">{{ __('free')}}</option>
                  <option value="2">{{ __('premium')}}</option>
                  <option value="3">{{ __('unlockWithVideoAds')}}</option>
                </select>
              </div>
            </div>
            <div class="col-lg-12">
              <div class="form-group">
                <label for="selectCategory" class="form-label">{{ __('selectCategory') }} ({{ __('multiple') }})</label>
                <select name="category_ids[]" class="form-control selectric" id="edit_selectCategory" multiple="multiple" required tabindex="-1" aria-hidden="true">
                  <option value="0" disabled class="d-none">{{ __('selectCategory') }}</option>
                  @foreach ($tvCategories as $tvCategory)
                  <option value="{{ $tvCategory->id }}">{{ $tvCategory->title }}</option>
                  @endforeach
                </select>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group">
                <label for="type" class="form-label">{{ __('selectSourceType')}}</label>
                <select name="type" id="edit_type" class="form-control selectric" aria-invalid="false" required="required">
                  <option value="" disabled selected class="d-none">{{ __('selectOption')}}</option>
                  <option value="1">{{ __('youtubeID')}}</option>
                  <option value="2">M3u8 Url</option>
                </select>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group">
                <label for="source" class="form-label source">{{ __('source') }}</label>
                <input name="source" type="text" class="form-control" id="edit_source" aria-describedby="source" required>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="form-group m-0">
                <label for="image" class="form-label">{{ __('image') }}</label>
                <div class="position-relative" style="width: 150px;">
                  <div class="upload-options">
                    <label for="poster1">
                      <input name="thumbnail" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_image')" id="poster1">
                    </label>
                  </div>
                  <img id="edit_image" class="custom_img img-fluid modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
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



<!-- M3u8 Modal -->
<div class="modal fade" id="m3u8Modal" tabindex="-1" aria-labelledby="m3u8Modal" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('m3u8') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <video id="m3u8_video" controls width="100%" height="350px"></video>
      </div>
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