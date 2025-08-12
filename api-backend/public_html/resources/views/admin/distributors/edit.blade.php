@extends('include.app')

@section('title', 'Edit Distributor')

@section('content')
<div class="container-fluid">
    <div class="row">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Edit Distributor: {{ $distributor->name }}</h3>
                </div>
                <form action="{{ route('distributors.update', $distributor->content_distributor_id) }}" method="POST">
                    @csrf
                    @method('PUT')
                    <div class="card-body">
                        @if($errors->any())
                            <div class="alert alert-danger">
                                <ul class="mb-0">
                                    @foreach($errors->all() as $error)
                                        <li>{{ $error }}</li>
                                    @endforeach
                                </ul>
                            </div>
                        @endif

                        <div class="form-group">
                            <label for="name">Distributor Name <span class="text-danger">*</span></label>
                            <input type="text" class="form-control @error('name') is-invalid @enderror" 
                                   id="name" name="name" value="{{ old('name', $distributor->name) }}" required>
                            @error('name')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <label for="code">Code <span class="text-danger">*</span></label>
                            <input type="text" class="form-control @error('code') is-invalid @enderror" 
                                   id="code" name="code" value="{{ old('code', $distributor->code) }}" 
                                   pattern="[A-Z0-9_]+" title="Use uppercase letters, numbers, and underscores only"
                                   required>
                            <small class="form-text text-muted">Unique identifier (uppercase, no spaces)</small>
                            @error('code')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <label for="description">Description</label>
                            <textarea class="form-control @error('description') is-invalid @enderror" 
                                      id="description" name="description" rows="3">{{ old('description', $distributor->description) }}</textarea>
                            @error('description')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <label for="logo_url">Logo URL</label>
                            <input type="text" class="form-control @error('logo_url') is-invalid @enderror" 
                                   id="logo_url" name="logo_url" value="{{ old('logo_url', $distributor->logo_url) }}">
                            @if($distributor->logo_url)
                                <img src="{{ $distributor->logo_url }}" alt="Current logo" style="max-height: 50px; margin-top: 10px;">
                            @endif
                            @error('logo_url')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <label>Subscription Type</label>
                            @php
                                $type = 'free';
                                if ($distributor->is_premium) $type = 'premium';
                                elseif ($distributor->is_base_included) $type = 'base';
                            @endphp
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="subscription_type" 
                                       id="type_premium" value="premium" {{ $type == 'premium' ? 'checked' : '' }}>
                                <label class="form-check-label" for="type_premium">
                                    Premium Distributor (Requires separate subscription)
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="subscription_type" 
                                       id="type_base" value="base" {{ $type == 'base' ? 'checked' : '' }}>
                                <label class="form-check-label" for="type_base">
                                    Base Included (Included with base subscription)
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="subscription_type" 
                                       id="type_free" value="free" {{ $type == 'free' ? 'checked' : '' }}>
                                <label class="form-check-label" for="type_free">
                                    Free Content (No subscription required)
                                </label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="display_order">Display Order</label>
                            <input type="number" class="form-control @error('display_order') is-invalid @enderror" 
                                   id="display_order" name="display_order" value="{{ old('display_order', $distributor->display_order) }}">
                            @error('display_order')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <div class="custom-control custom-checkbox">
                                <input type="checkbox" class="custom-control-input" id="is_active" 
                                       name="is_active" value="1" {{ old('is_active', $distributor->is_active) ? 'checked' : '' }}>
                                <label class="custom-control-label" for="is_active">Active</label>
                            </div>
                        </div>
                    </div>
                    <div class="card-footer">
                        <button type="submit" class="btn btn-primary">Update Distributor</button>
                        <a href="{{ route('distributors.index') }}" class="btn btn-default">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
        
        <!-- Subscription Pricing Card -->
        @if($distributor->is_premium)
        <div class="col-md-12 mt-4">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Subscription Pricing</h3>
                    <div class="card-tools">
                        <button type="button" class="btn btn-sm btn-primary" data-toggle="modal" data-target="#addPricingModal">
                            <i class="fas fa-plus"></i> Add Pricing Option
                        </button>
                    </div>
                </div>
                <div class="card-body">
                    <div id="pricingTable">
                        <table class="table table-bordered">
                            <thead>
                                <tr>
                                    <th>Billing Period</th>
                                    <th>Price</th>
                                    <th>Display Name</th>
                                    <th>Description</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody id="pricingList">
                                <!-- Pricing rows will be loaded here -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        @else
        <div class="col-md-12 mt-4">
            <div class="alert alert-info">
                <i class="fas fa-info-circle"></i> This distributor's content is included in the base subscription. 
                <a href="{{ route('distributors.base-pricing') }}" class="alert-link">Manage base subscription pricing</a>
            </div>
        </div>
        @endif
    </div>
</div>

<!-- Add Pricing Modal -->
@if($distributor->is_premium)
<div class="modal fade" id="addPricingModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <form id="addPricingForm">
                <div class="modal-header">
                    <h5 class="modal-title">Add Pricing Option</h5>
                    <button type="button" class="close" data-dismiss="modal">
                        <span>&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="content_distributor_id" value="{{ $distributor->content_distributor_id }}">
                    
                    <div class="form-group">
                        <label>Billing Period <span class="text-danger">*</span></label>
                        <select name="billing_period" class="form-control" required>
                            <option value="daily">Daily</option>
                            <option value="weekly">Weekly</option>
                            <option value="monthly" selected>Monthly</option>
                            <option value="quarterly">Quarterly</option>
                            <option value="yearly">Yearly</option>
                            <option value="lifetime">Lifetime</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label>Price (USD) <span class="text-danger">*</span></label>
                        <input type="number" name="price" class="form-control" step="0.01" min="0" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Display Name <span class="text-danger">*</span></label>
                        <input type="text" name="display_name" class="form-control" required>
                        <small class="form-text text-muted">E.g., "Monthly Premium", "Annual Subscription"</small>
                    </div>
                    
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" class="form-control" rows="3"></textarea>
                    </div>
                    
                    <div class="form-group">
                        <div class="custom-control custom-checkbox">
                            <input type="checkbox" class="custom-control-input" id="pricing_is_active" name="is_active" value="1" checked>
                            <label class="custom-control-label" for="pricing_is_active">Active</label>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Save Pricing</button>
                </div>
            </form>
            </div>
        </div>
    </div>
</div>
@endif
@endsection

@section('scripts')
<script>
$(document).ready(function() {
    @if($distributor->is_premium)
    // Handle subscription type
    $('input[name="subscription_type"]').on('change', function() {
        var type = $(this).val();
        $('input[name="is_base_included"]').val(type === 'base' ? 1 : 0);
        $('input[name="is_premium"]').val(type === 'premium' ? 1 : 0);
    });
    
    // Load pricing data
    loadPricing();
    
    function loadPricing() {
        $.ajax({
            url: "{{ route('distributors.pricing.list', $distributor->content_distributor_id) }}",
            method: 'GET',
            success: function(response) {
                var html = '';
                if (response.data && response.data.length > 0) {
                    response.data.forEach(function(pricing) {
                        html += `<tr>
                            <td>${pricing.billing_period}</td>
                            <td>$${parseFloat(pricing.price).toFixed(2)}</td>
                            <td>${pricing.display_name}</td>
                            <td>${pricing.description || ''}</td>
                            <td>
                                ${pricing.is_active ? 
                                    '<span class="badge badge-success">Active</span>' : 
                                    '<span class="badge badge-danger">Inactive</span>'}
                            </td>
                            <td>
                                <button class="btn btn-sm btn-info edit-pricing" data-id="${pricing.pricing_id}" data-pricing='${JSON.stringify(pricing)}'>
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-danger delete-pricing" data-id="${pricing.pricing_id}">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </td>
                        </tr>`;
                    });
                } else {
                    @if($distributor->is_base_included)
                    html = '<tr><td colspan="6" class="text-center">This distributor\'s content is included in the base subscription. <a href="{{ route('distributors.base-pricing') }}">Manage base subscription pricing</a></td></tr>';
                    @else
                    html = '<tr><td colspan="6" class="text-center">No pricing options configured</td></tr>';
                    @endif
                }
                $('#pricingList').html(html);
            }
        });
    }
    
    // Add pricing form submission
    $('#addPricingForm').on('submit', function(e) {
        e.preventDefault();
        var formData = $(this).serialize();
        
        $.ajax({
            url: "{{ route('distributors.pricing.store') }}",
            method: 'POST',
            data: formData + '&_token={{ csrf_token() }}',
            success: function(response) {
                $('#addPricingModal').modal('hide');
                $('#addPricingForm')[0].reset();
                loadPricing();
                alert('Pricing added successfully');
            },
            error: function(xhr) {
                alert('Error: ' + xhr.responseJSON.message);
            }
        });
    });
    
    // Delete pricing
    $(document).on('click', '.delete-pricing', function() {
        if (confirm('Are you sure you want to delete this pricing option?')) {
            var pricingId = $(this).data('id');
            
            $.ajax({
                url: "{{ route('distributors.pricing.destroy', '') }}/" + pricingId,
                method: 'DELETE',
                data: {_token: '{{ csrf_token() }}'},
                success: function(response) {
                    loadPricing();
                    alert('Pricing deleted successfully');
                },
                error: function(xhr) {
                    alert('Error: ' + xhr.responseJSON.message);
                }
            });
        }
    });
    @endif
});
</script>
@endsection