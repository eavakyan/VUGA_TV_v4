@extends('include.app')

@section('title', 'Create Distributor')

@section('content')
<div class="container-fluid">
    <div class="row">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Create New Distributor</h3>
                </div>
                <form action="{{ route('distributors.store') }}" method="POST">
                    @csrf
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
                                   id="name" name="name" value="{{ old('name') }}" required>
                            @error('name')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <label for="code">Code <span class="text-danger">*</span></label>
                            <input type="text" class="form-control @error('code') is-invalid @enderror" 
                                   id="code" name="code" value="{{ old('code') }}" 
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
                                      id="description" name="description" rows="3">{{ old('description') }}</textarea>
                            @error('description')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <label for="logo_url">Logo URL</label>
                            <input type="text" class="form-control @error('logo_url') is-invalid @enderror" 
                                   id="logo_url" name="logo_url" value="{{ old('logo_url') }}">
                            @error('logo_url')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <label>Subscription Type</label>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="subscription_type" 
                                       id="type_premium" value="premium" checked>
                                <label class="form-check-label" for="type_premium">
                                    Premium Distributor (Requires separate subscription)
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="subscription_type" 
                                       id="type_base" value="base">
                                <label class="form-check-label" for="type_base">
                                    Base Included (Included with base subscription)
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="subscription_type" 
                                       id="type_free" value="free">
                                <label class="form-check-label" for="type_free">
                                    Free Content (No subscription required)
                                </label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="display_order">Display Order</label>
                            <input type="number" class="form-control @error('display_order') is-invalid @enderror" 
                                   id="display_order" name="display_order" value="{{ old('display_order', 0) }}">
                            @error('display_order')
                                <span class="invalid-feedback">{{ $message }}</span>
                            @enderror
                        </div>

                        <div class="form-group">
                            <div class="custom-control custom-checkbox">
                                <input type="checkbox" class="custom-control-input" id="is_active" 
                                       name="is_active" value="1" {{ old('is_active', true) ? 'checked' : '' }}>
                                <label class="custom-control-label" for="is_active">Active</label>
                            </div>
                        </div>
                    </div>
                    <div class="card-footer">
                        <button type="submit" class="btn btn-primary">Create Distributor</button>
                        <a href="{{ route('distributors.index') }}" class="btn btn-default">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
$(document).ready(function() {
    // Auto-generate code from name
    $('#name').on('input', function() {
        var name = $(this).val();
        var code = name.toUpperCase().replace(/[^A-Z0-9]/g, '_').replace(/_+/g, '_');
        $('#code').val(code);
    });
    
    // Handle subscription type
    $('input[name="subscription_type"]').on('change', function() {
        var type = $(this).val();
        $('input[name="is_base_included"]').val(type === 'base' ? 1 : 0);
        $('input[name="is_premium"]').val(type === 'premium' ? 1 : 0);
    });
});
</script>
@endsection