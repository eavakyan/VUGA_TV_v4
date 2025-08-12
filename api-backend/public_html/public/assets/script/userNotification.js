$(document).ready(function() {
    // Helper function to show toast messages
    function showToast(type, message) {
        var config = {
            title: type === 'success' ? 'Success' : 'Error',
            message: message,
            color: type === 'success' ? 'green' : 'red',
            position: 'topRight',
            transitionIn: 'fadeInDown',
            transitionOut: 'fadeOutUp',
            timeout: type === 'success' ? 3000 : 4000,
            animateInside: false,
            iconUrl: domainUrl + 'assets/img/' + (type === 'success' ? 'check-circle.svg' : 'x.svg')
        };
        iziToast.show(config);
    }

    // Initialize DataTable
    var table = $('#userNotificationTable').DataTable({
        processing: true,
        serverSide: false,
        ajax: {
            url: '/api/v2/user-notification/admin/list',
            type: 'POST',
            headers: {
                'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
            },
            data: {
                per_page: 100
            },
            dataSrc: function(json) {
                if (json.status) {
                    return json.data;
                }
                return [];
            },
            error: function(xhr, error, thrown) {
                console.error('DataTable error:', error, thrown);
                showToast('error', 'Failed to load notifications');
            }
        },
        columns: [
            { 
                data: 'title',
                render: function(data) {
                    return data ? data : '-';
                }
            },
            { 
                data: 'message',
                render: function(data) {
                    return data ? (data.length > 50 ? data.substring(0, 50) + '...' : data) : '-';
                }
            },
            { 
                data: 'notification_type',
                render: function(data) {
                    var badge = 'secondary';
                    switch(data) {
                        case 'system': badge = 'primary'; break;
                        case 'promotional': badge = 'success'; break;
                        case 'update': badge = 'info'; break;
                        case 'maintenance': badge = 'warning'; break;
                    }
                    return '<span class="badge bg-' + badge + '">' + data + '</span>';
                }
            },
            { 
                data: 'priority',
                render: function(data) {
                    var badge = 'secondary';
                    switch(data) {
                        case 'urgent': badge = 'danger'; break;
                        case 'high': badge = 'warning'; break;
                        case 'medium': badge = 'info'; break;
                        case 'low': badge = 'secondary'; break;
                    }
                    return '<span class="badge bg-' + badge + '">' + data + '</span>';
                }
            },
            { 
                data: 'target_platforms',
                render: function(data) {
                    if (!data || data.length === 0) return '-';
                    return data.map(function(platform) {
                        return '<span class="badge bg-secondary me-1">' + platform + '</span>';
                    }).join('');
                }
            },
            { 
                data: 'is_active',
                render: function(data) {
                    return data ? '<span class="badge bg-success">Active</span>' : '<span class="badge bg-danger">Inactive</span>';
                }
            },
            { 
                data: null,
                orderable: false,
                render: function(data, type, row) {
                    return '<button class="btn btn-sm btn-info view-analytics" data-id="' + row.notification_id + '"><i class="fas fa-chart-bar"></i> View</button>';
                }
            },
            { 
                data: null,
                orderable: false,
                render: function(data, type, row) {
                    return '<div class="d-flex justify-content-end gap-2">' +
                        '<button class="btn btn-sm btn-primary edit-notification" data-id="' + row.notification_id + '"><i class="fas fa-edit"></i></button>' +
                        '<button class="btn btn-sm btn-danger delete-notification" data-id="' + row.notification_id + '"><i class="fas fa-trash"></i></button>' +
                        '</div>';
                }
            }
        ],
        order: [[0, 'desc']],
        drawCallback: function() {
            // Re-bind event handlers after table redraw
            bindEventHandlers();
        }
    });

    // Handle platform checkboxes
    $('.platform-check').on('change', function() {
        if ($(this).val() === 'all' && $(this).prop('checked')) {
            $('.platform-check').not(this).prop('checked', false);
        } else if ($(this).val() !== 'all' && $(this).prop('checked')) {
            $('#platform_all').prop('checked', false);
        }
    });

    $('.edit-platform-check').on('change', function() {
        if ($(this).val() === 'all' && $(this).prop('checked')) {
            $('.edit-platform-check').not(this).prop('checked', false);
        } else if ($(this).val() !== 'all' && $(this).prop('checked')) {
            $('#edit_platform_all').prop('checked', false);
        }
    });

    // Add notification form submission
    $('#addUserNotificationForm').on('submit', function(e) {
        e.preventDefault();
        
        var platforms = [];
        $('.platform-check:checked').each(function() {
            platforms.push($(this).val());
        });
        
        if (platforms.length === 0) {
            showToast('error', 'Please select at least one platform');
            return;
        }
        
        var formData = {
            title: $('#title').val(),
            message: $('#message').val(),
            notification_type: $('#notification_type').val(),
            priority: $('#priority').val(),
            target_platforms: platforms
        };
        
        // Only add dates if they have values
        if ($('#scheduled_at').val()) {
            formData.scheduled_at = $('#scheduled_at').val();
        }
        if ($('#expires_at').val()) {
            formData.expires_at = $('#expires_at').val();
        }
        
        console.log('Sending data:', formData);
        
        $.ajax({
            url: '/api/v2/user-notification/admin/create',
            type: 'POST',
            data: JSON.stringify(formData),
            contentType: 'application/json',
            headers: {
                'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
            },
            success: function(response) {
                if (response.status) {
                    showToast('success', 'Notification created successfully');
                    $('#addUserNotificationModal').modal('hide');
                    $('#addUserNotificationForm')[0].reset();
                    $('#platform_all').prop('checked', true);
                    table.ajax.reload();
                } else {
                    showToast('error', response.message || 'Failed to create notification');
                }
            },
            error: function(xhr) {
                console.log('Error response:', xhr);
                var errorMessage = 'An error occurred while creating notification';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMessage = xhr.responseJSON.message;
                } else if (xhr.responseText) {
                    console.log('Response text:', xhr.responseText);
                }
                showToast('error', errorMessage);
            }
        });
    });

    // Edit notification form submission
    $('#editUserNotificationForm').on('submit', function(e) {
        e.preventDefault();
        
        var platforms = [];
        $('.edit-platform-check:checked').each(function() {
            platforms.push($(this).val());
        });
        
        if (platforms.length === 0) {
            showToast('error', 'Please select at least one platform');
            return;
        }
        
        var notificationId = $('#edit_notification_id').val();
        var formData = {
            title: $('#edit_title').val(),
            message: $('#edit_message').val(),
            notification_type: $('#edit_notification_type').val(),
            priority: $('#edit_priority').val(),
            target_platforms: platforms,
            scheduled_at: $('#edit_scheduled_at').val() || null,
            expires_at: $('#edit_expires_at').val() || null,
            is_active: $('#edit_is_active').val() == '1'
        };
        
        $.ajax({
            url: '/api/v2/user-notification/admin/update/' + notificationId,
            type: 'POST',
            data: JSON.stringify(formData),
            contentType: 'application/json',
            headers: {
                'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
            },
            success: function(response) {
                if (response.status) {
                    showToast('success', 'Notification updated successfully');
                    $('#editUserNotificationModal').modal('hide');
                    table.ajax.reload();
                } else {
                    showToast('error', response.message || 'Failed to update notification');
                }
            },
            error: function(xhr) {
                showToast('error', 'An error occurred while updating notification');
            }
        });
    });

    // Bind event handlers
    function bindEventHandlers() {
        // Edit notification
        $('.edit-notification').off('click').on('click', function() {
            var notificationId = $(this).data('id');
            var rowData = table.row($(this).closest('tr')).data();
            
            $('#edit_notification_id').val(notificationId);
            $('#edit_title').val(rowData.title);
            $('#edit_message').val(rowData.message);
            $('#edit_notification_type').val(rowData.notification_type);
            $('#edit_priority').val(rowData.priority);
            $('#edit_is_active').val(rowData.is_active ? '1' : '0');
            
            // Set scheduled and expires dates
            if (rowData.scheduled_at) {
                var scheduledDate = new Date(rowData.scheduled_at);
                $('#edit_scheduled_at').val(scheduledDate.toISOString().slice(0, 16));
            }
            if (rowData.expires_at) {
                var expiresDate = new Date(rowData.expires_at);
                $('#edit_expires_at').val(expiresDate.toISOString().slice(0, 16));
            }
            
            // Set platforms
            $('.edit-platform-check').prop('checked', false);
            if (rowData.target_platforms) {
                rowData.target_platforms.forEach(function(platform) {
                    $('#edit_platform_' + platform).prop('checked', true);
                });
            }
            
            $('#editUserNotificationModal').modal('show');
        });

        // Delete notification
        $('.delete-notification').off('click').on('click', function() {
            var notificationId = $(this).data('id');
            
            Swal.fire({
                title: 'Are you sure?',
                text: "This will deactivate the notification!",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#3085d6',
                cancelButtonColor: '#d33',
                confirmButtonText: 'Yes, delete it!'
            }).then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        url: '/api/v2/user-notification/admin/delete/' + notificationId,
                        type: 'DELETE',
                        headers: {
                            'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
                        },
                        success: function(response) {
                            if (response.status) {
                                showToast('success', 'Notification deleted successfully');
                                table.ajax.reload();
                            } else {
                                showToast('error', response.message || 'Failed to delete notification');
                            }
                        },
                        error: function(xhr) {
                            showToast('error', 'An error occurred while deleting notification');
                        }
                    });
                }
            });
        });

        // View analytics
        $('.view-analytics').off('click').on('click', function() {
            var notificationId = $(this).data('id');
            
            $('#analyticsContent').html('<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>');
            $('#analyticsModal').modal('show');
            
            $.ajax({
                url: '/api/v2/user-notification/admin/analytics/' + notificationId,
                type: 'GET',
                headers: {
                    'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
                },
                success: function(response) {
                    if (response.status) {
                        var data = response.data;
                        var analytics = data.analytics;
                        var notification = data.notification;
                        
                        var html = '<div class="row">';
                        html += '<div class="col-md-12 mb-3">';
                        html += '<h5>' + notification.title + '</h5>';
                        html += '<p class="text-muted">' + notification.message + '</p>';
                        html += '</div>';
                        
                        html += '<div class="col-md-4 mb-3">';
                        html += '<div class="card">';
                        html += '<div class="card-body text-center">';
                        html += '<h6 class="card-subtitle mb-2 text-muted">Total Eligible Profiles</h6>';
                        html += '<h3 class="card-title">' + (analytics.total_eligible_profiles || 0) + '</h3>';
                        html += '</div></div></div>';
                        
                        html += '<div class="col-md-4 mb-3">';
                        html += '<div class="card">';
                        html += '<div class="card-body text-center">';
                        html += '<h6 class="card-subtitle mb-2 text-muted">Total Shown</h6>';
                        html += '<h3 class="card-title">' + (analytics.total_shown || 0) + '</h3>';
                        html += '<p class="mb-0 text-success">' + (analytics.show_rate || 0) + '% Show Rate</p>';
                        html += '</div></div></div>';
                        
                        html += '<div class="col-md-4 mb-3">';
                        html += '<div class="card">';
                        html += '<div class="card-body text-center">';
                        html += '<h6 class="card-subtitle mb-2 text-muted">Total Dismissed</h6>';
                        html += '<h3 class="card-title">' + (analytics.total_dismissed || 0) + '</h3>';
                        html += '<p class="mb-0 text-warning">' + (analytics.dismiss_rate || 0) + '% Dismiss Rate</p>';
                        html += '</div></div></div>';
                        
                        html += '<div class="col-md-12">';
                        html += '<h6>Platform Breakdown</h6>';
                        html += '<table class="table table-sm">';
                        html += '<thead><tr><th>Platform</th><th>Shown</th></tr></thead>';
                        html += '<tbody>';
                        html += '<tr><td>iOS</td><td>' + (analytics.ios_shown || 0) + '</td></tr>';
                        html += '<tr><td>Android</td><td>' + (analytics.android_shown || 0) + '</td></tr>';
                        html += '<tr><td>Android TV</td><td>' + (analytics.android_tv_shown || 0) + '</td></tr>';
                        html += '</tbody></table>';
                        html += '</div>';
                        
                        html += '</div>';
                        
                        $('#analyticsContent').html(html);
                    } else {
                        $('#analyticsContent').html('<div class="alert alert-danger">Failed to load analytics</div>');
                    }
                },
                error: function(xhr) {
                    $('#analyticsContent').html('<div class="alert alert-danger">An error occurred while loading analytics</div>');
                }
            });
        });
    }

    // Initial binding
    bindEventHandlers();
});