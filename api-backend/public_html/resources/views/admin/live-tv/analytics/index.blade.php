@extends('include.app')

@section('content')
<section class="section">
    <div class="section-body">
        <div class="d-flex justify-content-between">
            <div class="section-title">
                <h3>{{ __('Live TV Analytics Dashboard') }}</h3>
            </div>
            <div class="section-buttons">
                <div class="btn-group">
                    <button class="btn btn-outline-primary" id="refreshDashboard">
                        <i class="fas fa-sync"></i> Refresh
                    </button>
                    <button class="btn btn-success" onclick="exportAnalytics()">
                        <i class="fas fa-download"></i> Export Data
                    </button>
                </div>
            </div>
        </div>

        <!-- Filters -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3">
                                <div class="form-group">
                                    <label for="dateRange">Date Range:</label>
                                    <select class="form-control" id="dateRange">
                                        <option value="7">Last 7 Days</option>
                                        <option value="30" selected>Last 30 Days</option>
                                        <option value="90">Last 90 Days</option>
                                        <option value="365">Last Year</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <label for="channelFilter">Channel:</label>
                                    <select class="form-control" id="channelFilter">
                                        <option value="">All Channels</option>
                                        @foreach($channels as $channel)
                                            <option value="{{ $channel->tv_channel_id }}">
                                                {{ $channel->channel_number ? '#' . str_pad($channel->channel_number, 3, '0', STR_PAD_LEFT) . ' - ' : '' }}{{ $channel->title }}
                                            </option>
                                        @endforeach
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <label for="categoryFilter">Category:</label>
                                    <select class="form-control" id="categoryFilter">
                                        <option value="">All Categories</option>
                                        @foreach($categories as $category)
                                            <option value="{{ $category->tv_category_id }}">{{ $category->title }}</option>
                                        @endforeach
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="form-group">
                                    <label>&nbsp;</label>
                                    <button class="btn btn-primary btn-block" onclick="applyFilters()">
                                        <i class="fas fa-filter"></i> Apply Filters
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Overview Stats -->
        <div class="row mb-4">
            <div class="col-lg-3 col-md-6 col-sm-6 col-12">
                <div class="card card-statistic-1">
                    <div class="card-icon bg-primary">
                        <i class="fas fa-eye"></i>
                    </div>
                    <div class="card-wrap">
                        <div class="card-header">
                            <h4>Total Views</h4>
                        </div>
                        <div class="card-body" id="totalViews">
                            <div class="loading-placeholder">Loading...</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 col-sm-6 col-12">
                <div class="card card-statistic-1">
                    <div class="card-icon bg-success">
                        <i class="fas fa-users"></i>
                    </div>
                    <div class="card-wrap">
                        <div class="card-header">
                            <h4>Unique Viewers</h4>
                        </div>
                        <div class="card-body" id="uniqueViewers">
                            <div class="loading-placeholder">Loading...</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 col-sm-6 col-12">
                <div class="card card-statistic-1">
                    <div class="card-icon bg-warning">
                        <i class="fas fa-tv"></i>
                    </div>
                    <div class="card-wrap">
                        <div class="card-header">
                            <h4>Active Channels</h4>
                        </div>
                        <div class="card-body" id="activeChannels">
                            <div class="loading-placeholder">Loading...</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 col-sm-6 col-12">
                <div class="card card-statistic-1">
                    <div class="card-icon bg-info">
                        <i class="fas fa-chart-line"></i>
                    </div>
                    <div class="card-wrap">
                        <div class="card-header">
                            <h4>Avg. Views/User</h4>
                        </div>
                        <div class="card-body" id="avgViewsPerUser">
                            <div class="loading-placeholder">Loading...</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Charts Row 1 -->
        <div class="row mb-4">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <h4>Daily Views Trend</h4>
                        <div class="card-header-action">
                            <div class="btn-group">
                                <button class="btn btn-sm btn-outline-primary" onclick="switchChartType('dailyChart', 'line')">Line</button>
                                <button class="btn btn-sm btn-primary" onclick="switchChartType('dailyChart', 'bar')">Bar</button>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <canvas id="dailyChart" width="400" height="200"></canvas>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-header">
                        <h4>Device Breakdown</h4>
                    </div>
                    <div class="card-body">
                        <canvas id="deviceChart" width="400" height="300"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <!-- Charts Row 2 -->
        <div class="row mb-4">
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header">
                        <h4>Peak Viewing Hours</h4>
                    </div>
                    <div class="card-body">
                        <canvas id="hourlyChart" width="400" height="200"></canvas>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header">
                        <h4>Geographic Distribution</h4>
                        <div class="card-header-action">
                            <button class="btn btn-sm btn-outline-primary" onclick="loadGeographicData()">
                                <i class="fas fa-globe"></i> View More
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <canvas id="countryChart" width="400" height="200"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <!-- Top Channels and Popular Programs -->
        <div class="row mb-4">
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header">
                        <h4>Top Channels by Views</h4>
                        <div class="card-header-action">
                            <button class="btn btn-sm btn-outline-primary" onclick="showChannelComparison()">
                                <i class="fas fa-chart-bar"></i> Compare
                            </button>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <div id="topChannelsList">
                            <!-- Content will be loaded here -->
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header">
                        <h4>Popular Programs</h4>
                        <div class="card-header-action">
                            <select class="form-control form-control-sm" id="genreFilter" style="width: auto;">
                                <option value="">All Genres</option>
                                <option value="News">News</option>
                                <option value="Sports">Sports</option>
                                <option value="Entertainment">Entertainment</option>
                                <option value="Movies">Movies</option>
                                <option value="Drama">Drama</option>
                                <option value="Comedy">Comedy</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <div id="popularProgramsList">
                            <!-- Content will be loaded here -->
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Viewer Retention -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h4>Viewer Retention & Engagement</h4>
                        <div class="card-header-action">
                            <button class="btn btn-sm btn-info" onclick="loadRetentionData()">
                                <i class="fas fa-refresh"></i> Refresh
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-lg-8">
                                <canvas id="retentionChart" width="400" height="200"></canvas>
                            </div>
                            <div class="col-lg-4">
                                <div id="retentionStats">
                                    <!-- Stats will be loaded here -->
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Genre Analytics -->
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h4>Content Genre Performance</h4>
                    </div>
                    <div class="card-body">
                        <canvas id="genreChart" width="400" height="150"></canvas>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Channel Comparison Modal -->
<div class="modal fade" id="channelComparisonModal" tabindex="-1" role="dialog" aria-labelledby="channelComparisonModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="channelComparisonModalLabel">Channel Performance Comparison</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="compareChannels">Select Channels to Compare:</label>
                        <select class="form-control selectric" id="compareChannels" multiple>
                            @foreach($channels as $channel)
                                <option value="{{ $channel->tv_channel_id }}">
                                    {{ $channel->channel_number ? '#' . str_pad($channel->channel_number, 3, '0', STR_PAD_LEFT) . ' - ' : '' }}{{ $channel->title }}
                                </option>
                            @endforeach
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label>&nbsp;</label>
                        <button class="btn btn-primary btn-block" onclick="loadChannelComparison()">
                            <i class="fas fa-chart-line"></i> Generate Comparison
                        </button>
                    </div>
                </div>
                <div id="comparisonResults">
                    <!-- Comparison results will be loaded here -->
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-success" onclick="exportComparison()">
                    <i class="fas fa-download"></i> Export Comparison
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Geographic Details Modal -->
<div class="modal fade" id="geographicModal" tabindex="-1" role="dialog" aria-labelledby="geographicModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="geographicModalLabel">Geographic Distribution Details</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="geographicDetails">
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
<!-- Chart.js -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<script>
let charts = {};
let currentDateRange = '30';
let currentChannel = '';
let currentCategory = '';

$(document).ready(function() {
    // Initialize dashboard
    loadDashboardData();
    
    // Filter handlers
    $('#dateRange').change(function() {
        currentDateRange = $(this).val();
    });
    
    $('#channelFilter').change(function() {
        currentChannel = $(this).val();
    });
    
    $('#categoryFilter').change(function() {
        currentCategory = $(this).val();
    });
    
    $('#genreFilter').change(function() {
        loadPopularPrograms();
    });
    
    $('#refreshDashboard').click(function() {
        loadDashboardData();
    });

    // Auto-refresh every 5 minutes
    setInterval(function() {
        if (!$('.modal').is(':visible')) {
            loadDashboardData();
        }
    }, 300000); // 5 minutes
});

function applyFilters() {
    currentDateRange = $('#dateRange').val();
    currentChannel = $('#channelFilter').val();
    currentCategory = $('#categoryFilter').val();
    loadDashboardData();
}

function loadDashboardData() {
    showDashboardLoading();
    
    // Load overview stats
    loadOverviewStats();
    
    // Load charts
    loadDailyTrend();
    loadDeviceBreakdown();
    loadHourlyViews();
    loadCountryDistribution();
    loadTopChannels();
    loadPopularPrograms();
    loadRetentionData();
    loadGenreAnalytics();
}

function showDashboardLoading() {
    $('.loading-placeholder').show();
    $('.card-statistic-1 .card-body').not(':has(.loading-placeholder)').html('<div class="loading-placeholder">Loading...</div>');
}

function loadOverviewStats() {
    $.get('{{ route("admin.live-tv.analytics.dashboard") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel,
        category_id: currentCategory
    })
    .done(function(response) {
        if (response.success) {
            let data = response.data;
            
            $('#totalViews').html(formatNumber(data.overview.total_views));
            $('#uniqueViewers').html(formatNumber(data.overview.unique_viewers));
            $('#activeChannels').html(`${data.overview.active_channels}/${data.overview.total_channels}`);
            $('#avgViewsPerUser').html(data.overview.avg_views_per_user);
        }
    })
    .fail(function() {
        showErrorToast('Failed to load overview stats');
    });
}

function loadDailyTrend() {
    $.get('{{ route("admin.live-tv.analytics.dashboard") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel
    })
    .done(function(response) {
        if (response.success) {
            renderDailyChart(response.data.daily_views);
        }
    });
}

function renderDailyChart(data) {
    let ctx = document.getElementById('dailyChart').getContext('2d');
    
    if (charts.dailyChart) {
        charts.dailyChart.destroy();
    }
    
    let labels = Object.keys(data);
    let values = Object.values(data);
    
    charts.dailyChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Daily Views',
                data: values,
                borderColor: '#007bff',
                backgroundColor: 'rgba(0, 123, 255, 0.1)',
                borderWidth: 2,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function loadDeviceBreakdown() {
    $.get('{{ route("admin.live-tv.analytics.dashboard") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel
    })
    .done(function(response) {
        if (response.success) {
            renderDeviceChart(response.data.device_stats);
        }
    });
}

function renderDeviceChart(data) {
    let ctx = document.getElementById('deviceChart').getContext('2d');
    
    if (charts.deviceChart) {
        charts.deviceChart.destroy();
    }
    
    let labels = Object.keys(data);
    let values = Object.values(data);
    
    charts.deviceChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    '#007bff',
                    '#28a745',
                    '#ffc107',
                    '#dc3545',
                    '#17a2b8',
                    '#6f42c1'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false
        }
    });
}

function loadHourlyViews() {
    $.get('{{ route("admin.live-tv.analytics.peak-times") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel
    })
    .done(function(response) {
        if (response.success) {
            renderHourlyChart(response.data.hourly_stats);
        }
    });
}

function renderHourlyChart(data) {
    let ctx = document.getElementById('hourlyChart').getContext('2d');
    
    if (charts.hourlyChart) {
        charts.hourlyChart.destroy();
    }
    
    let labels = data.map(item => item.hour_label);
    let values = data.map(item => item.views);
    
    charts.hourlyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Views',
                data: values,
                backgroundColor: 'rgba(255, 193, 7, 0.8)',
                borderColor: '#ffc107',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function loadCountryDistribution() {
    $.get('{{ route("admin.live-tv.analytics.geographic") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel
    })
    .done(function(response) {
        if (response.success) {
            renderCountryChart(response.data);
        }
    });
}

function renderCountryChart(data) {
    let ctx = document.getElementById('countryChart').getContext('2d');
    
    if (charts.countryChart) {
        charts.countryChart.destroy();
    }
    
    // Take top 10 countries
    let topCountries = data.slice(0, 10);
    let labels = topCountries.map(item => item.country);
    let values = topCountries.map(item => item.views);
    
    charts.countryChart = new Chart(ctx, {
        type: 'horizontalBar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Views',
                data: values,
                backgroundColor: 'rgba(23, 162, 184, 0.8)',
                borderColor: '#17a2b8',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    beginAtZero: true
                }
            }
        }
    });
}

function loadTopChannels() {
    $.get('{{ route("admin.live-tv.analytics.dashboard") }}', {
        date_range: currentDateRange
    })
    .done(function(response) {
        if (response.success) {
            renderTopChannelsList(response.data.top_channels);
        }
    });
}

function renderTopChannelsList(channels) {
    let html = '<div class="table-responsive">';
    html += '<table class="table table-striped">';
    html += '<thead><tr><th>#</th><th>Channel</th><th>Views</th><th>Action</th></tr></thead><tbody>';
    
    channels.forEach(function(channel, index) {
        html += `<tr>
            <td>${index + 1}</td>
            <td>
                <div class="d-flex align-items-center">
                    <img src="${channel.channel.thumbnail || '/assets/img/default.png'}" alt="${channel.channel.title}" class="rounded mr-2" width="30" height="20">
                    <div>
                        <div class="font-weight-bold">${channel.channel.title}</div>
                        <small class="text-muted">${channel.channel.channel_number ? '#' + channel.channel.channel_number : 'No number'}</small>
                    </div>
                </div>
            </td>
            <td><strong>${formatNumber(channel.view_count)}</strong></td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="showChannelDetails(${channel.tv_channel_id})">
                    <i class="fas fa-chart-line"></i>
                </button>
            </td>
        </tr>`;
    });
    
    html += '</tbody></table></div>';
    $('#topChannelsList').html(html);
}

function loadPopularPrograms() {
    let genre = $('#genreFilter').val();
    
    $.get('{{ route("admin.live-tv.analytics.programs") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel,
        genre: genre
    })
    .done(function(response) {
        if (response.success) {
            renderPopularProgramsList(response.data);
        }
    });
}

function renderPopularProgramsList(programs) {
    let html = '<div class="table-responsive">';
    html += '<table class="table table-striped">';
    html += '<thead><tr><th>Program</th><th>Genre</th><th>Channel</th><th>Air Count</th></tr></thead><tbody>';
    
    programs.forEach(function(program) {
        html += `<tr>
            <td>
                <div class="font-weight-bold">${program.program_title}</div>
                <small class="text-muted">${program.avg_duration} min avg</small>
            </td>
            <td><span class="badge badge-info">${program.genre || 'N/A'}</span></td>
            <td>${program.channel ? program.channel.title : 'Unknown'}</td>
            <td><strong>${program.air_count}</strong></td>
        </tr>`;
    });
    
    html += '</tbody></table></div>';
    $('#popularProgramsList').html(html);
}

function loadRetentionData() {
    $.get('{{ route("admin.live-tv.analytics.retention") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel
    })
    .done(function(response) {
        if (response.success) {
            renderRetentionChart(response.data.engagement_levels);
            renderRetentionStats(response.data);
        }
    });
}

function renderRetentionChart(engagementLevels) {
    let ctx = document.getElementById('retentionChart').getContext('2d');
    
    if (charts.retentionChart) {
        charts.retentionChart.destroy();
    }
    
    charts.retentionChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['High Engagement', 'Medium Engagement', 'Low Engagement'],
            datasets: [{
                data: [engagementLevels.high, engagementLevels.medium, engagementLevels.low],
                backgroundColor: ['#28a745', '#ffc107', '#dc3545']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false
        }
    });
}

function renderRetentionStats(data) {
    let html = `
        <div class="row">
            <div class="col-12">
                <div class="card bg-light border-0">
                    <div class="card-body">
                        <h6>Viewer Statistics</h6>
                        <div class="mb-2">
                            <small class="text-muted">Return Rate:</small>
                            <div class="font-weight-bold">${data.return_rate}%</div>
                        </div>
                        <div class="mb-2">
                            <small class="text-muted">Avg Sessions/User:</small>
                            <div class="font-weight-bold">${data.avg_sessions_per_user}</div>
                        </div>
                        <div class="mb-2">
                            <small class="text-muted">Avg Watch Time:</small>
                            <div class="font-weight-bold">${data.avg_total_watch_time}h</div>
                        </div>
                        <div>
                            <small class="text-muted">Total Users:</small>
                            <div class="font-weight-bold">${formatNumber(data.total_users)}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    $('#retentionStats').html(html);
}

function loadGenreAnalytics() {
    $.get('{{ route("admin.live-tv.analytics.genres") }}', {
        date_range: currentDateRange
    })
    .done(function(response) {
        if (response.success) {
            renderGenreChart(response.data);
        }
    });
}

function renderGenreChart(data) {
    let ctx = document.getElementById('genreChart').getContext('2d');
    
    if (charts.genreChart) {
        charts.genreChart.destroy();
    }
    
    let labels = data.map(item => item.genre);
    let values = data.map(item => item.program_count);
    
    charts.genreChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Program Count',
                data: values,
                backgroundColor: 'rgba(111, 66, 193, 0.8)',
                borderColor: '#6f42c1',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function showChannelDetails(channelId) {
    $.get(`{{ route('admin.live-tv.analytics.channel', ':id') }}`.replace(':id', channelId), {
        date_range: currentDateRange
    })
    .done(function(response) {
        if (response.success) {
            // You could open a modal with detailed channel analytics here
            showInfoToast(`Channel: ${response.data.channel.title} - ${formatNumber(response.data.overview.total_views)} views`);
        }
    });
}

function showChannelComparison() {
    $('#channelComparisonModal').modal('show');
}

function loadChannelComparison() {
    let selectedChannels = $('#compareChannels').val();
    
    if (selectedChannels.length < 2) {
        showErrorToast('Please select at least 2 channels to compare');
        return;
    }
    
    $.get('{{ route("admin.live-tv.analytics.comparison") }}', {
        date_range: currentDateRange,
        channel_ids: selectedChannels
    })
    .done(function(response) {
        if (response.success) {
            renderChannelComparison(response.data);
        }
    });
}

function renderChannelComparison(data) {
    let html = '<div class="table-responsive">';
    html += '<table class="table table-bordered">';
    html += '<thead><tr><th>Channel</th><th>Views</th><th>Unique Viewers</th><th>Avg Duration (min)</th><th>Engagement</th></tr></thead><tbody>';
    
    data.forEach(function(channel) {
        html += `<tr>
            <td>
                <div class="font-weight-bold">${channel.channel_name}</div>
                <small class="text-muted">${channel.channel_number ? '#' + channel.channel_number : 'No number'}</small>
            </td>
            <td><strong>${formatNumber(channel.views)}</strong></td>
            <td>${formatNumber(channel.unique_viewers)}</td>
            <td>${channel.avg_duration_minutes}</td>
            <td>
                <span class="badge ${channel.engagement_rate > 2 ? 'badge-success' : channel.engagement_rate > 1 ? 'badge-warning' : 'badge-secondary'}">
                    ${channel.engagement_rate}
                </span>
            </td>
        </tr>`;
    });
    
    html += '</tbody></table></div>';
    $('#comparisonResults').html(html);
}

function loadGeographicData() {
    $.get('{{ route("admin.live-tv.analytics.geographic") }}', {
        date_range: currentDateRange,
        channel_id: currentChannel
    })
    .done(function(response) {
        if (response.success) {
            renderGeographicDetails(response.data);
            $('#geographicModal').modal('show');
        }
    });
}

function renderGeographicDetails(data) {
    let html = '<div class="table-responsive">';
    html += '<table class="table table-striped">';
    html += '<thead><tr><th>Country</th><th>Views</th><th>Unique Viewers</th><th>Share</th></tr></thead><tbody>';
    
    let totalViews = data.reduce((sum, item) => sum + parseInt(item.views), 0);
    
    data.forEach(function(item) {
        let share = totalViews > 0 ? ((item.views / totalViews) * 100).toFixed(1) : 0;
        html += `<tr>
            <td><strong>${item.country}</strong></td>
            <td>${formatNumber(item.views)}</td>
            <td>${formatNumber(item.unique_viewers)}</td>
            <td>${share}%</td>
        </tr>`;
    });
    
    html += '</tbody></table></div>';
    $('#geographicDetails').html(html);
}

function switchChartType(chartId, type) {
    if (charts[chartId]) {
        charts[chartId].config.type = type;
        charts[chartId].update();
    }
}

function exportAnalytics() {
    let params = new URLSearchParams();
    params.append('date_range', currentDateRange);
    if (currentChannel) params.append('channel_id', currentChannel);
    params.append('format', 'csv');
    
    window.open(`{{ route('admin.live-tv.analytics.export') }}?${params.toString()}`, '_blank');
}

function exportComparison() {
    let selectedChannels = $('#compareChannels').val();
    if (selectedChannels.length < 2) {
        showErrorToast('Please generate a comparison first');
        return;
    }
    
    // Export comparison data
    showInfoToast('Comparison export feature coming soon');
}

function formatNumber(number) {
    return new Intl.NumberFormat().format(number);
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

function showInfoToast(message) {
    iziToast.info({
        title: 'Info',
        message: message,
        position: 'topRight'
    });
}
</script>

<style>
.loading-placeholder {
    color: #6c757d;
    font-style: italic;
}

.card-statistic-1 .card-body {
    font-size: 1.2em;
    font-weight: bold;
    color: #2c3e50;
}

.table th {
    border-top: none;
    font-weight: 600;
}

.rounded {
    border-radius: 0.25rem !important;
    object-fit: cover;
}

.section-buttons .btn-group .btn {
    margin-left: 0;
}

.card-header-action .btn-group .btn {
    margin-left: 0;
    margin-right: 0.25rem;
}

.card-header-action .btn-group .btn:last-child {
    margin-right: 0;
}

@media (max-width: 768px) {
    .section-buttons {
        margin-top: 1rem;
    }
    
    .card-statistic-1 {
        margin-bottom: 1rem;
    }
    
    .row.mb-4 .col-lg-6,
    .row.mb-4 .col-lg-8,
    .row.mb-4 .col-lg-4 {
        margin-bottom: 1rem;
    }
}

/* Chart container height fixes */
.card canvas {
    max-height: 300px;
}

#dailyChart,
#hourlyChart,
#countryChart,
#genreChart {
    max-height: 250px;
}

#deviceChart,
#retentionChart {
    max-height: 200px;
}
</style>
@endsection