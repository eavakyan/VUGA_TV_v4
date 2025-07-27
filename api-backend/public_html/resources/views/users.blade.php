@extends('include.app')
@section('script')
<script src="{{ asset('assets/script/users.js') }}"></script>
@endsection

@section('content')
<section class="section">
  <div class="card">
    <div class="card-header">
      <div class="page-title w-100">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="mb-0 fw-semibold">{{ __('userList') }} (<span class="total_user">{{$totalUser}}</span>)</h4>
        </div>
      </div>
    </div>
    <div class="card-body">
      <table class="table table-striped w-100" id="usersTable">
        <thead>
          <tr>
            <th style="width: 150px"> {{ __('profile') }}</th>
            <th> {{ __('email') }} </th>
            <th> {{ __('deviceType') }} </th>
            <th> {{ __('loginType') }} </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</section>
@endsection