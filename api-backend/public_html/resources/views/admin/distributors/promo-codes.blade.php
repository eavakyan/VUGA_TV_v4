@extends('include.app')

@section('title', 'Promo Codes')

@section('content')
<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Promo Code Management</h3>
                    <div class="card-tools">
                        <button class="btn btn-primary btn-sm" data-toggle="modal" data-target="#createPromoModal">
                            <i class="fas fa-plus"></i> Create Promo Code
                        </button>
                    </div>
                </div>
                <div class="card-body">
                    <table class="table table-bordered table-striped" id="promoTable">
                        <thead>
                            <tr>
                                <th>Code</th>
                                <th>Description</th>
                                <th>Discount</th>
                                <th>Applies To</th>
                                <th>Usage</th>
                                <th>Valid Period</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($promoCodes as $promo)
                            <tr>
                                <td><code>{{ $promo->code }}</code></td>
                                <td>{{ $promo->description }}</td>
                                <td>
                                    @if($promo->discount_type == 'percentage')
                                        {{ $promo->discount_value }}%
                                    @elseif($promo->discount_type == 'fixed_amount')
                                        ${{ number_format($promo->discount_value, 2) }}
                                    @else
                                        {{ $promo->discount_value }} days free
                                    @endif
                                </td>
                                <td>
                                    @if($promo->applicable_to == 'all')
                                        <span class="badge badge-info">All Subscriptions</span>
                                    @elseif($promo->applicable_to == 'base')
                                        <span class="badge badge-primary">Base Only</span>
                                    @else
                                        <span class="badge badge-warning">{{ $promo->distributor_name ?? 'Distributors' }}</span>
                                    @endif
                                </td>
                                <td>
                                    {{ $promo->usage_count }} / {{ $promo->usage_limit ?? '∞' }}
                                    <div class="progress" style="height: 10px;">
                                        @php
                                            $percentage = $promo->usage_limit ? ($promo->usage_count / $promo->usage_limit * 100) : 0;
                                        @endphp
                                        <div class="progress-bar" style="width: {{ $percentage }}%"></div>
                                    </div>
                                </td>
                                <td>
                                    <small>
                                        From: {{ \Carbon\Carbon::parse($promo->valid_from)->format('M d, Y') }}<br>
                                        Until: {{ $promo->valid_until ? \Carbon\Carbon::parse($promo->valid_until)->format('M d, Y') : 'No expiry' }}
                                    </small>
                                </td>
                                <td>
                                    @if($promo->is_active)
                                        @if($promo->valid_until && \Carbon\Carbon::parse($promo->valid_until)->isPast())
                                            <span class="badge badge-warning">Expired</span>
                                        @else
                                            <span class="badge badge-success">Active</span>
                                        @endif
                                    @else
                                        <span class="badge badge-danger">Inactive</span>
                                    @endif
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-{{ $promo->is_active ? 'warning' : 'success' }} toggle-status"
                                            data-promo-id="{{ $promo->promo_code_id }}">
                                        <i class="fas fa-{{ $promo->is_active ? 'pause' : 'play' }}"></i>
                                    </button>
                                </td>
                            </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Create Promo Modal -->
<div class="modal fade" id="createPromoModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Create Promo Code</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <form id="createPromoForm">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Code <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" name="code" required
                                       pattern="[A-Z0-9]+" title="Uppercase letters and numbers only">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Description</label>
                                <input type="text" class="form-control" name="description">
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Discount Type <span class="text-danger">*</span></label>
                                <select class="form-control" name="discount_type" required>
                                    <option value="percentage">Percentage Off</option>
                                    <option value="fixed_amount">Fixed Amount Off</option>
                                    <option value="free_period">Free Trial Period</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Discount Value <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" name="discount_value" 
                                       min="0" step="0.01" required>
                                <small class="form-text text-muted discount-help">Enter percentage (0-100)</small>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Applies To <span class="text-danger">*</span></label>
                                <select class="form-control" name="applicable_to" required>
                                    <option value="all">All Subscriptions</option>
                                    <option value="base">Base Subscription Only</option>
                                    <option value="distributor">Specific Distributor</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group" id="distributorSelect" style="display: none;">
                                <label>Select Distributor</label>
                                <select class="form-control" name="content_distributor_id">
                                    <option value="">-- Select --</option>
                                    @foreach($distributors as $distributor)
                                        @if($distributor->is_premium)
                                            <option value="{{ $distributor->content_distributor_id }}">
                                                {{ $distributor->name }}
                                            </option>
                                        @endif
                                    @endforeach
                                </select>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Usage Limit</label>
                                <input type="number" class="form-control" name="usage_limit" min="1">
                                <small class="form-text text-muted">Leave empty for unlimited</small>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Per User Limit <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" name="user_limit" 
                                       min="1" value="1" required>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Minimum Purchase</label>
                                <input type="number" class="form-control" name="minimum_purchase" 
                                       min="0" step="0.01" value="0">
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Valid From <span class="text-danger">*</span></label>
                                <input type="datetime-local" class="form-control" name="valid_from" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Valid Until</label>
                                <input type="datetime-local" class="form-control" name="valid_until">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Create Promo Code</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
$(document).ready(function() {
    // DataTable
    $('#promoTable').DataTable({
        order: [[5, 'desc']]
    });
    
    // Auto uppercase code input
    $('input[name="code"]').on('input', function() {
        $(this).val($(this).val().toUpperCase());
    });
    
    // Handle discount type change
    $('select[name="discount_type"]').on('change', function() {
        var type = $(this).val();
        var helpText = '';
        var max = '';
        
        if (type === 'percentage') {
            helpText = 'Enter percentage (0-100)';
            max = '100';
        } else if (type === 'fixed_amount') {
            helpText = 'Enter dollar amount';
            max = '';
        } else {
            helpText = 'Enter number of days';
            max = '';
        }
        
        $('input[name="discount_value"]').attr('max', max);
        $('.discount-help').text(helpText);
    });
    
    // Handle applies to change
    $('select[name="applicable_to"]').on('change', function() {
        if ($(this).val() === 'distributor') {
            $('#distributorSelect').show();
        } else {
            $('#distributorSelect').hide();
        }
    });
    
    // Create promo code
    $('#createPromoForm').on('submit', function(e) {
        e.preventDefault();
        
        var formData = $(this).serializeArray();
        var data = {};
        formData.forEach(function(item) {
            if (item.value) {
                data[item.name] = item.value;
            }
        });
        data._token = '{{ csrf_token() }}';
        
        $.ajax({
            url: '{{ route("admin.distributors.promos.create") }}',
            method: 'POST',
            data: data,
            success: function(response) {
                alert('Promo code created successfully!');
                location.reload();
            },
            error: function(xhr) {
                var message = 'Failed to create promo code';
                if (xhr.responseJSON && xhr.responseJSON.errors) {
                    var errors = xhr.responseJSON.errors;
                    message = Object.values(errors).flat().join('\n');
                }
                alert(message);
            }
        });
    });
    
    // Toggle status
    $('.toggle-status').on('click', function() {
        var btn = $(this);
        var promoId = btn.data('promo-id');
        
        $.ajax({
            url: '/admin/distributors/promo-codes/' + promoId + '/toggle',
            method: 'POST',
            data: { _token: '{{ csrf_token() }}' },
            success: function(response) {
                location.reload();
            },
            error: function() {
                alert('Failed to update promo code status');
            }
        });
    });
});
</script>
@endsection