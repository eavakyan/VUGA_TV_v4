@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/topContents.js') }}"></script>
<style>
/* Dark theme styling for sort dropdown */
#sortBy {
    background-color: #2a2a2a !important;
    color: #ffffff !important;
    border: 1px solid #444444 !important;
}

#sortBy:focus {
    background-color: #333333 !important;
    border-color: #007bff !important;
    box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

#sortBy option {
    background-color: #2a2a2a !important;
    color: #ffffff !important;
}

/* Badge styling */
.badge-primary {
    background-color: #007bff !important;
}

.badge-info {
    background-color: #17a2b8 !important;
}

/* Ensure rank numbers are visible */
#topContentsTable tbody td:first-child {
    font-weight: bold;
    font-size: 1.1em;
    color: #ffc107;
}
</style>
@endsection

@section('content')
<section class="section px-2 px-md-0">
    <div class="card">
        <div class="card-header">
            <div class="page-title w-100">
                <div class="d-flex align-items-center justify-content-between">
                    <h4 class="mb-0 fw-semibold">{{ __('topContents') }}</h4>
                    <div class="d-flex align-items-center">
                        <label class="me-2 text-white">Sort by:</label>
                        <select id="sortBy" class="form-select form-select-sm me-3" style="width: 150px;">
                            <option value="views" selected>Most Viewed</option>
                            <option value="ratings">Highest Rated</option>
                            <option value="manual">Manual Selection</option>
                        </select>
                        <button type="button" class="btn theme-bg theme-btn text-white" id="selectContentBtn" data-bs-toggle="modal" data-bs-target="#selectContentModal" style="display: none;">
                            {{ __('selectContent') }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <div class="card-body">
            <table class="table table-striped w-100" id="topContentsTable">
                <thead>
                    <tr>
                        <th style="width: 50px !important;"> {{ __('rank') }}</th>
                        <th class="content-poster"> {{ __('poster') }}</th>
                        <th> {{ __('title') }}</th>
                        <th> {{ __('type') }}</th>
                        <th> {{ __('views') }}</th>
                        <th> {{ __('ratings') }}</th>
                        <th class="text-end" width="200px"> {{ __('action') }} </th>
                    </tr>
                </thead>
            </table>
        </div>
    </div>
</section>


<!-- Select Content Modal -->
<div class="modal fade" id="selectContentModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5 fw-semibold" id="exampleModalLabel"> {{ __('selectContent')}} </h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="selectTopContentForm" method="post">
                <div class="modal-body">
                    <div class="form-group">
                        <input type="text" id="searchInput" onkeyup="searchFunction()" class="form-control" placeholder="{{ __('search')}}">
                    </div>
                    <hr>
                    <div id="searchData">
                        <h6 class="mb-3 selectContentTitle fw-medium">{{ __('movies')}}</h6>
                        <div class="swiper mySwiper">
                            <ul class="swiper-wrapper">
                                @foreach($contents as $content)
                                @if($content->type == 1)
                                <li class="swiper-slide">
                                    <div class="content-data">
                                        <input type="checkbox" id="content{{$content->id}}" name="content_id" value="{{$content->id}}" rel="{{$content->id}}" class="content_checkbox" />
                                        <label for="content{{$content->id}}">
                                            <div class="content-image-div">
                                                <img class="content-image img-fluid img-border border-radius" loading="lazy" src="{{ $content->vertical_poster }}" alt="{{ $content->title }}">
                                            </div>
                                            <p class="mt-1 mb-0"> {{ $content->title }} </p>
                                        </label>
                                    </div>
                                </li>
                                @endif
                                @endforeach
                            </ul>
                            <div class="swiper-button-next"></div>
                            <div class="swiper-button-prev"></div>
                        </div>
                        <hr>
                        <h6 class="mb-3 selectContentTitle fw-medium">{{ __('series')}}</h6>
                        <div class="swiper mySwiper">
                            <ul class="swiper-wrapper">
                                @foreach($contents as $content)
                                @if($content->type == 2)
                                <li class="swiper-slide">
                                    <div class="content-data">
                                        <input type="checkbox" id="content{{$content->id}}" name="content_id" value="{{$content->id}}" rel="{{$content->id}}" class="content_checkbox" />
                                        <label for="content{{$content->id}}">
                                            <div class="content-image-div">
                                                <img class="content-image img-fluid img-border border-radius" loading="lazy" src="{{ $content->vertical_poster }}" alt="{{ $content->title }}">
                                            </div>
                                            <p class="mt-1 mb-0"> {{ $content->title }} </p>
                                        </label>
                                    </div>
                                </li>
                                @endif
                                @endforeach
                            </ul>
                            <div class="swiper-button-next"></div>
                            <div class="swiper-button-prev"></div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>


<!-- Swiper JS -->
<script src="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.js"></script>

<script>
    function searchFunction() {
        var input, filter, ul, li, a, i, txtValue;
        input = document.getElementById("searchInput");
        filter = input.value.toUpperCase();
        ul = document.getElementById("searchData");
        li = ul.getElementsByTagName("li");
        for (i = 0; i < li.length; i++) {
            a = li[i].getElementsByTagName("p")[0];
            txtValue = a.textContent || a.innerText;
            if (txtValue.toUpperCase().indexOf(filter) > -1) {
                li[i].style.display = "";
            } else {
                li[i].style.display = "none";
            }
        }
    }

    var swiper = new Swiper(".mySwiper", {
        slidesPerView: 6,
        spaceBetween: 20,
        slidesPerGroup: 6,
        navigation: {
            nextEl: ".swiper-button-next",
            prevEl: ".swiper-button-prev",
        },
        breakpoints: {
            0: {
                slidesPerView: 2,
                slidesPerGroup: 2,
            },
            768: {
                slidesPerView: 2,
                slidesPerGroup: 2,
            },
            1024: {
                slidesPerView: 4,
                slidesPerGroup: 4,
            },
            1440: {
                slidesPerView: 6,
                slidesPerGroup: 6,
            }
        }
    });
</script>

@endsection