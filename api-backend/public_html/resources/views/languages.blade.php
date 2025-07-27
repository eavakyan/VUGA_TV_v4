@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/language.js') }}"></script>
@endsection

@section('content')
<section class="section">
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="mb-0 fw-semibold">{{ __('languages') }}</h4>
          <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addLanguageModal">
            {{ __('addLanguage') }}
          </button>
        </div>
      </div>
    </div>
    <div class="card-body">
      <table class="table table-striped w-100" id="languagesTable">
        <thead>
          <tr>
            <th> {{ __('title') }}</th>
            <th> {{ __('code') }}</th>
            <th class="text-end" width="200px"> {{ __('action') }} </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</section>

<!-- Add Language Modal -->
<div class="modal fade" id="addLanguageModal" tabindex="-1" aria-labelledby="addLanguageModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addLanguage') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addLanguageForm" method="POST">
        <div class="modal-body">
          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label for="title" class="form-label">{{ __('title') }}</label>
                <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
              </div>
            </div>
            <div class="col-md-6">
              <div class="form-group">
                <label for="code" class="form-label">{{ __('code') }} (hi, en)</label>
                <input name="code" type="text" class="form-control" id="code" aria-describedby="code" required>
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

<!-- Edit Language Modal -->
<div class="modal fade" id="editLanguageModal" tabindex="-1" aria-labelledby="editLanguageModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editLanguage') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editLanguageForm" method="POST">
        <input type="hidden" name="Language_id" id="language_id">
        <div class="modal-body">
          <div class="row">
            <div class="col-md-6">
              <div class="form-group">
                <label for="edit_title" class="form-label">{{ __('title') }}</label>
                <input name="title" id="edit_title" type="text" class="form-control" aria-describedby="title" required>
              </div>
            </div>
            <div class="col-md-6">
              <div class="form-group">
                <label for="code" class="form-label">{{ __('code') }} (hi, en)</label>
                <input name="code" type="text" class="form-control" id="edit_code" aria-describedby="code" required>
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

@endsection