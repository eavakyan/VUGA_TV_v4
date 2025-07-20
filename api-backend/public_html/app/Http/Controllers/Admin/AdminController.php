<?php

namespace App\Http\Controllers\Admin;

use App\Admin;
use App\GlobalFunction;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\Facades\Validator;

class AdminController extends Controller
{
    /**
     * Show login page
     */
    public function showLogin()
    {
        if (Session::has('admin')) {
            return redirect()->route('dashboard');
        }
        return view('admin.login');
    }

    /**
     * Handle admin login
     */
    public function doLogin(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'email' => 'required|email',
            'password' => 'required',
        ]);

        if ($validator->fails()) {
            return back()->withErrors($validator)->withInput();
        }

        $admin = Admin::where('email', $request->email)->first();

        if ($admin && Hash::check($request->password, $admin->password)) {
            Session::put('admin', $admin);
            return redirect()->route('dashboard');
        }

        return back()->withErrors(['email' => 'Invalid credentials'])->withInput();
    }

    /**
     * Show dashboard
     */
    public function showDashboard()
    {
        return view('admin.dashboard');
    }

    /**
     * Show admin profile
     */
    public function MyProfile()
    {
        $admin = Session::get('admin');
        return view('admin.profile', compact('admin'));
    }

    /**
     * Update admin profile
     */
    public function updateAdminProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'email' => 'required|email',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $admin = Admin::find(Session::get('admin')->id);

        if (!$admin) {
            return response()->json([
                'status' => false,
                'message' => 'Admin not found'
            ]);
        }

        $admin->name = $request->name;
        $admin->email = $request->email;

        if ($request->has('password') && !empty($request->password)) {
            $admin->password = Hash::make($request->password);
        }

        if ($request->hasFile('profile_image')) {
            // Delete old image
            if ($admin->profile_image) {
                GlobalFunction::deleteFile($admin->profile_image);
            }
            
            // Upload new image
            $file = $request->file('profile_image');
            $path = GlobalFunction::saveFileAndGivePath($file);
            $admin->profile_image = $path;
        }

        $admin->save();

        // Update session
        Session::put('admin', $admin);

        return response()->json([
            'status' => true,
            'message' => 'Profile updated successfully'
        ]);
    }

    /**
     * Admin logout
     */
    public function logout($flag = null)
    {
        Session::forget('admin');
        return redirect()->route('login');
    }
} 