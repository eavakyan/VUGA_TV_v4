<!DOCTYPE html>  
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="description" content="">
  <meta name="author" content="">
  <title>Flix Movie</title>

  <link href="{{asset('assets/css/app.min.css')}}" rel="stylesheet">
  <link href="{{asset('assets/bundles/bootstrap-social/bootstrap-social.css')}}" rel="stylesheet">
  <link href="{{asset('assets/css/style.css')}}" rel="stylesheet">
   <link href="{{asset('assets/css/components.css')}}" id="theme"  rel="stylesheet">
  <link href="{{asset('assets/css/custom.css')}}" rel="stylesheet">
  <link rel='shortcut icon' type='image/x-icon' href='assets/img/favicon.ico' />
 <style>

   .logo_name a{
    font-weight: 700 !important;
    color: #6c757d !important;
   }
   </style>
</head>

<body class="theme-black">
  <div class="loader"></div>
  <div class="container">
    <div id="app">
    
        <?php 
        $data = \App\Settings::first();
        ?>
        <h2 class="mt-3 text-dark text-center logo_name"><a class="" href="{{ url('/dashboard') }}">  {{$data->app_name}} </a></h2>
              
      <section class="section">
          <div class="section-body">
              <div class="row mt-sm-4">
                  <div class="col-12 col-md-12 col-lg-12">
                      <h3>Privacy Policy</h3>
                      <hr>
                      @if($privacyPolicy){!! $privacyPolicy->policy !!}@endif
                  </div>
        
              </div>
          </div>
      </section>
    </div>
  </div>
  <!-- <script src="{{asset('plugins/bower_components/jquery/dist/jquery.min.js') }}"></script> -->
  <script src="{{asset('assets/js/app.min.js') }}"> </script>
  <script src="{{asset('assets/js/scripts.js') }}"> </script>
  <script src="{{asset('assets/js/custom.js') }}"></script>

</body>
</html>
