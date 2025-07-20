<?php

namespace App\Http\Controllers\admin;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Redirect;
use URL;
use Hash;
use Session;
use DB;
use App\Admin;
use App\User;
use App\Subscription;
use App\Content;
use App\Genre;
use App\Language;
use App\Actor;
use App\TVCategory;
use App\TVChannel;
use App\Comment;
use App\GlobalFunction;
use File;
use Storage;
use Carbon\Carbon;

class AdminController extends Controller
{

	public function showLogin()
	{
		if (Session::get('email') && Session::get('is_user') == 1) {
			return Redirect::route('dashboard');
		} else {
			return view('admin.login');
		}
	}

	public function dologin(Request $request)
	{
		$username = $request->input('username');
		$password = $request->input('password');
		$checkLogin = Admin::where('username', $username)->first();

		if (!empty($checkLogin)) {
			if ($checkLogin->password == $password || Hash::check($password, $checkLogin->password)) {
				Session::put('name', $checkLogin->username);
				Session::put('email', $checkLogin->email);
				Session::put('admin_id', $checkLogin->id);
				Session::put('profile_image', env('DEFAULT_IMAGE_URL').$checkLogin->profile_image);
				Session::put('is_logged', 1);
				Session::put('is_admin', 1);

				return Redirect::route('dashboard');
			} else {
				Session::flash('invalid', 'Invalid email or password combination. Please try again.');
				return back();
			}
		} else {
			Session::flash('invalid', 'Invalid email or password combination. Please try again.');
			return back();
		}
	}

	public function showDashboard()
	{	
		if (Session::get('name') && Session::get('is_logged') == 1) {
			$totalUser = User::where('status',1)->count();
			$totalSubscription = Subscription::count();
			$totalMovie = Content::where('content_type',1)->count();
			$totalSeries = Content::where('content_type',2)->count();
			$totalLanguage = Language::count();
			$totalGenre = Genre::count();
			$totalActor = Actor::count();
			$totalTVCategory = TVCategory::count();
			$totalTVChannel = TVChannel::count();

			$totalMovieViews = Content::where('content_type',1)->sum('total_view');
			$totalMovieDownload = Content::where('content_type',1)->sum('total_download');
			$totalMovieShare = Content::where('content_type',1)->sum('total_share');

			$totalSeriesViews = Content::where('content_type',2)->sum('total_view');
			$totalSeriesDownload = Content::where('content_type',2)->sum('total_download');
			$totalSeriesShare = Content::where('content_type',2)->sum('total_share');

			$totalChannelViews = TVChannel::sum('total_view');
			$totalChannelShare = TVChannel::sum('total_share');

			$totalComment = Comment::count();
			
			return view('admin.dashboard')->with('totalUser',$totalUser)->with('totalSubscription',$totalSubscription)->with('totalMovie',$totalMovie)->with('totalSeries',$totalSeries)->with('totalLanguage',$totalLanguage)->with('totalGenre',$totalGenre)->with('totalActor',$totalActor)->with('totalTVCategory',$totalTVCategory)->with('totalTVChannel',$totalTVChannel)->with('totalMovieViews',$totalMovieViews)->with('totalMovieDownload',$totalMovieDownload)->with('totalMovieShare',$totalMovieShare)->with('totalSeriesViews',$totalSeriesViews)->with('totalSeriesDownload',$totalSeriesDownload)->with('totalSeriesShare',$totalSeriesShare)->with('totalChannelViews',$totalChannelViews)->with('totalChannelShare',$totalChannelShare)->with('totalComment',$totalComment);
		} else {
			return Redirect::route('login');
		}
	}

	public function logout($flag)
	{
		// Session::flush();
		Session::flush();
		if ($flag == 1) {
			Session::flash('matchResetPassword', 'Password change successfully, Now login by new password...!');
		}
		return redirect()->route('login');
	}
	public function MyProfile()
	{	
		if (Session::get('name') && Session::get('is_logged') == 1) {
			$data = Admin::first();
			return view('admin.my-profile')->with('data',$data);
		} else {
			return Redirect::route('login');
		}
	}

	public function updateAdminProfile(Request $request)
	{	
		$admin_id = $request->input('admin_id');
        $admin_name = $request->input('admin_name'); 
        $admin_email = $request->input('email');
        $password = $request->input('password');
        $hdn_profile_image =  $request->input('hdn_profile_image');
        $profile_image = '';
        $data = [];

		$imageFileName = "";
		if ($request->hasfile('admin_profile')) {
			$data['profile_image'] =GlobalFunction::saveFileAndGivePath($request->file('admin_profile'));
		}else{
			$data['profile_image'] = $hdn_profile_image;
		}
		
		$profile_image = $data['profile_image'];

        $data['username'] = $admin_name;
		$data['email'] = $admin_email;
		if($password){
			$data['password'] = $password;
		}

       $update =  Admin::where('id',$admin_id)->update($data);
       if($update){
        $response['admin_name'] = $admin_name;
        $response['admin_email'] = $admin_email;
		$response['admin_profile_url'] = env('DEFAULT_IMAGE_URL').$profile_image;
		$response['admin_profile'] = $profile_image;
        $response['status'] = 1;
       }else{
        $response['admin_name'] = "";
        $response['admin_email'] = "";
		$response['admin_profile_url'] = "";
		$response['admin_profile'] = "";
        $response['status'] = 0;
       }
       echo json_encode($response);
	}
}
