<?php

namespace App\Http\Controllers;

use App\Actor;
use App\Admin;
use App\Admob;
use App\Content;
use App\CustomAd;
use App\Genre;
use App\GlobalSettings;
use App\Language;
use App\Notification;
use App\Pages;
use App\TVCategory;
use App\TVChannel;
use App\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\URL;

class SettingController extends Controller
{
    function index()
    {
        $totalUser = User::count();
        $contents = Content::count();
        $actors = Actor::count();
        $genres = Genre::count();
        $languages = Language::count();
        $liveTvCategories = TVCategory::count();
        $liveTvChannels = TVChannel::count();
        $notifications = Notification::count();
        $admob = Admob::count();
        $customAds = CustomAd::count();

        return view('index', [
            'totalUser' => $totalUser,
            'contents' => $contents,
            'actors' => $actors,
            'genres' => $genres,
            'languages' => $languages,
            'liveTvCategories' => $liveTvCategories,
            'liveTvChannels' => $liveTvChannels,
            'notifications' => $notifications,
            'admob' => $admob,
            'customAds' => $customAds,
        ]);
    }

    function setting()
    {
        $setting = GlobalSettings::first();
        $awsConfig = [
            'AWS_ACCESS_KEY_ID' => env('AWS_ACCESS_KEY_ID'),
            'AWS_SECRET_ACCESS_KEY' => env('AWS_SECRET_ACCESS_KEY'),
            'AWS_DEFAULT_REGION' => env('AWS_DEFAULT_REGION'),
            'AWS_BUCKET' => env('AWS_BUCKET')
        ];
        $doConfig = [
            'DO_SPACE_ACCESS_KEY_ID' => env('DO_SPACE_ACCESS_KEY_ID'),
            'DO_SPACE_SECRET_ACCESS_KEY' => env('DO_SPACE_SECRET_ACCESS_KEY'),
            'DO_SPACE_REGION' => env('DO_SPACE_REGION'),
            'DO_SPACE_BUCKET' => env('DO_SPACE_BUCKET')
        ];

        return view('setting', compact('setting', 'awsConfig', 'doConfig'));
    }
    
    function fetchSettings()
    {
        $setting = GlobalSettings::first();
        $genres = Genre::get();
        $languages = Language::get();
        $admob = Admob::get();

        return response()->json([
            'status' => true,
            'message' => 'Fetch Setting Successfully',
            'setting' => $setting,
            'genres' => $genres,
            'languages' => $languages,
            'admob' => $admob
        ]);
    }

    public function saveSettings(Request $request)
    {
        $setting = GlobalSettings::first();

        if($setting == null) {
            return response()->json([
                'status' => false,
                'message' => 'setting Not Found',
            ]);
        }
        if($request->has('app_name')) {
            $setting->app_name = $request->app_name;
            $request->session()->put('app_name', $setting['app_name']);
        }
        if($request->has('videoad_skip_time')) {
            $setting->videoad_skip_time = $request->videoad_skip_time;
        }
        if($request->has('is_live_tv_enable')) {
            $setting->is_live_tv_enable = $request->is_live_tv_enable;
        }
        if($request->has('is_admob_android')) {
            $setting->is_admob_android = $request->is_admob_android;
        }
        if($request->has('is_admob_ios')) {
            $setting->is_admob_ios = $request->is_admob_ios;
        }
        if($request->has('is_custom_android')) {
            $setting->is_custom_android = $request->is_custom_android;
        }
        if($request->has('is_custom_ios')) {
            $setting->is_custom_ios = $request->is_custom_ios;
        }
        if($request->has('storage_type')) {
            $setting->storage_type = $request->storage_type;
        }
        $setting->save();

        return response()->json([
            'status' => true,
            'message' => 'Setting Updated Successfully',
        ]);

        
    }

    public function changePassword(Request $request)
    {
        $admin = Admin::where('user_type', 1)->first();
        if ($admin) {
            if ($request->has('user_password')) {
                if ($admin->user_password == $request->user_password) {
                    $admin->user_password = $request->new_password;
                    $admin->save();
                    return response()->json([
                        'status' => true,
                        'message' => 'Change Password',
                    ]);
                } else {
                    return response()->json([
                        'status' => false,
                        'message' => 'Old Password does not match',
                    ]);
                }
            }
        } else {
            return response()->json([
                'status' => false,
                'message' => 'Admin not found',
            ]);
        }
    }

    public function privacyPolicy()
    {
        $data = Pages::first();
        return view('pages.privacyPolicy', [
            'data' => $data->privacy
        ]);
    }

    public function termsOfUse()
    {
        $data = Pages::first();
        return view('pages.termsOfUse', [
            'data' => $data->termsofuse
        ]);
    }

    public function viewTerms()
    {
        $data = Pages::first();
        return view('pages.viewTerms', ['data' => $data->termsofuse]);
    }

    public function updatePrivacy(Request $request)
    {
        $data = Pages::first();
        $data->privacy = $request->content;
        $data->save();

        return response()->json([
            'status' => true,
            'message' => 'Updated Successfully',
        ]);
    }

    public function updateTerms(Request $request)
    {
        $data = Pages::first();
        $data->termsofuse = $request->content;
        $data->save();

        return response()->json([
            'status' => true,
            'message' => 'Updated Successfully',
        ]);
    }

    public function viewPrivacy()
    {
        $data = Pages::first();
        return view('pages.viewPrivacy', ['data' => $data->privacy]);
    }

    public function addContentForm(Request $request)
    {
        $privacy = Pages::first();
        if ($privacy) {
            if ($request->has('privacy')) {
                $privacy->privacy = $request->privacy;
            }
        }
        $privacy->save();

        return response()->json([
            'status' => true,
            'message' => 'Updated Successfully',
        ]);
    }

    public function addTermsForm(Request $request)
    {
        $terms = Pages::first();
        if ($terms) {
            if ($request->has('termsofuse')) {
                $terms->termsofuse = $request->termsofuse;
            }
        }
        $terms->save();
        
        return response()->json([
            'status' => true,
            'message' => 'Updated Successfully',
        ]);
    }

    public function fetchContentFromTMDB()
    {
        $genres = Genre::orderBy('created_at', 'DESC')->get();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        return view('fetchContentFromTMDB', [
                'genres' => $genres,
                'languages' => $languages,
            ]
        );
    }
    
}
