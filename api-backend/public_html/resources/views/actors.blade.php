@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/actors.js') }}"></script>

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
<section class="section">
  <p class="text-end text-danger" id="TMDB_Note">
    *If you want to use TMDB Service Then add <b> TMDB_API_KEY </b> in <b> .env </b> file
  </p>
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-block d-md-flex align-items-center justify-content-between">
          <div class="d-flex align-items-center justify-content-between">
            <h4 class="mb-0 fw-semibold">{{ __('actors') }}</h4>
          </div>
          <div class="">
            <button type="button" class="btn btn-primary text-light px-4 me-2 my-2 my-md-0" data-bs-toggle="modal" data-bs-target="#addActorModal">
              {{ __('addActor') }}
            </button>
            <a href="{{ route('fetchActorFromTMDB') }}" id="TMDB_Button" class="btn btn-tmdb shadow-none text-light px-4 ">
              {{ __('fetchActorFromTMDB') }}
            </a>
          </div>
        </div>
      </div>
    </div>
    <div class="card-body">
      <table class="table table-striped w-100" id="actorsTable">
        <thead>
          <tr>
            <th style="width: 150px"> {{ __('profile') }}</th>
            <th> {{ __('dob') }}</th>
            <th class="text-end" width="200px"> {{ __('action') }} </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</section>

<!-- Add Actor Modal -->
<div class="modal fade" id="addActorModal" tabindex="-1" aria-labelledby="addActorModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addNewActor') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="addNewActorForm" method="POST">
        <div class="modal-body">
          <div class="row">
            <div class="d-flex">
              <div class="form-group m-0">
                <label for="profile_image" class="form-label">{{ __('actorProfile') }}</label>
                <div class="position-relative" style="width: 150px;">
                  <div class="upload-options">
                    <label for="poster">
                      <input name="profile_image" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'actor_profile')" id="poster" required>
                    </label>
                  </div>
                  <img id="actor_profile" class="custom_img img-fluid actor_profile modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                </div>
              </div>
              <div class="w-100 ms-4">
                <div class="form-group">
                  <label for="fullname" class="form-label">{{ __('fullname') }}</label>
                  <input name="fullname" type="text" class="form-control" id="fullname" aria-describedby="fullname" required>
                </div>
                <div class="form-group">
                  <label for="dob" class="form-label">{{ __('dob') }} (DD-MM-YYYY)</label>
                  <input name="dob" type="text" class="form-control datePicker" id="dob" aria-describedby="dob" required>
                </div>
              </div>
            </div>
            <div class="form-group m-0">
              <label for="bio" class="form-label">{{ __('bio') }}</label>
              <textarea name="bio" id="bio" rows="8" class="form-control"></textarea>
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

<!-- Edit Actor Modal -->
<div class="modal fade" id="editActorModal" tabindex="-1" aria-labelledby="editActorModalLabel" aria-hidden="true">
  <div class="modal-dialog  modal-md">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editActor') }}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form id="editActorForm" method="POST">
        <input type="hidden" id="actor_id">
        <div class="modal-body">
          <div class="row">
            <div class="d-flex">
              <div class="form-group m-0">
                <label for="profile_image" class="form-label">{{ __('actorProfile') }}</label>
                <div class="position-relative" style="width: 150px;">
                  <div class="upload-options">
                    <label for="poster">
                      <input name="profile_image" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'edit_profile_image')" id="poster1">
                    </label>
                  </div>
                  <img id="edit_profile_image" class="custom_img img-fluid actor_profile modal_placeholder_image " src="{{ asset('assets/img/placeholder-image.png')}}">
                </div>
              </div>
              <div class="w-100 ms-4">
                <div class="form-group">
                  <label for="edit_fullname" class="form-label">{{ __('fullname') }}</label>
                  <input name="fullname" type="text" class="form-control" id="edit_fullname" aria-describedby="fullname" required>
                </div>
                <div class="form-group">
                  <label for="edit_dob" class="form-label">{{ __('dob') }} (DD-MM-YYYY)</label>
                  <input name="dob" type="text" class="form-control datePicker" id="edit_dob" aria-describedby="dob" required>
                </div>
              </div>
            </div>
            <div class="form-group m-0">
              <label for="edit_bio" class="form-label">{{ __('bio') }}</label>
              <textarea name="bio" id="edit_bio" rows="8" class="form-control" required></textarea>
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