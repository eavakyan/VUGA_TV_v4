@extends('include.app')

@section('content')
<section class="section">
    <div class="section-body">
        <div class="d-flex justify-content-between">
            <div class="section-title">
                <h3>{{ __('Live TV Categories Management') }}</h3>
            </div>
            <div class="section-buttons">
                <button class="btn btn-primary" onclick="showAddCategoryModal()">
                    <i class="fas fa-plus"></i> Add New Category
                </button>
                <button class="btn btn-info" onclick="toggleSortMode()">
                    <i class="fas fa-sort"></i> Sort Categories
                </button>
                <button class="btn btn-success" onclick="exportCategories()">
                    <i class="fas fa-download"></i> Export
                </button>
            </div>
        </div>

        <!-- Sort Mode Toggle -->
        <div class="row mb-3" id="sortModeAlert" style="display: none;">
            <div class="col-12">
                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i>
                    <strong>Sort Mode Active:</strong> Drag and drop categories to reorder them. Click "Save Order" when finished.
                    <button class="btn btn-sm btn-success ml-2" onclick="saveSortOrder()">
                        <i class="fas fa-save"></i> Save Order
                    </button>
                    <button class="btn btn-sm btn-secondary ml-1" onclick="cancelSort()">
                        <i class="fas fa-times"></i> Cancel
                    </button>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <div class="row w-100">
                            <div class="col-md-3">
                                <select class="form-control" id="statusFilter">
                                    <option value="all">All Status</option>
                                    <option value="active">Active</option>
                                    <option value="inactive">Inactive</option>
                                </select>
                            </div>
                            <div class="col-md-9">
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
                        <div id="normalView">
                            <div class="table-responsive">
                                <table class="table table-striped" id="categoriesTable">
                                    <thead>
                                        <tr>
                                            <th>
                                                <input type="checkbox" id="selectAll">
                                            </th>
                                            <th>Image</th>
                                            <th>Title</th>
                                            <th>Channels</th>
                                            <th>Sort Order</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody></tbody>
                                </table>
                            </div>
                        </div>

                        <div id="sortView" style="display: none;">
                            <div id="sortableCategories" class="row">
                                <!-- Sortable category cards will be loaded here -->
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Add/Edit Category Modal -->
<div class="modal fade" id="categoryModal" tabindex="-1" role="dialog" aria-labelledby="categoryModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="categoryModalLabel">Add New Category</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <form id="categoryForm">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="title">Category Title <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="title" name="title" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="slug">Slug</label>
                                <input type="text" class="form-control" id="slug" name="slug">
                                <small class="form-text text-muted">Auto-generated if left empty</small>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="image">Category Image URL</label>
                                <input type="url" class="form-control" id="image" name="image">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="icon_url">Icon URL</label>
                                <input type="url" class="form-control" id="icon_url" name="icon_url">
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
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="sort_order">Sort Order</label>
                                <input type="number" class="form-control" id="sort_order" name="sort_order" min="0">
                                <small class="form-text text-muted">Lower numbers appear first</small>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group mt-4">
                                <div class="form-check">
                                    <input type="checkbox" class="form-check-input" id="is_active" name="is_active" checked>
                                    <label class="form-check-label" for="is_active">
                                        Category Active
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Preview Section -->
                    <div class="row" id="imagePreview" style="display: none;">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label>Image Preview:</label>
                                <div class="preview-container">
                                    <img id="previewImage" src="" alt="Preview" class="img-thumbnail" style="max-width: 200px; max-height: 150px;">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-info" id="duplicateBtn" style="display: none;">
                        <i class="fas fa-copy"></i> Duplicate
                    </button>
                    <button type="submit" class="btn btn-primary">
                        <span class="btn-text">Save Category</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Category Details Modal -->
<div class="modal fade" id="categoryDetailsModal" tabindex="-1" role="dialog" aria-labelledby="categoryDetailsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="categoryDetailsModalLabel">Category Details</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="categoryDetailsContent">
                    <!-- Content will be loaded here -->
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- Duplicate Category Modal -->
<div class="modal fade" id="duplicateCategoryModal" tabindex="-1" role="dialog" aria-labelledby="duplicateCategoryModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="duplicateCategoryModalLabel">Duplicate Category</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <form id="duplicateCategoryForm">
                <div class="modal-body">
                    <div class="form-group">
                        <label for="duplicate_title">New Category Title <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="duplicate_title" name="title" required>
                        <small class="form-text text-muted">All other settings will be copied from the original category</small>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <span class="btn-text">Create Duplicate</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Channel List Modal -->
<div class="modal fade" id="channelListModal" tabindex="-1" role="dialog" aria-labelledby="channelListModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="channelListModalLabel">Category Channels</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="channelListContent">
                    <!-- Content will be loaded here -->
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
@endsection

@section('script')
<!-- jQuery UI for sortable -->
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.min.js"></script>

<script>
let categoriesTable;
let isEditing = false;
let editingCategoryId = null;
let sortMode = false;
let originalOrder = [];

$(document).ready(function() {
    // Initialize DataTable
    categoriesTable = $('#categoriesTable').DataTable({
        processing: true,
        serverSide: true,
        ajax: {
            url: "{{ route('admin.live-tv.categories.list') }}",
            type: 'POST',
            data: function(d) {
                d._token = $('meta[name="csrf-token"]').attr('content');
                d.status_filter = $('#statusFilter').val();
            }
        },
        columns: [
            { 
                data: 'tv_category_id',
                orderable: false,
                searchable: false,
                render: function(data) {
                    return '<input type="checkbox" class="category-checkbox" value="' + data + '">';
                }
            },
            { data: 'image', orderable: false, searchable: false },
            { data: 'title' },
            { data: 'channels_count', orderable: false },
            { data: 'sort_order', orderable: false },
            { data: 'is_active', orderable: false },
            { data: 'action', orderable: false, searchable: false }
        ],
        order: [[4, 'asc']], // Sort by sort_order
        pageLength: 25,
        responsive: true
    });

    // Filter change handler
    $('#statusFilter').change(function() {
        categoriesTable.ajax.reload();
    });

    // Select all checkbox
    $('#selectAll').change(function() {
        $('.category-checkbox').prop('checked', $(this).prop('checked'));
        toggleBulkActions();
    });

    // Individual checkbox change
    $(document).on('change', '.category-checkbox', function() {
        toggleBulkActions();
    });

    // Form submissions
    $('#categoryForm').submit(function(e) {
        e.preventDefault();
        saveCategory();
    });

    $('#duplicateCategoryForm').submit(function(e) {
        e.preventDefault();
        duplicateCategory();
    });

    // Image preview
    $('#image').on('input', function() {
        let imageUrl = $(this).val();
        if (imageUrl) {
            $('#previewImage').attr('src', imageUrl);
            $('#imagePreview').show();
        } else {
            $('#imagePreview').hide();
        }
    });

    // Auto-generate slug from title
    $('#title').on('input', function() {
        if (!isEditing || !$('#slug').val()) {
            let slug = $(this).val().toLowerCase()
                .replace(/[^a-z0-9 -]/g, '')
                .replace(/\s+/g, '-')
                .replace(/-+/g, '-')
                .trim('-');
            $('#slug').val(slug);
        }
    });

    // Duplicate button handler
    $('#duplicateBtn').click(function() {
        showDuplicateModal();
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

function showAddCategoryModal() {
    isEditing = false;
    editingCategoryId = null;
    $('#categoryModalLabel').text('Add New Category');
    $('#categoryForm')[0].reset();
    $('#duplicateBtn').hide();
    $('#imagePreview').hide();
    $('#categoryModal').modal('show');
}

function editCategory(categoryId) {
    isEditing = true;
    editingCategoryId = categoryId;
    $('#categoryModalLabel').text('Edit Category');
    $('#duplicateBtn').show();

    $.get(`{{ route('admin.live-tv.categories.show', ':id') }}`.replace(':id', categoryId))
        .done(function(response) {
            if (response.success) {
                populateCategoryForm(response.data);
                $('#categoryModal').modal('show');
            }
        })
        .fail(function() {
            showErrorToast('Failed to load category details');
        });
}

function populateCategoryForm(category) {
    $('#title').val(category.title);
    $('#slug').val(category.slug);
    $('#image').val(category.image);
    $('#icon_url').val(category.icon_url);
    $('#description').val(category.description);
    $('#sort_order').val(category.sort_order);
    $('#is_active').prop('checked', category.is_active);

    // Show image preview if available
    if (category.image) {
        $('#previewImage').attr('src', category.image);
        $('#imagePreview').show();
    }
}

function saveCategory() {
    let formData = new FormData($('#categoryForm')[0]);
    
    let url = isEditing 
        ? `{{ route('admin.live-tv.categories.update', ':id') }}`.replace(':id', editingCategoryId)
        : `{{ route('admin.live-tv.categories.store') }}`;
    
    let method = isEditing ? 'PUT' : 'POST';

    showButtonLoading($('#categoryForm button[type="submit"]'));

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
            $('#categoryModal').modal('hide');
            categoriesTable.ajax.reload();
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
        hideButtonLoading($('#categoryForm button[type="submit"]'));
    });
}

function toggleCategory(categoryId, status) {
    $.post(`{{ route('admin.live-tv.categories.toggle', ':id') }}`.replace(':id', categoryId), {
        _token: $('meta[name="csrf-token"]').attr('content'),
        status: status
    })
    .done(function(response) {
        if (response.success) {
            categoriesTable.ajax.reload();
            showSuccessToast(response.message);
        }
    })
    .fail(function() {
        showErrorToast('Failed to update category status');
    });
}

function deleteCategory(categoryId) {
    Swal.fire({
        title: 'Are you sure?',
        text: 'This will permanently delete the category. Categories with assigned channels cannot be deleted.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: `{{ route('admin.live-tv.categories.destroy', ':id') }}`.replace(':id', categoryId),
                type: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
                }
            })
            .done(function(response) {
                if (response.success) {
                    categoriesTable.ajax.reload();
                    showSuccessToast(response.message);
                } else {
                    showErrorToast(response.message);
                }
            })
            .fail(function() {
                showErrorToast('Failed to delete category');
            });
        }
    });
}

function viewChannels(categoryId) {
    $.get(`{{ route('admin.live-tv.categories.channels', ':id') }}`.replace(':id', categoryId))
        .done(function(response) {
            if (response.success) {
                displayChannelList(response.data);
                $('#channelListModal').modal('show');
            }
        })
        .fail(function() {
            showErrorToast('Failed to load channels');
        });
}

function displayChannelList(data) {
    $('#channelListModalLabel').text(`${data.category.title} - Channels`);
    
    let html = '';
    if (data.channels.length === 0) {
        html = '<div class="alert alert-info">No channels assigned to this category.</div>';
    } else {
        html = '<div class="row">';
        data.channels.forEach(function(channel) {
            let statusBadge = channel.is_active ? 
                '<span class="badge badge-success">Active</span>' : 
                '<span class="badge badge-secondary">Inactive</span>';
            
            let channelNumber = channel.channel_number ? 
                '#' + String(channel.channel_number).padStart(3, '0') : 
                'N/A';

            html += `
                <div class="col-md-6 mb-3">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center">
                                <img src="${channel.thumbnail || '/assets/img/default.png'}" alt="${channel.title}" class="channel-thumb mr-3">
                                <div class="flex-grow-1">
                                    <h6 class="mb-1">${channelNumber} - ${channel.title}</h6>
                                    <small class="text-muted">${number_format(channel.total_views || 0)} views</small>
                                    <div class="mt-1">${statusBadge}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        html += '</div>';
    }
    
    $('#channelListContent').html(html);
}

function toggleSortMode() {
    if (sortMode) {
        cancelSort();
    } else {
        enterSortMode();
    }
}

function enterSortMode() {
    sortMode = true;
    $('#sortModeAlert').show();
    $('#normalView').hide();
    $('#sortView').show();
    loadSortableCategories();
}

function cancelSort() {
    sortMode = false;
    $('#sortModeAlert').hide();
    $('#normalView').show();
    $('#sortView').hide();
    categoriesTable.ajax.reload();
}

function loadSortableCategories() {
    $.get('{{ route("admin.live-tv.categories.sorted") }}')
        .done(function(response) {
            if (response.success) {
                renderSortableCategories(response.data);
                originalOrder = response.data.map(cat => cat.tv_category_id);
            }
        })
        .fail(function() {
            showErrorToast('Failed to load categories for sorting');
            cancelSort();
        });
}

function renderSortableCategories(categories) {
    let html = '';
    categories.forEach(function(category, index) {
        let statusClass = category.is_active ? 'border-success' : 'border-secondary';
        let statusIcon = category.is_active ? 'fas fa-eye text-success' : 'fas fa-eye-slash text-muted';
        
        html += `
            <div class="col-md-4 col-lg-3 mb-3">
                <div class="card sortable-category ${statusClass}" data-id="${category.tv_category_id}" data-order="${category.sort_order}">
                    <div class="card-body text-center">
                        <div class="sortable-handle">
                            <i class="fas fa-grip-vertical text-muted"></i>
                        </div>
                        <div class="sort-order-badge">${index + 1}</div>
                        <h6 class="mt-2">${category.title}</h6>
                        <div class="mt-2">
                            <i class="${statusIcon}"></i>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    $('#sortableCategories').html(html);
    
    // Make sortable
    $('#sortableCategories').sortable({
        handle: '.sortable-handle',
        placeholder: 'sortable-placeholder col-md-4 col-lg-3 mb-3',
        forcePlaceholderSize: true,
        update: function(event, ui) {
            updateSortNumbers();
        }
    });
}

function updateSortNumbers() {
    $('#sortableCategories .sortable-category').each(function(index) {
        $(this).find('.sort-order-badge').text(index + 1);
    });
}

function saveSortOrder() {
    let categories = [];
    $('#sortableCategories .sortable-category').each(function(index) {
        categories.push({
            id: $(this).data('id'),
            sort_order: (index + 1) * 10 // Give some spacing between orders
        });
    });

    $.post('{{ route("admin.live-tv.categories.sort") }}', {
        _token: $('meta[name="csrf-token"]').attr('content'),
        categories: categories
    })
    .done(function(response) {
        if (response.success) {
            showSuccessToast(response.message);
            cancelSort();
        } else {
            showErrorToast(response.message);
        }
    })
    .fail(function() {
        showErrorToast('Failed to save sort order');
    });
}

function showDuplicateModal() {
    $('#categoryModal').modal('hide');
    $('#duplicate_title').val('');
    $('#duplicateCategoryModal').modal('show');
}

function duplicateCategory() {
    let formData = new FormData($('#duplicateCategoryForm')[0]);
    
    showButtonLoading($('#duplicateCategoryForm button[type="submit"]'));

    $.ajax({
        url: `{{ route('admin.live-tv.categories.duplicate', ':id') }}`.replace(':id', editingCategoryId),
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false
    })
    .done(function(response) {
        if (response.success) {
            $('#duplicateCategoryModal').modal('hide');
            categoriesTable.ajax.reload();
            showSuccessToast(response.message);
        } else {
            showErrorToast(response.message);
        }
    })
    .fail(function() {
        showErrorToast('Duplication failed');
    })
    .always(function() {
        hideButtonLoading($('#duplicateCategoryForm button[type="submit"]'));
    });
}

function toggleBulkActions() {
    let checkedBoxes = $('.category-checkbox:checked').length;
    if (checkedBoxes > 0) {
        $('#bulkActivate, #bulkDeactivate, #bulkDelete').show();
    } else {
        $('#bulkActivate, #bulkDeactivate, #bulkDelete').hide();
    }
}

function performBulkAction(action) {
    let selectedIds = [];
    $('.category-checkbox:checked').each(function() {
        selectedIds.push($(this).val());
    });

    if (selectedIds.length === 0) {
        showErrorToast('Please select at least one category');
        return;
    }

    let actionText = action.charAt(0).toUpperCase() + action.slice(1);
    
    Swal.fire({
        title: `${actionText} Selected Categories?`,
        text: `This will ${action} ${selectedIds.length} category(ies).`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: `Yes, ${action}!`
    }).then((result) => {
        if (result.isConfirmed) {
            $.post(`{{ route('admin.live-tv.categories.bulk') }}`, {
                _token: $('meta[name="csrf-token"]').attr('content'),
                action: action,
                category_ids: selectedIds
            })
            .done(function(response) {
                if (response.success) {
                    categoriesTable.ajax.reload();
                    $('#selectAll').prop('checked', false);
                    toggleBulkActions();
                    showSuccessToast(response.message);
                } else {
                    showErrorToast(response.message);
                }
            })
            .fail(function() {
                showErrorToast(`Failed to ${action} categories`);
            });
        }
    });
}

function exportCategories() {
    window.open('/admin/live-tv/categories/export', '_blank');
}

function number_format(number) {
    return new Intl.NumberFormat().format(number);
}

// Utility functions
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
.category-image {
    border-radius: 4px;
    object-fit: cover;
}

.channel-thumb {
    width: 40px;
    height: 30px;
    object-fit: cover;
    border-radius: 4px;
}

.sortable-category {
    cursor: move;
    transition: all 0.3s;
    position: relative;
}

.sortable-category:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.sortable-handle {
    position: absolute;
    top: 5px;
    right: 5px;
    cursor: move;
}

.sort-order-badge {
    position: absolute;
    top: 5px;
    left: 5px;
    background: #007bff;
    color: white;
    border-radius: 50%;
    width: 25px;
    height: 25px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 0.8em;
}

.sortable-placeholder {
    border: 2px dashed #007bff;
    background: rgba(0, 123, 255, 0.1);
    border-radius: 0.25rem;
    min-height: 120px;
}

.ui-sortable-helper {
    opacity: 0.8;
    transform: rotate(5deg);
    z-index: 1000;
}

.no-image {
    width: 50px;
    height: 50px;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #6c757d;
    border: 1px solid #dee2e6;
    border-radius: 4px;
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
    
    .sortable-category {
        margin-bottom: 1rem;
    }
}
</style>
@endsection