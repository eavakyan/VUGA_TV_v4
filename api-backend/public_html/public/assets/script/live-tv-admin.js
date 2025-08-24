/**
 * Live TV Admin Management JavaScript
 * Enhanced functionality for Live TV admin interface
 */

class LiveTvAdmin {
    constructor() {
        this.baseUrl = window.location.origin;
        this.csrfToken = $('meta[name="csrf-token"]').attr('content');
        this.init();
    }

    init() {
        this.bindEvents();
        this.initializeComponents();
    }

    bindEvents() {
        // Global keyboard shortcuts
        $(document).on('keydown', (e) => {
            // Ctrl/Cmd + N for new channel/category/schedule
            if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
                e.preventDefault();
                this.handleQuickAdd();
            }
            
            // Ctrl/Cmd + S for save (in modals)
            if ((e.ctrlKey || e.metaKey) && e.key === 's' && $('.modal').is(':visible')) {
                e.preventDefault();
                $('.modal:visible form').submit();
            }
            
            // Escape to close modals
            if (e.key === 'Escape') {
                $('.modal:visible').modal('hide');
            }
        });

        // Auto-save draft functionality
        this.initAutoSave();
        
        // Real-time validation
        this.initRealTimeValidation();
        
        // Enhanced drag and drop
        this.initDragAndDrop();
    }

    initializeComponents() {
        // Initialize tooltips
        $('[data-toggle="tooltip"]').tooltip();
        
        // Initialize date pickers with proper formatting
        this.initDatePickers();
        
        // Initialize file upload areas
        this.initFileUpload();
        
        // Initialize clipboard functionality
        this.initClipboard();
        
        // Initialize export functionality
        this.initExportHandlers();
    }

    // Quick add functionality based on current page
    handleQuickAdd() {
        const currentPath = window.location.pathname;
        
        if (currentPath.includes('channels')) {
            if (typeof showAddChannelModal === 'function') {
                showAddChannelModal();
            }
        } else if (currentPath.includes('schedule')) {
            if (typeof showAddScheduleModal === 'function') {
                showAddScheduleModal();
            }
        } else if (currentPath.includes('categories')) {
            if (typeof showAddCategoryModal === 'function') {
                showAddCategoryModal();
            }
        }
    }

    // Auto-save functionality for forms
    initAutoSave() {
        let autoSaveTimer;
        const AUTOSAVE_DELAY = 10000; // 10 seconds

        $(document).on('input', '.auto-save-form input, .auto-save-form textarea, .auto-save-form select', () => {
            clearTimeout(autoSaveTimer);
            
            // Show auto-save indicator
            $('.auto-save-indicator').remove();
            $('<span class="auto-save-indicator text-warning ml-2"><i class="fas fa-clock"></i> Auto-saving...</span>')
                .appendTo('.modal-title:visible');
            
            autoSaveTimer = setTimeout(() => {
                this.performAutoSave();
            }, AUTOSAVE_DELAY);
        });
    }

    performAutoSave() {
        const visibleModal = $('.modal:visible');
        if (visibleModal.length === 0) return;

        const form = visibleModal.find('form');
        const formData = new FormData(form[0]);
        const formId = form.attr('id');
        
        // Save to localStorage as backup
        const formDataObj = {};
        for (let [key, value] of formData.entries()) {
            formDataObj[key] = value;
        }
        
        localStorage.setItem(`autosave_${formId}`, JSON.stringify(formDataObj));
        
        // Update indicator
        $('.auto-save-indicator').html('<i class="fas fa-check text-success"></i> Auto-saved').removeClass('text-warning');
        
        setTimeout(() => {
            $('.auto-save-indicator').fadeOut();
        }, 2000);
    }

    // Real-time form validation
    initRealTimeValidation() {
        $(document).on('blur', 'input[required], select[required], textarea[required]', function() {
            const $field = $(this);
            const value = $field.val().trim();
            
            $field.removeClass('is-invalid is-valid');
            $field.next('.invalid-feedback').remove();
            
            if (value === '') {
                $field.addClass('is-invalid');
                $field.after('<div class="invalid-feedback">This field is required</div>');
            } else {
                // Field-specific validation
                if ($field.attr('type') === 'url' && !this.isValidUrl(value)) {
                    $field.addClass('is-invalid');
                    $field.after('<div class="invalid-feedback">Please enter a valid URL</div>');
                } else if ($field.attr('type') === 'email' && !this.isValidEmail(value)) {
                    $field.addClass('is-invalid');
                    $field.after('<div class="invalid-feedback">Please enter a valid email</div>');
                } else {
                    $field.addClass('is-valid');
                }
            }
        });
    }

    // Enhanced drag and drop for file uploads and sorting
    initDragAndDrop() {
        // File drop areas
        $(document).on('dragover', '.file-drop-area', function(e) {
            e.preventDefault();
            $(this).addClass('drag-over');
        });

        $(document).on('dragleave', '.file-drop-area', function(e) {
            e.preventDefault();
            $(this).removeClass('drag-over');
        });

        $(document).on('drop', '.file-drop-area', function(e) {
            e.preventDefault();
            $(this).removeClass('drag-over');
            
            const files = e.originalEvent.dataTransfer.files;
            if (files.length > 0) {
                const fileInput = $(this).find('input[type="file"]')[0];
                if (fileInput) {
                    fileInput.files = files;
                    $(fileInput).trigger('change');
                }
            }
        });
    }

    // Enhanced date pickers
    initDatePickers() {
        if (typeof flatpickr !== 'undefined') {
            $('.datetime-picker').flatpickr({
                enableTime: true,
                dateFormat: "Y-m-d H:i",
                time_24hr: true,
                minuteIncrement: 15
            });

            $('.date-picker').flatpickr({
                dateFormat: "Y-m-d"
            });

            $('.time-picker').flatpickr({
                enableTime: true,
                noCalendar: true,
                dateFormat: "H:i",
                time_24hr: true,
                minuteIncrement: 15
            });
        }
    }

    // File upload enhancements
    initFileUpload() {
        $(document).on('change', 'input[type="file"]', function() {
            const file = this.files[0];
            const $input = $(this);
            const $preview = $input.siblings('.file-preview');
            
            if (file) {
                // Show file info
                const fileInfo = `
                    <div class="file-info mt-2">
                        <small class="text-muted">
                            <i class="fas fa-file"></i> ${file.name} (${this.formatFileSize(file.size)})
                        </small>
                    </div>
                `;
                $input.after(fileInfo);
                
                // Image preview
                if (file.type.startsWith('image/')) {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        $preview.html(`<img src="${e.target.result}" class="img-thumbnail mt-2" style="max-width: 200px; max-height: 150px;">`);
                    };
                    reader.readAsDataURL(file);
                }
            }
        });
    }

    // Clipboard functionality
    initClipboard() {
        if (typeof ClipboardJS !== 'undefined') {
            new ClipboardJS('.copy-btn');
            
            $(document).on('click', '.copy-btn', function() {
                showSuccessToast('Copied to clipboard');
            });
        }
    }

    // Export handlers
    initExportHandlers() {
        $(document).on('click', '.export-btn', function() {
            const format = $(this).data('format') || 'csv';
            const type = $(this).data('type') || 'data';
            const url = $(this).data('url');
            
            if (url) {
                window.open(`${url}?format=${format}`, '_blank');
            }
        });
    }

    // URL validation
    isValidUrl(string) {
        try {
            new URL(string);
            return true;
        } catch (_) {
            return false;
        }
    }

    // Email validation
    isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // File size formatting
    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // Advanced search functionality
    initAdvancedSearch() {
        let searchTimer;
        
        $(document).on('input', '.advanced-search', function() {
            const $input = $(this);
            const searchTerm = $input.val();
            const targetTable = $input.data('target');
            
            clearTimeout(searchTimer);
            
            searchTimer = setTimeout(() => {
                if (targetTable && window[targetTable]) {
                    window[targetTable].search(searchTerm).draw();
                }
            }, 300);
        });
    }

    // Bulk operations enhancement
    enhanceBulkOperations() {
        // Select all with filters
        $(document).on('change', '.select-all-filtered', function() {
            const isChecked = $(this).is(':checked');
            const table = $(this).closest('table');
            const visibleCheckboxes = table.find('tbody tr:visible .bulk-checkbox');
            
            visibleCheckboxes.prop('checked', isChecked);
            this.updateBulkActionButtons();
        });
    }

    updateBulkActionButtons() {
        const selectedCount = $('.bulk-checkbox:checked').length;
        const $bulkActions = $('.bulk-actions');
        
        if (selectedCount > 0) {
            $bulkActions.show();
            $bulkActions.find('.selection-count').text(selectedCount);
        } else {
            $bulkActions.hide();
        }
    }

    // Theme management
    initThemeToggle() {
        const currentTheme = localStorage.getItem('admin-theme') || 'light';
        this.applyTheme(currentTheme);
        
        $(document).on('click', '.theme-toggle', () => {
            const newTheme = currentTheme === 'light' ? 'dark' : 'light';
            localStorage.setItem('admin-theme', newTheme);
            this.applyTheme(newTheme);
        });
    }

    applyTheme(theme) {
        $('body').removeClass('light-theme dark-theme').addClass(`${theme}-theme`);
    }

    // API helper methods
    async apiCall(endpoint, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': this.csrfToken,
                'Accept': 'application/json'
            }
        };
        
        const config = { ...defaultOptions, ...options };
        
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, config);
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || 'API call failed');
            }
            
            return data;
        } catch (error) {
            console.error('API call error:', error);
            showErrorToast(error.message);
            throw error;
        }
    }

    // Channel-specific helpers
    validateChannelData(formData) {
        const errors = [];
        
        if (!formData.get('title')) {
            errors.push('Channel title is required');
        }
        
        if (!formData.get('stream_url')) {
            errors.push('Stream URL is required');
        } else if (!this.isValidUrl(formData.get('stream_url'))) {
            errors.push('Stream URL must be a valid URL');
        }
        
        const channelNumber = formData.get('channel_number');
        if (channelNumber && (channelNumber < 1 || channelNumber > 9999)) {
            errors.push('Channel number must be between 1 and 9999');
        }
        
        return errors;
    }

    // Schedule-specific helpers
    validateScheduleData(formData) {
        const errors = [];
        
        if (!formData.get('tv_channel_id')) {
            errors.push('Channel selection is required');
        }
        
        if (!formData.get('program_title')) {
            errors.push('Program title is required');
        }
        
        const startTime = new Date(formData.get('start_time'));
        const endTime = new Date(formData.get('end_time'));
        
        if (startTime >= endTime) {
            errors.push('End time must be after start time');
        }
        
        return errors;
    }

    // Real-time updates via WebSocket (if available)
    initRealTimeUpdates() {
        if (typeof Echo !== 'undefined') {
            Echo.channel('live-tv-admin')
                .listen('ChannelUpdated', (e) => {
                    this.handleChannelUpdate(e.channel);
                })
                .listen('ScheduleUpdated', (e) => {
                    this.handleScheduleUpdate(e.schedule);
                });
        }
    }

    handleChannelUpdate(channel) {
        // Update channel in any visible tables
        $(`.channel-row[data-id="${channel.id}"]`).addClass('updated-row');
        showInfoToast(`Channel "${channel.title}" was updated`);
        
        // Auto-refresh tables after 2 seconds
        setTimeout(() => {
            if (typeof channelsTable !== 'undefined') {
                channelsTable.ajax.reload(null, false);
            }
        }, 2000);
    }

    handleScheduleUpdate(schedule) {
        showInfoToast(`Schedule for "${schedule.program_title}" was updated`);
        
        // Refresh schedule views
        if (typeof loadScheduleData === 'function') {
            setTimeout(loadScheduleData, 2000);
        }
    }

    // Performance monitoring
    initPerformanceMonitoring() {
        // Track page load times
        window.addEventListener('load', () => {
            const loadTime = performance.now();
            console.log(`Page loaded in ${loadTime.toFixed(2)}ms`);
        });
        
        // Track AJAX performance
        $(document).ajaxComplete((event, xhr, settings) => {
            if (xhr.responseTime) {
                console.log(`AJAX ${settings.type} ${settings.url} completed in ${xhr.responseTime}ms`);
            }
        });
    }

    // Accessibility enhancements
    initAccessibility() {
        // Add ARIA labels to dynamically created elements
        $(document).on('DOMNodeInserted', function(e) {
            const $element = $(e.target);
            
            // Add labels to buttons without text
            $element.find('button:not([aria-label]):has(i.fas)').each(function() {
                const iconClass = $(this).find('i').attr('class');
                let label = 'Button';
                
                if (iconClass.includes('fa-edit')) label = 'Edit';
                else if (iconClass.includes('fa-trash')) label = 'Delete';
                else if (iconClass.includes('fa-eye')) label = 'View';
                else if (iconClass.includes('fa-download')) label = 'Download';
                
                $(this).attr('aria-label', label);
            });
        });
        
        // Keyboard navigation for tables
        $(document).on('keydown', '.table tbody tr', function(e) {
            const $current = $(this);
            let $next;
            
            switch(e.key) {
                case 'ArrowDown':
                    $next = $current.next();
                    break;
                case 'ArrowUp':
                    $next = $current.prev();
                    break;
                case 'Enter':
                    $current.find('button:first').click();
                    return;
            }
            
            if ($next && $next.length) {
                $next.focus();
                e.preventDefault();
            }
        });
    }

    // Error handling and logging
    initErrorHandling() {
        window.addEventListener('error', (e) => {
            console.error('Global error:', e.error);
            
            // Send error to logging service if available
            this.logError({
                message: e.message,
                filename: e.filename,
                lineno: e.lineno,
                colno: e.colno,
                error: e.error?.stack
            });
        });
        
        window.addEventListener('unhandledrejection', (e) => {
            console.error('Unhandled promise rejection:', e.reason);
            this.logError({
                type: 'unhandledrejection',
                reason: e.reason
            });
        });
    }

    async logError(errorData) {
        try {
            await this.apiCall('/admin/log-error', {
                method: 'POST',
                body: JSON.stringify(errorData)
            });
        } catch (err) {
            console.error('Failed to log error:', err);
        }
    }
}

// Utility functions for global use
window.LiveTvUtils = {
    formatNumber: (number) => {
        return new Intl.NumberFormat().format(number);
    },
    
    formatDuration: (seconds) => {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;
        
        if (hours > 0) {
            return `${hours}h ${minutes}m ${secs}s`;
        } else if (minutes > 0) {
            return `${minutes}m ${secs}s`;
        } else {
            return `${secs}s`;
        }
    },
    
    formatFileSize: (bytes) => {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    debounce: (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },
    
    throttle: (func, limit) => {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
};

// Initialize when DOM is ready
$(document).ready(() => {
    window.liveTvAdmin = new LiveTvAdmin();
});

// Global toast functions for consistency
function showSuccessToast(message) {
    iziToast.success({
        title: 'Success',
        message: message,
        position: 'topRight',
        timeout: 5000
    });
}

function showErrorToast(message) {
    iziToast.error({
        title: 'Error',
        message: message,
        position: 'topRight',
        timeout: 8000
    });
}

function showWarningToast(message) {
    iziToast.warning({
        title: 'Warning',
        message: message,
        position: 'topRight',
        timeout: 6000
    });
}

function showInfoToast(message) {
    iziToast.info({
        title: 'Info',
        message: message,
        position: 'topRight',
        timeout: 4000
    });
}

// Loading utilities
function showButtonLoading(button) {
    const $btn = $(button);
    $btn.prop('disabled', true);
    $btn.find('.btn-text').hide();
    $btn.find('.spinner-border').removeClass('d-none');
}

function hideButtonLoading(button) {
    const $btn = $(button);
    $btn.prop('disabled', false);
    $btn.find('.btn-text').show();
    $btn.find('.spinner-border').addClass('d-none');
}

function showLoading(element) {
    $(element).html('<div class="text-center p-4"><div class="spinner-border" role="status"></div><div class="mt-2">Loading...</div></div>');
}

function hideLoading(element) {
    // This would typically be handled by replacing content
}

// Enhanced confirmation dialogs
function confirmAction(title, text, confirmCallback, cancelCallback) {
    Swal.fire({
        title: title,
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, proceed!',
        cancelButtonText: 'Cancel'
    }).then((result) => {
        if (result.isConfirmed && typeof confirmCallback === 'function') {
            confirmCallback();
        } else if (typeof cancelCallback === 'function') {
            cancelCallback();
        }
    });
}

// Export the main class for module use
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LiveTvAdmin;
}