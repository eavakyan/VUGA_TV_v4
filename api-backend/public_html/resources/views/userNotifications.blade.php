@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/userNotification.js') }}"></script>
@endsection

@section('content')

<section class="section">
    <div class="card">
        <div class="card-header">
            <div class="page-title w-100">
                <div class="d-flex align-items-center justify-content-between">
                    <h4 class="mb-0 fw-semibold">{{ __('User Notifications') }} <small class="text-muted">(One-time Messages)</small></h4>
                    <button type="button" class="btn theme-bg theme-btn text-white" data-bs-toggle="modal" data-bs-target="#addUserNotificationModal">
                        {{ __('Create New Notification') }}
                    </button>
                </div>
            </div>
        </div>
        <div class="card-body">
            <table class="table table-striped w-100" id="userNotificationTable">
                <thead>
                    <tr>
                        <th>{{ __('Title') }}</th>
                        <th>{{ __('Message') }}</th>
                        <th>{{ __('Type') }}</th>
                        <th>{{ __('Priority') }}</th>
                        <th>{{ __('Platforms') }}</th>
                        <th>{{ __('Status') }}</th>
                        <th>{{ __('Analytics') }}</th>
                        <th width="250px" style="text-align: right">{{ __('Actions') }}</th>
                    </tr>
                </thead>
            </table>
        </div>
    </div>
</section>

<!-- Add User Notification Modal -->
<div class="modal fade" id="addUserNotificationModal" tabindex="-1" aria-labelledby="addModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5 fw-semibold" id="addModalLabel">{{ __('Create User Notification') }}</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addUserNotificationForm" method="post">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group mb-3">
                                <label for="title" class="form-label">{{ __('Title') }} <span class="text-danger">*</span></label>
                                <input name="title" type="text" class="form-control" id="title" maxlength="255" required>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group mb-3">
                                <label for="message" class="form-label">{{ __('Message') }} <span class="text-danger">*</span></label>
                                <textarea name="message" class="form-control" id="message" rows="4" required></textarea>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="notification_type" class="form-label">{{ __('Notification Type') }}</label>
                                <select name="notification_type" class="form-control" id="notification_type">
                                    <option value="system">System</option>
                                    <option value="promotional">Promotional</option>
                                    <option value="update">Update</option>
                                    <option value="maintenance">Maintenance</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="priority" class="form-label">{{ __('Priority') }}</label>
                                <select name="priority" class="form-control" id="priority">
                                    <option value="low">Low</option>
                                    <option value="medium" selected>Medium</option>
                                    <option value="high">High</option>
                                    <option value="urgent">Urgent</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group mb-3">
                                <label class="form-label">{{ __('Target Platforms') }}</label>
                                <div class="d-flex gap-3">
                                    <div class="form-check">
                                        <input class="form-check-input platform-check" type="checkbox" value="all" id="platform_all" checked>
                                        <label class="form-check-label" for="platform_all">All Platforms</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input platform-check" type="checkbox" value="ios" id="platform_ios">
                                        <label class="form-check-label" for="platform_ios">iOS</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input platform-check" type="checkbox" value="android" id="platform_android">
                                        <label class="form-check-label" for="platform_android">Android</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input platform-check" type="checkbox" value="android_tv" id="platform_android_tv">
                                        <label class="form-check-label" for="platform_android_tv">Android TV</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="scheduled_at" class="form-label">{{ __('Schedule For') }} <small class="text-muted">(Optional)</small></label>
                                <input name="scheduled_at" type="datetime-local" class="form-control" id="scheduled_at">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="expires_at" class="form-label">{{ __('Expires At') }} <small class="text-muted">(Optional)</small></label>
                                <input name="expires_at" type="datetime-local" class="form-control" id="expires_at">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('Cancel') }}</button>
                    <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('Create Notification') }}</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Edit User Notification Modal -->
<div class="modal fade" id="editUserNotificationModal" tabindex="-1" aria-labelledby="editModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5 fw-semibold" id="editModalLabel">{{ __('Edit User Notification') }}</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editUserNotificationForm" method="post">
                <input type="hidden" id="edit_notification_id">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group mb-3">
                                <label for="edit_title" class="form-label">{{ __('Title') }} <span class="text-danger">*</span></label>
                                <input name="title" type="text" class="form-control" id="edit_title" maxlength="255" required>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group mb-3">
                                <label for="edit_message" class="form-label">{{ __('Message') }} <span class="text-danger">*</span></label>
                                <textarea name="message" class="form-control" id="edit_message" rows="4" required></textarea>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="edit_notification_type" class="form-label">{{ __('Notification Type') }}</label>
                                <select name="notification_type" class="form-control" id="edit_notification_type">
                                    <option value="system">System</option>
                                    <option value="promotional">Promotional</option>
                                    <option value="update">Update</option>
                                    <option value="maintenance">Maintenance</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="edit_priority" class="form-label">{{ __('Priority') }}</label>
                                <select name="priority" class="form-control" id="edit_priority">
                                    <option value="low">Low</option>
                                    <option value="medium">Medium</option>
                                    <option value="high">High</option>
                                    <option value="urgent">Urgent</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group mb-3">
                                <label class="form-label">{{ __('Target Platforms') }}</label>
                                <div class="d-flex gap-3">
                                    <div class="form-check">
                                        <input class="form-check-input edit-platform-check" type="checkbox" value="all" id="edit_platform_all">
                                        <label class="form-check-label" for="edit_platform_all">All Platforms</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input edit-platform-check" type="checkbox" value="ios" id="edit_platform_ios">
                                        <label class="form-check-label" for="edit_platform_ios">iOS</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input edit-platform-check" type="checkbox" value="android" id="edit_platform_android">
                                        <label class="form-check-label" for="edit_platform_android">Android</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input edit-platform-check" type="checkbox" value="android_tv" id="edit_platform_android_tv">
                                        <label class="form-check-label" for="edit_platform_android_tv">Android TV</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="edit_scheduled_at" class="form-label">{{ __('Schedule For') }} <small class="text-muted">(Optional)</small></label>
                                <input name="scheduled_at" type="datetime-local" class="form-control" id="edit_scheduled_at">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group mb-3">
                                <label for="edit_expires_at" class="form-label">{{ __('Expires At') }} <small class="text-muted">(Optional)</small></label>
                                <input name="expires_at" type="datetime-local" class="form-control" id="edit_expires_at">
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group mb-3">
                                <label for="edit_is_active" class="form-label">{{ __('Status') }}</label>
                                <select name="is_active" class="form-control" id="edit_is_active">
                                    <option value="1">Active</option>
                                    <option value="0">Inactive</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('Cancel') }}</button>
                    <button type="submit" class="btn theme-btn text-light px-4 saveButton">{{ __('Update Notification') }}</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Analytics Modal -->
<div class="modal fade" id="analyticsModal" tabindex="-1" aria-labelledby="analyticsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5 fw-semibold" id="analyticsModalLabel">{{ __('Notification Analytics') }}</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div id="analyticsContent">
                    <div class="text-center">
                        <div class="spinner-border" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn text-light" data-bs-dismiss="modal">{{ __('Close') }}</button>
            </div>
        </div>
    </div>
</div>

@endsection