@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/genre.js') }}"></script>
@endsection

@section('content')
<section class="section">
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="mb-0 fw-semibold">{{ __('genres') }}</h4>
          <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addGenreModal">
            {{ __('addGenre') }}
          </button>
        </div>
      </div>
    </div>
    <div class="card-body">
      <table class="table table-striped w-100" id="genresTable">
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

<!-- Add Genre Modal -->
<div class="modal fade" id="addGenreModal" tabindex="-1" aria-labelledby="addGenreModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addGenre') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addGenreForm" method="POST">
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

<!-- Edit Genre Modal -->
<div class="modal fade" id="editGenreModal" tabindex="-1" aria-labelledby="editGenreModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editGenre') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editGenreForm" method="POST">
        <input type="hidden" name="genre_id" id="genre_id">
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