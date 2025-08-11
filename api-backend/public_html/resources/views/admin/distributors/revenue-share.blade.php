@extends('include.app')

@section('title', 'Revenue Share Configuration')

@section('content')
<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Revenue Share Configuration</h3>
                </div>
                <div class="card-body">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i> 
                        Configure revenue sharing percentages for premium distributors. Revenue share is calculated on subscription revenues.
                    </div>

                    <div id="successAlert" class="alert alert-success d-none"></div>
                    <div id="errorAlert" class="alert alert-danger d-none"></div>

                    <table class="table table-bordered">
                        <thead>
                            <tr>
                                <th>Distributor</th>
                                <th>Revenue Share %</th>
                                <th>Minimum Payout</th>
                                <th>Payment Terms</th>
                                <th>Notes</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($distributors as $distributor)
                            <tr>
                                <td>
                                    <strong>{{ $distributor->name }}</strong><br>
                                    <small class="text-muted">{{ $distributor->code }}</small>
                                </td>
                                <td>
                                    <div class="input-group">
                                        <input type="number" class="form-control" 
                                               id="percentage_{{ $distributor->content_distributor_id }}"
                                               value="{{ $distributor->revenueShareConfig->revenue_share_percentage ?? 0 }}"
                                               min="0" max="100" step="0.01">
                                        <div class="input-group-append">
                                            <span class="input-group-text">%</span>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <span class="input-group-text">$</span>
                                        </div>
                                        <input type="number" class="form-control" 
                                               id="minimum_{{ $distributor->content_distributor_id }}"
                                               value="{{ $distributor->revenueShareConfig->minimum_payout ?? 0 }}"
                                               min="0" step="0.01">
                                    </div>
                                </td>
                                <td>
                                    <div class="input-group">
                                        <input type="number" class="form-control" 
                                               id="terms_{{ $distributor->content_distributor_id }}"
                                               value="{{ $distributor->revenueShareConfig->payment_terms_days ?? 30 }}"
                                               min="1" max="365">
                                        <div class="input-group-append">
                                            <span class="input-group-text">days</span>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <textarea class="form-control" rows="2"
                                              id="notes_{{ $distributor->content_distributor_id }}">{{ $distributor->revenueShareConfig->notes ?? '' }}</textarea>
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-primary save-config" 
                                            data-distributor-id="{{ $distributor->content_distributor_id }}">
                                        <i class="fas fa-save"></i> Save
                                    </button>
                                </td>
                            </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="card mt-4">
                <div class="card-header">
                    <h3 class="card-title">Revenue Share Formula</h3>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <h5>How Revenue Share is Calculated:</h5>
                            <ol>
                                <li>Total subscription revenue for distributor is collected monthly</li>
                                <li>Platform fee is deducted (100% - Revenue Share %)</li>
                                <li>Distributor receives their percentage of net revenue</li>
                                <li>Payments are processed according to payment terms</li>
                                <li>Minimum payout threshold must be met</li>
                            </ol>
                        </div>
                        <div class="col-md-6">
                            <h5>Example Calculation:</h5>
                            <div class="bg-light p-3 rounded">
                                <strong>Monthly Revenue:</strong> $10,000<br>
                                <strong>Revenue Share:</strong> 70%<br>
                                <strong>Platform Keeps:</strong> $3,000 (30%)<br>
                                <strong>Distributor Receives:</strong> $7,000 (70%)<br>
                                <small class="text-muted">*Subject to minimum payout threshold</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
$(document).ready(function() {
    $('.save-config').on('click', function() {
        var btn = $(this);
        var distributorId = btn.data('distributor-id');
        
        var data = {
            _token: '{{ csrf_token() }}',
            revenue_share_percentage: $('#percentage_' + distributorId).val(),
            minimum_payout: $('#minimum_' + distributorId).val(),
            payment_terms_days: $('#terms_' + distributorId).val(),
            notes: $('#notes_' + distributorId).val()
        };
        
        btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i>');
        
        $.ajax({
            url: '/admin/distributors/' + distributorId + '/revenue-share',
            method: 'POST',
            data: data,
            success: function(response) {
                $('#successAlert').removeClass('d-none').text(response.message);
                $('#errorAlert').addClass('d-none');
                
                setTimeout(function() {
                    $('#successAlert').addClass('d-none');
                }, 3000);
            },
            error: function(xhr) {
                var message = 'Failed to update configuration';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                $('#errorAlert').removeClass('d-none').text(message);
                $('#successAlert').addClass('d-none');
            },
            complete: function() {
                btn.prop('disabled', false).html('<i class="fas fa-save"></i> Save');
            }
        });
    });
});
</script>
@endsection