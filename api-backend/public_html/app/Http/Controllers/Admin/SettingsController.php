<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Setting;
use App\Notification;
use App\SubscriptionPackage;
use Illuminate\Http\Request;

class SettingsController extends Controller
{
    public function viewSettings()
    {
        $settings = Setting::first();
        return view('admin.settings.index', compact('settings'));
    }

    public function addUpdateSetting(Request $request)
    {
        $settings = Setting::first();
        if (!$settings) {
            $settings = new Setting();
        }
        
        $settings->fill($request->all());
        $settings->save();

        return response()->json([
            'status' => true,
            'message' => 'Settings updated successfully'
        ]);
    }

    public function viewAds()
    {
        return view('admin.ads.index');
    }

    public function addUpdateAndriodAds(Request $request)
    {
        // Handle Android ads update
        return response()->json([
            'status' => true,
            'message' => 'Android ads updated successfully'
        ]);
    }

    public function addUpdateIosAds(Request $request)
    {
        // Handle iOS ads update
        return response()->json([
            'status' => true,
            'message' => 'iOS ads updated successfully'
        ]);
    }

    public function viewCustomAds()
    {
        return view('admin.ads.custom.list');
    }

    // Notification Methods
    public function sendNotification(Request $request)
    {
        $notification = new Notification();
        $notification->title = $request->title;
        $notification->message = $request->message;
        $notification->save();

        return response()->json([
            'status' => true,
            'message' => 'Notification sent successfully'
        ]);
    }

    public function viewListNotification()
    {
        return view('admin.notification.list');
    }

    public function showNotificationList(Request $request)
    {
        $query = Notification::query();
        
        if ($request->has('search') && !empty($request->search)) {
            $query->where('title', 'LIKE', "%{$request->search}%");
        }

        $notifications = $query->orderBy('created_at', 'DESC')
                              ->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $notifications->items(),
            'recordsTotal' => $notifications->total(),
            'recordsFiltered' => $notifications->total()
        ]);
    }

    public function UpdateNotification(Request $request)
    {
        $notification = Notification::find($request->id);
        if ($notification) {
            $notification->update($request->only(['title', 'message']));
            return response()->json(['status' => true, 'message' => 'Notification updated successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Notification not found']);
    }

    public function deleteNotification(Request $request)
    {
        $notification = Notification::find($request->id);
        if ($notification) {
            $notification->delete();
            return response()->json(['status' => true, 'message' => 'Notification deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Notification not found']);
    }

    // Subscription Package Methods
    public function viewSubscriptionPackage()
    {
        $packages = SubscriptionPackage::all();
        return view('admin.subscription.package.index', compact('packages'));
    }

    public function addUpdateSubscriptionPackage(Request $request)
    {
        if ($request->id) {
            $package = SubscriptionPackage::find($request->id);
        } else {
            $package = new SubscriptionPackage();
        }
        
        $package->fill($request->all());
        $package->save();

        return response()->json([
            'status' => true,
            'message' => 'Subscription Package saved successfully'
        ]);
    }
} 