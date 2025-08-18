@extends('include.app')
@section('title', 'Enhanced Bulk Content Import')
@section('content')

<div class="section-body">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h4>Enhanced Bulk Content Import with Cast & Crew</h4>
                    <div class="card-header-action">
                        <a href="{{ route('admin.enhanced-csv-import.template') }}" class="btn btn-primary">
                            <i class="fas fa-download"></i> Download Enhanced Template
                        </a>
                    </div>
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
                        
                        <!-- Import Summary -->
                        @if($results['success'] > 0)
                            <div class="alert alert-info">
                                <h6>Import Summary:</h6>
                                <ul class="mb-0">
                                    <li>Successfully imported: <strong>{{ $results['success'] }}</strong> records</li>
                                    @if(!empty($results['imported_content']))
                                        <li>Content imported: {{ count($results['imported_content']) }} titles</li>
                                    @endif
                                    @if(!empty($results['imported_actors']))
                                        <li>Actors/Crew imported: {{ count(array_unique($results['imported_actors'])) }} people</li>
                                    @endif
                                    @if(!empty($results['imported_sources']))
                                        <li>Video sources added: {{ count($results['imported_sources']) }} files</li>
                                    @endif
                                </ul>
                            </div>
                        @endif
                        
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

                    <form action="{{ route('admin.enhanced-csv-import.import') }}" method="POST" enctype="multipart/form-data">
                        @csrf
                        
                        <div class="row">
                            <div class="col-md-8">
                                <div class="form-group">
                                    <label>CSV File</label>
                                    <div class="custom-file">
                                        <input type="file" name="csv_file" class="custom-file-input @error('csv_file') is-invalid @enderror" id="csv-file" accept=".csv,text/csv" required>
                                        <label class="custom-file-label" for="csv-file">Choose CSV file</label>
                                    </div>
                                    @error('csv_file')
                                        <div class="invalid-feedback">{{ $message }}</div>
                                    @enderror
                                    <small class="form-text text-muted">
                                        Maximum file size: 20MB. File must be in CSV format with UTF-8 encoding.
                                    </small>
                                </div>

                                <div class="form-group">
                                    <label>Import Options</label>
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" name="import_cast" value="1" class="custom-control-input" id="import-cast" checked>
                                        <label class="custom-control-label" for="import-cast">
                                            Import Cast & Crew Information
                                        </label>
                                    </div>
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" name="import_sources" value="1" class="custom-control-input" id="import-sources" checked>
                                        <label class="custom-control-label" for="import-sources">
                                            Import Video Sources (MP4 URLs)
                                        </label>
                                    </div>
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" name="dry_run" value="1" class="custom-control-input" id="dry-run">
                                        <label class="custom-control-label" for="dry-run">
                                            <strong>Dry Run</strong> (validate without importing)
                                        </label>
                                    </div>
                                    <small class="form-text text-muted">
                                        Dry run will validate your CSV and show what would be imported without actually saving to database.
                                    </small>
                                </div>

                                <div class="form-group">
                                    <button type="submit" class="btn btn-primary btn-lg">
                                        <i class="fas fa-upload"></i> Import Content
                                    </button>
                                    <button type="button" class="btn btn-info btn-lg" data-toggle="modal" data-target="#previewModal">
                                        <i class="fas fa-eye"></i> Preview Template Data
                                    </button>
                                </div>
                            </div>
                            
                            <div class="col-md-4">
                                <div class="card bg-light">
                                    <div class="card-body">
                                        <h6 class="card-title">Enhanced Import Features</h6>
                                        <div class="alert alert-info mb-3">
                                            <strong>New!</strong> This enhanced importer includes:
                                            <ul class="small mb-0 mt-2">
                                                <li>Cast & crew information</li>
                                                <li>Direct MP4 video URLs</li>
                                                <li>Director, producer, writer details</li>
                                                <li>Production metadata</li>
                                                <li>Multiple video quality options</li>
                                            </ul>
                                        </div>
                                        
                                        <h6>Import Process:</h6>
                                        <ol class="small mb-3">
                                            <li>Download the enhanced template</li>
                                            <li>Fill in content & cast data</li>
                                            <li>Add video URLs (MP4, YouTube, etc.)</li>
                                            <li>Save as CSV (UTF-8)</li>
                                            <li>Run dry run to validate</li>
                                            <li>Import when ready</li>
                                        </ol>
                                        
                                        <h6>Core Fields:</h6>
                                        <ul class="small mb-3">
                                            <li><strong>title</strong> - Movie/series title</li>
                                            <li><strong>type</strong> - "movie" or "series"</li>
                                            <li><strong>cast</strong> - Actor:Character format</li>
                                            <li><strong>video_url</strong> - Direct MP4 URL</li>
                                            <li><strong>director</strong> - Director name</li>
                                        </ul>
                                        
                                        <h6>Cast Format:</h6>
                                        <code class="small">
                                            Actor Name:Character Name,<br>
                                            Actor2:Character2
                                        </code>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Public Domain Movies Card -->
            <div class="card">
                <div class="card-header">
                    <h4>Available Public Domain Movies</h4>
                </div>
                <div class="card-body">
                    <p>The enhanced template includes 10 classic public domain movies with downloadable MP4 files:</p>
                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>Title</th>
                                    <th>Year</th>
                                    <th>Genre</th>
                                    <th>Director</th>
                                    <th>Source</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Night of the Living Dead</td>
                                    <td>1968</td>
                                    <td>Horror</td>
                                    <td>George A. Romero</td>
                                    <td><span class="badge badge-success">Archive.org</span></td>
                                </tr>
                                <tr>
                                    <td>The General</td>
                                    <td>1926</td>
                                    <td>Comedy/Action</td>
                                    <td>Buster Keaton</td>
                                    <td><span class="badge badge-success">Archive.org</span></td>
                                </tr>
                                <tr>
                                    <td>Nosferatu</td>
                                    <td>1922</td>
                                    <td>Horror</td>
                                    <td>F.W. Murnau</td>
                                    <td><span class="badge badge-success">Archive.org</span></td>
                                </tr>
                                <tr>
                                    <td>His Girl Friday</td>
                                    <td>1940</td>
                                    <td>Comedy</td>
                                    <td>Howard Hawks</td>
                                    <td><span class="badge badge-success">Archive.org</span></td>
                                </tr>
                                <tr>
                                    <td>Charade</td>
                                    <td>1963</td>
                                    <td>Thriller</td>
                                    <td>Stanley Donen</td>
                                    <td><span class="badge badge-success">Archive.org</span></td>
                                </tr>
                                <tr>
                                    <td>Metropolis</td>
                                    <td>1927</td>
                                    <td>Sci-Fi</td>
                                    <td>Fritz Lang</td>
                                    <td><span class="badge badge-success">Archive.org</span></td>
                                </tr>
                                <tr>
                                    <td colspan="5" class="text-center">
                                        <small>...and 4 more classic films in the template</small>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Preview Modal -->
<div class="modal fade" id="previewModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-xl" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Template Data Preview</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="table-responsive">
                    <table class="table table-bordered table-sm">
                        <thead>
                            <tr>
                                <th>Title</th>
                                <th>Cast</th>
                                <th>Director</th>
                                <th>Video URL</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Night of the Living Dead</td>
                                <td>Judith O'Dea:Barbra, Duane Jones:Ben...</td>
                                <td>George A. Romero</td>
                                <td><code>archive.org/download/...</code></td>
                            </tr>
                            <tr>
                                <td>The General</td>
                                <td>Buster Keaton:Johnnie Gray...</td>
                                <td>Buster Keaton</td>
                                <td><code>archive.org/download/...</code></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

@endsection

@section('scripts')
<script>
$(document).ready(function() {
    // Update file input label
    $('#csv-file').on('change', function() {
        var fileName = $(this).val().split('\\').pop();
        $(this).next('.custom-file-label').addClass("selected").html(fileName);
    });
    
    // Add loading state on form submit
    $('form').on('submit', function() {
        var $btn = $(this).find('button[type="submit"]');
        $btn.prop('disabled', true);
        $btn.html('<i class="fas fa-spinner fa-spin"></i> Processing...');
    });
});
</script>
@endsection