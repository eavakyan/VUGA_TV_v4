@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/customAd.js') }}"></script>
<script>
  const startDateInput = document.getElementById("start_date");
  const endDateInput = document.getElementById("end_date");

  // Get today's date
  const today = new Date().toISOString().split("T")[0];

  // Set the minimum date for both inputs to today
  startDateInput.setAttribute("min", today);
  endDateInput.setAttribute("min", today);

  // Disable end date picker initially
  endDateInput.disabled = true;

  // Function to enable end date picker when start date is selected
  startDateInput.addEventListener("input", function() {
    endDateInput.removeAttribute("disabled");
    endDateInput.setAttribute("min", startDateInput.value);
  });



  const androidCheckbox = document.getElementById("is_android_checkbox");
  const androidLinkInput = document.getElementById("android_link");
  const androidHiddenInput = document.getElementById("is_android_hidden");

  const iosCheckbox = document.getElementById("is_ios_checkbox");
  const iosLinkInput = document.getElementById("ios_link");
  const iosHiddenInput = document.getElementById("is_ios_hidden");

  function toggleInputState(checkbox, input, hiddenInput) {
    input.disabled = !checkbox.checked;
    hiddenInput.value = checkbox.checked ? "1" : "0";
  }

  androidCheckbox.addEventListener("change", function() {
    toggleInputState(
      androidCheckbox,
      androidLinkInput,
      androidHiddenInput
    );
  });

  iosCheckbox.addEventListener("change", function() {
    toggleInputState(iosCheckbox, iosLinkInput, iosHiddenInput);
  });



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
<section class="section">
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="mb-0 fw-semibold">{{ __('customAds') }}</h4>
          <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addCustomAdModal">
            {{ __('addCustomAd') }}

          </button>
        </div>
      </div>
    </div>

    <div class="card-body">
      <table class="table table-striped w-100" id="customAdTable">
        <thead>
          <tr>
            <th width="150px"> {{ __('title') }}</th>
            <th> {{ __('brandName') }}</th>
            <th> {{ __('plateform') }}</th>
            <th> {{ __('views') }}</th>
            <th> {{ __('clicks') }}</th>
            <th> {{ __('startDate') }}</th>
            <th> {{ __('endDate') }}</th>
            <th> {{ __('on/Off') }}</th>
            <th class="text-end" width="200px"> {{ __('action') }} </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</section>

<!-- Add Custom Ad Modal -->
<div class="modal fade" id="addCustomAdModal" tabindex="-1" aria-labelledby="addCustomAdModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addCustomAd') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addCustomAdForm" method="POST">
        <div class="modal-body">
          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label for="title" class="form-label">{{ __('title') }} ({{ __('forReferenceOnly')}})</label>
                <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
              </div>
            </div>
            <div class="col-md-6">
              <div class="form-group">
                <label for="brand_name" class="form-label">{{ __('brandName') }}</label>
                <input name="brand_name" type="text" class="form-control" id="brand_name" aria-describedby="title" required>
              </div>
            </div>
            <div class="col-md-2">
              <div class="form-group">
                <label for="brand_logo" class="form-label">{{ __('brandIcon') }}</label>
                <div class="position-relative" style="width: 80px;">
                  <div class="upload-options">
                    <label for="poster1">
                      <input name="brand_logo" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'brand_icon')" id="poster1" required>
                    </label>
                  </div>
                  <img id="brand_icon" class="custom_img brand_icon img-fluid modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                </div>
              </div>
            </div>
            <div class="col-lg-4">
              <div class="form-group">
                <label for="button_text" class="form-label">{{ __('buttonText') }}</label>
                <input name="button_text" type="text" class="form-control" id="button_text" aria-describedby="button_text" required>
              </div>
            </div>
            <div class="col-lg-3">
              <div class="form-group">
                <label for="start_date" class="form-label">{{ __('startDate') }}</label>
                <input name="start_date" type="date" class="form-control" id="start_date" aria-describedby="start_date" required>
              </div>
            </div>
            <div class="col-lg-3">
              <div class="form-group">
                <label for="end_date" class="form-label">{{ __('endDate') }}</label>
                <input name="end_date" type="date" class="form-control" id="end_date" aria-describedby="end_date" required>
              </div>
            </div>
            <div class="col-md-6">
              <div class="checkbox-slider d-flex align-items-center mb-2">
                <label>
                  <input type="checkbox" id="is_android_checkbox" class="opacity-0 position-absolute">
                  <input type="hidden" id="is_android_hidden" name="is_android" value="0">
                  <span class="toggle_background">
                    <div class="circle-icon"></div>
                    <div class="vertical_line"></div>
                  </span>
                </label>
                <label class="form-check-label ms-2" for="is_android_checkbox">{{ __('forAndroid') }}</label>
              </div>
              <div class="form-group">
                <label for="android_link" class="form-label">{{ __('androidLink') }}</label>
                <input name="android_link" type="text" class="form-control" id="android_link" aria-describedby="android_link" required disabled>
              </div>
            </div>
            <div class="col-md-6">
              <div class="checkbox-slider d-flex align-items-center mb-2">
                <label>
                  <input type="checkbox" id="is_ios_checkbox" class="opacity-0 position-absolute">
                  <input type="hidden" id="is_ios_hidden" name="is_ios" value="0">
                  <span class="toggle_background">
                    <div class="circle-icon"></div>
                    <div class="vertical_line"></div>
                  </span>
                </label>
                <label class="form-check-label ms-2" for="is_ios_checkbox">{{ __('foriOS') }}</label>
              </div>
              <div class="form-group">
                <label for="ios_link" class="form-label">{{ __('iOSLink') }}</label>
                <input name="ios_link" type="text" class="form-control" id="ios_link" aria-describedby="ios_link" required disabled>
              </div>
            </div>
          </div>
          <ul>
            <li>* The ads will be OFF by default. You need to turn it ON after creating.</li>
            <li>* Ads can be turned ON only once it has minimum 1 Image/Video resource.</li>
          </ul>
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
          <ul>
            <li>* The ads will be OFF by default. You need to turn it ON after creating.</li>
            <li>* Ads can be turned ON only once it has minimum 1 Image/Video resource.</li>
          </ul>
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