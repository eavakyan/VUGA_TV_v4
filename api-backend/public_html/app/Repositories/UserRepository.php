<?php

namespace App\Repositories;

use App\User;

class UserRepository
{
    /**
     * Find user by ID
     */
    public function findById($id)
    {
        return User::find($id);
    }

    /**
     * Find user by email
     */
    public function findByEmail($email)
    {
        return User::where('email', $email)->first();
    }

    /**
     * Find user by identity
     */
    public function findByIdentity($identity)
    {
        return User::where('identity', $identity)->first();
    }

    /**
     * Create new user
     */
    public function create($data)
    {
        return User::create($data);
    }

    /**
     * Update user
     */
    public function update($id, $data)
    {
        $user = User::find($id);
        if ($user) {
            $user->update($data);
            return $user;
        }
        return null;
    }

    /**
     * Delete user
     */
    public function delete($id)
    {
        $user = User::find($id);
        if ($user) {
            return $user->delete();
        }
        return false;
    }

    /**
     * Get users with search and pagination
     */
    public function getUsersForAdmin($filters = [])
    {
        $query = User::query();

        if (isset($filters['search']) && !empty($filters['search'])) {
            $search = $filters['search'];
            $query->where(function($q) use ($search) {
                $q->where('fullname', 'LIKE', "%{$search}%")
                  ->orWhere('email', 'LIKE', "%{$search}%");
            });
        }

        return $query->orderBy('created_at', 'DESC');
    }

    /**
     * Update user's watchlist
     */
    public function updateWatchlist($userId, $watchlistIds)
    {
        return User::where('id', $userId)->update([
            'watchlist_content_ids' => $watchlistIds
        ]);
    }

    /**
     * Get user's watchlist content IDs
     */
    public function getUserWatchlistIds($userId)
    {
        $user = User::find($userId);
        if ($user && !empty($user->watchlist_content_ids)) {
            return explode(',', $user->watchlist_content_ids);
        }
        return [];
    }

    /**
     * Add content to user's watchlist
     */
    public function addToWatchlist($userId, $contentId)
    {
        $user = User::find($userId);
        if (!$user) {
            return false;
        }

        $watchlistIds = $this->getUserWatchlistIds($userId);
        
        if (!in_array($contentId, $watchlistIds)) {
            $watchlistIds[] = $contentId;
            $this->updateWatchlist($userId, implode(',', $watchlistIds));
            return true;
        }
        
        return false; // Already in watchlist
    }

    /**
     * Remove content from user's watchlist
     */
    public function removeFromWatchlist($userId, $contentId)
    {
        $user = User::find($userId);
        if (!$user) {
            return false;
        }

        $watchlistIds = $this->getUserWatchlistIds($userId);
        
        if (in_array($contentId, $watchlistIds)) {
            $watchlistIds = array_diff($watchlistIds, [$contentId]);
            $this->updateWatchlist($userId, implode(',', $watchlistIds));
            return true;
        }
        
        return false; // Not in watchlist
    }

    /**
     * Clear user's watchlist
     */
    public function clearWatchlist($userId)
    {
        return $this->updateWatchlist($userId, null);
    }

    /**
     * Update user device token
     */
    public function updateDeviceToken($userId, $deviceToken)
    {
        return User::where('id', $userId)->update([
            'device_token' => $deviceToken
        ]);
    }

    /**
     * Clear user device token (logout)
     */
    public function clearDeviceToken($userId)
    {
        return $this->updateDeviceToken($userId, null);
    }

    /**
     * Get user count
     */
    public function getUserCount()
    {
        return User::count();
    }

    /**
     * Get recently registered users
     */
    public function getRecentUsers($limit = 10)
    {
        return User::orderBy('created_at', 'DESC')->limit($limit)->get();
    }

    /**
     * Get users by login type
     */
    public function getUsersByLoginType($loginType)
    {
        return User::where('login_type', $loginType)->get();
    }

    /**
     * Get active users (users with device tokens)
     */
    public function getActiveUsers()
    {
        return User::whereNotNull('device_token')->get();
    }
} 