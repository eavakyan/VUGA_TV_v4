<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppUser;
use App\Models\V2\PopupDefinition;
use App\Models\V2\AppUserPopupStatus;
use App\Models\V2\PopupAnalytics;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Cache;

class PopupController extends Controller
{
    /**
     * Get pending popups for a user
     * These are popups that haven't been shown to the user yet
     */
    public function getPendingPopups(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'device_type' => 'nullable|string|in:iOS,Android,AndroidTV',
            'limit' => 'nullable|integer|min:1|max:10'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        $deviceType = $request->device_type ?? 'Unknown';
        $limit = $request->limit ?? 3; // Default to 3 popups max at once
        
        // Get user context for targeting
        $userContext = [
            'device_type' => $deviceType,
            'has_subscription' => $this->checkUserSubscription($user),
            'profile_count' => $user->profiles()->count()
        ];
        
        // Get popup keys that user has already seen
        $shownPopupIds = AppUserPopupStatus::where('app_user_id', $request->app_user_id)
            ->pluck('popup_definition_id')
            ->toArray();
        
        // Get active popups that user hasn't seen yet
        $candidatePopups = PopupDefinition::active()
            ->byPriority()
            ->whereNotIn('popup_definition_id', $shownPopupIds)
            ->get();
        
        // Filter by targeting rules
        $pendingPopups = collect();
        foreach ($candidatePopups as $popup) {
            if ($popup->isTargetedToUser($user, $userContext)) {
                $pendingPopups->push($popup);
                
                if ($pendingPopups->count() >= $limit) {
                    break;
                }
            }
        }
        
        // Format response
        $formattedPopups = $pendingPopups->map(function ($popup) use ($request, $deviceType) {
            // Immediately mark as shown when retrieved
            AppUserPopupStatus::create([
                'app_user_id' => $request->app_user_id,
                'popup_definition_id' => $popup->popup_definition_id,
                'popup_key' => $popup->popup_key,
                'status' => 'shown',
                'device_type' => $deviceType,
                'shown_at' => now()
            ]);
            
            return [
                'popup_id' => $popup->popup_definition_id,
                'popup_key' => $popup->popup_key,
                'title' => $popup->title,
                'content' => $popup->content,
                'popup_type' => $popup->popup_type,
                'priority' => $popup->priority
            ];
        });
        
        // Update analytics asynchronously
        $this->updateAnalyticsAsync($pendingPopups->pluck('popup_definition_id')->toArray());
        
        return response()->json([
            'status' => true,
            'message' => 'Pending popups retrieved successfully',
            'data' => [
                'popups' => $formattedPopups,
                'total_count' => $formattedPopups->count()
            ]
        ]);
    }
    
    /**
     * Mark a popup as dismissed by the user
     */
    public function dismissPopup(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'popup_key' => 'required|string|exists:popup_definition,popup_key'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        // Find the popup status record
        $popup = PopupDefinition::where('popup_key', $request->popup_key)->first();
        $popupStatus = AppUserPopupStatus::where('app_user_id', $request->app_user_id)
            ->where('popup_definition_id', $popup->popup_definition_id)
            ->first();
        
        if (!$popupStatus) {
            return response()->json([
                'status' => false,
                'message' => 'Popup status not found'
            ], 404);
        }
        
        // Mark as dismissed
        $popupStatus->markAsDismissed();
        
        // Update analytics
        $this->updateAnalyticsAsync([$popup->popup_definition_id]);
        
        return response()->json([
            'status' => true,
            'message' => 'Popup dismissed successfully'
        ]);
    }
    
    /**
     * Mark a popup as acknowledged (user took action)
     */
    public function acknowledgePopup(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'popup_key' => 'required|string|exists:popup_definition,popup_key'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        // Find the popup status record
        $popup = PopupDefinition::where('popup_key', $request->popup_key)->first();
        $popupStatus = AppUserPopupStatus::where('app_user_id', $request->app_user_id)
            ->where('popup_definition_id', $popup->popup_definition_id)
            ->first();
        
        if (!$popupStatus) {
            return response()->json([
                'status' => false,
                'message' => 'Popup status not found'
            ], 404);
        }
        
        // Mark as acknowledged
        $popupStatus->markAsAcknowledged();
        
        // Update analytics
        $this->updateAnalyticsAsync([$popup->popup_definition_id]);
        
        return response()->json([
            'status' => true,
            'message' => 'Popup acknowledged successfully'
        ]);
    }
    
    /**
     * Get user's popup history (for debugging/support)
     */
    public function getUserPopupHistory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'limit' => 'nullable|integer|min:1|max:100'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        $limit = $request->limit ?? 20;
        
        $history = AppUserPopupStatus::with('popupDefinition')
            ->where('app_user_id', $request->app_user_id)
            ->orderBy('shown_at', 'desc')
            ->limit($limit)
            ->get();
        
        $formattedHistory = $history->map(function ($status) {
            return [
                'popup_key' => $status->popup_key,
                'title' => $status->popupDefinition->title ?? 'Unknown',
                'status' => $status->status,
                'shown_at' => $status->shown_at,
                'dismissed_at' => $status->dismissed_at,
                'device_type' => $status->device_type
            ];
        });
        
        return response()->json([
            'status' => true,
            'message' => 'Popup history retrieved successfully',
            'data' => $formattedHistory
        ]);
    }
    
    // ADMIN ENDPOINTS
    
    /**
     * Create a new popup definition (Admin only)
     */
    public function createPopupDefinition(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'popup_key' => 'required|string|unique:popup_definition,popup_key|max:100',
            'title' => 'required|string|max:255',
            'content' => 'required|string',
            'popup_type' => 'required|in:info,feature,warning,promotion,onboarding',
            'target_audience' => 'nullable|array',
            'priority' => 'nullable|integer|min:0|max:999',
            'is_active' => 'nullable|boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        $popup = PopupDefinition::create([
            'popup_key' => $request->popup_key,
            'title' => $request->title,
            'content' => $request->content,
            'popup_type' => $request->popup_type,
            'target_audience' => $request->target_audience,
            'priority' => $request->priority ?? 0,
            'is_active' => $request->is_active ?? true
        ]);
        
        // Initialize analytics
        PopupAnalytics::create([
            'popup_definition_id' => $popup->popup_definition_id,
            'popup_key' => $popup->popup_key,
            'total_shown' => 0,
            'total_dismissed' => 0,
            'total_acknowledged' => 0,
            'last_calculated_at' => now()
        ]);
        
        return response()->json([
            'status' => true,
            'message' => 'Popup definition created successfully',
            'data' => $popup
        ], 201);
    }
    
    /**
     * Get all popup definitions with analytics (Admin only)
     */
    public function getPopupDefinitions(Request $request)
    {
        $popups = PopupDefinition::with('analytics')
            ->orderBy('priority', 'desc')
            ->orderBy('created_at', 'desc')
            ->get();
        
        $formattedPopups = $popups->map(function ($popup) {
            return [
                'popup_id' => $popup->popup_definition_id,
                'popup_key' => $popup->popup_key,
                'title' => $popup->title,
                'content' => $popup->content,
                'popup_type' => $popup->popup_type,
                'target_audience' => $popup->target_audience,
                'priority' => $popup->priority,
                'is_active' => $popup->is_active,
                'created_at' => $popup->created_at,
                'analytics' => $popup->analytics ? [
                    'total_shown' => $popup->analytics->total_shown,
                    'total_dismissed' => $popup->analytics->total_dismissed,
                    'total_acknowledged' => $popup->analytics->total_acknowledged,
                    'dismissal_rate' => $popup->analytics->dismissal_rate,
                    'acknowledgment_rate' => $popup->analytics->acknowledgment_rate,
                    'last_calculated_at' => $popup->analytics->last_calculated_at
                ] : null
            ];
        });
        
        return response()->json([
            'status' => true,
            'message' => 'Popup definitions retrieved successfully',
            'data' => $formattedPopups
        ]);
    }
    
    /**
     * Update popup definition (Admin only)
     */
    public function updatePopupDefinition(Request $request, $popupId)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'nullable|string|max:255',
            'content' => 'nullable|string',
            'popup_type' => 'nullable|in:info,feature,warning,promotion,onboarding',
            'target_audience' => 'nullable|array',
            'priority' => 'nullable|integer|min:0|max:999',
            'is_active' => 'nullable|boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        $popup = PopupDefinition::find($popupId);
        
        if (!$popup) {
            return response()->json([
                'status' => false,
                'message' => 'Popup definition not found'
            ], 404);
        }
        
        $popup->update($request->only([
            'title', 'content', 'popup_type', 'target_audience', 'priority', 'is_active'
        ]));
        
        return response()->json([
            'status' => true,
            'message' => 'Popup definition updated successfully',
            'data' => $popup->fresh()
        ]);
    }
    
    // PRIVATE HELPER METHODS
    
    /**
     * Check if user has active subscription
     */
    private function checkUserSubscription($user)
    {
        // This would integrate with your subscription system
        // For now, return false as a placeholder
        return false;
    }
    
    /**
     * Update analytics asynchronously
     */
    private function updateAnalyticsAsync($popupIds)
    {
        // In a production environment, you'd queue this
        // For now, we'll do it synchronously but cache-aware
        foreach ($popupIds as $popupId) {
            // Use cache to prevent frequent updates
            $cacheKey = "popup_analytics_update_{$popupId}";
            if (!Cache::has($cacheKey)) {
                PopupAnalytics::updateAnalyticsForPopup($popupId);
                Cache::put($cacheKey, true, 300); // 5 minutes cache
            }
        }
    }
}