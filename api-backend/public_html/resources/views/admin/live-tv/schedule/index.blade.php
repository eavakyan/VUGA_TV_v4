@extends('include.app')

@section('content')
<section class="section">
    <div class="section-body">
        <div class="d-flex justify-content-between">
            <div class="section-title">
                <h3>{{ __('Live TV Schedule Management') }}</h3>
            </div>
            <div class="section-buttons">
                <button class="btn btn-primary" onclick="showAddScheduleModal()">
                    <i class="fas fa-plus"></i> Add Program
                </button>
                <button class="btn btn-info" onclick="showBulkImportModal()">
                    <i class="fas fa-upload"></i> Bulk Import
                </button>
                <button class="btn btn-success" onclick="exportSchedule()">
                    <i class="fas fa-download"></i> Export
                </button>
            </div>
        </div>

        <div class="row mb-3">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <div class="row w-100">
                            <div class="col-md-3">
                                <select class="form-control" id="channelFilter">
                                    <option value="">All Channels</option>
                                    @foreach($channels as $channel)
                                        <option value="{{ $channel->tv_channel_id }}" 
                                                @if($selectedChannel == $channel->tv_channel_id) selected @endif>
                                            {{ $channel->channel_number ? '#' . str_pad($channel->channel_number, 3, '0', STR_PAD_LEFT) . ' - ' : '' }}{{ $channel->title }}
                                        </option>
                                    @endforeach
                                </select>
                            </div>
                            <div class="col-md-3">
                                <input type="date" class="form-control" id="dateFilter" value="{{ $selectedDate }}">
                            </div>
                            <div class="col-md-3">
                                <div class="btn-group" role="group">
                                    <button type="button" class="btn btn-outline-primary" id="gridView">
                                        <i class="fas fa-th"></i> Grid
                                    </button>
                                    <button type="button" class="btn btn-primary" id="calendarView">
                                        <i class="fas fa-calendar"></i> Calendar
                                    </button>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <button class="btn btn-outline-info btn-block" onclick="refreshSchedule()">
                                    <i class="fas fa-sync"></i> Refresh
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Grid View -->
        <div id="gridViewContainer">
            <div class="row">
                <div class="col-12">
                    <div class="card">
                        <div class="card-header">
                            <h4>Program Schedule - <span id="selectedDateDisplay">{{ $selectedDate }}</span></h4>
                        </div>
                        <div class="card-body">
                            <div id="scheduleGrid">
                                <!-- Schedule grid will be loaded here -->
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Calendar View -->
        <div id="calendarViewContainer" style="display: none;">
            <div class="row">
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <div id="scheduleCalendar"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Add/Edit Schedule Modal -->
<div class="modal fade" id="scheduleModal" tabindex="-1" role="dialog" aria-labelledby="scheduleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="scheduleModalLabel">Add New Program</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <form id="scheduleForm">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="tv_channel_id">Channel <span class="text-danger">*</span></label>
                                <select class="form-control" id="tv_channel_id" name="tv_channel_id" required>
                                    <option value="">Select Channel</option>
                                    @foreach($channels as $channel)
                                        <option value="{{ $channel->tv_channel_id }}">
                                            {{ $channel->channel_number ? '#' . str_pad($channel->channel_number, 3, '0', STR_PAD_LEFT) . ' - ' : '' }}{{ $channel->title }}
                                        </option>
                                    @endforeach
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="genre">Genre</label>
                                <select class="form-control" id="genre" name="genre">
                                    <option value="">Select Genre</option>
                                    <option value="News">News</option>
                                    <option value="Sports">Sports</option>
                                    <option value="Entertainment">Entertainment</option>
                                    <option value="Movies">Movies</option>
                                    <option value="Documentary">Documentary</option>
                                    <option value="Drama">Drama</option>
                                    <option value="Comedy">Comedy</option>
                                    <option value="Reality">Reality TV</option>
                                    <option value="Kids">Kids</option>
                                    <option value="Music">Music</option>
                                    <option value="Education">Educational</option>
                                    <option value="Lifestyle">Lifestyle</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label for="program_title">Program Title <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="program_title" name="program_title" required>
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
                                <label for="start_time">Start Time <span class="text-danger">*</span></label>
                                <input type="datetime-local" class="form-control" id="start_time" name="start_time" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="end_time">End Time <span class="text-danger">*</span></label>
                                <input type="datetime-local" class="form-control" id="end_time" name="end_time" required>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="rating">Rating</label>
                                <select class="form-control" id="rating" name="rating">
                                    <option value="">No Rating</option>
                                    <option value="G">G - General</option>
                                    <option value="PG">PG - Parental Guidance</option>
                                    <option value="PG-13">PG-13 - Parents Strongly Cautioned</option>
                                    <option value="R">R - Restricted</option>
                                    <option value="TV-Y">TV-Y - Children</option>
                                    <option value="TV-Y7">TV-Y7 - Children 7+</option>
                                    <option value="TV-G">TV-G - General Audience</option>
                                    <option value="TV-PG">TV-PG - Parental Guidance</option>
                                    <option value="TV-14">TV-14 - Parents Strongly Cautioned</option>
                                    <option value="TV-MA">TV-MA - Mature Audiences</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="original_air_year">Original Air Year</label>
                                <input type="number" class="form-control" id="original_air_year" name="original_air_year" 
                                       min="1900" max="{{ date('Y') + 10 }}">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="season_number">Season Number</label>
                                <input type="number" class="form-control" id="season_number" name="season_number" min="1">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="episode_number">Episode Number</label>
                                <input type="number" class="form-control" id="episode_number" name="episode_number" min="1">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="thumbnail_url">Thumbnail URL</label>
                                <input type="url" class="form-control" id="thumbnail_url" name="thumbnail_url">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group mt-4">
                                <div class="form-check">
                                    <input type="checkbox" class="form-check-input" id="is_repeat" name="is_repeat">
                                    <label class="form-check-label" for="is_repeat">
                                        Repeat/Rerun
                                    </label>
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
                        <span class="btn-text">Save Program</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Bulk Import Modal -->
<div class="modal fade" id="bulkImportScheduleModal" tabindex="-1" role="dialog" aria-labelledby="bulkImportScheduleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="bulkImportScheduleModalLabel">Bulk Import Schedule</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <form id="bulkImportScheduleForm" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="form-group">
                        <label for="schedule_import_file">Import File</label>
                        <input type="file" class="form-control-file" id="schedule_import_file" name="import_file" accept=".csv,.json" required>
                        <small class="form-text text-muted">Supported formats: CSV, JSON</small>
                    </div>
                    <div class="form-group">
                        <label for="schedule_import_format">Format</label>
                        <select class="form-control" id="schedule_import_format" name="import_format" required>
                            <option value="csv">CSV</option>
                            <option value="json">JSON</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="import_channel_id">Default Channel (Optional)</label>
                        <select class="form-control" id="import_channel_id" name="tv_channel_id">
                            <option value="">Use channel from file</option>
                            @foreach($channels as $channel)
                                <option value="{{ $channel->tv_channel_id }}">
                                    {{ $channel->channel_number ? '#' . str_pad($channel->channel_number, 3, '0', STR_PAD_LEFT) . ' - ' : '' }}{{ $channel->title }}
                                </option>
                            @endforeach
                        </select>
                        <small class="form-text text-muted">If selected, all imported programs will be assigned to this channel</small>
                    </div>
                    <div class="alert alert-info">
                        <strong>Download templates:</strong>
                        <a href="#" onclick="downloadScheduleTemplate('csv')" class="btn btn-sm btn-outline-info ml-2">CSV Template</a>
                        <a href="#" onclick="downloadScheduleTemplate('json')" class="btn btn-sm btn-outline-info ml-2">JSON Template</a>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <span class="btn-text">Import Schedule</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Duplicate Schedule Modal -->
<div class="modal fade" id="duplicateScheduleModal" tabindex="-1" role="dialog" aria-labelledby="duplicateScheduleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="duplicateScheduleModalLabel">Duplicate Program</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <form id="duplicateScheduleForm">
                <div class="modal-body">
                    <div class="form-group">
                        <label>Select Target Dates:</label>
                        <div class="target-dates">
                            <div class="date-input-group d-flex mb-2">
                                <input type="date" class="form-control" name="target_dates[]" required>
                                <button type="button" class="btn btn-sm btn-danger ml-2 remove-date">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                        <button type="button" class="btn btn-sm btn-success add-date">
                            <i class="fas fa-plus"></i> Add Date
                        </button>
                    </div>
                    <div class="alert alert-info">
                        <strong>Note:</strong> The program will be duplicated with the same time slots on the selected dates.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <span class="btn-text">Duplicate Program</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@section('script')
<!-- FullCalendar CSS and JS -->
<link href="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.0/main.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.0/main.min.js"></script>

<script>
let isEditing = false;
let editingScheduleId = null;
let currentView = 'grid';
let calendar = null;

$(document).ready(function() {
    // Initialize view
    loadScheduleData();

    // View toggle
    $('#gridView').click(function() {
        switchToGridView();
    });

    $('#calendarView').click(function() {
        switchToCalendarView();
    });

    // Filter handlers
    $('#channelFilter, #dateFilter').change(function() {
        loadScheduleData();
    });

    // Form submissions
    $('#scheduleForm').submit(function(e) {
        e.preventDefault();
        saveSchedule();
    });

    $('#bulkImportScheduleForm').submit(function(e) {
        e.preventDefault();
        processBulkImportSchedule();
    });

    $('#duplicateScheduleForm').submit(function(e) {
        e.preventDefault();
        duplicateSchedule();
    });

    // Duplicate functionality
    $('#duplicateBtn').click(function() {
        showDuplicateModal();
    });

    // Date management for duplicate modal
    $(document).on('click', '.add-date', function() {
        addDateInput();
    });

    $(document).on('click', '.remove-date', function() {
        $(this).closest('.date-input-group').remove();
    });

    // Auto-calculate end time based on start time (default 1 hour)
    $('#start_time').change(function() {
        let startTime = new Date($(this).val());
        if (!isNaN(startTime.getTime()) && !$('#end_time').val()) {
            startTime.setHours(startTime.getHours() + 1);
            $('#end_time').val(startTime.toISOString().slice(0, 16));
        }
    });
});

function switchToGridView() {
    currentView = 'grid';
    $('#gridView').removeClass('btn-outline-primary').addClass('btn-primary');
    $('#calendarView').removeClass('btn-primary').addClass('btn-outline-primary');
    $('#gridViewContainer').show();
    $('#calendarViewContainer').hide();
    loadScheduleData();
}

function switchToCalendarView() {
    currentView = 'calendar';
    $('#calendarView').removeClass('btn-outline-primary').addClass('btn-primary');
    $('#gridView').removeClass('btn-primary').addClass('btn-outline-primary');
    $('#gridViewContainer').hide();
    $('#calendarViewContainer').show();
    loadScheduleData();
}

function loadScheduleData() {
    if (currentView === 'grid') {
        loadGridData();
    } else {
        loadCalendarData();
    }
}

function loadGridData() {
    let date = $('#dateFilter').val();
    let channelId = $('#channelFilter').val();
    
    $('#selectedDateDisplay').text(date);

    $.get('{{ route("admin.live-tv.schedule.grid") }}', {
        date: date,
        channel_id: channelId
    })
    .done(function(response) {
        if (response.success) {
            renderScheduleGrid(response.data);
        }
    })
    .fail(function() {
        showErrorToast('Failed to load schedule data');
    });
}

function loadCalendarData() {
    let date = new Date($('#dateFilter').val());
    let channelId = $('#channelFilter').val();
    
    $.get('{{ route("admin.live-tv.schedule.calendar") }}', {
        month: date.getMonth() + 1,
        year: date.getFullYear(),
        channel_id: channelId
    })
    .done(function(response) {
        if (response.success) {
            renderScheduleCalendar(response.data);
        }
    })
    .fail(function() {
        showErrorToast('Failed to load calendar data');
    });
}

function renderScheduleGrid(scheduleData) {
    let gridHtml = '<div class="schedule-timeline">';

    if (Object.keys(scheduleData).length === 0) {
        gridHtml += '<div class="alert alert-info">No programs scheduled for this date and channel selection.</div>';
    } else {
        Object.keys(scheduleData).forEach(function(channelId) {
            let channelSchedules = scheduleData[channelId];
            let channelInfo = @json($channels->keyBy('tv_channel_id'));
            let channel = channelInfo[channelId];

            if (channel) {
                gridHtml += `<div class="channel-row mb-4">
                    <div class="channel-header d-flex align-items-center mb-2">
                        <img src="${channel.thumbnail || '/assets/img/default.png'}" alt="${channel.title}" class="channel-thumb mr-3">
                        <div>
                            <h6 class="mb-0">${channel.channel_number ? '#' + String(channel.channel_number).padStart(3, '0') + ' - ' : ''}${channel.title}</h6>
                        </div>
                    </div>
                    <div class="programs-timeline">`;

                channelSchedules.forEach(function(program) {
                    let statusClass = program.is_currently_airing ? 'currently-airing' : '';
                    let repeatBadge = program.is_repeat ? '<span class="badge badge-warning badge-sm">Repeat</span>' : '';
                    let episodeInfo = program.episode_info ? `<small class="text-muted">${program.episode_info}</small>` : '';

                    gridHtml += `<div class="program-item ${statusClass}" data-id="${program.schedule_id}">
                        <div class="program-time">
                            <strong>${program.start_time} - ${program.end_time}</strong>
                            <small class="text-muted d-block">${program.duration_minutes} min</small>
                        </div>
                        <div class="program-content">
                            <div class="program-title">
                                ${program.program_title}
                                ${repeatBadge}
                                ${program.genre ? '<span class="badge badge-info badge-sm">' + program.genre + '</span>' : ''}
                            </div>
                            ${episodeInfo}
                            ${program.description ? '<small class="program-desc text-muted">' + program.description + '</small>' : ''}
                        </div>
                        <div class="program-actions">
                            <button class="btn btn-sm btn-primary" onclick="editSchedule(${program.schedule_id})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteSchedule(${program.schedule_id})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>`;
                });

                gridHtml += '</div></div>';
            }
        });
    }

    gridHtml += '</div>';
    $('#scheduleGrid').html(gridHtml);
}

function renderScheduleCalendar(calendarData) {
    if (calendar) {
        calendar.destroy();
    }

    let events = [];
    Object.keys(calendarData).forEach(function(date) {
        calendarData[date].forEach(function(program) {
            events.push({
                id: program.schedule_id,
                title: program.program_title,
                start: date + 'T' + program.start_time,
                end: date + 'T' + program.end_time,
                extendedProps: {
                    channel_title: program.channel_title,
                    genre: program.genre,
                    is_repeat: program.is_repeat
                }
            });
        });
    });

    calendar = new FullCalendar.Calendar(document.getElementById('scheduleCalendar'), {
        initialView: 'dayGridMonth',
        height: 600,
        events: events,
        eventClick: function(info) {
            editSchedule(info.event.id);
        },
        eventDisplay: 'block',
        dayMaxEvents: true,
        moreLinkClick: 'popover'
    });

    calendar.render();
}

function showAddScheduleModal() {
    isEditing = false;
    editingScheduleId = null;
    $('#scheduleModalLabel').text('Add New Program');
    $('#scheduleForm')[0].reset();
    $('#duplicateBtn').hide();
    
    // Set default channel if filtered
    let selectedChannel = $('#channelFilter').val();
    if (selectedChannel) {
        $('#tv_channel_id').val(selectedChannel);
    }

    // Set default start time to current date + current time
    let now = new Date();
    let defaultStart = $('#dateFilter').val() + 'T' + String(now.getHours()).padStart(2, '0') + ':00';
    $('#start_time').val(defaultStart);

    $('#scheduleModal').modal('show');
}

function editSchedule(scheduleId) {
    isEditing = true;
    editingScheduleId = scheduleId;
    $('#scheduleModalLabel').text('Edit Program');
    $('#duplicateBtn').show();

    $.get(`{{ route('admin.live-tv.schedule.show', ':id') }}`.replace(':id', scheduleId))
        .done(function(response) {
            if (response.success) {
                populateScheduleForm(response.data);
                $('#scheduleModal').modal('show');
            }
        })
        .fail(function() {
            showErrorToast('Failed to load schedule details');
        });
}

function populateScheduleForm(schedule) {
    $('#tv_channel_id').val(schedule.tv_channel_id);
    $('#program_title').val(schedule.program_title);
    $('#description').val(schedule.description);
    $('#genre').val(schedule.genre);
    $('#rating').val(schedule.rating);
    $('#original_air_year').val(schedule.original_air_year);
    $('#season_number').val(schedule.season_number);
    $('#episode_number').val(schedule.episode_number);
    $('#thumbnail_url').val(schedule.thumbnail_url);
    $('#is_repeat').prop('checked', schedule.is_repeat);

    // Format datetime for input
    let startTime = new Date(schedule.start_time).toISOString().slice(0, 16);
    let endTime = new Date(schedule.end_time).toISOString().slice(0, 16);
    $('#start_time').val(startTime);
    $('#end_time').val(endTime);
}

function saveSchedule() {
    let formData = new FormData($('#scheduleForm')[0]);
    
    let url = isEditing 
        ? `{{ route('admin.live-tv.schedule.update', ':id') }}`.replace(':id', editingScheduleId)
        : `{{ route('admin.live-tv.schedule.store') }}`;
    
    let method = isEditing ? 'PUT' : 'POST';

    showButtonLoading($('#scheduleForm button[type="submit"]'));

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
            $('#scheduleModal').modal('hide');
            loadScheduleData();
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
        hideButtonLoading($('#scheduleForm button[type="submit"]'));
    });
}

function deleteSchedule(scheduleId) {
    Swal.fire({
        title: 'Are you sure?',
        text: 'This will permanently delete the program from the schedule.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: `{{ route('admin.live-tv.schedule.destroy', ':id') }}`.replace(':id', scheduleId),
                type: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
                }
            })
            .done(function(response) {
                if (response.success) {
                    loadScheduleData();
                    showSuccessToast(response.message);
                }
            })
            .fail(function() {
                showErrorToast('Failed to delete program');
            });
        }
    });
}

function showBulkImportModal() {
    $('#bulkImportScheduleModal').modal('show');
}

function processBulkImportSchedule() {
    let formData = new FormData($('#bulkImportScheduleForm')[0]);
    
    showButtonLoading($('#bulkImportScheduleForm button[type="submit"]'));

    $.ajax({
        url: '{{ route("admin.live-tv.schedule.import") }}',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false
    })
    .done(function(response) {
        if (response.success) {
            $('#bulkImportScheduleModal').modal('hide');
            loadScheduleData();
            showSuccessToast(response.message);
        } else {
            showErrorToast(response.message);
        }
    })
    .fail(function() {
        showErrorToast('Import failed');
    })
    .always(function() {
        hideButtonLoading($('#bulkImportScheduleForm button[type="submit"]'));
    });
}

function downloadScheduleTemplate(format) {
    window.open(`{{ route('admin.live-tv.schedule.template', ':format') }}`.replace(':format', format), '_blank');
}

function exportSchedule() {
    let channelId = $('#channelFilter').val();
    let date = $('#dateFilter').val();
    let params = new URLSearchParams();
    if (channelId) params.append('channel_id', channelId);
    if (date) params.append('date', date);
    
    window.open(`/admin/live-tv/schedule/export?${params.toString()}`, '_blank');
}

function showDuplicateModal() {
    $('#scheduleModal').modal('hide');
    $('#duplicateScheduleModal').modal('show');
}

function duplicateSchedule() {
    let formData = new FormData($('#duplicateScheduleForm')[0]);
    
    showButtonLoading($('#duplicateScheduleForm button[type="submit"]'));

    $.ajax({
        url: `{{ route('admin.live-tv.schedule.duplicate', ':id') }}`.replace(':id', editingScheduleId),
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false
    })
    .done(function(response) {
        if (response.success) {
            $('#duplicateScheduleModal').modal('hide');
            loadScheduleData();
            showSuccessToast(response.message);
        } else {
            showErrorToast(response.message);
        }
    })
    .fail(function() {
        showErrorToast('Duplication failed');
    })
    .always(function() {
        hideButtonLoading($('#duplicateScheduleForm button[type="submit"]'));
    });
}

function addDateInput() {
    let dateInputHtml = `
        <div class="date-input-group d-flex mb-2">
            <input type="date" class="form-control" name="target_dates[]" required>
            <button type="button" class="btn btn-sm btn-danger ml-2 remove-date">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;
    $('.target-dates').append(dateInputHtml);
}

function refreshSchedule() {
    loadScheduleData();
    showSuccessToast('Schedule refreshed');
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
.schedule-timeline {
    max-height: 600px;
    overflow-y: auto;
}

.channel-row {
    border: 1px solid #e9ecef;
    border-radius: 8px;
    padding: 15px;
    background: #fff;
}

.channel-thumb {
    width: 40px;
    height: 30px;
    object-fit: cover;
    border-radius: 4px;
}

.programs-timeline {
    border-left: 3px solid #007bff;
    padding-left: 15px;
}

.program-item {
    display: flex;
    align-items: flex-start;
    padding: 10px;
    margin-bottom: 10px;
    background: #f8f9fa;
    border-radius: 6px;
    border-left: 4px solid #6c757d;
    transition: all 0.3s;
}

.program-item:hover {
    background: #e9ecef;
    transform: translateX(5px);
}

.program-item.currently-airing {
    border-left-color: #28a745;
    background: #d4edda;
}

.program-time {
    min-width: 100px;
    margin-right: 15px;
    font-size: 0.9em;
}

.program-content {
    flex: 1;
}

.program-title {
    font-weight: 600;
    margin-bottom: 5px;
}

.program-desc {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.program-actions {
    display: flex;
    gap: 5px;
}

.badge-sm {
    font-size: 0.75em;
    margin-left: 5px;
}

.date-input-group {
    align-items: center;
}

.section-buttons .btn {
    margin-left: 0.5rem;
}

@media (max-width: 768px) {
    .program-item {
        flex-direction: column;
    }
    
    .program-time {
        min-width: auto;
        margin-right: 0;
        margin-bottom: 10px;
    }
    
    .program-actions {
        margin-top: 10px;
        justify-content: flex-end;
    }
    
    .section-buttons {
        margin-top: 1rem;
    }
    
    .section-buttons .btn {
        margin-bottom: 0.5rem;
        width: 100%;
    }
}

/* FullCalendar customizations */
.fc-event {
    border: none !important;
    background: #007bff !important;
}

.fc-event-title {
    font-weight: 600;
}

.fc-daygrid-event {
    margin: 1px 0;
}
</style>
@endsection