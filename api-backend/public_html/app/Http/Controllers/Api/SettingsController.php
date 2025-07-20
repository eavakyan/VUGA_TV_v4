<?php

namespace App\Http\Controllers\Api;

use App\Setting;
use App\Notification;
use App\SubscriptionPackage;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class SettingsController extends Controller
{
    /**
     * Get app settings
     */
    public function getSettings()
    {
        $settings = Setting::first();
        
        if (!$settings) {
            return response()->json([
                'status' => false,
                'message' => 'Settings Not Found'
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => 'Settings Retrieved Successfully',
            'data' => $settings
        ]);
    }

    /**
     * Get all notifications for user
     */
    public function getAllNotification(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'start' => 'required|integer',
            'limit' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $notifications = Notification::orderBy('created_at', 'DESC')
                                   ->offset($request->start)
                                   ->limit($request->limit)
                                   ->get();

        return response()->json([
            'status' => true,
            'message' => 'Notifications Retrieved Successfully',
            'data' => $notifications
        ]);
    }

    /**
     * Get subscription packages
     */
    public function getSubscriptionPackage()
    {
        $packages = SubscriptionPackage::where('is_active', 1)
                                     ->orderBy('sort_order', 'ASC')
                                     ->get();

        return response()->json([
            'status' => true,
            'message' => 'Subscription Packages Retrieved Successfully',
            'data' => $packages
        ]);
    }
} 