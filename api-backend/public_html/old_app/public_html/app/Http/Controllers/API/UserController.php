<?php

namespace App\Http\Controllers\API;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rule;
use Validator;
use Hash;
use DB;
use File;
use Log;
use Image;
use App\User;
use App\Admin;
use App\Content;
use App\Subscription;
use App\SubscriptionPackage;
use App\Common;
use App\GlobalFunction;
use Illuminate\Support\Facades\Validator as FacadesValidator;
use Laravel\Passport\Token;
use Storage;

class UserController extends Controller
{
    public function deleteMyAccount(Request $request)
    {
        $headers = $request->headers->all();

        $verify_request_base = Admin::verify_request_base($headers);

        if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
            return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
            exit();
        }

        $rules = [
            'id' => 'required'
        ];
        $validator = FacadesValidator::make($request->all(), $rules);
        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => 401, 'message' => $msg]);
        }

        $data = User::where('id', $request->id)->first();

        if ($data == null) {
            return json_encode(['status' => false, 'message' => 'id Not valid']);
        }
        $data->delete();
        return json_encode(['status' => true, 'message' => 'Accont Deleted successfully !']);
    }

    public function registration(Request $request)
    {

        $headers = $request->headers->all();

        $verify_request_base = Admin::verify_request_base($headers);

        if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
            return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
            exit();
        }

        $messages = [
            // 'fullname.regex' => 'Firstname contains only alphabetic and space.',
        ];

        $rules = [
            'fullname' => 'required',
            'email' => 'required',
            'login_type' => 'required',
            'identity' => 'required',
            'device_token' => 'required',
            'device_type' => 'required',
        ];

        $validator = Validator::make($request->all(), $rules, $messages);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => 401, 'message' => $msg]);
        }

        $CheckUSer =  User::where('identity', $request->get('identity'))->first();

        if (empty($CheckUSer)) {

            $UserTypeModel = new User;
            $user_id = $UserTypeModel->get_random_string();
            $data['user_id'] = $user_id;
            $data['fullname'] = $request->get('fullname');
            $data['email'] = $request->get('email');
            $data['login_type'] = $request->get('login_type');
            $data['identity'] = $request->get('identity');
            $data['device_token'] = $request->get('device_token');
            $data['device_type'] = $request->get('device_type');

            $result =  User::insert($data);
        } else {
            $data['login_type'] = $request->get('login_type');
            $data['identity'] = $request->get('identity');
            $data['device_token'] = $request->get('device_token');
            $data['device_type'] = $request->get('device_type');
            $user_id = $CheckUSer->user_id;
            $result =  User::where('user_id', $user_id)->update($data);
        }
        if (!empty($result)) {

            $User =  User::where('user_id', $user_id)->first();

            $User['token'] = 'Bearer ' . $User->createToken(env('APP_NAME'))->accessToken;
            $User['is_subscribed'] = 0;
            $SubscriptionData  = Subscription::where('user_id', $user_id)->first();
            if (!empty($SubscriptionData)) {
                $User['is_subscribed'] = 1;
            }
            $User['SubscriptionData'] = $SubscriptionData;
            unset($User->created_at);
            unset($User->updated_at);
            unset($User->timezone);

            return response()->json(['status' => 200, 'message' => "User registered successfully.", 'data' => $User]);
        } else {
            return response()->json(['status' => 401, 'message' => "Error While User Registeration"]);
        }
    }

    public function Logout(Request $request)
    {


        $rules = [
            'user_id' => 'required',
        ];

        $validator = Validator::make($request->all(), $rules);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => 401, 'message' => $msg]);
        }
        $user_id = $request->get('user_id');
        $data['device_token'] = "";
        $data['device_type'] = 0;
        $result =  User::where('user_id', $user_id)->update($data);
        return response()->json(['success_code' => 200, 'response_code' => 1, 'response_message' => "User logout successfully."]);
    }

    public function getProfile(Request $request)
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
        $User->profile_image = $User->profile_image ? $User->profile_image : "";
        $User['is_subscribed'] = 0;
        $SubscriptionData  = Subscription::where('user_id', $user_id)->get();
        if (count($SubscriptionData) > 0) {
            $User['is_subscribed'] = 1;
        }
        $User['SubscriptionData'] = $SubscriptionData;
        unset($User->created_at);
        unset($User->updated_at);
        unset($User->timezone);

        return response()->json(['status' => 200, 'message' => "User Profile Get successfully.", 'data' => $User]);
    }

    public function updateProfile(Request $request)
    {


        $headers = $request->headers->all();

        $verify_request_base = Admin::verify_request_base($headers);

        if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
            return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
            exit();
        }

        $messages = [
            // 'fullname.regex' => 'Firstname contains only alphabetic and space.',
        ];

        $rules = [
            'fullname' => 'required',
            'email' => 'required',
            'user_id' => 'required',
        ];

        $validator = Validator::make($request->all(), $rules, $messages);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => 401, 'message' => $msg]);
        }


        $user_id = $request->get('user_id');

        $CheckUSer =  User::where('user_id', $user_id)->first();
        if (empty($CheckUSer)) {
            return response()->json(['status' => 401, 'message' => "User Not Found"]);
        }

        $imageFileName = "";

        if ($request->hasfile('profile_image')) {
            $data['profile_image'] = GlobalFunction::saveFileAndGivePath($request->file('profile_image'));
        }

        $data['user_id'] = $user_id;
        $data['fullname'] = $request->get('fullname');
        $data['email'] = $request->get('email');

        $result =  User::where('user_id', $user_id)->update($data);

        if (!empty($result)) {

            $User =  User::where('user_id', $user_id)->first();
            $User['is_subscribed'] = 0;

            $SubscriptionData  = Subscription::where('user_id', $user_id)->first();

            if (!empty($SubscriptionData)) {
                $User['is_subscribed'] = 1;
            }
            $User['SubscriptionData'] = $SubscriptionData;
            unset($User->created_at);
            unset($User->updated_at);
            unset($User->timezone);


            return response()->json(['status' => 200, 'message' => "User Profile Update successfully.", 'data' => $User]);
        } else {
            return response()->json(['status' => 401, 'message' => "Error While User Profile Update"]);
        }
    }


    public function makeUserSubscribe(Request $request)
    {

        // $user_id = $request->user()->user_id;

        // if (empty($user_id)) {
        //     $msg = "user id is required";
        //     return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => $msg]);
        // }


        $headers = $request->headers->all();

        $verify_request_base = Admin::verify_request_base($headers);

        if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
            return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => "Unauthorized Access!"]);
            exit();
        }

        $rules = [
            'package_id' => 'required',
            'payment_type' => 'required',
            'user_id' => 'required',
        ];


        $validator = Validator::make($request->all(), $rules);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => $msg]);
        }
        $user_id = $request->get('user_id');
        $package_id = $request->get('package_id');
        $payment_type = $request->get('payment_type');

        if ($payment_type == 2) {
            $rules = [
                'transaction_id	' => 'required',
            ];


            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => $msg]);
            }
        }
        $transaction_id     = $request->get('transaction_id	');
        $User =  User::where('user_id', $user_id)->first();
        if (empty($User)) {
            return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => "User Not Found"]);
        }
        $subscriptionPackage = SubscriptionPackage::where('package_id', $package_id)->first();
        $SubscriptionData  = Subscription::where('user_id', $user_id)->first();

        if (!empty($SubscriptionData)) {

            $data['days'] = $subscriptionPackage->days;
            $data['package_id'] = $package_id;
            $data['amount'] = $subscriptionPackage->price;
            $data['currency'] = $subscriptionPackage->currency;
            $data['start_date'] = date("Y-m-d");
            $data['expired_date'] = date('Y-m-d', strtotime($data['start_date'] . ' + ' . $subscriptionPackage->days . ' days'));
            $data['payment_type'] = $payment_type;
            if ($payment_type == 2) {
                $data['transaction_id	'] = $transaction_id;
            }

            $result =  Subscription::where('subscription_id', $SubscriptionData->subscription_id)->where('user_id', $user_id)->update($data);
        } else {

            $SubscriptionModel = new Subscription;
            $subscription_id = $SubscriptionModel->get_random_string();
            $data['subscription_id'] = $subscription_id;
            $data['user_id'] = $user_id;
            $data['days'] = $subscriptionPackage->days;
            $data['package_id'] = $package_id;
            $data['amount'] = $subscriptionPackage->price;
            $data['currency'] = $subscriptionPackage->currency;
            $data['start_date'] = date("Y-m-d");
            $data['expired_date'] = date('Y-m-d', strtotime($data['start_date'] . ' + ' . $subscriptionPackage->days . ' days'));
            $data['payment_type'] = $payment_type;
            if ($payment_type == 2) {
                $data['transaction_id	'] = $transaction_id;
            }

            $result =  Subscription::insert($data);
        }
        if (!empty($result)) {

            // $data1['is_premium'] = 1;
            // User::where('user_id', $user_id)->update($data1);
            return response()->json(['status' => TRUE, 'response_code' => 200, 'message' => "User Subscribe Content successfully."]);
        } else {
            return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => "Error While User Subscribe Content"]);
        }
    }

    public function getSubscriptionList(Request $request)
    {


        $headers = $request->headers->all();
        $verify_request_base = Admin::verify_request_base($headers);
        if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
            return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => "Unauthorized Access!"]);
            exit();
        }

        $rules = [
            'user_id' => 'required',
        ];

        $validator = Validator::make($request->all(), $rules);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => $msg]);
        }

        $user_id = $request->get('user_id');
        // $user_id = $request->user()->user_id;
        $User =  User::where('user_id', $user_id)->first();
        if (empty($User)) {
            return response()->json(['status' => 401, 'message' => "User Not Found"]);
        }

        $SubscriptionData  = Subscription::where('user_id', $user_id)->first();

        if (!empty($SubscriptionData)) {

            $packageData = SubscriptionPackage::where('package_id', $SubscriptionData['package_id'])->first();

            $Subscriptionlist['subscription_id'] = $SubscriptionData['subscription_id'];
            $Subscriptionlist['package_id'] = $SubscriptionData['package_id'];
            $Subscriptionlist['duration'] = $packageData['duration'];
            $Subscriptionlist['start_date'] = $SubscriptionData['start_date'];
            $Subscriptionlist['expired_date'] = $SubscriptionData['expired_date'];
            $Subscriptionlist['amount'] = $SubscriptionData['amount'];
            $Subscriptionlist['currency'] = $SubscriptionData['currency'];
            $Subscriptionlist['payment_type'] = $SubscriptionData['payment_type'];
            $Subscriptionlist['transaction_id'] = $SubscriptionData['transaction_id'];
            return response()->json(['status' => TRUE, 'response_code' => 200, 'message' => "Subscription Data Get Successfully.", 'data' => $Subscriptionlist]);
        } else {
            return response()->json(['status' => FALSE, 'response_code' => 401, 'message' => "No Data Found."]);
        }
    }
}
