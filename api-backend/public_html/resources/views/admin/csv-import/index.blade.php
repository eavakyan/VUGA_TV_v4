@extends('include.app')
@section('title', 'Bulk Content Import')
@section('content')

<div class="section-body">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h4>Bulk Content Import via CSV</h4>
                </div>
                <div class="card-body">
                    @if(session('success'))
                        <div class="alert alert-success alert-dismissible show fade">
                            <div class="alert-body">
                                <button class="close" data-dismiss="alert">
                                    <span>&times;</span>
                                </button>
                                {{ session('success') }}
                            </div>
                        </div>
                    @endif

                    @if(session('error'))
                        <div class="alert alert-danger alert-dismissible show fade">
                            <div class="alert-body">
                                <button class="close" data-dismiss="alert">
                                    <span>&times;</span>
                                </button>
                                {{ session('error') }}
                            </div>
                        </div>
                    @endif

                    @if(session('import_results'))
                        @php $results = session('import_results'); @endphp
                        
                        @if(!empty($results['warnings']))
                            <div class="alert alert-warning">
                                <h6>Warnings:</h6>
                                <ul class="mb-0">
                                    @foreach($results['warnings'] as $warning)
                                        <li>{{ $warning }}</li>
                                    @endforeach
                                </ul>
                            </div>
                        @endif

                        @if(!empty($results['errors']))
                            <div class="alert alert-danger">
                                <h6>Errors:</h6>
                                <ul class="mb-0" style="max-height: 300px; overflow-y: auto;">
                                    @foreach($results['errors'] as $error)
                                        <li>{{ $error }}</li>
                                    @endforeach
                                </ul>
                            </div>
                        @endif
                    @endif

                    <div class="row">
                        <div class="col-md-8">
                            <form action="{{ route('admin.csv-import.import') }}" method="POST" enctype="multipart/form-data">
                                @csrf
                                
                                <div class="form-group">
                                    <label>CSV File</label>
                                    <input type="file" name="csv_file" class="form-control @error('csv_file') is-invalid @enderror" accept=".csv,text/csv" required>
                                    @error('csv_file')
                                        <div class="invalid-feedback">{{ $message }}</div>
                                    @enderror
                                    <small class="form-text text-muted">
                                        Maximum file size: 10MB. File must be in CSV format.
                                    </small>
                                </div>

                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" name="dry_run" value="1" class="custom-control-input" id="dry-run">
                                        <label class="custom-control-label" for="dry-run">
                                            Dry Run (validate without importing)
                                        </label>
                                    </div>
                                    <small class="form-text text-muted">
                                        Check this to validate the CSV file without actually importing any data.
                                    </small>
                                </div>

                                <div class="form-group">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-upload"></i> Import Content
                                    </button>
                                    <a href="{{ route('admin.csv-import.template') }}" class="btn btn-secondary">
                                        <i class="fas fa-download"></i> Download Template
                                    </a>
                                </div>
                            </form>
                        </div>
                        
                        <div class="col-md-4">
                            <div class="card bg-light">
                                <div class="card-body">
                                    <h6 class="card-title">Import Instructions</h6>
                                    <ol class="small mb-0">
                                        <li>Download the template CSV file</li>
                                        <li>Fill in your content data following the format</li>
                                        <li>Save the file as CSV (UTF-8 encoding recommended)</li>
                                        <li>Upload the file using the form</li>
                                        <li>Use "Dry Run" to validate before importing</li>
                                    </ol>
                                    
                                    <hr>
                                    
                                    <h6>Required Fields:</h6>
                                    <ul class="small mb-0">
                                        <li><strong>title</strong> - Content title</li>
                                        <li><strong>description</strong> - Content description</li>
                                        <li><strong>type</strong> - "movie" or "series"</li>
                                        <li><strong>release_year</strong> - 4-digit year</li>
                                        <li><strong>language</strong> - Language name</li>
                                        <li><strong>genres</strong> - Comma-separated list</li>
                                        <li><strong>is_featured</strong> - 0/1 or yes/no</li>
                                    </ul>
                                    
                                    <hr>
                                    
                                    <h6>Optional Fields:</h6>
                                    <ul class="small mb-0">
                                        <li><strong>duration</strong> - Minutes (movies only)</li>
                                        <li><strong>ratings</strong> - 0-10 decimal</li>
                                        <li><strong>vertical_poster</strong> - Image URL</li>
                                        <li><strong>horizontal_poster</strong> - Image URL</li>
                                        <li><strong>trailer_url</strong> - Video URL</li>
                                        <li><strong>age_rating</strong> - e.g., PG-13, TV-MA</li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

@section('scripts')
<script>
$(document).ready(function() {
    // Add loading state on form submit
    $('form').on('submit', function() {
        var $btn = $(this).find('button[type="submit"]');
        $btn.prop('disabled', true);
        $btn.html('<i class="fas fa-spinner fa-spin"></i> Processing...');
    });
});
</script>
@endsection

@endsection