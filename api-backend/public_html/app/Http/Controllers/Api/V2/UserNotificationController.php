<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\UserNotification;
use App\Models\V2\ProfileNotificationStatus;
use App\Models\V2\NotificationAnalytics;
use App\Models\V2\AppUserProfile;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Log;

class UserNotificationController extends Controller
{
    /**
     * Get pending notifications for a profile
     */
    public function getPendingNotifications(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'platform' => 'required|in:ios,android,android_tv'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            $profileId = $request->profile_id;
            $platform = $request->platform;
            
            // Cache key for this profile's notifications
            $cacheKey = "pending_notifications_{$profileId}_{$platform}";
            
            $notifications = Cache::remember($cacheKey, 300, function () use ($profileId, $platform) {
                return UserNotification::active()
                    ->scheduled()
                    ->notExpired()
                    ->forPlatform($platform)
                    ->whereNotExists(function ($query) use ($profileId) {
                        $query->select(DB::raw(1))
                              ->from('profile_notification_status')
                              ->whereColumn('profile_notification_status.notification_id', 'user_notification.notification_id')
                              ->where('profile_notification_status.profile_id', $profileId);
                    })
                    ->orderBy('priority', 'desc')
                    ->orderBy('created_at', 'desc')
                    ->limit(5) // Limit to prevent overwhelming users
                    ->select(['notification_id', 'title', 'message', 'notification_type', 'priority'])
                    ->get();
            });

            return response()->json([
                'status' => true,
                'data' => $notifications
            ]);
            
        } catch (\Exception $e) {
            Log::error('Error fetching pending notifications: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to fetch notifications'
            ], 500);
        }
    }

    /**
     * Mark notification as shown to a profile
     */
    public function markNotificationShown(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'notification_id' => 'required|integer|exists:user_notification,notification_id',
            'platform' => 'required|in:ios,android,android_tv'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            DB::beginTransaction();

            // Check if already marked
            $exists = ProfileNotificationStatus::where('profile_id', $request->profile_id)
                ->where('notification_id', $request->notification_id)
                ->exists();

            if (!$exists) {
                // Create the status record
                ProfileNotificationStatus::create([
                    'profile_id' => $request->profile_id,
                    'notification_id' => $request->notification_id,
                    'platform' => $request->platform,
                    'shown_at' => now()
                ]);

                // Update analytics
                $this->updateNotificationAnalytics($request->notification_id, 'shown', $request->platform);
            }

            // Clear cache for this profile
            $cacheKey = "pending_notifications_{$request->profile_id}_{$request->platform}";
            Cache::forget($cacheKey);

            DB::commit();

            return response()->json([
                'status' => true,
                'message' => 'Notification marked as shown'
            ]);

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Error marking notification as shown: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to mark notification as shown'
            ], 500);
        }
    }

    /**
     * Dismiss a notification
     */
    public function dismissNotification(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'notification_id' => 'required|integer|exists:user_notification,notification_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            $status = ProfileNotificationStatus::where('profile_id', $request->profile_id)
                ->where('notification_id', $request->notification_id)
                ->first();

            if ($status) {
                $status->dismissed_at = now();
                $status->save();

                // Update analytics
                $this->updateNotificationAnalytics($request->notification_id, 'dismissed', null);
            }

            return response()->json([
                'status' => true,
                'message' => 'Notification dismissed'
            ]);

        } catch (\Exception $e) {
            Log::error('Error dismissing notification: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to dismiss notification'
            ], 500);
        }
    }

    /**
     * Update notification analytics
     */
    private function updateNotificationAnalytics($notificationId, $action, $platform = null)
    {
        try {
            $analytics = NotificationAnalytics::firstOrCreate(
                ['notification_id' => $notificationId],
                ['total_eligible_profiles' => AppUserProfile::where('status', 1)->count()]
            );

            if ($action === 'shown') {
                $analytics->increment('total_shown');
                if ($platform) {
                    $analytics->increment("{$platform}_shown");
                }
            } elseif ($action === 'dismissed') {
                $analytics->increment('total_dismissed');
            }

            $analytics->updated_at = now();
            $analytics->save();

        } catch (\Exception $e) {
            Log::error('Error updating notification analytics: ' . $e->getMessage());
        }
    }

    /**
     * Create a new notification (Admin endpoint)
     */
    public function createNotification(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255',
            'message' => 'required|string',
            'notification_type' => 'in:system,promotional,update,maintenance',
            'target_platforms' => 'array',
            'target_platforms.*' => 'in:all,ios,android,android_tv',
            'priority' => 'in:low,medium,high,urgent',
            'scheduled_at' => 'nullable|date',
            'expires_at' => 'nullable|date'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            Log::info('Creating notification with data:', $request->all());
            
            $notification = UserNotification::create([
                'title' => $request->title,
                'message' => $request->message,
                'notification_type' => $request->notification_type ?? 'system',
                'target_platforms' => $request->target_platforms ?? ['all'],
                'priority' => $request->priority ?? 'medium',
                'scheduled_at' => $request->scheduled_at,
                'expires_at' => $request->expires_at,
                'is_active' => true,
                'created_by' => 1  // TODO: Get from authenticated admin user
            ]);

            // Clear cached notifications
            // Cache::forget('pending_notifications_*');

            return response()->json([
                'status' => true,
                'message' => 'Notification created successfully',
                'data' => $notification
            ]);

        } catch (\Exception $e) {
            Log::error('Error creating notification: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to create notification'
            ], 500);
        }
    }

    /**
     * Get all notifications with pagination (Admin endpoint)
     */
    public function getNotificationsList(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
            'is_active' => 'nullable|boolean',
            'notification_type' => 'nullable|in:system,promotional,update,maintenance'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            $query = UserNotification::with(['analytics']);

            if ($request->has('is_active')) {
                $query->where('is_active', $request->is_active);
            }

            if ($request->notification_type) {
                $query->where('notification_type', $request->notification_type);
            }

            $perPage = $request->per_page ?? 20;
            $notifications = $query->orderBy('created_at', 'desc')->paginate($perPage);

            return response()->json([
                'status' => true,
                'data' => $notifications->items(),
                'pagination' => [
                    'current_page' => $notifications->currentPage(),
                    'last_page' => $notifications->lastPage(),
                    'per_page' => $notifications->perPage(),
                    'total' => $notifications->total()
                ]
            ]);

        } catch (\Exception $e) {
            Log::error('Error fetching notifications list: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to fetch notifications'
            ], 500);
        }
    }

    /**
     * Update notification (Admin endpoint)
     */
    public function updateNotification(Request $request, $notificationId)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'string|max:255',
            'message' => 'string',
            'notification_type' => 'in:system,promotional,update,maintenance',
            'target_platforms' => 'array',
            'target_platforms.*' => 'in:all,ios,android,android_tv',
            'priority' => 'in:low,medium,high,urgent',
            'scheduled_at' => 'nullable|date',
            'expires_at' => 'nullable|date',
            'is_active' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            $notification = UserNotification::find($notificationId);
            
            if (!$notification) {
                return response()->json([
                    'status' => false,
                    'message' => 'Notification not found'
                ], 404);
            }

            $notification->update($request->only([
                'title', 'message', 'notification_type', 'target_platforms',
                'priority', 'scheduled_at', 'expires_at', 'is_active'
            ]));

            // Clear cached notifications
            // Cache::tags(['notifications'])->flush();

            return response()->json([
                'status' => true,
                'message' => 'Notification updated successfully',
                'data' => $notification
            ]);

        } catch (\Exception $e) {
            Log::error('Error updating notification: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to update notification'
            ], 500);
        }
    }

    /**
     * Delete notification (Admin endpoint)
     */
    public function deleteNotification($notificationId)
    {
        try {
            $notification = UserNotification::find($notificationId);
            
            if (!$notification) {
                return response()->json([
                    'status' => false,
                    'message' => 'Notification not found'
                ], 404);
            }

            // Soft delete by deactivating
            $notification->is_active = false;
            $notification->save();

            // Clear cached notifications
            // Cache::tags(['notifications'])->flush();

            return response()->json([
                'status' => true,
                'message' => 'Notification deleted successfully'
            ]);

        } catch (\Exception $e) {
            Log::error('Error deleting notification: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to delete notification'
            ], 500);
        }
    }

    /**
     * Get notification analytics (Admin endpoint)
     */
    public function getNotificationAnalytics($notificationId)
    {
        try {
            $notification = UserNotification::with(['analytics', 'statuses'])->find($notificationId);
            
            if (!$notification) {
                return response()->json([
                    'status' => false,
                    'message' => 'Notification not found'
                ], 404);
            }

            $analytics = $notification->analytics;
            
            if (!$analytics) {
                return response()->json([
                    'status' => true,
                    'data' => [
                        'notification' => $notification,
                        'analytics' => [
                            'total_eligible_profiles' => 0,
                            'total_shown' => 0,
                            'total_dismissed' => 0,
                            'ios_shown' => 0,
                            'android_shown' => 0,
                            'android_tv_shown' => 0,
                            'show_rate' => 0,
                            'dismiss_rate' => 0
                        ]
                    ]
                ]);
            }

            $showRate = $analytics->total_eligible_profiles > 0 
                ? round(($analytics->total_shown / $analytics->total_eligible_profiles) * 100, 2) 
                : 0;
                
            $dismissRate = $analytics->total_shown > 0 
                ? round(($analytics->total_dismissed / $analytics->total_shown) * 100, 2) 
                : 0;

            return response()->json([
                'status' => true,
                'data' => [
                    'notification' => $notification,
                    'analytics' => array_merge($analytics->toArray(), [
                        'show_rate' => $showRate,
                        'dismiss_rate' => $dismissRate
                    ])
                ]
            ]);

        } catch (\Exception $e) {
            Log::error('Error fetching notification analytics: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to fetch analytics'
            ], 500);
        }
    }
}