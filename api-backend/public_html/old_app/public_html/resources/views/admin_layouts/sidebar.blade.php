<div class="main-sidebar sidebar-style-2">
    <aside id="sidebar-wrapper">
        <div class="sidebar-brand">
            <a href="{{ url('/dashboard') }}">
                <?php
                $data = \App\Settings::first();
                ?>
                <h3 class="mt-3 logo_name">{{ $data->app_name }}</h3>
            </a>
        </div>
        <ul class="sidebar-menu">
            <li class="menu-header">Main</li>
            <li class="dropdown {{ active_class(['dashboard']) }}">
                <a href="{{ url('/dashboard') }}" class="nav-link"><i
                        data-feather="monitor"></i><span>Dashboard</span></a>
            </li>
            <li class="dropdown {{ active_class(['user*']) }}">
                <a href="{{ url('/user/list') }}" class="nav-link"><i data-feather="users"></i><span>User</span></a>
            </li>

            <li
                class="dropdown {{ request()->is('content*') || request()->is('movie/source*') || request()->is('series/source*') || request()->is('movie/cast*') || request()->is('movie/subtitle*') || request()->is('series/subtitle*') || request()->is('series/season*') ? 'active' : '' }}">
                <a href="{{ url('/content/list') }}" class="nav-link"><i
                        data-feather="video"></i><span>Content</span></a>
            </li>

            <li class="dropdown {{ request()->is('tv/channel*') ? 'active' : '' }}">
                <a href="{{ url('/tv/channel/list') }}" class="nav-link"><i data-feather="tv"></i><span>Live TV
                        Channel</span></a>
            </li>


            <li class="dropdown {{ active_class(['tv/category/list']) }}">
                <a href="{{ url('/tv/category/list') }}" class="nav-link"><i data-feather="box"></i><span>Live TV
                        Categories</span></a>
            </li>


            <li class="dropdown {{ active_class(['actor/list']) }}">
                <a href="{{ url('/actor/list') }}" class="nav-link"><i
                        class="fas fa-users ml-0"></i><span>Actors</span></a>
            </li>

            <li class="dropdown {{ active_class(['genre/list']) }}">
                <a href="{{ url('/genre/list') }}" class="nav-link"><i data-feather="tag"></i><span>Genres</span></a>
            </li>

            <li class="dropdown {{ active_class(['language/list']) }}">
                <a href="{{ url('/language/list') }}" class="nav-link"><i
                        class="fas fa-language ml-0"></i><span>Language</span></a>
            </li>

            <li class="dropdown {{ active_class(['subscription/package']) }}">
                <a href="{{ url('/subscription/package') }}" class="nav-link"><i
                        data-feather="package"></i><span>Pack Subscription</span></a>
            </li>

            <li class="dropdown {{ active_class(['subscription/list']) }}">
                <a href="{{ url('/subscription/list') }}" class="nav-link"><i
                        data-feather="credit-card"></i><span>Subscription</span></a>
            </li>

            <li class="dropdown {{ active_class(['notification/list']) }}">
                <a href="{{ url('/notification/list') }}" class="nav-link"><i
                        data-feather="bell"></i><span>Notification</span></a>
            </li>

            <li class="dropdown {{ active_class(['ads']) }}">
                <a href="{{ url('/ads') }}" class="nav-link"><i data-feather="trending-up"></i><span>Admob
                        Ads</span></a>
            </li>

            <li class="dropdown {{ active_class(['ads/customAdsList']) }}">
                <a href="{{ url('/ads/customAdsList') }}" class="nav-link"><i data-feather="layout"></i><span>Custom
                        Ads</span></a>
            </li>

            <li class="dropdown {{ active_class(['settings']) }}">
                <a href="{{ url('/settings') }}" class="nav-link"><i
                        data-feather="settings"></i><span>Settings</span></a>
            </li>

            <li class="dropdown {{ active_class(['privacypolicy']) }}">
                <a href="{{ url('/privacypolicy') }}" class="nav-link"><i data-feather="target"></i><span>Privacy
                        Policy</span></a>
            </li>

            <li class="dropdown {{ active_class(['termscondition']) }}">
                <a href="{{ url('/termscondition') }}" class="nav-link"><i data-feather="lock"></i><span>Terms &
                        Condition</span></a>
            </li>

        </ul>
    </aside>
</div>
