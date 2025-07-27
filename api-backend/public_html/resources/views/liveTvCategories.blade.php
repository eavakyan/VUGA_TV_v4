@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/tv_category.js') }}"></script>
@endsection
@section('content')



<section class="section">
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-block d-sm-flex align-items-center justify-content-between">
          <h4 class="mb-0 fw-semibold">{{ __('liveTvCategories') }} </h4>
          <button type="button" class="btn btn-primary text-light px-4 mt-2 mt-sm-0" data-bs-toggle="modal" data-bs-target="#addTvCategoryModal">
            {{ __('addTvCategory') }}
          </button>
        </div>
      </div>
    </div>
    <div class="card-body">
      <table class="table table-striped w-100" id="tvCategoryTable">
        <thead>
          <tr>
            <th width="200px"> {{ __('image') }}</th>
            <th class="text-end" width="200px"> {{ __('action') }} </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</section>



<!-- Add Tv Category Modal -->
<div class="modal fade" id="addTvCategoryModal" tabindex="-1" aria-labelledby="addTvCategoryModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addTvCategory') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addTvCategoryForm" method="POST">
        <div class="modal-body">
          <div class="row">
            <div class="form-group">
              <label for="title" class="form-label">{{ __('title') }}</label>
              <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
            </div>
            <div class="form-group m-0">
              <label for="image" class="form-label">{{ __('image') }}</label>
              <div class="position-relative" style="width: 150px;">
                <div class="upload-options">
                  <label for="poster">
                    <input name="image" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'image')" id="poster" required>
                  </label>
                </div>
                <img id="image" class="custom_img img-fluid modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
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


<!-- Edit Tv Category Modal -->
<div class="modal fade" id="editTvCategoryModal" tabindex="-1" aria-labelledby="editTvCategoryModal" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editTvCategory') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editTvCategoryForm" method="POST">
        <input type="hidden" id="tv_category_id">
        <div class="modal-body">
          <div class="row">
            <div class="form-group">
              <label for="edit_title" class="form-label">{{ __('title') }}</label>
              <input name="title" type="text" class="form-control" id="edit_title" aria-describedby="edit_title" required>
            </div>
            <div class="form-group m-0">
              <label for="image" class="form-label">{{ __('image') }}</label>
              <div class="position-relative" style="width: 150px;">
                <div class="upload-options">
                  <label for="image1">
                    <input name="image" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_image')" id="image1">
                  </label>
                </div>
                <img id="edit_image" class="custom_img img-fluid modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
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