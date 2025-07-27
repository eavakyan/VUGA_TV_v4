@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/viewTerms.js') }}"></script>
<script>
    var toolbarOptions3 = [
        [{
            'header': [1, 2, 3, 4, 5, 6, false]
        }],
        ['bold', 'italic', 'underline', 'link', 'strike'],
        [{
            'list': 'ordered'
        }, {
            'list': 'bullet'
        }],
        [{
            'color': ["#000000", "#e60000", "#ff9900", "#ffff00", "#008a00", "#0066cc", "#9933ff",
                "#ffffff", "#facccc", "#ffebcc", "#ffffcc", "#cce8cc", "#cce0f5", "#ebd6ff",
                "#bbbbbb", "#f06666", "#ffc266", "#ffff66", "#66b966", "#66a3e0", "#c285ff",
                "#888888", "#a10000", "#b26b00", "#b2b200", "#006100", "#0047b2", "#6b24b2",
                "#444444", "#5c0000", "#663d00", "#666600", "#003700", "#002966", "#3d1466"
            ]
        }],
        [{
            'background': ["#000000", "#e60000", "#ff9900", "#ffff00", "#008a00", "#0066cc", "#9933ff",
                "#ffffff", "#facccc", "#ffebcc", "#ffffcc", "#cce8cc", "#cce0f5", "#ebd6ff",
                "#bbbbbb", "#f06666", "#ffc266", "#ffff66", "#66b966", "#66a3e0", "#c285ff",
                "#888888", "#a10000", "#b26b00", "#b2b200", "#006100", "#0047b2", "#6b24b2",
                "#444444", "#5c0000", "#663d00", "#666600", "#003700", "#002966", "#3d1466"
            ]
        }],
        [{
            'align': []
        }],
    ];

    var quill3 = new Quill('#termsContent', {
        modules: {
            toolbar: toolbarOptions3
        },
        theme: 'snow'
    });

    var content = {!!json_encode($data) !!};

    const delta = quill3.clipboard.convert(content);
    quill3.setContents(delta);
</script>
@endsection

@section('content')
<div class="card">
    <div class="card-header">
        <h4 class="mb-0 fw-semibold">{{ __('Terms of use') }}</h4>
        <a href="termsOfUse" target="_blank" class="btn theme-btn text-white" style="padding: 3px 25px;">
            {{ __('Preview') }}
        </a>
    </div>

    <div class="card-body px-4">
        <form Autocomplete="off" action="" method="post" id="terms" required>
            <div class="form-group">
                <div id="termsContent" class="quillEditorPrivacy"></div>
            </div>
            <button type="submit" class="btn theme-btn text-white">{{ __('Save') }}</button>
        </form>
    </div>
</div>
@endsection