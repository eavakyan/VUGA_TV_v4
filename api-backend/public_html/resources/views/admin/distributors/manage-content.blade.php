@extends('include.app')

@section('title', 'Manage Content Distributors')

@section('content')
<style>
/* Inline override to ensure highest specificity */
#contentTable tbody tr td,
#contentTable thead tr th {
    color: #ffffff !important;
    opacity: 1 !important;
}
#contentTable tbody tr td:nth-child(2) {
    color: #ffffff !important;
    font-weight: 500 !important;
    opacity: 1 !important;
}
.card-body {
    background-color: #1a1a1a !important;
}
#contentTable {
    background-color: #000000 !important;
}
</style>
<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Assign Content to Distributors</h3>
                    <div class="card-tools">
                        <button id="saveChanges" class="btn btn-success btn-sm" disabled>
                            <i class="fas fa-save"></i> Save Changes
                        </button>
                    </div>
                </div>
                <div class="card-body">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i> 
                        Assign content to distributors to control access. Content without a distributor is freely accessible.
                    </div>

                    <div id="successAlert" class="alert alert-success d-none"></div>
                    <div id="errorAlert" class="alert alert-danger d-none"></div>

                    <div class="row mb-3">
                        <div class="col-md-4">
                            <div class="form-group">
                                <label for="contentSearch">Search Content</label>
                                <input type="text" class="form-control" id="contentSearch" placeholder="Type to search...">
                            </div>
                        </div>
                    </div>

                    <table class="table table-bordered" id="contentTable">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Type</th>
                                <th>Current Distributor</th>
                                <th>Assign To</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($content as $item)
                            <tr>
                                <td>{{ $item->content_id }}</td>
                                <td>{{ $item->title }}</td>
                                <td>
                                    @if($item->type == 1)
                                        <span class="badge badge-primary">Movie</span>
                                    @else
                                        <span class="badge badge-info">Series</span>
                                    @endif
                                </td>
                                <td>
                                    @if($item->distributor)
                                        <span class="badge badge-secondary">{{ $item->distributor->name }}</span>
                                    @else
                                        <span class="badge badge-success">Free Access</span>
                                    @endif
                                </td>
                                <td>
                                    <select class="form-control form-control-sm distributor-select" 
                                            data-content-id="{{ $item->content_id }}"
                                            data-original="{{ $item->content_distributor_id }}">
                                        <option value="">Free Access (No Distributor)</option>
                                        @foreach($distributors as $distributor)
                                            <option value="{{ $distributor->content_distributor_id }}"
                                                    {{ $item->content_distributor_id == $distributor->content_distributor_id ? 'selected' : '' }}>
                                                {{ $distributor->name }}
                                                @if($distributor->is_base_included)
                                                    (Base)
                                                @elseif($distributor->is_premium)
                                                    (Premium)
                                                @endif
                                            </option>
                                        @endforeach
                                    </select>
                                </td>
                            </tr>
                            @endforeach
                        </tbody>
                    </table>

                    {{ $content->links() }}
                </div>
            </div>
        </div>
    </div>
</div>
@endsection

@section('styles')
<style>
/* Set dark background for the entire table area */
.card-body {
    background-color: #1a1a1a !important;
}

/* Dark theme for the table */
#contentTable {
    background-color: #000000 !important;
    color: #ffffff !important;
}

/* Table header styling */
#contentTable thead {
    background-color: #2a2a2a !important;
}

#contentTable thead th {
    background-color: #2a2a2a !important;
    color: #ffffff !important;
    border-color: #444444 !important;
    font-weight: 600;
}

/* Table body styling */
#contentTable tbody {
    background-color: #000000 !important;
}

#contentTable tbody tr {
    background-color: #1a1a1a !important;
    border-bottom: 1px solid #333333;
}

#contentTable tbody tr:hover {
    background-color: #2a2a2a !important;
}

/* Force all table cells to have white text */
#contentTable td,
#contentTable th {
    color: #ffffff !important;
    border-color: #333333 !important;
}

/* Specific column styling */
#contentTable tbody tr td:nth-child(1) { /* ID column */
    color: #cccccc !important;
    font-family: monospace;
}

#contentTable tbody tr td:nth-child(2) { /* Title column - ENSURE WHITE */
    color: #ffffff !important;
    font-weight: 500;
    opacity: 1 !important;
}

/* Badge styling with proper contrast */
#contentTable .badge {
    font-weight: 600;
    padding: 0.375rem 0.75rem;
}

#contentTable .badge-primary {
    background-color: #007bff !important;
    color: #ffffff !important;
}

#contentTable .badge-info {
    background-color: #17a2b8 !important;
    color: #ffffff !important;
}

#contentTable .badge-secondary {
    background-color: #6c757d !important;
    color: #ffffff !important;
}

#contentTable .badge-success {
    background-color: #28a745 !important;
    color: #ffffff !important;
}

/* Select dropdown styling */
.distributor-select {
    background-color: #2a2a2a !important;
    color: #ffffff !important;
    border-color: #444444 !important;
}

.distributor-select option {
    background-color: #2a2a2a !important;
    color: #ffffff !important;
}

.distributor-select.changed {
    background-color: #3a3a00 !important;
    border-color: #ffc107 !important;
    color: #ffffff !important;
}

/* Search input styling */
#contentSearch {
    background-color: #2a2a2a !important;
    color: #ffffff !important;
    border-color: #444444 !important;
}

#contentSearch::placeholder {
    color: #999999 !important;
}

/* Alert styling for dark theme */
.alert-info {
    background-color: #1a3a4a !important;
    border-color: #17a2b8 !important;
    color: #ffffff !important;
}

/* Labels */
label {
    color: #ffffff !important;
}

/* Pagination styling */
.pagination .page-link {
    background-color: #2a2a2a !important;
    border-color: #444444 !important;
    color: #ffffff !important;
}

.pagination .page-item.active .page-link {
    background-color: #007bff !important;
    border-color: #007bff !important;
}

/* Override any Bootstrap or theme defaults */
.table-bordered td,
.table-bordered th {
    border-color: #333333 !important;
}

/* Final override for any stubborn elements */
#contentTable *:not(.badge) {
    color: #ffffff !important;
}
</style>
@endsection

@section('scripts')
<script>
$(document).ready(function() {
    console.log('Document ready - initializing search functionality');
    
    // Apply dark theme styles via JavaScript as backup
    function applyDarkTheme() {
        // Set backgrounds
        $('.card-body').css('background-color', '#1a1a1a');
        $('#contentTable').css('background-color', '#000000');
        $('#contentTable tbody tr').css('background-color', '#1a1a1a');
        
        // Force white text on all table elements
        $('#contentTable, #contentTable *').each(function() {
            if (!$(this).hasClass('badge')) {
                $(this).css({
                    'color': '#ffffff',
                    'opacity': '1'
                });
            }
        });
        
        // Specifically ensure title column is white
        $('#contentTable tbody tr').each(function() {
            $(this).find('td:eq(1)').css({
                'color': '#ffffff',
                'opacity': '1',
                'font-weight': '500'
            });
        });
        
        // Remove any conflicting classes
        $('#contentTable').find('.text-muted, .text-secondary, .text-gray, .text-dark').each(function() {
            $(this).removeClass('text-muted text-secondary text-gray text-dark');
        });
    }
    
    // Apply immediately
    applyDarkTheme();
    
    // Apply again after a delay to catch any dynamic updates
    setTimeout(applyDarkTheme, 100);
    setTimeout(applyDarkTheme, 500);
    
    // Initialize search functionality
    initializeSearch();
    
    var changes = {};
    
    // Track changes
    $('.distributor-select').on('change', function() {
        var contentId = $(this).data('content-id');
        var originalValue = $(this).data('original');
        var newValue = $(this).val();
        
        if (newValue != originalValue) {
            changes[contentId] = newValue;
            $(this).addClass('changed');
        } else {
            delete changes[contentId];
            $(this).removeClass('changed');
        }
        
        $('#saveChanges').prop('disabled', Object.keys(changes).length === 0);
    });
    
    // Save changes
    $('#saveChanges').on('click', function() {
        var btn = $(this);
        btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Saving...');
        
        $.ajax({
            url: '{{ route("admin.distributors.content.update") }}',
            method: 'POST',
            data: {
                _token: '{{ csrf_token() }}',
                assignments: changes
            },
            success: function(response) {
                $('#successAlert').removeClass('d-none').text(response.message);
                $('#errorAlert').addClass('d-none');
                
                // Reset tracking
                $('.distributor-select').each(function() {
                    $(this).data('original', $(this).val()).removeClass('changed');
                });
                changes = {};
                
                setTimeout(function() {
                    $('#successAlert').addClass('d-none');
                }, 5000);
            },
            error: function(xhr) {
                var message = 'Failed to update assignments';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                $('#errorAlert').removeClass('d-none').text(message);
                $('#successAlert').addClass('d-none');
            },
            complete: function() {
                btn.prop('disabled', Object.keys(changes).length === 0)
                   .html('<i class="fas fa-save"></i> Save Changes');
            }
        });
    });
    
    // Search functionality
    function initializeSearch() {
        console.log('Initializing search functionality');
        
        var searchTimeout;
        var $searchInput = $('#contentSearch');
        var $contentRows = $('#contentTable tbody tr');
        
        console.log('Search input found:', $searchInput.length);
        console.log('Content rows found:', $contentRows.length);
        
        function performSearch() {
            var searchTerm = $searchInput.val().toLowerCase().trim();
            console.log('Performing search for:', searchTerm);
            
            if (searchTerm === '') {
                // Show all rows if search is empty
                $contentRows.show();
            } else {
                // Filter rows based on search term
                $contentRows.each(function() {
                    var $row = $(this);
                    var title = $row.find('td:eq(1)').text().toLowerCase();
                    var id = $row.find('td:eq(0)').text().toLowerCase();
                    var type = $row.find('td:eq(2)').text().toLowerCase();
                    var distributor = $row.find('td:eq(3)').text().toLowerCase();
                    
                    // Search in multiple columns
                    if (title.indexOf(searchTerm) > -1 || 
                        id.indexOf(searchTerm) > -1 || 
                        type.indexOf(searchTerm) > -1 ||
                        distributor.indexOf(searchTerm) > -1) {
                        $row.show();
                    } else {
                        $row.hide();
                    }
                });
            }
        }
        
        // Trigger search on input event (covers typing, paste, etc.)
        $searchInput.on('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(performSearch, 150);
        });
        
        // Trigger search on keyup (covers all key releases)
        $searchInput.on('keyup', function(e) {
            // Skip if it's just Enter key (handled separately)
            if (e.which !== 13) {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(performSearch, 150);
            }
        });
        
        // Immediate search on Enter key
        $searchInput.on('keypress', function(e) {
            if (e.which === 13) { // Enter key
                e.preventDefault();
                clearTimeout(searchTimeout);
                performSearch();
            }
        });
        
        // Also bind to change event as fallback
        $searchInput.on('change', function() {
            performSearch();
        });
    }
});
</script>
@endsection