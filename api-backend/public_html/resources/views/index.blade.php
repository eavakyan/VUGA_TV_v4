@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/index.js') }}"></script>
@endsection

@section('content')

<section class="section dashboard-section">
    <div class="dashboard-cards">
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="users"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$totalUser}} </p>
                    <a href="{{ route('users') }}">
                        {{ __('users')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="video"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$contents}} </p>
                    <a href="{{ url('contentList') }}">
                        {{ __('contents')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="star"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$actors}} </p>
                    <a href="{{ url('actors') }}">
                        {{ __('actors')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="package"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$genres}} </p>
                    <a href="{{ route('genres') }}">
                        {{ __('genres')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="globe"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$languages}} </p>
                    <a href="{{ url('languages') }}">
                        {{ __('languages')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="cast"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$liveTvCategories}} </p>
                    <a href="{{ url('liveTvCategories') }}">
                        {{ __('liveTvCategories')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="airplay"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$liveTvChannels}} </p>
                    <a href="{{ url('liveTvChannels') }}">
                        {{ __('liveTvChannels')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="bell"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$notifications}} </p>
                    <a href="{{ url('notification') }}">
                        {{ __('notifications')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="activity"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$admob}} </p>
                    <a href="{{ url('admob') }}">
                        {{ __('admob')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="fast-forward"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> {{$customAds}} </p>
                    <a href="{{ url('customAds') }}">
                        {{ __('customAds')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="dashboard-blog">
            <div class="dashboard-blog-content">
                <div class="card-icon">
                    <i data-feather="settings"></i>
                </div>
                <div class="dashboard-blog-content-top">
                    <p> </p>
                    <a href="{{ url('setting') }}">
                        {{ __('settings')}}
                        <i data-feather="arrow-up-right"></i>
                    </a>
                </div>
            </div>
        </div>
    </div>
</section>

@endsection