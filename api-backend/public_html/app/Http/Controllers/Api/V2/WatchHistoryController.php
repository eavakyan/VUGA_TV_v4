<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppUser;
use App\Models\V2\AppUserProfile;
use App\Models\V2\AppProfileWatchHistory;
use App\Models\V2\Content;
use App\Models\V2\Episode;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Carbon\Carbon;

class WatchHistoryController extends Controller
{
    /**
     * Update watch progress
     */
    public function updateWatchProgress(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id',
            'content_id' => 'required_without:episode_id|integer|exists:content,content_id',
            'episode_id' => 'required_without:content_id|integer|exists:episode,episode_id',
            'last_watched_position' => 'required|integer|min:0',
            'total_duration' => 'required|integer|min:1',
            'device_type' => 'nullable|integer|in:0,1',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        $profileId = $request->profile_id;
        
        // If no profile_id provided, use last active profile
        if (!$profileId) {
            $profileId = $user->last_active_profile_id;
        }

        // Determine if content is completed
        $completed = false;
        if ($request->total_duration > 0) {
            $percentageWatched = ($request->last_watched_position / $request->total_duration) * 100;
            $completed = $percentageWatched >= 90; // Mark as completed if 90% watched
        }
        
        // Profile is required for watch history
        if (!$profileId) {
            return response()->json([
                'status' => false,
                'message' => 'Profile ID is required'
            ], 400);
        }
        
        $profile = AppUserProfile::find($profileId);
        if (!$profile || $profile->app_user_id != $request->app_user_id) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found or unauthorized'
            ], 404);
        }
        
        $watchHistory = AppProfileWatchHistory::updateOrCreate(
            [
                'profile_id' => $profileId,
                'content_id' => $request->content_id,
                'episode_id' => $request->episode_id,
            ],
            [
                'last_watched_position' => $request->last_watched_position,
                'total_duration' => $request->total_duration,
                'completed' => $completed,
                'device_type' => $request->device_type ?? 0,
            ]
        );

        // Update view count if newly completed
        if ($completed && $watchHistory->wasRecentlyCreated) {
            if ($request->content_id) {
                Content::where('content_id', $request->content_id)->increment('total_view');
            }
            if ($request->episode_id) {
                Episode::where('episode_id', $request->episode_id)->increment('total_view');
            }
        }

        return response()->json([
            'status' => true,
            'message' => 'Watch progress updated successfully',
            'data' => $watchHistory
        ]);
    }

    /**
     * Get continue watching list
     */
    public function getContinueWatching(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id',
            'limit' => 'nullable|integer|min:1|max:50',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        $profileId = $request->profile_id;
        $limit = $request->limit ?? 20;
        
        // If no profile_id provided, use last active profile
        if (!$profileId) {
            $profileId = $user->last_active_profile_id;
        }
        
        // Profile is required for continue watching
        if (!$profileId) {
            return response()->json([
                'status' => false,
                'message' => 'Profile ID is required'
            ], 400);
        }
        
        $profile = AppUserProfile::find($profileId);
        if (!$profile || $profile->app_user_id != $request->app_user_id) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found or unauthorized'
            ], 404);
        }
        
        $watchHistory = AppProfileWatchHistory::with([
                'content.language', 
                'content.genres',
                'episode.season.content'
            ])
            ->where('profile_id', $profileId)
            ->where('completed', 0)
            ->where('last_watched_position', '>', 0)
            ->orderBy('updated_at', 'desc')
            ->limit($limit)
            ->get();

        $formattedData = $watchHistory->map(function($history) {
            $contentData = null;
            
            if ($history->content) {
                $contentData = $history->content->toArray();
                $contentData['genre_ids'] = $history->content->genres->pluck('genre_id')->implode(',');
            } elseif ($history->episode && $history->episode->season && $history->episode->season->content) {
                $contentData = $history->episode->season->content->toArray();
                $contentData['genre_ids'] = $history->episode->season->content->genres->pluck('genre_id')->implode(',');
            }
            
            return [
                'watch_history_id' => $history->watch_history_id,
                'content' => $contentData,
                'episode' => $history->episode ? [
                    'episode_id' => $history->episode->episode_id,
                    'title' => $history->episode->title,
                    'number' => $history->episode->number,
                    'season_id' => $history->episode->season_id,
                    'season_title' => $history->episode->season->title
                ] : null,
                'last_watched_position' => $history->last_watched_position,
                'total_duration' => $history->total_duration,
                'percentage_watched' => round(($history->last_watched_position / $history->total_duration) * 100, 2),
                'updated_at' => $history->updated_at
            ];
        });

        return response()->json([
            'status' => true,
            'message' => 'Continue watching list fetched successfully',
            'data' => $formattedData
        ]);
    }

    /**
     * Mark content/episode as completed
     */
    public function markAsCompleted(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id',
            'content_id' => 'required_without:episode_id|integer|exists:content,content_id',
            'episode_id' => 'required_without:content_id|integer|exists:episode,episode_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        $profileId = $request->profile_id;
        
        // If no profile_id provided, use last active profile
        if (!$profileId) {
            $profileId = $user->last_active_profile_id;
        }
        
        // Profile is required for marking as completed
        if (!$profileId) {
            return response()->json([
                'status' => false,
                'message' => 'Profile ID is required'
            ], 400);
        }
        
        $profile = AppUserProfile::find($profileId);
        if (!$profile || $profile->app_user_id != $request->app_user_id) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found or unauthorized'
            ], 404);
        }
        
        $watchHistory = AppProfileWatchHistory::where('profile_id', $profileId)
            ->where('content_id', $request->content_id)
            ->where('episode_id', $request->episode_id)
            ->first();

        if ($watchHistory) {
            $watchHistory->completed = 1;
            $watchHistory->save();
        }

        return response()->json([
            'status' => true,
            'message' => 'Marked as completed successfully'
        ]);
    }

    /**
     * Sync watch history from mobile apps to server
     * Accepts an array of watch history items and syncs them to the database
     */
    public function syncWatchHistory(Request $request)
    {
        try {
            // Handle both JSON and form-encoded data
            $data = $request->all();
            
            // Log the request for debugging
            Log::info("WatchHistory sync request", [
                'content_type' => $request->header('Content-Type'),
                'has_profile_id' => isset($data['profile_id']),
                'watch_history_count' => isset($data['watch_history']) ? count($data['watch_history']) : 0,
                'sync_mode' => isset($data['sync_mode']) ? $data['sync_mode'] : 'merge'
            ]);
            
            $validator = Validator::make($data, [
                'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
                'watch_history' => 'required|array',
                'watch_history.*.content_id' => 'required|integer',
                'watch_history.*.episode_id' => 'nullable|integer',
                'watch_history.*.last_watched_position' => 'required|integer|min:0',
                'watch_history.*.total_duration' => 'nullable|integer|min:0',
                'watch_history.*.completed' => 'nullable|boolean',
                'watch_history.*.device_type' => 'nullable|integer',
                'watch_history.*.watched_at' => 'nullable|date',
                'sync_mode' => 'nullable|string|in:merge,replace' // New field for sync mode
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'status' => false,
                    'message' => 'Validation failed',
                    'errors' => $validator->errors()
                ], 400);
            }

            $profileId = $data['profile_id'];
            $watchHistoryItems = $data['watch_history'];
            $syncMode = $data['sync_mode'] ?? 'replace'; // Default to replace mode
            $syncedCount = 0;
            $updatedCount = 0;
            $deletedCount = 0;

            DB::beginTransaction();
            
            // If sync_mode is 'replace', delete all existing watch history for this profile first
            if ($syncMode === 'replace') {
                $deletedCount = AppProfileWatchHistory::where('profile_id', $profileId)->delete();
                Log::info("WatchHistory sync: Deleted $deletedCount existing items for profile $profileId");
            }

            foreach ($watchHistoryItems as $item) {
                if ($syncMode === 'replace') {
                    // In replace mode, just create all items (old ones were already deleted)
                    $watchedAt = isset($item['watched_at']) ? $item['watched_at'] : now();
                    
                    AppProfileWatchHistory::create([
                        'profile_id' => $profileId,
                        'content_id' => $item['content_id'],
                        'episode_id' => $item['episode_id'] ?? null,
                        'last_watched_position' => $item['last_watched_position'],
                        'total_duration' => $item['total_duration'] ?? 0,
                        'completed' => $item['completed'] ?? false,
                        'device_type' => $item['device_type'] ?? 1, // 1=mobile
                        'created_at' => $watchedAt,
                        'updated_at' => $watchedAt
                    ]);
                    $syncedCount++;
                } else {
                    // Merge mode - check if exists and update or create
                    $existingHistory = AppProfileWatchHistory::where([
                        'profile_id' => $profileId,
                        'content_id' => $item['content_id'],
                        'episode_id' => $item['episode_id'] ?? null
                    ])->first();

                    $watchedAt = isset($item['watched_at']) ? $item['watched_at'] : now();

                    if ($existingHistory) {
                        // Update existing entry if the new position is greater
                        if ($item['last_watched_position'] > $existingHistory->last_watched_position) {
                            $existingHistory->update([
                                'last_watched_position' => $item['last_watched_position'],
                                'total_duration' => $item['total_duration'] ?? $existingHistory->total_duration,
                                'completed' => $item['completed'] ?? $existingHistory->completed,
                                'device_type' => $item['device_type'] ?? $existingHistory->device_type,
                                'updated_at' => $watchedAt
                            ]);
                            $updatedCount++;
                        }
                    } else {
                        // Create new entry
                        AppProfileWatchHistory::create([
                            'profile_id' => $profileId,
                            'content_id' => $item['content_id'],
                            'episode_id' => $item['episode_id'] ?? null,
                            'last_watched_position' => $item['last_watched_position'],
                            'total_duration' => $item['total_duration'] ?? 0,
                            'completed' => $item['completed'] ?? false,
                            'device_type' => $item['device_type'] ?? 1, // 1=mobile
                            'created_at' => $watchedAt,
                            'updated_at' => $watchedAt
                        ]);
                        $syncedCount++;
                    }
                }
            }

            DB::commit();

            Log::info("WatchHistory sync completed", [
                'profile_id' => $profileId,
                'sync_mode' => $syncMode,
                'deleted' => $deletedCount,
                'synced_new' => $syncedCount,
                'updated_existing' => $updatedCount,
                'total_items' => count($watchHistoryItems)
            ]);

            return response()->json([
                'status' => true,
                'message' => 'Watch history synced successfully',
                'data' => [
                    'sync_mode' => $syncMode,
                    'deleted' => $deletedCount,
                    'synced_new' => $syncedCount,
                    'updated_existing' => $updatedCount,
                    'total_processed' => count($watchHistoryItems)
                ]
            ]);

        } catch (\Exception $e) {
            DB::rollback();
            Log::error("WatchHistory sync failed", [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);

            return response()->json([
                'status' => false,
                'message' => 'Failed to sync watch history: ' . $e->getMessage()
            ], 500);
        }
    }
}