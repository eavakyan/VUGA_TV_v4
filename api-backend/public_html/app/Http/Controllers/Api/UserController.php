<?php

namespace App\Http\Controllers\Api;

use App\User;
use App\GlobalFunction;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class UserController extends Controller
{
    /**
     * User registration
     */
    public function registration(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'identity' => 'required',
            'email' => 'required',
            'login_type' => 'required',
            'device_type' => 'required',
            'device_token' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('identity', $request->identity)->first();
        $emailUser = User::where('email', $request->email)->first();

        if ($user != null || $emailUser != null) {
            $user->login_type = (int) $request->login_type;
            $user->device_type = (int) $request->device_type;
            $user->device_token = $request->device_token;
            $user->save();
            return response()->json([
                'status' => false,
                'message' => 'User is Already Exist',
                'data' => $user,
            ]);
        }

        $user = new User;
        $user->fullname = $request->fullname;
        $user->email = $request->email;
        $user->login_type = (int) $request->login_type;
        $user->identity = $request->identity;
        $user->device_token = $request->device_token;
        $user->device_type = (int) $request->device_type;
        $user->save();

        return response()->json([
            'status' => true,
            'message' => 'User Added Successfully',
            'data' => $user,
        ]);
    }

    /**
     * Get user profile
     */
    public function getProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('id', $request->user_id)->first();
        if ($user == null) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found',
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => 'User Profile Retrieved Successfully',
            'data' => $user,
        ]);
    }

    /**
     * Update user profile
     */
    public function updateProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('id', $request->user_id)->first();
        if ($user == null) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found',
            ]);
        }

        if ($request->has('fullname')) {
            $user->fullname = $request->fullname;
        }
        if ($request->has('email')) {
            $user->email = $request->email;
        }
        if ($request->has('watchlist_content_ids')) {
            $user->watchlist_content_ids = $request->watchlist_content_ids;
        }
        if ($request->has('login_type')) {
            $user->login_type = $request->login_type;
        }
        if ($request->hasFile('profile_image')) {
            GlobalFunction::deleteFile($user->profile_image);
            $file = $request->file('profile_image');
            $path = GlobalFunction::saveFileAndGivePath($file);
            $user->profile_image = $path;
        }

        if ($request->has('device_type')) {
            $user->device_type = $request->device_type;
        }
        if ($request->has('device_token')) {
            $user->device_token = $request->device_token;
        }
        $user->save();

        return response()->json([
            'status' => true,
            'message' => 'User Updated Successfully',
            'data' => $user,
        ]);
    }

    /**
     * User logout
     */
    public function logout(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('id', $request->user_id)->first();
        if ($user == null) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found',
            ]);
        }

        $user->device_token = null;
        $user->save();
        
        return response()->json([
            'status' => true,
            'message' => 'User logout successfully',
            'data' => $user
        ]);
    }
    
    /**
     * Delete user account
     */
    public function deleteMyAccount(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('id', $request->user_id)->first();
        if ($user == null) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found',
            ]);
        }

        $user->delete();
        
        return response()->json([
            'status' => true,
            'message' => 'User Delete successfully',
        ]);
    }

    /**
     * Make user subscribe (placeholder for subscription logic)
     */
    public function makeUserSubscribe(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('id', $request->user_id)->first();
        if ($user == null) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found',
            ]);
        }

        // TODO: Implement subscription logic
        
        return response()->json([
            'status' => true,
            'message' => 'User subscribed successfully',
            'data' => $user
        ]);
    }

    /**
     * Get subscription list (placeholder)
     */
    public function getSubscriptionList(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        // TODO: Implement subscription list logic
        
        return response()->json([
            'status' => true,
            'message' => 'Subscription list retrieved successfully',
            'data' => []
        ]);
    }
} 