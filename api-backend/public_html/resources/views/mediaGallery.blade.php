@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/mediaGallery.js') }}"></script>
@endsection

@section('content')
<section class="section">
    <div class="card">
        <div class="card-header">
            <div class="page-title w-100">
                <div class="d-flex align-items-center justify-content-between">
                    <h4 class="mb-0 fw-semibold">{{ __('mediaGallery') }}</h4>
                    <button type="button" class="btn btn-primary text-light px-4" data-bs-toggle="modal" data-bs-target="#addMediaModal">
                        {{ __('addMedia') }}
                    </button>
                </div>
            </div>
        </div>
        <div class="card-body">
            <table class="table table-striped w-100" id="mediaGalleryTable">
                <thead>
                    <tr>
                        <th width="80px"> {{ __('source') }}</th>
                        <th width="100%"> {{ __('title') }}</th>
                        <th class="text-end" width="200px"> {{ __('action') }} </th>
                    </tr>
                </thead>
            </table>
        </div>
    </div>
</section>

<!-- Add Media Modal -->
<div class="modal fade" id="addMediaModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="addMediaModalLabel" aria-hidden="true">
    <div class="modal-dialog  modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('addMedia') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addMediaForm" method="POST">
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group">
                            <label for="title" class="form-label">{{ __('title') }}</label>
                            <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
                        </div>
                        <div class="form-group sourceMediaFile">
                            <label for="source_file" class="form-label">{{ __('sourceFile')}}</label>
                            <div class="file-input form-control w-100">
                                <input name="file" id="file" type="file" class="form-control" aria-describedby="sourceFile" accept="video/*,video/x-matroska" required>
                                <span class='button'>{{ __('choose')}}</span>
                                <span class='label' data-js-label>{{ __('noFileSelected')}}</label>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="progress mb-2" id="progress" style="height: 25px; display: none;">
                                <div class="progress-bar" id="progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">0%</div>
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

<!-- Edit Media Modal -->
<div class="modal fade" id="editMediaModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="editMediaModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold mb-0 text-white">{{ __('editMedia') }}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editMediaForm" method="POST">
                <input type="hidden" name="media_id" id="media_id">
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group">
                            <label for="title" class="form-label">{{ __('title') }}</label>
                            <input name="title" type="text" class="form-control" id="edit_title" aria-describedby="title" required>
                        </div>
                        <div class="form-group sourceMediaFile">
                            <label for="source_file" class="form-label">{{ __('sourceFile')}}</label>
                            <div class="file-input form-control w-100">
                                <input name="file" id="edit_file" type="file" class="form-control" aria-describedby="sourceFile" accept="video/*">
                                <span class='button'>{{ __('choose')}}</span>
                                <span class='label' data-js-label>{{ __('changeFile')}}</label>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="progress mb-2" id="edit-progress" style="height: 25px; display: none;">
                                <div class="progress-bar" id="edit-progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">0%</div>
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