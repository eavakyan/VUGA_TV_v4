@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/notification.js') }}"></script>
@endsection

@section('content')

<section class="section">
    <div class="card">
        <div class="card-header">
            <div class="page-title w-100">
                <div class="d-flex align-items-center justify-content-between">
                    <h4 class="mb-0 fw-semibold"> {{ __('notifications') }} </h4>
                    <button type="button" class="btn theme-bg theme-btn text-white" data-bs-toggle="modal" data-bs-target="#addNotificationModal">
                        {{ __('addNotification') }}
                    </button>
                </div>
            </div>
        </div>
        <div class="card-body">
            <table class="table table-striped w-100" id="notificationTable">
                <thead>
                    <tr>
                        <th>{{ __('title') }}</th>
                        <th>{{ __('description') }}</th>
                        <th width="250px" style="text-align: right"> {{ __('action')}} </th>
                    </tr>
                </thead>
            </table>
        </div>
    </div>
</section>

<!-- Notification Modal -->
<div class="modal fade" id="addNotificationModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5 fw-semibold" id="exampleModalLabel"> {{ __('addNotification')}} </h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addNotificationForm" method="post">
                <div class="modal-body">
                    <div class="form-group">
                        <label for="title" class="form-label">{{ __('title') }}</label>
                        <input name="title" type="text" class="form-control" id="title" aria-describedby="title" required>
                    </div>
                    <div class="form-group">
                        <label for="title" class="form-label"> {{ __('description') }}</label>
                        <textarea type="text" name="description" class="form-control" rows="5" required></textarea>
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

<!-- Notification Modal -->
<div class="modal fade" id="editNotificationModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5 fw-semibold" id="exampleModalLabel"> {{ __('editNotification')}} </h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editNotificationForm" method="post">
                <input type="hidden" id="notification_id">
                <div class="modal-body">
                    <div class="form-group">
                        <label for="title" class="form-label">{{ __('title') }}</label>
                        <input name="title" type="text" class="form-control" id="edit_title" aria-describedby="title" required>
                    </div>
                    <div class="form-group">
                        <label for="title" class="form-label"> {{ __('description') }}</label>
                        <textarea type="text" name="description" class="form-control" id="edit_description" rows="5" required></textarea>
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