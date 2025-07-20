<?php

namespace App\Http\Controllers\API;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rule;
use Session;
use DB;
use Log;
use App\Admin;
use App\User;
use App\Settings;
use App\Ads;
use App\Notification;
use App\SubscriptionPackage;
use Validator;

class SettingsController extends Controller
{

    public function getSettings(Request $request)
    {

        $headers = $request->headers->all();

        $verify_request_base = Admin::verify_request_base($headers);

        if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
            return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
            exit();
        }

        $Data =  Settings::first();
        $AdsData =  Ads::first();
        return response()->json(['status' => 200, 'message' => "Settings Data Get Successfully.", 'data' => $Data, 'ads' => $AdsData]);
    }

    public function getAllNotification(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }

            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'user_id' => 'required',
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $user_id = $request->get('user_id');

            $User =  User::where('user_id', $user_id)->first();
            if (empty($User)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }

            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;

            $CheckUSer =  User::where('user_id', $user_id)->first();
            if (empty($CheckUSer)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }

            $notificationData = Notification::orderBy('notification_id', 'DESC')
                ->offset($start)
                ->limit($limit)
                ->get();

            if (count($notificationData) > 0) {
                $Data = [];
                foreach ($notificationData as $k => $value) {
                    // $list['user_id'] = $value['user_id'] ? $value['user_id'] : $user_id;
                    // $list['item_id'] = $value['item_id'] ? $value['item_id'] : 0;
                    $list['notification_type'] = $value['notification_type'] ? $value['notification_type'] : 0;
                    $list['title'] = $value['title'];
                    $list['message'] = $value['message'];
                    $list['image'] = $value['image'];
                    // $list['created_at'] = $value['created_at'] ? \Carbon\Carbon::parse($value['created_at'])->diffForHumans() : "";
                    $list['created_at'] = $value['created_at'] ? \Carbon\Carbon::parse($value['created_at'])->format('Y-m-d h:i:s') : "";
                    $Data[] = $list;
                }
            } else {
                $Data = [];
            }

            if (!empty($Data)) {
                return response()->json(['status' => 200, 'message' => "Notification Data Found.", 'data' => $Data]);
            } else {
                return response()->json(['status' => 401, 'message' => "Notification Data Not Found."]);
            }
       
    }

    public function getSubscriptionPackage(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }

            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $monthlyData = SubscriptionPackage::where('duration', 1)->first();
            $yearlyData = SubscriptionPackage::where('duration', 2)->first();

            return response()->json(['status' => 200, 'message' => "Subscription Data Found.", 'monthly_data' => $monthlyData,  'yearly_data' => $yearlyData]);
       
    }
}
