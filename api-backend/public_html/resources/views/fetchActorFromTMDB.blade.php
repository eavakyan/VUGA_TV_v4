@extends('include.app')
@section('script')
<script>
    const localization = {
        importData: "{{ __('importData') }}",
        profile: "{{ __('profile') }}",
        name: "{{ __('name') }}",
    };
</script>
<script src="{{ asset('assets/script/actors.js') }}"></script>
@endsection

@section('content')
<section class="section">
    <div class="card">
        <div class="card-header">
            <div class="page-title w-100">
                <div class="d-flex align-items-center justify-content-between">
                    <h4 class="mb-0 fw-semibold">{{ __('fetchActorFromTMDB') }}</h4>
                </div>
            </div>
        </div>
        <div class="card-body pb-0">
            <form id="searchActorTMDBForm" method="post">
                <div class="row justify-content-center">
                    <div class="col-xl-4 col-lg-4 col-md-4 col-sm-12">
                        <div id="parameters" class="searchInput">
                            <div class="form-group">
                                <input type="text" class="select-selected form-control" id="actorQuery" name="query" maxlength="64" placeholder="{{ __('searchActor') }}" required>
                            </div>
                            <button type="submit" id="add_parameters" class="btn btn-primary px-3 searchIcon">
                                <i data-feather="search"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </form>
            <div class="table-loader-bg" style="display:none;">
                <div class="table-loader"></div>
            </div>
            <table class="table table-striped w-100" id="resultOfActorTable">
                <thead>
                    <tr>
                        <th width="200px"> {{ __('profile') }}</th>
                        <th class="text-end" width="250px"> {{ __('action') }} </th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>

            <ul class="pagination-actor pagination-controls">
                <li class="paginate_button page-item previous">
                    <a href="#" id="prev-page-actor" disabled class="pagination-ui">
                        <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1">
                            <polyline points="15 18 9 12 15 6"></polyline>
                        </svg>
                    </a>
                </li>
                <li id="actor-page-buttons"></li>
                <li class="paginate_button page-item next">
                    <a href="#" id="next-page-actor" disabled class="pagination-ui">
                        <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1">
                            <polyline points="9 18 15 12 9 6"></polyline>
                        </svg>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</section>

<!-- Add Actor Modal -->
<div class="modal fade" id="addActorModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="addActorModalLabel" aria-hidden="true">
    <div class="modal-dialog  modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addNewActor') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addNewActorFormTMDB" method="POST">
                <div class="modal-body">
                    <div class="row">
                        <div class="d-flex">
                            <input type="hidden" id="profile_path_url" name="profile_path_url">
                            <div class="form-group m-0">
                                <label for="profile_image" class="form-label">{{ __('actorProfile') }}</label>
                                <div class="position-relative" style="width: 150px;">
                                    <div class="upload-options">
                                        <label for="poster">
                                            <input name="profile_path_url" type="file" accept=".png, .jpg, .jpeg, .webp" onchange="loadFile(event, 'set_actor_profile')" id="poster">
                                        </label>
                                    </div>
                                    <img id="set_actor_profile" class="custom_img img-fluid actor_profile modal_placeholder_image" src="{{ asset('assets/img/placeholder-image.png')}}">
                                </div>
                            </div>
                            <div class="w-100 ms-4">
                                <div class="form-group">
                                    <label for="fullname" class="form-label">{{ __('fullname') }}</label>
                                    <input name="fullname" type="text" class="form-control" id="set_fullname" aria-describedby="fullname" required>
                                </div>
                                <div class="form-group">
                                    <label for="dob" class="form-label">{{ __('dob') }} (DD-MM-YYYY)</label>
                                    <input name="dob" type="text" class="form-control datePicker" id="set_dob" aria-describedby="dob" required>
                                </div>
                            </div>
                        </div>
                        <div class="form-group m-0">
                            <label for="bio" class="form-label">{{ __('bio') }}</label>
                            <textarea name="bio" id="set_bio" rows="8" class="form-control"></textarea>
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