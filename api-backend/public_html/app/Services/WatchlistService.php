<?php

namespace App\Services;

use App\Constants;
use App\Content;
use App\User;

class WatchlistService
{
    /**
     * Get user's watchlist with pagination
     */
    public function getUserWatchlist($userId, $start = 0, $limit = 10)
    {
        $user = User::where('id', $userId)->first();
        
        if (!$user || empty($user->watchlist_content_ids)) {
            return [];
        }

        $watchlistContentIds = explode(',', $user->watchlist_content_ids);
        
        return Content::where('is_show', Constants::showContent)
                     ->whereIn('id', $watchlistContentIds)
                     ->orderBy('created_at', 'DESC')
                     ->offset($start)
                     ->limit($limit)
                     ->get();
    }

    /**
     * Add content to user's watchlist
     */
    public function addToWatchlist($userId, $contentId)
    {
        $user = User::where('id', $userId)->first();
        $content = Content::where('id', $contentId)->first();

        if (!$user || !$content) {
            return ['success' => false, 'message' => 'User or Content not found'];
        }

        $watchlistContentIds = [];
        if (!empty($user->watchlist_content_ids)) {
            $watchlistContentIds = explode(',', $user->watchlist_content_ids);
        }

        // Check if content is already in watchlist
        if (in_array($contentId, $watchlistContentIds)) {
            return ['success' => false, 'message' => 'Content already in watchlist'];
        }

        // Add content to watchlist
        $watchlistContentIds[] = $contentId;
        $user->watchlist_content_ids = implode(',', $watchlistContentIds);
        $user->save();

        return ['success' => true, 'message' => 'Content added to watchlist successfully', 'user' => $user];
    }

    /**
     * Remove content from user's watchlist
     */
    public function removeFromWatchlist($userId, $contentId)
    {
        $user = User::where('id', $userId)->first();
        
        if (!$user || empty($user->watchlist_content_ids)) {
            return ['success' => false, 'message' => 'User not found or watchlist is empty'];
        }

        $watchlistContentIds = explode(',', $user->watchlist_content_ids);

        // Check if content is in watchlist
        if (!in_array($contentId, $watchlistContentIds)) {
            return ['success' => false, 'message' => 'Content not in watchlist'];
        }

        // Remove content from watchlist
        $watchlistContentIds = array_diff($watchlistContentIds, [$contentId]);
        $user->watchlist_content_ids = implode(',', $watchlistContentIds);
        $user->save();

        return ['success' => true, 'message' => 'Content removed from watchlist successfully', 'user' => $user];
    }

    /**
     * Check if content is in user's watchlist
     */
    public function isInWatchlist($userId, $contentId)
    {
        $user = User::where('id', $userId)->first();
        
        if (!$user || empty($user->watchlist_content_ids)) {
            return false;
        }

        $watchlistContentIds = explode(',', $user->watchlist_content_ids);
        return in_array($contentId, $watchlistContentIds);
    }

    /**
     * Get watchlist count for user
     */
    public function getWatchlistCount($userId)
    {
        $user = User::where('id', $userId)->first();
        
        if (!$user || empty($user->watchlist_content_ids)) {
            return 0;
        }

        $watchlistContentIds = explode(',', $user->watchlist_content_ids);
        return count($watchlistContentIds);
    }

    /**
     * Clear user's entire watchlist
     */
    public function clearWatchlist($userId)
    {
        $user = User::where('id', $userId)->first();
        
        if (!$user) {
            return ['success' => false, 'message' => 'User not found'];
        }

        $user->watchlist_content_ids = null;
        $user->save();

        return ['success' => true, 'message' => 'Watchlist cleared successfully'];
    }
} 