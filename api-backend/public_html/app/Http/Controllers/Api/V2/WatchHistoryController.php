<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppUserWatchHistory;
use App\Models\V2\Content;
use App\Models\V2\Episode;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class WatchHistoryController extends Controller
{
    /**
     * Update watch progress
     */
    public function updateWatchProgress(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
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

        // Determine if content is completed
        $completed = false;
        if ($request->total_duration > 0) {
            $percentageWatched = ($request->last_watched_position / $request->total_duration) * 100;
            $completed = $percentageWatched >= 90; // Mark as completed if 90% watched
        }

        $watchHistory = AppUserWatchHistory::updateOrCreate(
            [
                'app_user_id' => $request->app_user_id,
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
            'limit' => 'nullable|integer|min:1|max:50',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $limit = $request->limit ?? 20;
        
        $watchHistory = AppUserWatchHistory::with([
                'content.language', 
                'content.genres',
                'episode.season.content'
            ])
            ->where('app_user_id', $request->app_user_id)
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
            'content_id' => 'required_without:episode_id|integer|exists:content,content_id',
            'episode_id' => 'required_without:content_id|integer|exists:episode,episode_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $watchHistory = AppUserWatchHistory::where('app_user_id', $request->app_user_id)
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
}