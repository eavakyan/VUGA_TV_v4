@extends('include.app')

@section('script')
<script>
$(document).ready(function() {
    $('#distributorsTable').DataTable({
        "order": [[5, "asc"], [1, "asc"]]
    });
});
</script>
@endsection

@section('content')
<section class="section">
    <div class="card">
        <div class="card-header">
            <div class="page-title w-100">
                <div class="d-flex align-items-center justify-content-between">
                    <h4 class="mb-0 fw-semibold">{{ __('Content Distributors') }}</h4>
                    <a href="{{ route('distributors.create') }}" class="btn btn-primary text-light px-4">
                        {{ __('Add Distributor') }}
                    </a>
                </div>
            </div>
        </div>
                <div class="card-body">
                    @if(session('success'))
                        <div class="alert alert-success alert-dismissible">
                            <button type="button" class="close" data-dismiss="alert">&times;</button>
                            {{ session('success') }}
                        </div>
                    @endif
                    
                    @if(session('error'))
                        <div class="alert alert-danger alert-dismissible">
                            <button type="button" class="close" data-dismiss="alert">&times;</button>
                            {{ session('error') }}
                        </div>
                    @endif

                    <table class="table table-striped w-100" id="distributorsTable">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Code</th>
                                <th>Type</th>
                                <th>Content Count</th>
                                <th>Status</th>
                                <th class="text-end" width="200px">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($distributors as $distributor)
                            <tr>
                                <td>{{ $distributor->content_distributor_id }}</td>
                                <td>
                                    {{ $distributor->name }}
                                    @if($distributor->logo_url)
                                        <img src="{{ $distributor->logo_url }}" alt="{{ $distributor->name }}" style="height: 20px; margin-left: 10px;">
                                    @endif
                                </td>
                                <td><code>{{ $distributor->code }}</code></td>
                                <td>
                                    @if($distributor->is_base_included)
                                        <span class="badge badge-info">Base Included</span>
                                    @elseif($distributor->is_premium)
                                        <span class="badge badge-warning">Premium</span>
                                    @else
                                        <span class="badge badge-secondary">Free</span>
                                    @endif
                                </td>
                                <td>{{ $distributor->content_count }}</td>
                                <td>
                                    @if($distributor->is_active)
                                        <span class="badge badge-success">Active</span>
                                    @else
                                        <span class="badge badge-danger">Inactive</span>
                                    @endif
                                </td>
                                <td>
                                    <a href="{{ route('distributors.edit', $distributor->content_distributor_id) }}" 
                                       class="btn btn-sm btn-info">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    @if($distributor->content_count == 0)
                                    <form action="{{ route('distributors.destroy', $distributor->content_distributor_id) }}" 
                                          method="POST" style="display: inline-block;"
                                          onsubmit="return confirm('Are you sure you want to delete this distributor?')">
                                        @csrf
                                        @method('DELETE')
                                        <button type="submit" class="btn btn-sm btn-danger">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </form>
                                    @endif
                                </td>
                            </tr>
                            @endforeach
                        </tbody>
                    </table>
        </div>
    </div>
</section>
@endsection