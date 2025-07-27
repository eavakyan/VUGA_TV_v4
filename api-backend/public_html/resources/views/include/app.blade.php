<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, shrink-to-fit=no" name="viewport">
    <title>{!! Session::get('app_name') !!}</title>
    <link rel='shortcut icon' type='image/x-icon' href="{{ asset('assets/img/favicon.png') }}" style="width: 2px !important;" />
    <link href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="{{ asset('assets/bundles/sweetalert/css/sweetalert.css') }}">
    <link rel="stylesheet" href="{{ asset('assets/css/selectric.css') }}" type="text/css" />
    <link rel="stylesheet" href="{{ asset('assets/css/quill.css') }}" />
    <link rel="stylesheet" href="{{ asset('assets/css/bootstrap.min.css') }}" type="text/css" />
    <link rel="stylesheet" href="{{ asset('assets/css/iziToast.css') }}" type="text/css" />
    <link rel="stylesheet" href="{{ asset('assets/css/flatpickr.min.css') }}" type="text/css" />
    <link rel="stylesheet" href="{{ asset('assets/css/fancybox.css') }}" type="text/css" />
    <link rel="stylesheet" href="{{ asset('assets/css/swiper-bundle.min.css') }}" type="text/css" />
    <link rel="stylesheet" href="{{ asset('assets/css/jquery-ui.css') }}" type="text/css" />
    <link rel="stylesheet" href="{{ asset('assets/css/style.css') }}">
</head>

<body>

    <div class="loader-bg">
        <div class="loader"></div>
    </div>
    <div id="app">
        <div class="main-wrapper main-wrapper-1">
            <div class="navbar-bg"></div>
            <nav class="navbar navbar-expand-lg main-navbar sticky">
                <div class="form-inline mr-auto">
                    <ul class="navbar-nav mr-3">
                        <li>
                            <a href="#" data-toggle="sidebar" class="nav-link nav-link-lg collapse-btn">
                                <i data-feather="menu"></i>
                            </a>
                        </li>
                    </ul>
                </div>
                <ul class="navbar-nav navbar-right">
                    <li class="dropdown">
                        <a href="#" data-toggle="dropdown" class="nav-link dropdown-toggle nav-link-lg nav-link-user">
                            <span class="profile-btn">
                                <i data-feather="user"></i>
                            </span>
                        </a>
                        <div class="dropdown-menu dropdown-menu-right pullDown">
                            <a href="{{ route('logout') }}" class="dropdown-item has-icon text-danger">
                                <i data-feather="log-out"></i>
                                {{ __('logOut') }}
                            </a>
                        </div>
                    </li>
                </ul>
            </nav>

            <div class="main-sidebar sidebar-style-2">
                <div class="sidebar-brand" id="reloadContent">
                    <a href="{{ route('index') }}">
                        <span class="logo-name">{!! Session::get('app_name') !!}</span>
                        <span class="logo-name-small">{!! Session::get('app_name') !!}</span>
                    </a>
                </div>
                <aside id="sidebar-wrapper">
                    <ul class="sidebar-menu">
                        <li class="sideBarli indexSideA">
                            <a href="{{ route('index') }}" class="nav-link">
                                <i data-feather="home"></i>
                                <span> {{ __('dashboard') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli userSideA">
                            <a href="{{ route('users') }}" class="nav-link">
                                <i data-feather="users"></i>
                                <span> {{ __('users') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli contentSideA">
                            <a href="{{ route('contentList') }}" class="nav-link">
                                <i data-feather="video"></i>
                                <span> {{ __('content') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli mediaGallerySideA">
                            <a href="{{ route('mediaGallery') }}" class="nav-link">
                                <i data-feather="hard-drive"></i>
                                <span> {{ __('mediaGallery') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli topContentsSideA">
                            <a href="{{ route('topContents') }}" class="nav-link">
                                <i data-feather="trending-up"></i>
                                <span> {{ __('topContents') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli actorSideA">
                            <a href="{{ route('actors') }}" class="nav-link">
                                <i data-feather="star"></i>
                                <span> {{ __('actors') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli genreSideA">
                            <a href="{{ route('genres') }}" class="nav-link">
                                <i data-feather="package"></i>
                                <span> {{ __('genres') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli languageSideA">
                            <a href="{{ route('languages') }}" class="nav-link">
                                <i data-feather="globe"></i>
                                <span> {{ __('languages') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli liveTvCategorySideA">
                            <a href="{{ route('liveTvCategories') }}" class="nav-link">
                                <i data-feather="cast"></i>
                                <span> {{ __('liveTvCategories') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli liveTvChannelSideA">
                            <a href="{{ route('liveTvChannels') }}" class="nav-link">
                                <i data-feather="airplay"></i>
                                <span> {{ __('liveTvChannels') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli notificationSideA">
                            <a href="{{ route('notification') }}" class="nav-link">
                                <i data-feather="bell"></i>
                                <span> {{ __('notification') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli admobSideA">
                            <a href="{{ route('admob') }}" class="nav-link">
                                <i data-feather="activity"></i>
                                <span> {{ __('admob') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli customAdSideA">
                            <a href="{{ route('customAds') }}" class="nav-link">
                                <i data-feather="fast-forward"></i>
                                <span> {{ __('customAds') }} </span>
                            </a>
                        </li>
                        <li class="sideBarli settingSideA">
                            <a href="{{ route('setting') }}" class="nav-link">
                                <i data-feather="settings"></i>
                                <span> {{ __('setting') }} </span>
                            </a>
                        </li>
                        <hr>
                        <li class="sideBarli privacySideA">
                            <a href="{{ route('viewPrivacy') }}" class="nav-link">
                                <i data-feather="shield"></i>
                                <span>{{ __('privacyPolicy') }}</span>
                            </a>
                        </li>
                        <li class="sideBarli termsSideA">
                            <a href="{{ route('viewTerms') }}" class="nav-link">
                                <i data-feather="clipboard"></i>
                                <span>{{ __('termsOfUse') }}</span>
                            </a>
                        </li>
                    </ul>
                </aside>
            </div>
            <!-- Main Content -->
            <div class="main-content">
                @yield('content')
                <form action="">
                    <input type="hidden" id="user_type" value="{{ session('user_type') }}">
                </form>
            </div>
        </div>
    </div>

    <input type="hidden" value="{{ env('APP_URL')}}" id="appUrl">
    <input type="hidden" value="{{ env('TMDB_API_KEY')}}" id="TMDB_API_KEY">

    <script src="{{ asset('assets/js/jquery.min.js') }}"></script>
    <script src="{{ asset('assets/js/jquery-3.6.0.min.js') }}"></script>
    <script src="{{ asset('assets/js/quill.js') }}"></script>
    <script src="{{ asset('assets/js/app.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/izitoast/js/iziToast.min.js') }}"></script>
    <script src="{{ asset('assets/bundles/sweetalert/sweetalert.min.js') }}"></script>
    <script src="{{ asset('assets/script/env.js') }}"></script>
    <script src="{{ asset('assets/bundles/datatables/datatables.min.js') }}"></script>
    <script src="{{ asset('assets/js/custom.js') }}"></script>
    <script src="{{ asset('assets/js/bootstrap.min.js') }}"></script>
    <script src="{{ asset('assets/js/app.js') }}"></script>
    <script src="{{ asset('assets/js/selectric.min.js') }}"></script>
    <script src="{{ asset('assets/js/fancybox.umd.js') }}"></script>
    <script src="{{ asset('assets/js/flatpickr.js') }}"></script>
    <script src="{{ asset('assets/js/hls.js') }}"></script>
    <script src="{{ asset('assets/js/jquery-ui.min.js') }}"></script>
    <script>
        $(document).ready(function() {
            $(".selectric").selectric({
                multiple: {
                    separator: ", ", // You can adjust this if needed
                    keepMenuOpen: true,
                    maxLabelEntries: false,
                }
            });

            Fancybox.bind("[data-fancybox]");
            $(".datePicker").flatpickr({
                dateFormat: "d-m-Y",
            });
        });
    </script>
    @yield('script')

    <!-- videoPreviewModal -->
    <div class="modal fade" id="videoPreviewModal" tabindex="-1" aria-labelledby="videoPreviewModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="videoPreview">
                    <video src="" id="showVideoUrl" playsinline autoplay controls type="video/mp4"></video>
                </div>
            </div>
        </div>
    </div>

</body>

</html>