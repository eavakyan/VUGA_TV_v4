@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/category.js') }}"></script>
@endsection

@section('content')
<section class="section">
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="mb-0 fw-semibold">{{ __('categories') }}</h4>
          <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addCategoryModal">
            {{ __('addCategory') }}
          </button>
        </div>
      </div>
    </div>
    <div class="card-body">
      <table class="table table-striped w-100" id="categoriesTable">
        <thead>
          <tr>
            <th> {{ __('title') }}</th>
            <th class="text-end" width="200px"> {{ __('action') }} </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</section>

<!-- Add Category Modal -->
<div class="modal fade" id="addCategoryModal" tabindex="-1" aria-labelledby="addCategoryModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addCategory') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addCategoryForm" method="POST">
        <div class="modal-body">
          <div class="row">
            <div class="form-group">
              <label for="title" class="form-label">{{ __('title') }}</label>
              <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
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

<!-- Edit Category Modal -->
<div class="modal fade" id="editCategoryModal" tabindex="-1" aria-labelledby="editCategoryModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editCategory') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editCategoryForm" method="POST">
        <input type="hidden" name="category_id" id="category_id">
        <div class="modal-body">
          <div class="row">
            <div class="form-group">
              <label for="edit_title" class="form-label">{{ __('title') }}</label>
              <input name="title" id="edit_title" type="text" class="form-control" aria-describedby="title" required>
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

@endsection