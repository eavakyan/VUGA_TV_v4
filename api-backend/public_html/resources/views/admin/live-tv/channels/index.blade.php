@extends('include.app')

@section('content')
<section class="section">
    <div class="section-body">
        <div class="d-flex justify-content-between">
            <div class="section-title">
                <h3>{{ __('Live TV Channels Management') }}</h3>
            </div>
            <div class="section-buttons">
                <button class="btn btn-primary" onclick="showAddChannelModal()">
                    <i class="fas fa-plus"></i> Add New Channel
                </button>
                <button class="btn btn-info" onclick="bulkImportModal()">
                    <i class="fas fa-upload"></i> Bulk Import
                </button>
                <button class="btn btn-success" onclick="exportChannels()">
                    <i class="fas fa-download"></i> Export
                </button>
            </div>
        </div>

        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <div class="row w-100">
                            <div class="col-md-3">
                                <select class="form-control" id="categoryFilter">
                                    <option value="all">All Categories</option>
                                    @foreach($categories as $category)
                                        <option value="{{ $category->tv_category_id }}">{{ $category->title }}</option>
                                    @endforeach
                                </select>
                            </div>
                            <div class="col-md-3">
                                <select class="form-control" id="statusFilter">
                                    <option value="all">All Status</option>
                                    <option value="active">Active</option>
                                    <option value="inactive">Inactive</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <div class="btn-group float-right">
                                    <button class="btn btn-sm btn-success" id="bulkActivate" style="display: none;">
                                        <i class="fas fa-eye"></i> Activate Selected
                                    </button>
                                    <button class="btn btn-sm btn-warning" id="bulkDeactivate" style="display: none;">
                                        <i class="fas fa-eye-slash"></i> Deactivate Selected
                                    </button>
                                    <button class="btn btn-sm btn-danger" id="bulkDelete" style="display: none;">
                                        <i class="fas fa-trash"></i> Delete Selected
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-striped" id="channelsTable">
                                <thead>
                                    <tr>
                                        <th>
                                            <input type="checkbox" id="selectAll">
                                        </th>
                                        <th>Thumbnail</th>
                                        <th>Channel #</th>
                                        <th>Title</th>
                                        <th>Categories</th>
                                        <th>Status</th>
                                        <th>Views</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Add/Edit Channel Modal -->
<div class="modal fade" id="channelModal" tabindex="-1" role="dialog" aria-labelledby="channelModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="channelModalLabel">Add New Channel</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <form id="channelForm">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="title">Channel Title <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="title" name="title" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="channel_number">Channel Number</label>
                                <input type="number" class="form-control" id="channel_number" name="channel_number">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label for="stream_url">Stream URL <span class="text-danger">*</span></label>
                                <input type="url" class="form-control" id="stream_url" name="stream_url" required>
                                <small class="form-text text-muted">HLS (.m3u8) or DASH streams supported</small>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="thumbnail">Thumbnail URL</label>
                                <input type="url" class="form-control" id="thumbnail" name="thumbnail">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="logo_url">Logo URL</label>
                                <input type="url" class="form-control" id="logo_url" name="logo_url">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="language">Language</label>
                                <input type="text" class="form-control" id="language" name="language" placeholder="e.g., en, es, fr">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="country_code">Country Code</label>
                                <input type="text" class="form-control" id="country_code" name="country_code" placeholder="e.g., US, UK, CA">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label for="category_ids">Categories</label>
                                <select class="form-control selectric" id="category_ids" name="category_ids[]" multiple>
                                    @foreach($categories as $category)
                                        <option value="{{ $category->tv_category_id }}">{{ $category->title }}</option>
                                    @endforeach
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label for="description">Description</label>
                                <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label for="epg_url">EPG URL (Electronic Program Guide)</label>
                                <input type="url" class="form-control" id="epg_url" name="epg_url">
                                <small class="form-text text-muted">Optional URL for automatic program schedule import</small>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="access_type">Access Type</label>
                                <select class="form-control" id="access_type" name="access_type">
                                    <option value="1">Free</option>
                                    <option value="2">Premium</option>
                                    <option value="3">Subscription Required</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group mt-4">
                                <div class="form-check">
                                    <input type="checkbox" class="form-check-input" id="is_active" name="is_active" checked>
                                    <label class="form-check-label" for="is_active">
                                        Channel Active
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- DRM Configuration Section -->
                    <div class="card mt-3">
                        <div class="card-header">
                            <h6 class="mb-0">DRM Configuration (Optional)</h6>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="form-group">
                                        <label>Streaming Qualities</label>
                                        <div class="streaming-qualities">
                                            <div class="quality-item d-flex mb-2">
                                                <input type="text" class="form-control mr-2" placeholder="Quality (e.g., 720p)" name="quality_name[]">
                                                <input type="url" class="form-control mr-2" placeholder="Stream URL" name="quality_url[]">
                                                <button type="button" class="btn btn-sm btn-danger remove-quality">
                                                    <i class="fas fa-trash"></i>
                                                </button>
                                            </div>
                                        </div>
                                        <button type="button" class="btn btn-sm btn-success add-quality">
                                            <i class="fas fa-plus"></i> Add Quality
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <span class="btn-text">Save Channel</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Channel Details Modal -->
<div class="modal fade" id="channelDetailsModal" tabindex="-1" role="dialog" aria-labelledby="channelDetailsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="channelDetailsModalLabel">Channel Details</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="channelDetailsContent">
                    <!-- Content will be loaded here -->
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- Bulk Import Modal -->
<div class="modal fade" id="bulkImportModal" tabindex="-1" role="dialog" aria-labelledby="bulkImportModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="bulkImportModalLabel">Bulk Import Channels</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <form id="bulkImportForm" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="form-group">
                        <label for="import_file">Import File</label>
                        <input type="file" class="form-control-file" id="import_file" name="import_file" accept=".csv,.json" required>
                        <small class="form-text text-muted">Supported formats: CSV, JSON</small>
                    </div>
                    <div class="form-group">
                        <label for="import_format">Format</label>
                        <select class="form-control" id="import_format" name="import_format" required>
                            <option value="csv">CSV</option>
                            <option value="json">JSON</option>
                        </select>
                    </div>
                    <div class="alert alert-info">
                        <strong>Download templates:</strong>
                        <a href="#" onclick="downloadTemplate('csv')" class="btn btn-sm btn-outline-info ml-2">CSV Template</a>
                        <a href="#" onclick="downloadTemplate('json')" class="btn btn-sm btn-outline-info ml-2">JSON Template</a>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <span class="btn-text">Import Channels</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@section('script')
<script>
let channelsTable;
let isEditing = false;
let editingChannelId = null;

$(document).ready(function() {
    // Initialize DataTable
    channelsTable = $('#channelsTable').DataTable({
        processing: true,
        serverSide: true,
        ajax: {
            url: "{{ route('admin.live-tv.channels.list') }}",
            type: 'POST',
            data: function(d) {
                d._token = $('meta[name="csrf-token"]').attr('content');
                d.category_filter = $('#categoryFilter').val();
                d.status_filter = $('#statusFilter').val();
            }
        },
        columns: [
            { 
                data: 'tv_channel_id',
                orderable: false,
                searchable: false,
                render: function(data) {
                    return '<input type="checkbox" class="channel-checkbox" value="' + data + '">';
                }
            },
            { data: 'thumbnail', orderable: false, searchable: false },
            { data: 'channel_number' },
            { data: 'title' },
            { data: 'categories', orderable: false },
            { data: 'is_active', orderable: false },
            { data: 'total_views' },
            { data: 'action', orderable: false, searchable: false }
        ],
        order: [[2, 'asc']],
        pageLength: 25,
        responsive: true
    });

    // Filter change handlers
    $('#categoryFilter, #statusFilter').change(function() {
        channelsTable.ajax.reload();
    });

    // Select all checkbox
    $('#selectAll').change(function() {
        $('.channel-checkbox').prop('checked', $(this).prop('checked'));
        toggleBulkActions();
    });

    // Individual checkbox change
    $(document).on('change', '.channel-checkbox', function() {
        toggleBulkActions();
    });

    // Form submission
    $('#channelForm').submit(function(e) {
        e.preventDefault();
        saveChannel();
    });

    // Bulk import form
    $('#bulkImportForm').submit(function(e) {
        e.preventDefault();
        processBulkImport();
    });

    // Quality management
    $(document).on('click', '.add-quality', function() {
        addQualityItem();
    });

    $(document).on('click', '.remove-quality', function() {
        $(this).closest('.quality-item').remove();
    });

    // Bulk action handlers
    $('#bulkActivate').click(function() {
        performBulkAction('activate');
    });

    $('#bulkDeactivate').click(function() {
        performBulkAction('deactivate');
    });

    $('#bulkDelete').click(function() {
        performBulkAction('delete');
    });
});

function showAddChannelModal() {
    isEditing = false;
    editingChannelId = null;
    $('#channelModalLabel').text('Add New Channel');
    $('#channelForm')[0].reset();
    $('.streaming-qualities').html(getQualityItemHtml());
    $('#channelModal').modal('show');
}

function editChannel(channelId) {
    isEditing = true;
    editingChannelId = channelId;
    $('#channelModalLabel').text('Edit Channel');

    // Show loading state
    showLoading($('#channelModal .modal-body'));

    $.get(`{{ route('admin.live-tv.channels.show', ':id') }}`.replace(':id', channelId))
        .done(function(response) {
            if (response.success) {
                populateChannelForm(response.data);
                $('#channelModal').modal('show');
            }
        })
        .fail(function() {
            showErrorToast('Failed to load channel details');
        })
        .always(function() {
            hideLoading($('#channelModal .modal-body'));
        });
}

function populateChannelForm(channel) {
    $('#title').val(channel.title);
    $('#channel_number').val(channel.channel_number);
    $('#stream_url').val(channel.stream_url);
    $('#thumbnail').val(channel.thumbnail);
    $('#logo_url').val(channel.logo_url);
    $('#language').val(channel.language);
    $('#country_code').val(channel.country_code);
    $('#description').val(channel.description);
    $('#epg_url').val(channel.epg_url);
    $('#access_type').val(channel.access_type);
    $('#is_active').prop('checked', channel.is_active);

    // Set categories
    if (channel.categories) {
        let categoryIds = channel.categories.map(cat => cat.tv_category_id);
        $('#category_ids').val(categoryIds).selectric('refresh');
    }

    // Set streaming qualities
    if (channel.streaming_qualities && channel.streaming_qualities.length > 0) {
        $('.streaming-qualities').html('');
        channel.streaming_qualities.forEach(function(quality) {
            let qualityHtml = getQualityItemHtml(quality.name, quality.url);
            $('.streaming-qualities').append(qualityHtml);
        });
    } else {
        $('.streaming-qualities').html(getQualityItemHtml());
    }
}

function saveChannel() {
    let formData = new FormData($('#channelForm')[0]);
    
    // Collect streaming qualities
    let qualities = [];
    $('.quality-item').each(function() {
        let name = $(this).find('input[name="quality_name[]"]').val();
        let url = $(this).find('input[name="quality_url[]"]').val();
        if (name && url) {
            qualities.push({ name: name, url: url });
        }
    });
    formData.append('streaming_qualities', JSON.stringify(qualities));

    let url = isEditing 
        ? `{{ route('admin.live-tv.channels.update', ':id') }}`.replace(':id', editingChannelId)
        : `{{ route('admin.live-tv.channels.store') }}`;
    
    let method = isEditing ? 'PUT' : 'POST';

    // Show loading state
    showButtonLoading($('#channelForm button[type="submit"]'));

    $.ajax({
        url: url,
        type: method,
        data: formData,
        processData: false,
        contentType: false,
        headers: {
            'X-HTTP-Method-Override': method
        }
    })
    .done(function(response) {
        if (response.success) {
            $('#channelModal').modal('hide');
            channelsTable.ajax.reload();
            showSuccessToast(response.message);
        } else {
            showErrorToast(response.message);
        }
    })
    .fail(function(xhr) {
        let message = 'An error occurred';
        if (xhr.responseJSON && xhr.responseJSON.message) {
            message = xhr.responseJSON.message;
        }
        showErrorToast(message);
    })
    .always(function() {
        hideButtonLoading($('#channelForm button[type="submit"]'));
    });
}

function toggleChannel(channelId, status) {
    $.post(`{{ route('admin.live-tv.channels.toggle', ':id') }}`.replace(':id', channelId), {
        _token: $('meta[name="csrf-token"]').attr('content'),
        status: status
    })
    .done(function(response) {
        if (response.success) {
            channelsTable.ajax.reload();
            showSuccessToast(response.message);
        }
    })
    .fail(function() {
        showErrorToast('Failed to update channel status');
    });
}

function deleteChannel(channelId) {
    Swal.fire({
        title: 'Are you sure?',
        text: 'This will permanently delete the channel and all associated data.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: `{{ route('admin.live-tv.channels.destroy', ':id') }}`.replace(':id', channelId),
                type: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
                }
            })
            .done(function(response) {
                if (response.success) {
                    channelsTable.ajax.reload();
                    showSuccessToast(response.message);
                }
            })
            .fail(function() {
                showErrorToast('Failed to delete channel');
            });
        }
    });
}

function viewSchedule(channelId) {
    window.location.href = `{{ route('admin.live-tv.schedule.index') }}?channel_id=${channelId}`;
}

function toggleBulkActions() {
    let checkedBoxes = $('.channel-checkbox:checked').length;
    if (checkedBoxes > 0) {
        $('#bulkActivate, #bulkDeactivate, #bulkDelete').show();
    } else {
        $('#bulkActivate, #bulkDeactivate, #bulkDelete').hide();
    }
}

function performBulkAction(action) {
    let selectedIds = [];
    $('.channel-checkbox:checked').each(function() {
        selectedIds.push($(this).val());
    });

    if (selectedIds.length === 0) {
        showErrorToast('Please select at least one channel');
        return;
    }

    let actionText = action.charAt(0).toUpperCase() + action.slice(1);
    
    Swal.fire({
        title: `${actionText} Selected Channels?`,
        text: `This will ${action} ${selectedIds.length} channel(s).`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: `Yes, ${action}!`
    }).then((result) => {
        if (result.isConfirmed) {
            $.post(`{{ route('admin.live-tv.channels.bulk') }}`, {
                _token: $('meta[name="csrf-token"]').attr('content'),
                action: action,
                channel_ids: selectedIds
            })
            .done(function(response) {
                if (response.success) {
                    channelsTable.ajax.reload();
                    $('#selectAll').prop('checked', false);
                    toggleBulkActions();
                    showSuccessToast(response.message);
                }
            })
            .fail(function() {
                showErrorToast(`Failed to ${action} channels`);
            });
        }
    });
}

function bulkImportModal() {
    $('#bulkImportModal').modal('show');
}

function processBulkImport() {
    let formData = new FormData($('#bulkImportForm')[0]);
    
    showButtonLoading($('#bulkImportForm button[type="submit"]'));

    $.ajax({
        url: '{{ route("admin.live-tv.channels.bulk") }}', // This would need to be a different route for import
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false
    })
    .done(function(response) {
        if (response.success) {
            $('#bulkImportModal').modal('hide');
            channelsTable.ajax.reload();
            showSuccessToast(response.message);
        } else {
            showErrorToast(response.message);
        }
    })
    .fail(function() {
        showErrorToast('Import failed');
    })
    .always(function() {
        hideButtonLoading($('#bulkImportForm button[type="submit"]'));
    });
}

function downloadTemplate(format) {
    window.open(`/admin/live-tv/channels/template/${format}`, '_blank');
}

function exportChannels() {
    window.open(`/admin/live-tv/channels/export`, '_blank');
}

function getQualityItemHtml(name = '', url = '') {
    return `
        <div class="quality-item d-flex mb-2">
            <input type="text" class="form-control mr-2" placeholder="Quality (e.g., 720p)" name="quality_name[]" value="${name}">
            <input type="url" class="form-control mr-2" placeholder="Stream URL" name="quality_url[]" value="${url}">
            <button type="button" class="btn btn-sm btn-danger remove-quality">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;
}

function addQualityItem() {
    $('.streaming-qualities').append(getQualityItemHtml());
}

// Utility functions
function showLoading(element) {
    element.html('<div class="text-center p-4"><div class="spinner-border" role="status"></div></div>');
}

function hideLoading(element) {
    // This would be handled by the success callback
}

function showButtonLoading(button) {
    button.prop('disabled', true);
    button.find('.btn-text').hide();
    button.find('.spinner-border').removeClass('d-none');
}

function hideButtonLoading(button) {
    button.prop('disabled', false);
    button.find('.btn-text').show();
    button.find('.spinner-border').addClass('d-none');
}

function showSuccessToast(message) {
    iziToast.success({
        title: 'Success',
        message: message,
        position: 'topRight'
    });
}

function showErrorToast(message) {
    iziToast.error({
        title: 'Error',
        message: message,
        position: 'topRight'
    });
}
</script>

<style>
.channel-thumbnail {
    border-radius: 4px;
    object-fit: cover;
}

.channel-number {
    font-weight: bold;
    font-family: monospace;
}

.no-image {
    width: 60px;
    height: 40px;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #6c757d;
    border: 1px solid #dee2e6;
    border-radius: 4px;
}

.quality-item {
    align-items: center;
}

.streaming-qualities {
    max-height: 200px;
    overflow-y: auto;
}

.card-header .row {
    align-items: center;
}

.section-buttons .btn {
    margin-left: 0.5rem;
}

@media (max-width: 768px) {
    .section-buttons {
        margin-top: 1rem;
    }
    
    .section-buttons .btn {
        margin-bottom: 0.5rem;
        width: 100%;
    }
}
</style>
@endsection