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
                <form id="updateForm" action="{{ route('distributors.update', $distributor->content_distributor_id) }}" method="POST">
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
                    <div class="row mb-3">
                        <div class="col-md-8">
                            <p class="text-muted mb-0">
                                <i class="fas fa-info-circle"></i>
                                Configure subscription pricing options for <strong>{{ $distributor->name }}</strong> premium content.
                            </p>
                        </div>
                        <div class="col-md-4 text-right">
                            <small class="text-muted">
                                <i class="fas fa-lightbulb"></i>
                                Tip: Most users prefer monthly and yearly options
                            </small>
                        </div>
                    </div>
                    
                    <div id="pricingTable">
                        <div class="table-responsive">
                            <table class="table table-bordered table-hover">
                                <thead class="thead-light">
                                    <tr>
                                        <th width="15%">
                                            <i class="fas fa-calendar-alt"></i> Billing Period
                                        </th>
                                        <th width="15%">
                                            <i class="fas fa-dollar-sign"></i> Price
                                        </th>
                                        <th width="25%">
                                            <i class="fas fa-tag"></i> Display Name
                                        </th>
                                        <th width="25%">
                                            <i class="fas fa-align-left"></i> Description
                                        </th>
                                        <th width="10%">
                                            <i class="fas fa-toggle-on"></i> Status
                                        </th>
                                        <th width="10%">
                                            <i class="fas fa-cog"></i> Actions
                                        </th>
                                    </tr>
                                </thead>
                                <tbody id="pricingList">
                                    <!-- Pricing rows will be loaded here -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                    
                    <!-- Quick Actions -->
                    <div class="mt-3" id="quickActions" style="display: none;">
                        <div class="alert alert-info">
                            <h6><i class="fas fa-rocket"></i> Quick Setup</h6>
                            <p class="mb-2">Get started with common pricing options:</p>
                            <button type="button" class="btn btn-sm btn-outline-primary mr-2" onclick="addQuickPricing('monthly', 9.99, 'Monthly Premium')">
                                <i class="fas fa-plus"></i> Add $9.99/month
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-primary mr-2" onclick="addQuickPricing('yearly', 99.99, 'Annual Premium')">
                                <i class="fas fa-plus"></i> Add $99.99/year
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-primary" onclick="addQuickPricing('lifetime', 299.99, 'Lifetime Access')">
                                <i class="fas fa-plus"></i> Add $299.99 lifetime
                            </button>
                        </div>
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

<!-- Add/Edit Pricing Modal -->
@if($distributor->is_premium)
<div class="modal fade" id="addPricingModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <form id="addPricingForm">
                <div class="modal-header">
                    <h5 class="modal-title" id="pricingModalTitle">Add Pricing Option</h5>
                    <button type="button" class="close" data-dismiss="modal">
                        <span>&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="content_distributor_id" value="{{ $distributor->content_distributor_id }}">
                    <input type="hidden" name="pricing_id" value="">
                    <input type="hidden" name="edit_mode" value="false">
                    
                    <div class="form-group">
                        <label>Billing Period <span class="text-danger">*</span></label>
                        <select name="billing_period" class="form-control" required>
                            <option value="">Select billing period...</option>
                            <option value="daily">Daily - Perfect for trial periods</option>
                            <option value="weekly">Weekly - Short-term access</option>
                            <option value="monthly">Monthly - Most popular choice</option>
                            <option value="quarterly">Quarterly - 3 months (25% savings)</option>
                            <option value="yearly">Yearly - Best value (40% savings)</option>
                            <option value="lifetime">Lifetime - One-time payment</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label>Price (USD) <span class="text-danger">*</span></label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text">$</span>
                            </div>
                            <input type="number" name="price" class="form-control" step="0.01" min="0" placeholder="0.00" required>
                        </div>
                        <small class="form-text text-muted">Enter amount in USD (e.g., 9.99 for $9.99)</small>
                    </div>
                    
                    <div class="form-group">
                        <label>Display Name <span class="text-danger">*</span></label>
                        <input type="text" name="display_name" class="form-control" placeholder="e.g., Monthly Disney Premium" required>
                        <small class="form-text text-muted">This name will be shown to users</small>
                    </div>
                    
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" class="form-control" rows="3" placeholder="Optional description of what this subscription includes..."></textarea>
                    </div>
                    
                    <div class="form-group">
                        <div class="custom-control custom-checkbox">
                            <input type="checkbox" class="custom-control-input" id="pricing_is_active" name="is_active" value="1" checked>
                            <label class="custom-control-label" for="pricing_is_active">
                                <strong>Active</strong>
                                <br><small class="text-muted">Users can purchase this subscription option</small>
                            </label>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-save"></i> Save Pricing
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endif
@endsection

@section('scripts')
<script>
$(document).ready(function() {
    // Setup CSRF token for all AJAX requests
    var csrfToken = $('meta[name="csrf-token"]').attr('content');
    $.ajaxSetup({
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });
    
    // Handle subscription type changes - always initialize this
    var isPremium = {{ $distributor->is_premium ? 'true' : 'false' }};
    
    $('input[name="subscription_type"]').on('change', function() {
        var type = $(this).val();
        // Reload page when changing to/from premium to show/hide pricing section
        if (type === 'premium' && !isPremium) {
            if (confirm('Changing to Premium will allow you to set pricing. Save and reload?')) {
                $('#updateForm').submit();
            }
        } else if (type !== 'premium' && isPremium) {
            if (confirm('Changing from Premium will remove pricing options. Save and reload?')) {
                $('#updateForm').submit();
            }
        }
    });
    
    @if($distributor->is_premium)
    
    // Use server-side loaded data
    var pricingData = @json($pricing);
    
    // Display pricing data on page load
    displayPricing();
    
    function displayPricing() {
        console.log('Displaying pricing data:', pricingData);
        var html = '';
        
        if (pricingData && pricingData.length > 0) {
            pricingData.forEach(function(pricing) {
                        html += `<tr>
                            <td>
                                <span class="badge badge-primary text-capitalize">${pricing.billing_period}</span>
                            </td>
                            <td>
                                <strong class="text-success">$${parseFloat(pricing.price).toFixed(2)}</strong>
                            </td>
                            <td>
                                <strong>${pricing.display_name || 'N/A'}</strong>
                            </td>
                            <td>
                                <small class="text-muted">${pricing.description || 'No description provided'}</small>
                            </td>
                            <td>
                                ${pricing.is_active == 1 ? 
                                    '<span class="badge badge-success"><i class="fas fa-check"></i> Active</span>' : 
                                    '<span class="badge badge-danger"><i class="fas fa-times"></i> Inactive</span>'}
                            </td>
                            <td>
                                <div class="btn-group" role="group">
                                    <button class="btn btn-sm btn-outline-info edit-pricing" data-id="${pricing.pricing_id}" data-pricing='${JSON.stringify(pricing)}' title="Edit">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger delete-pricing" data-id="${pricing.pricing_id}" title="Delete">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>`;
            });
            // Hide quick actions when there are pricing options
            $('#quickActions').hide();
        } else {
            html = `<tr>
                <td colspan="6" class="text-center py-4">
                    <div class="text-muted">
                        <i class="fas fa-info-circle fa-2x mb-2"></i>
                        <h6>No pricing options configured yet</h6>
                        <p class="mb-0">Click "Add Pricing Option" above or use the quick setup options below.</p>
                    </div>
                </td>
            </tr>`;
            // Show quick actions when there are no pricing options
            $('#quickActions').show();
        }
        
        $('#pricingList').html(html);
    }
    
    // Function to reload pricing data via AJAX after adding/editing
    function loadPricing() {
        $.ajax({
            url: "{{ route('distributors.pricing.list', $distributor->content_distributor_id) }}",
            method: 'GET',
            dataType: 'json',
            success: function(response) {
                console.log('Pricing data reloaded:', response);
                if (response.success && response.data) {
                    pricingData = response.data;
                    displayPricing();
                }
            },
            error: function(xhr) {
                console.error('Error reloading pricing:', xhr);
                // On error, just reload the page to get fresh data
                window.location.reload();
            }
        });
    }
    
    // Add/Edit pricing form submission
    $('#addPricingForm').on('submit', function(e) {
        e.preventDefault();
        
        var $form = $(this);
        var $submitBtn = $form.find('button[type="submit"]');
        var originalHtml = $submitBtn.html();
        var isEditMode = $form.find('input[name="edit_mode"]').val() === 'true';
        var pricingId = $form.find('input[name="pricing_id"]').val();
        
        // Disable submit button and show loading
        $submitBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Saving...');
        
        // Prepare form data
        var formData = new FormData(this);
        formData.append('_token', csrfToken);
        
        // Determine URL and method based on edit mode
        var url, method;
        if (isEditMode && pricingId) {
            url = "{{ route('distributors.pricing.update', '') }}/" + pricingId;
            method = 'PUT';
            formData.append('_method', 'PUT');
        } else {
            url = "{{ route('distributors.pricing.store') }}";
            method = 'POST';
        }
        
        $.ajax({
            url: url,
            method: method,
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                console.log('Pricing saved successfully:', response);
                
                // Hide modal and reset form
                $('#addPricingModal').modal('hide');
                $form[0].reset();
                
                // Reload pricing table
                loadPricing();
                
                // Show success message
                var action = isEditMode ? 'updated' : 'added';
                alert('Pricing option ' + action + ' successfully!');
            },
            error: function(xhr) {
                console.error('Error saving pricing:', xhr);
                
                var action = isEditMode ? 'update' : 'add';
                var errorMsg = 'Failed to ' + action + ' pricing option';
                
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                } else if (xhr.responseJSON && xhr.responseJSON.errors) {
                    // Handle validation errors
                    var errors = xhr.responseJSON.errors;
                    errorMsg = Object.values(errors).flat().join('\n');
                }
                
                alert('Error: ' + errorMsg);
            },
            complete: function() {
                // Re-enable submit button
                $submitBtn.prop('disabled', false).html(originalHtml);
            }
        });
    });
    
    // Edit pricing
    $(document).on('click', '.edit-pricing', function() {
        var pricingData = $(this).data('pricing');
        console.log('Editing pricing:', pricingData);
        
        // Change modal title and form state
        $('#pricingModalTitle').text('Edit Pricing Option');
        $('input[name="edit_mode"]').val('true');
        $('input[name="pricing_id"]').val(pricingData.pricing_id);
        
        // Fill form with existing data
        $('select[name="billing_period"]').val(pricingData.billing_period);
        $('input[name="price"]').val(pricingData.price);
        $('input[name="display_name"]').val(pricingData.display_name);
        $('textarea[name="description"]').val(pricingData.description || '');
        $('input[name="is_active"]').prop('checked', pricingData.is_active == 1);
        
        // Show modal
        $('#addPricingModal').modal('show');
    });
    
    // Delete pricing
    $(document).on('click', '.delete-pricing', function() {
        if (!confirm('Are you sure you want to delete this pricing option?')) {
            return;
        }
        
        var pricingId = $(this).data('id');
        var $btn = $(this);
        
        // Disable button and show loading
        $btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i>');
        
        $.ajax({
            url: "{{ route('distributors.pricing.destroy', '') }}/" + pricingId,
            method: 'DELETE',
            data: {
                _token: csrfToken
            },
            success: function(response) {
                console.log('Pricing deleted successfully:', response);
                loadPricing();
                alert('Pricing option deleted successfully!');
            },
            error: function(xhr) {
                console.error('Error deleting pricing:', xhr);
                
                var errorMsg = 'Failed to delete pricing option';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                }
                
                alert('Error: ' + errorMsg);
                
                // Re-enable button on error
                $btn.prop('disabled', false).html('<i class="fas fa-trash"></i>');
            }
        });
    });
    
    // Reset modal when it's closed
    $('#addPricingModal').on('hidden.bs.modal', function() {
        // Reset form
        $('#addPricingForm')[0].reset();
        
        // Reset modal state
        $('#pricingModalTitle').text('Add Pricing Option');
        $('input[name="edit_mode"]').val('false');
        $('input[name="pricing_id"]').val('');
        
        // Reset button
        $('#addPricingForm button[type="submit"]').prop('disabled', false).html('<i class="fas fa-save"></i> Save Pricing');
    });
    
    @endif
    
    // Quick pricing setup function (available globally)
    window.addQuickPricing = function(period, price, displayName) {
        // Fill form with quick values
        $('select[name="billing_period"]').val(period);
        $('input[name="price"]').val(price);
        $('input[name="display_name"]').val(displayName + ' - {{ $distributor->name }}');
        $('textarea[name="description"]').val('Access to premium ' + displayName.toLowerCase() + ' content from {{ $distributor->name }}');
        $('input[name="is_active"]').prop('checked', true);
        
        // Reset edit mode
        $('input[name="edit_mode"]').val('false');
        $('input[name="pricing_id"]').val('');
        $('#pricingModalTitle').text('Add Pricing Option');
        
        // Show modal
        $('#addPricingModal').modal('show');
    };
});
</script>
@endsection