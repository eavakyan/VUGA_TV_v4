<?php

namespace App\Http\Controllers;

use App\GlobalFunction;
use App\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class UserController extends Controller
{
    public function userRegistration(Request $request)
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

    public function fetchProfile(Request $request)
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
            'message' => 'User Updated Successfully',
            'data' => $user,
        ]);
    }

    public function logOut(Request $request)
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

    public function viewUserList()
    {
        $totalUser = User::count();
        return view('users', [
            'totalUser' => $totalUser,
        ]);
    }

    public function usersList(Request $request)
    {
        $columns = [
            0 => 'id',
            1 => 'image',
        ];

        $query = User::query();

        $totalData = $query->count();
        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where('identity', 'LIKE', "%{$searchValue}%")
                ->orWhere('fullname', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            $imageUrl = $item->profile_image ?  $item->profile_image : './assets/img/profile.svg';

            $imageHtml = "
            <div class='d-flex align-items-center'>
                <img src='{$imageUrl}' class='tbl_img'>
                <span class='ms-3'>{$item->fullname}</span>
            </div>";

            $deviceType = $item->device_type == 1 ? 'Android' : 'iOS';

            $loginType = match ($item->login_type) {
                1 => 'Google',
                2 => 'Facebook ',
                3 => 'Apple',
                4 => 'Email',
            };

            return [
                $imageHtml,
                $item->identity,
                $deviceType,
                $loginType,
            ];
        });

        $json_data = [
            'draw' => intval($request->input('draw')),
            'recordsTotal' => intval($totalData),
            'recordsFiltered' => intval($totalFiltered),
            'data' => $data,
        ];

        return response()->json($json_data);
    }

 
}
