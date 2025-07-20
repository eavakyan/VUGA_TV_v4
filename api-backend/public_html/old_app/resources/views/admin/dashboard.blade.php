@extends('admin_layouts/main')
@section('pageSpecificCss')
<style>
.small-text{
    font-size: 15px;
}
.card-icon2 {
    width: 50px;
    height: 50px;
    line-height: 50px;
    font-size: 22px;
    margin: 25px 65px;
    box-shadow: 5px 3px 10px 0 rgba(21,15,15,0.3);
    border-radius: 10px;
    background: #6777ef;
    text-align: center;
}
.card-icon2 i{
    font-size: 22px;
    color: #fff;
}
</style>
@stop
@section('content')

<section class="section">
    
    <div class="row ">

        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-box"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalSubscription}}</h2>
                    </h3>
                    <a href="#"><h5 class="font-15"> Subscription List</h5></a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-users"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalUser}}</h2>
                    </h3>
                    <a href="{{ url('/genre/list') }}"> <h5 class="font-15">Users</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-film"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalMovie}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"><h5 class="font-15"> Movie</h5></a>
                    </div>
                </div>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-play"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalSeries}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15"> Series</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-tv"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalLanguage}}</h2>
                    </h3>
                    <a href="{{ url('/tv/channel/list') }}"> <h5 class="font-15">TV Channels</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-cube"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalTVCategory}}</h2>
                    </h3>
                    <a href="{{ url('/tv/category/list') }}"> <h5 class="font-15">TV Categories</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-tag"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalGenre}}</h2>
                    </h3>
                    <a href="{{ url('/genre/list') }}"> <h5 class="font-15">Genres</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-language"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalLanguage}}</h2>
                    </h3>
                    <a href="{{ url('/language/list') }}"> <h5 class="font-15">Language</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-users"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalActor}}</h2>
                    </h3>
                    <a href="{{ url('/actor/list') }}"> <h5 class="font-15">Actors</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div>

        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-eye"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalMovieViews}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15">Movie Views</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->
        <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <!-- <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-cloud-download-alt"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalMovieDownload}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15">Movie Downloads</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->
        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-share-alt"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalMovieShare}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15">Movie Shares</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->

        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-eye"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalSeriesViews}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15">Series Views</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->
        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-cloud-download-alt"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalSeriesDownload}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15">Series Downloads</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->
        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-share-alt"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalSeriesShare}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15">Series Shares</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->

        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-eye"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalChannelViews}}</h2>
                    </h3>
                    <a href="{{ url('/tv/channel/list') }}"> <h5 class="font-15">Channels Views</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->
        
        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-share-alt"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalChannelShare}}</h2>
                    </h3>
                    <a href="{{ url('/tv/channel/list') }}"> <h5 class="font-15">Channels Shares</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->

        <!-- <div class="col-lg-3 col-md-6 col-sm-6 col-12">
            <div class="card card-statistic-1">
                <div class="card-icon bg-dark">
                <i class="fas fa-comment"></i>
                </div>
                <div class="card-wrap">
                <div class="padding-20">
                    <div class="text-right">
                    <h3 class="font-light mb-0">
                        <i class="ti-arrow-up text-success"></i>  <h2>{{$totalComment}}</h2>
                    </h3>
                    <a href="{{ url('/content/list') }}"> <h5 class="font-15">Comments</h5> </a>
                    </div>
                </div>
                </div>
            </div>
        </div> -->

    </div>
</section>

@endsection
@section('pageSpecificJs')
<script src="{{asset('assets/bundles/chartjs/chart.min.js')}}"></script>
<script src="{{asset('assets/dist/js/custom.js')}}"></script>
@stop