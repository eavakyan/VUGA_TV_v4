<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Notification;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class NotificationController extends Controller
{
    /**
     * Get all notifications
     */
    public function getAllNotifications(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $perPage = $request->per_page ?? 20;
        $notifications = Notification::orderBy('created_at', 'desc')
                                    ->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Notifications fetched successfully',
            'data' => $notifications->items(),
            'pagination' => [
                'current_page' => $notifications->currentPage(),
                'last_page' => $notifications->lastPage(),
                'per_page' => $notifications->perPage(),
                'total' => $notifications->total()
            ]
        ]);
    }
    
    /**
     * Send notification (admin function - requires authentication in production)
     */
    public function sendNotification(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255',
            'description' => 'required|string|max:900',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $notification = new Notification;
        $notification->title = $request->title;
        $notification->description = $request->description;
        $notification->save();

        // TODO: Implement push notification logic here
        // This would typically integrate with Firebase Cloud Messaging or similar

        return response()->json([
            'status' => true,
            'message' => 'Notification sent successfully',
            'data' => $notification
        ]);
    }
}