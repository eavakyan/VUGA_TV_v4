@extends('include.app')

@section('title', 'Base Subscription Pricing')

@section('content')
<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Base Subscription Pricing</h3>
                    <div class="card-tools">
                        <button type="button" class="btn btn-sm btn-primary" data-toggle="modal" data-target="#addBasePricingModal">
                            <i class="fas fa-plus"></i> Add Pricing Option
                        </button>
                    </div>
                </div>
                <div class="card-body">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i> Base subscription provides access to all non-premium content. Users must have an active base subscription before they can purchase premium distributor access.
                    </div>
                    
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
                        <tbody id="basePricingList">
                            <!-- Pricing rows will be loaded here -->
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Add Base Pricing Modal -->
<div class="modal fade" id="addBasePricingModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <form id="addBasePricingForm">
                <div class="modal-header">
                    <h5 class="modal-title">Add Base Subscription Pricing</h5>
                    <button type="button" class="close" data-dismiss="modal">
                        <span>&times;</span>
                    </button>
                </div>
                <div class="modal-body">
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
                        <small class="form-text text-muted">E.g., "Monthly Basic", "Annual Basic Plan"</small>
                    </div>
                    
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" class="form-control" rows="3"></textarea>
                    </div>
                    
                    <div class="form-group">
                        <div class="custom-control custom-checkbox">
                            <input type="checkbox" class="custom-control-input" id="base_pricing_is_active" name="is_active" value="1" checked>
                            <label class="custom-control-label" for="base_pricing_is_active">Active</label>
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
@endsection

@section('scripts')
<script>
$(document).ready(function() {
    // Setup AJAX to include CSRF token
    $.ajaxSetup({
        headers: {
            'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
        }
    });
    
    // Use server-side loaded data
    var pricingData = @json($pricing);
    
    // Display pricing data on page load
    displayBasePricing();
    
    function displayBasePricing() {
        console.log('Displaying base pricing data:', pricingData);
        var html = '';
        if (pricingData && pricingData.length > 0) {
            pricingData.forEach(function(pricing) {
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
                                <button class="btn btn-sm btn-danger delete-pricing" data-id="${pricing.pricing_id}">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </td>
                        </tr>`;
            });
        } else {
            html = '<tr><td colspan="6" class="text-center">No base pricing options configured</td></tr>';
        }
        $('#basePricingList').html(html);
    }
    
    // Function to reload pricing data via AJAX after adding/editing
    function loadBasePricing() {
        $.ajax({
            url: "{{ route('distributors.base-pricing.list') }}",
            method: 'GET',
            dataType: 'json',
            success: function(response) {
                console.log('Base pricing data reloaded:', response);
                if (response.success && response.data) {
                    pricingData = response.data;
                    displayBasePricing();
                }
            },
            error: function(xhr) {
                console.error('Error reloading base pricing:', xhr);
                // On error, just reload the page to get fresh data
                window.location.reload();
            }
        });
    }
    
    // Add base pricing form submission
    $('#addBasePricingForm').on('submit', function(e) {
        e.preventDefault();
        var formData = $(this).serialize();
        
        $.ajax({
            url: "{{ route('distributors.base-pricing.store') }}",
            method: 'POST',
            data: formData + '&_token={{ csrf_token() }}',
            success: function(response) {
                $('#addBasePricingModal').modal('hide');
                $('#addBasePricingForm')[0].reset();
                loadBasePricing();
                alert('Base pricing added successfully');
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
                    loadBasePricing();
                    alert('Pricing deleted successfully');
                },
                error: function(xhr) {
                    alert('Error: ' + xhr.responseJSON.message);
                }
            });
        }
    });
});
</script>
@endsection