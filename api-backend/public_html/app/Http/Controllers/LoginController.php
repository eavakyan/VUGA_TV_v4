<?php

namespace App\Http\Controllers;

use App\Admin;
use App\GlobalSettings;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Session;

class LoginController extends Controller
{

    function login()
    {
        $setting = GlobalSettings::first();
        if ($setting) {
            Session::put('app_name', $setting->app_name);
        }
        Artisan::call('storage:link');
        if (Session::get('user_name')) {
            return redirect('/index');
        }
        return view('login');
    }

    function checklogin(Request $req)
    {

        $data = Admin::where('user_name', $req->user_name)->first();

        if ($data && $req->user_name == $data['user_name'] && $req->user_password == $data->user_password) {
            $req->session()->put('user_name', $data['user_name']);
            $req->session()->put('user_type', $data['user_type']);
            return redirect('index');
        } else {
            Session::flash('message', 'Wrong credentials !');
            return redirect()->route('/');
        }
    }

    function logout()
    {

        session()->pull('user_name');
        session()->pull('user_type');
        return  redirect(url('/'));
    }
}
