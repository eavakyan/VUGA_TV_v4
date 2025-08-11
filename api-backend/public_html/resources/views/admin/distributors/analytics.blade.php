@extends('include.app')

@section('title', 'Subscription Analytics')

@section('content')
<div class="container-fluid">
    <!-- Summary Cards -->
    <div class="row">
        <div class="col-lg-3 col-6">
            <div class="small-box bg-info">
                <div class="inner">
                    <h3>{{ $baseStats->total_subscribers ?? 0 }}</h3>
                    <p>Total Base Subscribers</p>
                </div>
                <div class="icon">
                    <i class="fas fa-users"></i>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-6">
            <div class="small-box bg-success">
                <div class="inner">
                    <h3>{{ $baseStats->active_subscribers ?? 0 }}</h3>
                    <p>Active Base Subscriptions</p>
                </div>
                <div class="icon">
                    <i class="fas fa-check-circle"></i>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-6">
            <div class="small-box bg-warning">
                <div class="inner">
                    <h3>{{ $baseStats->monthly_subscribers ?? 0 }}</h3>
                    <p>Monthly Subscribers</p>
                </div>
                <div class="icon">
                    <i class="fas fa-calendar-alt"></i>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-6">
            <div class="small-box bg-primary">
                <div class="inner">
                    <h3>{{ $baseStats->yearly_subscribers ?? 0 }}</h3>
                    <p>Yearly Subscribers</p>
                </div>
                <div class="icon">
                    <i class="fas fa-calendar"></i>
                </div>
            </div>
        </div>
    </div>

    <!-- Distributor Subscriptions -->
    <div class="row">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Distributor Subscriptions</h3>
                </div>
                <div class="card-body">
                    <table class="table table-bordered">
                        <thead>
                            <tr>
                                <th>Distributor</th>
                                <th>Total</th>
                                <th>Active</th>
                                <th>Conversion</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($distributorStats as $stat)
                            <tr>
                                <td>{{ $stat->name }}</td>
                                <td>{{ $stat->total_subscribers }}</td>
                                <td>
                                    <span class="badge badge-success">{{ $stat->active_subscribers }}</span>
                                </td>
                                <td>
                                    @php
                                        $conversion = $stat->total_subscribers > 0 
                                            ? round(($stat->active_subscribers / $stat->total_subscribers) * 100, 1) 
                                            : 0;
                                    @endphp
                                    <div class="progress" style="height: 20px;">
                                        <div class="progress-bar" style="width: {{ $conversion }}%">
                                            {{ $conversion }}%
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Revenue Chart -->
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Monthly Revenue Trend</h3>
                </div>
                <div class="card-body">
                    <canvas id="revenueChart" style="height: 300px;"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Revenue Details -->
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Revenue Breakdown by Month</h3>
                </div>
                <div class="card-body">
                    <table class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>Month</th>
                                <th>Base Revenue</th>
                                <th>Distributor Revenue</th>
                                <th>Total Revenue</th>
                                <th>Transactions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @php
                                $monthlyTotals = [];
                                foreach($revenueStats as $stat) {
                                    if (!isset($monthlyTotals[$stat->month])) {
                                        $monthlyTotals[$stat->month] = [
                                            'base' => 0,
                                            'distributor' => 0,
                                            'total' => 0,
                                            'count' => 0
                                        ];
                                    }
                                    if ($stat->subscription_type == 'base') {
                                        $monthlyTotals[$stat->month]['base'] += $stat->revenue;
                                    } else {
                                        $monthlyTotals[$stat->month]['distributor'] += $stat->revenue;
                                    }
                                    $monthlyTotals[$stat->month]['total'] += $stat->revenue;
                                    $monthlyTotals[$stat->month]['count'] += $stat->transaction_count;
                                }
                            @endphp
                            @foreach($monthlyTotals as $month => $totals)
                            <tr>
                                <td>{{ \Carbon\Carbon::parse($month . '-01')->format('F Y') }}</td>
                                <td>${{ number_format($totals['base'], 2) }}</td>
                                <td>${{ number_format($totals['distributor'], 2) }}</td>
                                <td><strong>${{ number_format($totals['total'], 2) }}</strong></td>
                                <td>{{ $totals['count'] }}</td>
                            </tr>
                            @endforeach
                        </tbody>
                        <tfoot>
                            <tr>
                                <th>Total</th>
                                <th>${{ number_format(array_sum(array_column($monthlyTotals, 'base')), 2) }}</th>
                                <th>${{ number_format(array_sum(array_column($monthlyTotals, 'distributor')), 2) }}</th>
                                <th>${{ number_format(array_sum(array_column($monthlyTotals, 'total')), 2) }}</th>
                                <th>{{ array_sum(array_column($monthlyTotals, 'count')) }}</th>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
$(document).ready(function() {
    // Prepare data for chart
    var revenueData = @json($revenueStats);
    var months = [];
    var baseRevenue = [];
    var distributorRevenue = [];
    
    // Group by month
    var monthlyData = {};
    revenueData.forEach(function(item) {
        if (!monthlyData[item.month]) {
            monthlyData[item.month] = { base: 0, distributor: 0 };
        }
        if (item.subscription_type === 'base') {
            monthlyData[item.month].base = parseFloat(item.revenue);
        } else {
            monthlyData[item.month].distributor = parseFloat(item.revenue);
        }
    });
    
    // Sort months and prepare arrays
    Object.keys(monthlyData).sort().reverse().slice(0, 12).reverse().forEach(function(month) {
        months.push(moment(month + '-01').format('MMM YYYY'));
        baseRevenue.push(monthlyData[month].base);
        distributorRevenue.push(monthlyData[month].distributor);
    });
    
    // Create chart
    var ctx = document.getElementById('revenueChart').getContext('2d');
    var chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: months,
            datasets: [{
                label: 'Base Subscriptions',
                data: baseRevenue,
                backgroundColor: 'rgba(54, 162, 235, 0.8)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }, {
                label: 'Distributor Subscriptions',
                data: distributorRevenue,
                backgroundColor: 'rgba(255, 206, 86, 0.8)',
                borderColor: 'rgba(255, 206, 86, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    stacked: true
                },
                y: {
                    stacked: true,
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value.toLocaleString();
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': $' + context.parsed.y.toLocaleString();
                        }
                    }
                }
            }
        }
    });
});
</script>
@endsection