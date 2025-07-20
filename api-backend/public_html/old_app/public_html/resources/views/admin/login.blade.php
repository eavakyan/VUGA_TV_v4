<!DOCTYPE html>  
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="description" content="">
  <meta name="author" content="">
  <title>Flix Movie - Login</title>

  <link href="{{asset('assets/css/app.min.css')}}" rel="stylesheet">
  <link href="{{asset('assets/bundles/bootstrap-social/bootstrap-social.css')}}" rel="stylesheet">
  <link href="{{asset('assets/css/style.css')}}" rel="stylesheet">
   <link href="{{asset('assets/css/components.css')}}" id="theme"  rel="stylesheet">
  <link href="{{asset('assets/css/custom.css')}}" rel="stylesheet">
  <link rel='shortcut icon' type='image/x-icon' href='assets/img/favicon.ico' />
 
</head>

<body class="theme-black">
  <div class="loader"></div>
  <div id="app">
    <section class="section">
      <div class="container mt-5">
        <div class="row">
        <div class="col-12 col-sm-8 offset-sm-2 col-md-6 offset-md-3">
        @if(Session::has('sessionExpired'))
        <div class="alert alert-danger alert-dismissible show fade">
          <div class="alert-body">
            <button class="close" data-dismiss="alert">
              <span>×</span>
            </button>
            {{  session('sessionExpired') }}
          </div>
        </div>
        @endif
        @if(Session::has('matchResetPassword'))
        <div class="alert alert-danger alert-dismissible show fade">
          <div class="alert-body">
            <button class="close" data-dismiss="alert">
              <span>×</span>
            </button>
            {{  session('matchResetPassword') }}
          </div>
        </div>
        @endif
        @if(Session::has('invalid'))

        <div class="alert alert-danger alert-dismissible show fade">
          <div class="alert-body">
            <button class="close" data-dismiss="alert">
              <span>×</span>
            </button>
            {{  session('invalid') }}
          </div>
        </div>
        @endif
      </div>
          <div class="col-12 col-sm-8 offset-sm-2 col-md-6 offset-md-3 col-lg-6 offset-lg-3 col-xl-4 offset-xl-4">
            <div class="card card-primary">
              <div class="card-header">
                <h4>Login</h4>
              </div>
              <div class="card-body">
                <form class="needs-validation" id="loginform" action="{{ route('login.submit') }}" method="post" novalidate="">
                {{ csrf_field() }}
                  <div class="form-group">
                    <label for="username">Username</label>
                    <input id="username" type="text" class="form-control" name="username" tabindex="1" required autofocus>
                    <div class="invalid-feedback">
                      Please fill in your username
                    </div>
                  </div>
                  <div class="form-group">
                    <div class="d-block">
                      <label for="password" class="control-label">Password</label>
                    </div>
                    <input id="password" type="password" class="form-control" name="password" tabindex="2" required>
                    <div class="invalid-feedback">
                      please fill in your password
                    </div>
                  </div>
                  <div class="form-group">
                    <div class="custom-control custom-checkbox">
                      <input type="checkbox" name="remember" class="custom-control-input" tabindex="3" id="remember-me">
                      <label class="custom-control-label" for="remember-me">Remember Me</label>
                    </div>
                  </div>
                  <div class="form-group">
                    <button type="submit" class="btn btn-primary btn-lg btn-block" tabindex="4">
                      Login
                    </button>
                  </div>
                </form>
                
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>

  <!-- <script src="{{asset('plugins/bower_components/jquery/dist/jquery.min.js') }}"></script> -->
  <script src="{{asset('assets/js/app.min.js') }}"> </script>
  <script src="{{asset('assets/js/scripts.js') }}"> </script>
  <script src="{{asset('assets/js/custom.js') }}"></script>
  <script type="text/javascript">
  $(document).ready(function(){
    setTimeout(function(){ 
      $('.alert-dismissable').hide();
    }, 10000);
  });
</script>


</body>
</html>


