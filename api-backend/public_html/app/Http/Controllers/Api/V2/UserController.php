<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppUser;
use App\Models\V2\AppUserProfile;
use App\Models\V2\AppUserWatchlist;
use App\Models\V2\AppUserFavorite;
use App\Models\V2\AppProfileEpisodeWatchlist;
use App\Models\V2\Content;
use App\Models\V2\Episode;
use App\GlobalFunction;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;

class UserController extends Controller
{
    /**
     * User registration
     */
    public function userRegistration(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'identity' => 'required',
            'email' => 'required',  // Removed email validation due to PHP compatibility issue
            'login_type' => 'required|integer',
            'device_type' => 'required|integer',
            'device_token' => 'required',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        // Check if user exists by identity or email
        $user = AppUser::where('identity', $request->identity)
                       ->orWhere('email', $request->email)
                       ->first();

        if ($user) {
            // Update existing user
            $user->login_type = (int) $request->login_type;
            $user->device_type = (int) $request->device_type;
            $user->device_token = $request->device_token;
            $user->save();
            
            return response()->json([
                'status' => false,
                'message' => 'User already exists',
                'data' => $this->formatUserResponse($user)
            ]);
        }

        // Create new user
        $user = new AppUser;
        $user->fullname = $request->fullname;
        $user->email = $request->email;
        $user->login_type = (int) $request->login_type;
        $user->identity = $request->identity;
        $user->device_token = $request->device_token;
        $user->device_type = (int) $request->device_type;
        $user->save();
        
        // Create default profile for new user
        $profile = \App\Models\V2\AppUserProfile::create([
            'app_user_id' => $user->app_user_id,
            'name' => $user->fullname ?? 'Profile 1',
            'avatar_type' => 'default',
            'avatar_id' => 1,
            'is_kids' => false
        ]);
        
        // Set as last active profile
        $user->last_active_profile_id = $profile->profile_id;
        $user->save();

        return response()->json([
            'status' => true,
            'message' => 'User registered successfully',
            'data' => $this->formatUserResponse($user)
        ], 201);
    }

    /**
     * Update user profile
     */
    public function updateProfile(Request $request)
    {
        // Accept both user_id (V1) and app_user_id (V2)
        $userId = $request->user_id ?? $request->app_user_id;
        
        // Log the incoming request for debugging
        \Log::info('updateProfile request', [
            'user_id' => $request->user_id,
            'app_user_id' => $request->app_user_id,
            'userId' => $userId,
            'all_params' => $request->all()
        ]);
        
        $validator = Validator::make(['app_user_id' => $userId], [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            \Log::error('updateProfile validation failed', [
                'errors' => $validator->errors()->toArray(),
                'userId' => $userId
            ]);
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($userId);

        if ($request->has('fullname')) {
            $user->fullname = $request->fullname;
        }
        
        if ($request->has('email')) {
            $user->email = $request->email;
        }
        
        // Handle watchlist - now using profile-based watchlist
        if ($request->has('watchlist_content_ids')) {
            // Get the profile_id from request or use last active profile
            $profileId = $request->profile_id ?? $user->last_active_profile_id;
            
            \Log::info('updateProfile - watchlist update', [
                'user_id' => $userId,
                'profile_id' => $profileId,
                'watchlist_content_ids' => $request->watchlist_content_ids
            ]);
            
            if ($profileId) {
                $profile = AppUserProfile::find($profileId);
                if ($profile && $profile->app_user_id == $user->app_user_id) {
                    $this->updateProfileWatchlist($profile, $request->watchlist_content_ids);
                } else {
                    \Log::warning('updateProfile - profile mismatch', [
                        'profile_id' => $profileId,
                        'profile_found' => $profile ? 'yes' : 'no',
                        'profile_user_id' => $profile ? $profile->app_user_id : null,
                        'request_user_id' => $user->app_user_id
                    ]);
                }
            } else {
                \Log::warning('updateProfile - no profile_id', [
                    'user_id' => $userId,
                    'last_active_profile_id' => $user->last_active_profile_id
                ]);
            }
        }
        
        if ($request->has('login_type')) {
            $user->login_type = (int) $request->login_type;
        }
        
        if ($request->hasFile('profile_image')) {
            GlobalFunction::deleteFile($user->profile_image);
            $file = $request->file('profile_image');
            $path = GlobalFunction::saveFileAndGivePath($file);
            $user->profile_image = $path;
        }

        if ($request->has('device_type')) {
            $user->device_type = (int) $request->device_type;
        }
        
        if ($request->has('device_token')) {
            $user->device_token = $request->device_token;
        }

        // Persist contact preference consents
        if ($request->has('email_consent')) {
            $emailConsent = (int) $request->email_consent === 1 ? 1 : 0;
            $user->email_consent = $emailConsent;
            // Only set/update date when explicitly provided in request
            $user->email_consent_date = now();
            // Track IP for audit
            if (property_exists($user, 'consent_ip_address')) {
                $user->consent_ip_address = $request->ip();
            }
        }

        if ($request->has('sms_consent')) {
            $smsConsent = (int) $request->sms_consent === 1 ? 1 : 0;
            $user->sms_consent = $smsConsent;
            $user->sms_consent_date = now();
            if (property_exists($user, 'consent_ip_address')) {
                $user->consent_ip_address = $request->ip();
            }
        }
        
        $user->save();

        // Reload user with watchlist relation
        $user->load('watchlist');
        
        $responseData = $this->formatUserResponse($user);
        
        \Log::info('updateProfile - response', [
            'user_id' => $userId,
            'watchlist_content_ids' => $responseData['watchlist_content_ids'] ?? 'not set',
            'last_active_profile_id' => $responseData['last_active_profile_id'] ?? 'not set'
        ]);
        
        return response()->json([
            'status' => true,
            'message' => 'Profile updated successfully',
            'data' => $responseData
        ]);
    }

    /**
     * Fetch user profile (V1 Compatible)
     */
    public function fetchProfile(Request $request)
    {
        // Accept both user_id (V1) and app_user_id (V2)
        $userId = $request->user_id ?? $request->app_user_id;
        
        // Allow user_id 0 for non-logged in users
        if ($userId == 0) {
            return response()->json([
                'status' => false,
                'message' => 'User not logged in'
            ]);
        }
        
        $validator = Validator::make(['user_id' => $userId], [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::with(['watchlist', 'favorites', 'ratings'])
                       ->find($userId);

        return response()->json([
            'status' => true,
            'message' => 'Profile fetched successfully',
            'data' => $this->formatUserResponse($user)
        ]);
    }

    /**
     * User logout
     */
    public function logOut(Request $request)
    {
        // Accept both user_id (V1) and app_user_id (V2)
        $userId = $request->user_id ?? $request->app_user_id;
        
        $validator = Validator::make(['user_id' => $userId], [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($userId);
        $user->device_token = null;
        $user->save();

        return response()->json([
            'status' => true,
            'message' => 'Log Out Successfully'
        ]);
    }
    
    /**
     * Delete user account (V1 compatible)
     */
    public function deleteMyAccount(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->user_id);
        
        // Delete profile image if exists
        if ($user->profile_image) {
            GlobalFunction::deleteFile($user->profile_image);
        }
        
        $user->delete();
        
        return response()->json([
            'status' => true,
            'message' => 'Account deleted successfully'
        ]);
    }

    /**
     * Add/Remove content from watchlist
     */
    public function toggleWatchlist(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'content_id' => 'required|integer|exists:content,content_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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
        
        \Log::info('toggleWatchlist called', [
            'app_user_id' => $request->app_user_id,
            'content_id' => $request->content_id,
            'profile_id' => $profileId,
            'has_profile' => !empty($profileId)
        ]);
        
        // Use profile-specific watchlist if profile exists
        if ($profileId) {
            $profile = AppUserProfile::find($profileId);
            if ($profile && $profile->app_user_id == $request->app_user_id) {
                if ($profile->watchlist()->where('app_user_watchlist.content_id', $request->content_id)->exists()) {
                    // Remove from watchlist
                    $profile->watchlist()->detach($request->content_id);
                    $message = 'Removed from watchlist';
                } else {
                    // Add to watchlist
                    $profile->watchlist()->attach($request->content_id);
                    $message = 'Added to watchlist';
                }
            } else {
                return response()->json([
                    'status' => false,
                    'message' => 'Profile not found or unauthorized'
                ], 404);
            }
        } else {
            // Fallback to user-level watchlist for backward compatibility
            if ($user->watchlist()->where('app_user_watchlist.content_id', $request->content_id)->exists()) {
                // Remove from watchlist
                $user->watchlist()->detach($request->content_id);
                $message = 'Removed from watchlist';
            } else {
                // Add to watchlist
                $user->watchlist()->attach($request->content_id);
                $message = 'Added to watchlist';
            }
        }

        return response()->json([
            'status' => true,
            'message' => $message,
            'data' => $this->formatUserResponse($user->fresh(['watchlist', 'profiles']))
        ]);
    }

    /**
     * Add/Remove content from favorites
     */
    public function toggleFavorite(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'content_id' => 'required|integer|exists:content,content_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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
        
        // Profile is required for favorites
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
        
        if ($profile->favorites()->where('app_profile_favorite.content_id', $request->content_id)->exists()) {
            // Remove from favorites
            $profile->favorites()->detach($request->content_id);
            $message = 'Removed from favorites';
        } else {
            // Add to favorites
            $profile->favorites()->attach($request->content_id);
            $message = 'Added to favorites';
        }

        return response()->json([
            'status' => true,
            'message' => $message,
            'data' => $this->formatUserResponse($user->fresh(['favorites', 'profiles']))
        ]);
    }

    /**
     * Rate content
     */
    public function rateContent(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'content_id' => 'required|integer|exists:content,content_id',
            'rating' => 'required|numeric|min:0|max:10',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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
        
        // Profile is required for ratings
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
        
        // Update or create rating
        $profile->ratings()->syncWithoutDetaching([
            $request->content_id => ['rating' => $request->rating]
        ]);

        // Update content average rating
        $this->updateContentAverageRating($request->content_id);

        return response()->json([
            'status' => true,
            'message' => 'Rating submitted successfully'
        ]);
    }

    /**
     * Rate episode (for TV series)
     */
    public function rateEpisode(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'episode_id' => 'required|integer|exists:episode,episode_id',
            'rating' => 'required|numeric|min:0|max:10',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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
        
        // Profile is required for ratings
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
        
        // Update or create episode rating
        DB::table('app_profile_episode_rating')->upsert([
            'profile_id' => $profileId,
            'episode_id' => $request->episode_id,
            'rating' => $request->rating,
            'created_at' => now(),
            'updated_at' => now()
        ], ['profile_id', 'episode_id'], ['rating', 'updated_at']);

        // Update episode average rating
        $this->updateEpisodeAverageRating($request->episode_id);

        return response()->json([
            'status' => true,
            'message' => 'Episode rating submitted successfully'
        ]);
    }

    /**
     * Get user's watch history
     */
    public function getWatchHistory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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

        $watchHistory = AppUserWatchHistory::with(['content', 'episode.season'])
                            ->where('profile_id', $profileId)
                            ->orderBy('updated_at', 'desc')
                            ->get();

        return response()->json([
            'status' => true,
            'message' => 'Watch history fetched successfully',
            'data' => $watchHistory
        ]);
    }

    /**
     * Format user response to include new primary key
     */
    private function formatUserResponse($user)
    {
        $data = $user->toArray();
        
        // Get watchlist from the active profile for backward compatibility
        $watchlistIds = '';
        if ($user->last_active_profile_id) {
            $profile = AppUserProfile::with('watchlist')->find($user->last_active_profile_id);
            if ($profile) {
                $watchlistIds = $profile->watchlist->pluck('content_id')->implode(',');
            }
        }
        $data['watchlist_content_ids'] = $watchlistIds;
        
        // Include profile information
        $user->load(['profiles' => function($query) {
            $query->where('is_active', 1)->with('defaultAvatar');
        }, 'lastActiveProfile.defaultAvatar']);
        
        $data['profiles'] = $user->profiles->map(function ($profile) {
            return [
                'profile_id' => $profile->profile_id,
                'name' => $profile->name,
                'avatar_type' => $profile->avatar_type,
                'avatar_url' => $profile->avatar_url,
                'avatar_color' => $profile->avatar_color,
                'is_kids' => (bool) $profile->is_kids
            ];
        });
        
        $data['last_active_profile'] = $user->lastActiveProfile ? [
            'profile_id' => $user->lastActiveProfile->profile_id,
            'name' => $user->lastActiveProfile->name,
            'avatar_type' => $user->lastActiveProfile->avatar_type,
            'avatar_url' => $user->lastActiveProfile->avatar_url,
            'avatar_color' => $user->lastActiveProfile->avatar_color,
            'is_kids' => (bool) $user->lastActiveProfile->is_kids
        ] : null;
        
        // Include consent fields
        $data['email_consent'] = $user->email_consent;
        $data['sms_consent'] = $user->sms_consent;
        $data['email_consent_date'] = $user->email_consent_date;
        $data['sms_consent_date'] = $user->sms_consent_date;
        
        return $data;
    }

    /**
     * Update profile's watchlist (helper method)
     */
    private function updateProfileWatchlist($profile, $contentIds)
    {
        \Log::info('updateProfileWatchlist called', [
            'profile_id' => $profile->profile_id,
            'content_ids' => $contentIds,
            'is_empty' => empty($contentIds)
        ]);
        
        if (empty($contentIds)) {
            $profile->watchlist()->detach();
            \Log::info('Watchlist cleared for profile', ['profile_id' => $profile->profile_id]);
            return;
        }

        $ids = is_string($contentIds) ? explode(',', $contentIds) : $contentIds;
        $ids = array_filter($ids, 'is_numeric');
        
        \Log::info('Processing watchlist IDs', [
            'profile_id' => $profile->profile_id,
            'raw_ids' => $ids,
            'count' => count($ids)
        ]);
        
        // Only sync content IDs that actually exist in the database
        $validIds = \App\Models\V2\Content::whereIn('content_id', $ids)->pluck('content_id')->toArray();
        
        \Log::info('Syncing watchlist', [
            'profile_id' => $profile->profile_id,
            'valid_ids' => $validIds,
            'valid_count' => count($validIds),
            'invalid_count' => count($ids) - count($validIds)
        ]);
        
        $profile->watchlist()->sync($validIds);
    }

    /**
     * Update content's average rating (helper method)
     */
    private function updateContentAverageRating($contentId)
    {
        $avgRating = DB::table('app_profile_rating')
                        ->where('content_id', $contentId)
                        ->avg('rating');
        
        Content::where('content_id', $contentId)
               ->update(['ratings' => $avgRating ?: 0]);
    }
    
    /**
     * Update episode's average rating (helper method)
     */
    private function updateEpisodeAverageRating($episodeId)
    {
        $avgRating = DB::table('app_profile_episode_rating')
                        ->where('episode_id', $episodeId)
                        ->avg('rating');
        
        DB::table('episode')
            ->where('episode_id', $episodeId)
            ->update(['ratings' => $avgRating ?: 0]);
    }
    
    /**
     * Fetch user's watchlist (V1 compatible)
     */
    public function fetchWatchList(Request $request)
    {
        \Log::info('fetchWatchList called', [
            'all_params' => $request->all()
        ]);
        
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
            'start' => 'required|integer',
            'limit' => 'required|integer',
            'type' => 'nullable|integer',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->user_id);
        $profileId = $request->profile_id;
        
        // If no profile_id provided, use last active profile
        if (!$profileId) {
            $profileId = $user->last_active_profile_id;
        }
        
        // Get watchlist content IDs
        $watchlistIds = [];
        \Log::info('fetchWatchList request', [
            'user_id' => $request->user_id,
            'profile_id' => $profileId,
            'has_profile_id' => !empty($profileId)
        ]);
        
        if ($profileId) {
            $profile = AppUserProfile::find($profileId);
            if ($profile && $profile->app_user_id == $request->user_id) {
                $watchlistIds = $profile->watchlist()
                    ->pluck('content.content_id')
                    ->toArray();
                \Log::info('Profile watchlist fetched', [
                    'profile_id' => $profileId,
                    'watchlist_count' => count($watchlistIds),
                    'watchlist_ids' => $watchlistIds
                ]);
            } else {
                \Log::warning('Profile not found or mismatch', [
                    'profile_id' => $profileId,
                    'profile_found' => $profile ? 'yes' : 'no',
                    'profile_user_id' => $profile ? $profile->app_user_id : null,
                    'request_user_id' => $request->user_id
                ]);
            }
        } else {
            // Fallback to user-level watchlist for backward compatibility
            $watchlistIds = $user->watchlist()
                ->pluck('content.content_id')
                ->toArray();
            \Log::info('User watchlist fetched (fallback)', [
                'user_id' => $request->user_id,
                'watchlist_count' => count($watchlistIds)
            ]);
        }
        
        // If no watchlist items, return empty array
        if (empty($watchlistIds)) {
            \Log::info('No watchlist items found, returning empty array');
            return response()->json([
                'status' => true,
                'message' => 'Fetch WatchList Successfully',
                'data' => []
            ]);
        }
        
        // Build query
        $query = Content::whereIn('content_id', $watchlistIds)
            ->where('is_show', 1);
        
        // Filter by type if provided
        if ($request->has('type') && $request->type != 0) {
            $query->where('type', $request->type);
            \Log::info('Filtering by type', ['type' => $request->type]);
        }
        
        // Debug: Log content types in watchlist
        $contentTypes = Content::whereIn('content_id', $watchlistIds)
            ->pluck('type', 'content_id')
            ->toArray();
        \Log::info('Content types in watchlist', [
            'content_types' => $contentTypes,
            'requested_type' => $request->type
        ]);
        
        // Log the query before pagination
        \Log::info('Query before pagination', [
            'watchlist_ids' => $watchlistIds,
            'sql' => $query->toSql(),
            'bindings' => $query->getBindings()
        ]);
        
        // Apply pagination
        $contents = $query->offset($request->start)
            ->limit($request->limit)
            ->get();
            
        \Log::info('Query results', [
            'count' => $contents->count(),
            'start' => $request->start,
            'limit' => $request->limit
        ]);
        
        // Format response to match V1
        $formattedContents = $contents->map(function ($content) {
            return [
                'id' => $content->content_id,
                'title' => $content->title,
                'description' => $content->description,
                'type' => (int) $content->type,
                'duration' => (string) $content->duration,
                'release_year' => (int) $content->release_year,
                'ratings' => (float) $content->ratings,
                'language_id' => (int) $content->language_id,
                'download_link' => $content->download_link,
                'trailer_url' => $content->trailer_url,
                'vertical_poster' => $content->vertical_poster,
                'horizontal_poster' => $content->horizontal_poster,
                'genre_ids' => $content->genre_ids,
                'is_featured' => (int) $content->is_featured,
                'total_view' => (int) $content->total_view,
                'total_download' => (int) $content->total_download,
                'total_share' => (int) $content->total_share,
                'actor_ids' => $content->actor_ids,
                'is_watchlist' => true
            ];
        });
        
        \Log::info('fetchWatchList response', [
            'profile_id' => $profileId,
            'watchlist_ids' => $watchlistIds,
            'formatted_count' => $formattedContents->count(),
            'response_data' => $formattedContents->pluck('id')->toArray(),
            'type_filter' => $request->type,
            'all_content_before_filter' => Content::whereIn('content_id', $watchlistIds)->pluck('content_id')->toArray()
        ]);
        
        return response()->json([
            'status' => true,
            'message' => 'Fetch WatchList Successfully',
            'data' => $formattedContents
        ]);
    }
    
    /**
     * V1 Compatible: Get user subscription
     */
    public function getUserSubscription(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer|exists:app_user,app_user_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }
        
        // For now, return no active subscription
        // In the future, you would check the actual subscription status
        return response()->json([
            'status' => true,
            'message' => 'Get User Subscription Successfully',
            'subscription' => null
        ]);
    }

    /**
     * Toggle episode watchlist status
     */
    public function toggleEpisodeWatchlist(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'episode_id' => 'required|integer|exists:episode,episode_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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
        
        // Profile is required for episode watchlist
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
        
        // Check if episode exists in watchlist using direct query
        $exists = DB::table('app_profile_episode_watchlist')
            ->where('profile_id', $profileId)
            ->where('episode_id', $request->episode_id)
            ->exists();
            
        if ($exists) {
            // Remove from watchlist
            $profile->episodeWatchlist()->detach($request->episode_id);
            $message = 'Removed from watchlist';
            $isInWatchlist = false;
        } else {
            // Add to watchlist
            $profile->episodeWatchlist()->attach($request->episode_id);
            $message = 'Added to watchlist';
            $isInWatchlist = true;
        }

        return response()->json([
            'status' => true,
            'message' => $message,
            'is_in_watchlist' => $isInWatchlist
        ]);
    }

    /**
     * Check if episode is in watchlist
     */
    public function checkEpisodeWatchlist(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
            'episode_id' => 'required|integer|exists:episode,episode_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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
        
        // Profile is required for episode watchlist
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
        
        // Check if episode exists in watchlist using direct query
        $isInWatchlist = DB::table('app_profile_episode_watchlist')
            ->where('profile_id', $profileId)
            ->where('episode_id', $request->episode_id)
            ->exists();

        return response()->json([
            'status' => true,
            'is_in_watchlist' => $isInWatchlist,
            'message' => 'Episode watchlist status retrieved'
        ]);
    }

    /**
     * Fetch unified watchlist (movies, TV shows, and episodes)
     */
    public function fetchUnifiedWatchlist(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
            'start' => 'required|integer|min:0',
            'limit' => 'required|integer|min:1|max:100',
            'type' => 'nullable|integer|in:1,2,3', // 1=movies, 2=series, 3=episodes
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->user_id);
        $profileId = $request->profile_id;
        
        // If no profile_id provided, use last active profile
        if (!$profileId) {
            $profileId = $user->last_active_profile_id;
        }
        
        // Profile is required for unified watchlist
        if (!$profileId) {
            return response()->json([
                'status' => false,
                'message' => 'Profile ID is required'
            ], 400);
        }
        
        $profile = AppUserProfile::find($profileId);
        if (!$profile || $profile->app_user_id != $request->user_id) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found or unauthorized'
            ], 404);
        }

        $watchlistItems = [];
        $type = $request->type;

        // Get content watchlist (movies and series)
        if (!$type || in_array($type, [1, 2])) {
            $contentQuery = $profile->watchlist()
                ->where('content.is_show', 1);
            
            if ($type) {
                $contentQuery->where('content.type', $type);
            }
            
            $contentItems = $contentQuery->get()->map(function ($content) {
                // Use horizontal_poster for consistency with watchlist cards
                $poster = $content->horizontal_poster ?: $content->vertical_poster;
                
                return [
                    'item_type' => 'content',
                    'content_id' => $content->content_id,
                    'episode_id' => null,
                    'title' => $content->title,
                    'type' => $content->type,
                    'poster' => $poster,
                    'horizontal_poster' => $content->horizontal_poster,
                    'vertical_poster' => $content->vertical_poster,
                    'ratings' => $content->ratings,
                    'duration' => $content->duration,
                    'release_year' => $content->release_year,
                    'genre_ids' => $content->genre_ids,
                    'series_title' => null,
                    'season_number' => null,
                    'episode_number' => null,
                    'episode_thumbnail' => null,
                    'added_at' => $content->pivot->created_at->toISOString()
                ];
            });
            
            $watchlistItems = array_merge($watchlistItems, $contentItems->toArray());
        }

        // Get episode watchlist
        if (!$type || $type == 3) {
            $episodeItems = $profile->episodeWatchlist()
                ->with(['season.content'])
                ->get()
                ->map(function ($episode) {
                    $season = $episode->season;
                    $content = $season ? $season->content : null;
                    
                    return [
                        'item_type' => 'episode',
                        'content_id' => $content ? $content->content_id : null,
                        'episode_id' => $episode->episode_id,
                        'title' => $episode->title,
                        'type' => 2, // Episodes are part of TV series
                        'poster' => $episode->thumbnail,
                        'horizontal_poster' => $content ? $content->horizontal_poster : null,
                        'vertical_poster' => $content ? $content->vertical_poster : null,
                        'ratings' => $episode->ratings,
                        'duration' => $episode->duration, // Episode duration in minutes
                        'release_year' => $content ? $content->release_year : null,
                        'genre_ids' => $content ? $content->genre_ids : null,
                        'series_title' => $content ? $content->title : null,
                        'season_number' => $season ? $season->season_number : null,
                        'episode_number' => $episode->number,
                        'episode_thumbnail' => $episode->thumbnail,
                        'added_at' => $episode->pivot->created_at->toISOString()
                    ];
                });
            
            $watchlistItems = array_merge($watchlistItems, $episodeItems->toArray());
        }

        // Sort by added_at date (newest first)
        usort($watchlistItems, function ($a, $b) {
            return strtotime($b['added_at']) - strtotime($a['added_at']);
        });

        // Apply pagination
        $totalItems = count($watchlistItems);
        $start = $request->start;
        $limit = $request->limit;
        $paginatedItems = array_slice($watchlistItems, $start, $limit);

        return response()->json([
            'status' => true,
            'message' => 'Unified watchlist fetched successfully',
            'data' => $paginatedItems,
            'pagination' => [
                'current_page' => floor($start / $limit) + 1,
                'total_items' => $totalItems,
                'items_per_page' => $limit,
                'total_pages' => ceil($totalItems / $limit)
            ]
        ]);
    }
}