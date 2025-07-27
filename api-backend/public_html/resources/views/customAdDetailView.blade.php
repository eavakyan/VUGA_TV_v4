@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/customAd.js') }}"></script>
<script>
  // Edit 

  const editStartDateInput = document.getElementById("edit_start_date");
  const editEndDateInput = document.getElementById("edit_end_date");

  // Get today's date
  const editToday = new Date().toISOString().split("T")[0];

  // Set the minimum date for both inputs to today
  editStartDateInput.setAttribute("min", editToday);
  editEndDateInput.setAttribute("min", editToday);

  // Disable end date picker initially
  // editEndDateInput.disabled = true;

  // Function to enable end date picker when start date is selected
  editStartDateInput.addEventListener("input", function() {
    editEndDateInput.removeAttribute("disabled");
    editEndDateInput.setAttribute("min", editStartDateInput.value);
  });

  const editAndroidCheckbox = document.getElementById("edit_is_android_checkbox");
  const editAndroidLinkInput = document.getElementById("edit_android_link");
  const editAndroidHiddenInput = document.getElementById("edit_is_android_hidden");

  const editIosCheckbox = document.getElementById("edit_is_ios_checkbox");
  const editIosLinkInput = document.getElementById("edit_ios_link");
  const editIosHiddenInput = document.getElementById("edit_is_ios_hidden");

  function toggleInputState(checkbox, input, hiddenInput) {
    input.disabled = !checkbox.checked;
    hiddenInput.value = checkbox.checked ? "1" : "0";
  }

  editAndroidCheckbox.addEventListener("change", function() {
    toggleInputState(editAndroidCheckbox, editAndroidLinkInput, editAndroidHiddenInput);
  });

  editIosCheckbox.addEventListener("change", function() {
    toggleInputState(editIosCheckbox, editIosLinkInput, editIosHiddenInput);
  });
</script>

@endsection

@section('content')
<input type="hidden" id="customAd_id" value="{{ $customAd->id }}">

<div class="page-content">
  <section class="detail_list">
    <div class="card" id="detail_content">
      <div class="card-header border-0 d-block d-md-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center">
          <img src='{{ $customAd->brand_logo ? $customAd->brand_logo : asset("assets/img/default.png") }}' data-fancybox alt="" class="rounded border-muted object-fit-cover" data-fancybox style="background: rgb(118 118 128 / 12%);" width="100px" height="60px" class="">
          <h4 class="fw-normal ms-3 mb-0"> {{ $customAd->title }} </h4>
        </div>
        <div class="d-flex align-items-center justify-content-end">
          <a class='me-2 btn btn-success px-4 py-2 text-white editCustomAdModal' rel='{{$customAd->id}}' data-brand_logo='{{ $customAd->brand_logo }}' data-title='{{$customAd->title}}' data-brand_name='{{$customAd->brand_name}}' data-button_text='{{$customAd->button_text}}' data-is_android='{{$customAd->is_android}}' data-android_link='{{$customAd->android_link}}' data-is_ios='{{$customAd->is_ios}}' data-ios_link='{{$customAd->ios_link}}' data-start_date='{{$customAd->start_date}}' data-end_date='{{$customAd->end_date}}' data-status='{{$customAd->status}}'>{{ __('edit') }}</a>
          <a class='btn btn-danger px-4 py-2 text-white deleteCustomAd' rel='{{$customAd->id}}'>{{ __('delete') }}</a>
        </div>
      </div>
    </div>
    <nav class="card-tab mb-4 d-block d-lg-flex align-items-center justify-content-between">
      <div class="nav nav-tabs" id="nav-tab" role="tablist">
        <button class="nav-link active" id="nav-image-tab" data-bs-toggle="tab" data-bs-target="#nav-image" type="button" role="tab" aria-controls="nav-image" aria-selected="false">
          {{ __('image') }}
        </button>
        <button class="nav-link" id="nav-video-tab" data-bs-toggle="tab" data-bs-target="#nav-video" type="button" role="tab" aria-controls="nav-video" aria-selected="false">
          {{ __('video') }}
        </button>
      </div>
      <div class="d-block d-md-flex align-items-center justify-content-between">
        <p class="btn badge badge-secondary text-light px-4 m-0 ms-2 mt-2 mt-md-0">
          <i data-feather="eye"></i>
          <span class="ms-2"> {{ $customAd->views }} Views </span>
        </p>
        <p class="btn badge badge-secondary text-light px-4 mb-0 ms-2 mt-2 mt-md-0">
          <i data-feather="mouse-pointer"></i>
          <span class="ms-2"> {{ $customAd->clicks }} Clicks </span>
        </p>
        <p class="btn badge badge-secondary text-light px-4 mb-0 ms-2 mt-2 mt-md-0">
          <span> CTR : </span>
          <span class="ms-2 fw-bolder fs-6 text-info">{{ number_format(($customAd->clicks / max(1, $customAd->views)) * 100, 2) }}%</span>
        </p>
      </div>
    </nav>
    <div class="tab-content" id="nav-tabContent">
      <div class="tab-pane show active" id="nav-image" role="tabpanel" aria-labelledby="nav-image-tab" tabindex="0">
        <div class="card">
          <div class="card-header d-flex align-items-center justify-content-between">
            <h4 class="fw-normal m-0"> {{ __('imageList') }} </h4>
            <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addCustomAdSourceImageModal">
              {{ __('addImageSource') }}
            </button>
          </div>
          <div class="card-body">
            <table class="table table-striped w-100" id="customAdImageSourceTable">
              <thead>
                <tr>
                  <th> {{ __('image') }}</th>
                  <th class="w-100"> {{ __('description') }}</th>
                  <th> {{ __('showTime') }}</th>
                  <th class="text-end" width="200px"> {{ __('action') }} </th>
                </tr>
              </thead>
            </table>
          </div>
        </div>
      </div>
      <div class="tab-pane" id="nav-video" role="tabpanel" aria-labelledby="nav-video-tab" tabindex="0">
        <div class="card">
          <div class="card-header d-flex align-items-center justify-content-between">
            <h4 class="fw-normal m-0"> {{ __('videoList') }} </h4>
            <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addCustomAdSourceVideoModal">
              {{ __('addVideoSource') }}
            </button>
          </div>
          <div class="card-body">
            <table class="table table-striped w-100" id="customAdVideoSourceTable">
              <thead>
                <tr>
                  <th> {{ __('source') }}</th>
                  <th> {{ __('description') }}</th>
                  <th> {{ __('type') }}</th>
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
<div class="modal fade" id="addCustomAdSourceImageModal" tabindex="-1" aria-labelledby="addCustomAdSourceImageModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addImageSource')}}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addCustomAdSourceImageForm" method="POST">
        <div class="modal-body">
          <input type="hidden" name="custom_ad_id" id="custom_ad_id" value="{{ $customAd->id }}">
          <div class="form-group">
            <label for="content" class="form-label">{{ __('content') }}</label>
            <div class="horizontalPosterImg position-relative">
              <div class="upload-options">
                <label for="poster1">
                  <input name="content" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'content')" id="poster1" required>
                </label>
              </div>
              <img id="content" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
            </div>
          </div>
          <div class="form-group">
            <label for="headline" class="form-label">{{ __('headline')}}</label>
            <input name="headline" id="headline" type="text" class="form-control" required>
          </div>
          <div class="form-group">
            <label for="description" class="form-label">{{ __('description')}}</label>
            <textarea name="description" id="description" class="form-control" required rows="6"></textarea>
          </div>
          <div class="form-group">
            <label for="show_time" class="form-label">{{ __('showTime')}}</label>
            <input name="show_time" id="show_time" type="number" class="form-control" required>
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

<!-- Edit Image Source Modal -->
<div class="modal fade" id="editCustomAdSourceImageModal" tabindex="-1" aria-labelledby="editCustomAdSourceImageModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editSource')}}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editCustomAdSourceImageForm" method="POST">
        <div class="modal-body">
          <input type="hidden" name="custom_ad_source_id" id="custom_ad_source_id" value="">
          <div class="form-group">
            <label for="content" class="form-label">{{ __('content') }}</label>
            <div class="horizontalPosterImg position-relative">
              <div class="upload-options">
                <label for="poster2">
                  <input name="content" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_content')" id="poster2">
                </label>
              </div>
              <img id="edit_content" class="custom_img img-fluid horizontal_poster modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
            </div>
          </div>
          <div class="form-group">
            <label for="headline" class="form-label">{{ __('headline')}}</label>
            <input name="headline" id="edit_headline" type="text" class="form-control" required>
          </div>
          <div class="form-group">
            <label for="description" class="form-label">{{ __('description')}}</label>
            <textarea name="description" id="edit_description" class="form-control" rows="6"></textarea>
          </div>
          <div class="form-group">
            <label for="show_time" class="form-label">{{ __('showTime')}}</label>
            <input name="show_time" id="edit_show_time" type="number" class="form-control" required>
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

<!-- Video Source Modal -->
<div class="modal fade" id="addCustomAdSourceVideoModal" tabindex="-1" aria-labelledby="addCustomAdSourceVideoModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addVideoSource')}}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addCustomAdSourceVideoForm" method="POST">
        <div class="modal-body">
          <input type="hidden" name="custom_ad_id" value="{{ $customAd->id }}">
          <div class="form-group">
            <label for="source_file" class="form-label">{{ __('sourceFile')}}</label>
            <div class="file-input form-control w-100">
              <input name="content" id="video_content" type="file" class="form-control" aria-describedby="sourceFile" accept="video/mp4,video/x-m4v,video/*" required>
              <span class='button'>{{ __('choose')}}</span>
              <span class='label' data-js-label>{{ __('noFileSelected')}}</label>
            </div>
          </div>
          <div class="form-group">
            <label for="headline" class="form-label">{{ __('headline')}}</label>
            <input name="headline" id="headline" type="text" class="form-control" required>
          </div>
          <div class="form-group">
            <label for="description" class="form-label">{{ __('description')}}</label>
            <textarea name="description" id="description" class="form-control" required rows="6"></textarea>
          </div>
          <div class="form-group">
            <label for="show_time" class="form-label">{{ __('type')}}</label>
            <select name="is_skippable" id="is_skippable" class="form-control selectric" required>
              <option selected disabled class="d-none">{{ __('selectType')}}</option>
              <option value="0">{{ __('mustWatch')}}</option>
              <option value="1">{{ __('skippable')}}</option>
            </select>
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

<!-- Edit Video Source Modal -->
<div class="modal fade" id="editCustomAdSourceVideoModal" tabindex="-1" aria-labelledby="editCustomAdSourceVideoModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editVideoSource')}}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editCustomAdSourceVideoForm" method="POST">
        <div class="modal-body">
          <input type="hidden" name="custom_ad_id" id="custom_ad_source_video_id">
          <div class="form-group">
            <label for="source_file" class="form-label">{{ __('sourceFile')}}</label>
            <div class="file-input form-control w-100">
              <input name="content" id="edit_video_content" type="file" class="form-control" aria-describedby="sourceFile" accept="video/mp4,video/x-m4v,video/*">
              <span class='button'>{{ __('choose')}}</span>
              <span class='label' data-js-label>{{ __('changeFile')}}</label>
            </div>
          </div>
          <div class="form-group">
            <label for="headline" class="form-label">{{ __('headline')}}</label>
            <input name="headline" id="edit_headline_video" type="text" class="form-control" required>
          </div>
          <div class="form-group">
            <label for="description" class="form-label">{{ __('description')}}</label>
            <textarea name="description" id="edit_descriptioon_video" class="form-control" required rows="6"></textarea>
          </div>
          <div class="form-group">
            <label for="show_time" class="form-label">{{ __('type')}}</label>
            <select name="is_skippable" id="edit_is_skippable" class="form-control selectric" required>
              <option selected disabled class="d-none">{{ __('selectType')}}</option>
              <option value="0">{{ __('mustWatch')}}</option>
              <option value="1">{{ __('skippable')}}</option>
            </select>
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

<!-- Edit Custom Ad Modal -->
<div class="modal fade" id="editCustomAdModal" tabindex="-1" aria-labelledby="editCustomAdModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editCustomAd') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editCustomAdForm" method="POST">
        <input type="hidden" name="custom_ad_id" id="custom_ad_id">
        <div class="modal-body">
          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label for="edit_title" class="form-label">{{ __('title') }} ({{ __('forReferenceOnly')}})</label>
                <input name="title" type="text" class="form-control" id="edit_title" aria-describedby="edit_title" required>
              </div>
            </div>
            <div class="col-md-6">
              <div class="form-group">
                <label for="edit_brand_name" class="form-label">{{ __('brandName') }}</label>
                <input name="brand_name" type="text" class="form-control" id="edit_brand_name" aria-describedby="edit_title" required>
              </div>
            </div>
            <div class="col-md-2">
              <div class="form-group">
                <label for="edit_brand_logo" class="form-label">{{ __('brandIcon') }}</label>
                <div class="position-relative" style="width: 80px;">
                  <div class="upload-options">
                    <label for="edit_poster1">
                      <input name="brand_logo" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_brand_icon')" id="edit_poster1">
                    </label>
                  </div>
                  <img id="edit_brand_icon" class="custom_img brand_icon img-fluid modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                </div>
              </div>
            </div>
            <div class="col-lg-4">
              <div class="form-group">
                <label for="edit_button_text" class="form-label">{{ __('buttonText') }}</label>
                <input name="button_text" type="text" class="form-control" id="edit_button_text" aria-describedby="edit_button_text" required>
              </div>
            </div>
            <div class="col-lg-3">
              <div class="form-group">
                <label for="edit_start_date" class="form-label">{{ __('startDate') }}</label>
                <input name="start_date" type="date" class="form-control" id="edit_start_date" aria-describedby="edit_start_date" required>
              </div>
            </div>
            <div class="col-lg-3">
              <div class="form-group">
                <label for="edit_end_date" class="form-label">{{ __('endDate') }}</label>
                <input name="end_date" type="date" class="form-control" id="edit_end_date" aria-describedby="edit_end_date" required>
              </div>
            </div>
            <div class="col-md-6">
              <div class="checkbox-slider d-flex align-items-center mb-2">
                <label>
                  <input type="checkbox" id="edit_is_android_checkbox" class="opacity-0 position-absolute">
                  <input type="hidden" id="edit_is_android_hidden" name="is_android" value="0">
                  <span class="toggle_background">
                    <div class="circle-icon"></div>
                    <div class="vertical_line"></div>
                  </span>
                </label>
                <label class="form-check-label ms-2" for="edit_is_android_checkbox">{{ __('forAndroid') }}</label>
              </div>
              <div class="form-group">
                <label for="edit_android_link" class="form-label">{{ __('androidLink') }}</label>
                <input name="android_link" type="text" class="form-control" id="edit_android_link" aria-describedby="edit_android_link" required disabled>
              </div>
            </div>
            <div class="col-md-6">
              <div class="checkbox-slider d-flex align-items-center mb-2">
                <label>
                  <input type="checkbox" id="edit_is_ios_checkbox" class="opacity-0 position-absolute">
                  <input type="hidden" id="edit_is_ios_hidden" name="is_ios" value="0">
                  <span class="toggle_background">
                    <div class="circle-icon"></div>
                    <div class="vertical_line"></div>
                  </span>
                </label>
                <label class="form-check-label ms-2" for="edit_is_ios_checkbox">{{ __('foriOS') }}</label>
              </div>
              <div class="form-group">
                <label for="edit_ios_link" class="form-label">{{ __('iOSLink') }}</label>
                <input name="ios_link" type="text" class="form-control" id="edit_ios_link" aria-describedby="edit_ios_link" required disabled>
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