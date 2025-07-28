<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppUser;
use App\Models\V2\AppUserWatchlist;
use App\Models\V2\AppUserFavorite;
use App\Models\V2\Content;
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
            'email' => 'required|email',
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
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);

        if ($request->has('fullname')) {
            $user->fullname = $request->fullname;
        }
        
        if ($request->has('email')) {
            $user->email = $request->email;
        }
        
        // Handle watchlist - now using normalized table
        if ($request->has('watchlist_content_ids')) {
            $this->updateUserWatchlist($user, $request->watchlist_content_ids);
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
        
        $user->save();

        return response()->json([
            'status' => true,
            'message' => 'Profile updated successfully',
            'data' => $this->formatUserResponse($user)
        ]);
    }

    /**
     * Fetch user profile
     */
    public function fetchProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::with(['watchlist', 'favorites', 'ratings'])
                       ->find($request->app_user_id);

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
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        $user->device_token = null;
        $user->save();

        return response()->json([
            'status' => true,
            'message' => 'Logged out successfully',
            'data' => $this->formatUserResponse($user)
        ]);
    }
    
    /**
     * Delete user account
     */
    public function deleteMyAccount(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        
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
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        
        if ($user->watchlist()->where('content_id', $request->content_id)->exists()) {
            // Remove from watchlist
            $user->watchlist()->detach($request->content_id);
            $message = 'Removed from watchlist';
        } else {
            // Add to watchlist
            $user->watchlist()->attach($request->content_id);
            $message = 'Added to watchlist';
        }

        return response()->json([
            'status' => true,
            'message' => $message,
            'data' => $this->formatUserResponse($user->fresh(['watchlist']))
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
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        
        if ($user->favorites()->where('content_id', $request->content_id)->exists()) {
            // Remove from favorites
            $user->favorites()->detach($request->content_id);
            $message = 'Removed from favorites';
        } else {
            // Add to favorites
            $user->favorites()->attach($request->content_id);
            $message = 'Added to favorites';
        }

        return response()->json([
            'status' => true,
            'message' => $message,
            'data' => $this->formatUserResponse($user->fresh(['favorites']))
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
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $user = AppUser::find($request->app_user_id);
        
        // Update or create rating
        $user->ratings()->syncWithoutDetaching([
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
     * Get user's watch history
     */
    public function getWatchHistory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $watchHistory = AppUserWatchHistory::with(['content', 'episode.season'])
                            ->where('app_user_id', $request->app_user_id)
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
        
        // Include watchlist as comma-separated IDs for backward compatibility
        if ($user->relationLoaded('watchlist')) {
            $data['watchlist_content_ids'] = $user->watchlist->pluck('content_id')->implode(',');
        }
        
        return $data;
    }

    /**
     * Update user's watchlist (helper method)
     */
    private function updateUserWatchlist($user, $contentIds)
    {
        if (empty($contentIds)) {
            $user->watchlist()->detach();
            return;
        }

        $ids = is_string($contentIds) ? explode(',', $contentIds) : $contentIds;
        $ids = array_filter($ids, 'is_numeric');
        
        $user->watchlist()->sync($ids);
    }

    /**
     * Update content's average rating (helper method)
     */
    private function updateContentAverageRating($contentId)
    {
        $avgRating = DB::table('app_user_rating')
                        ->where('content_id', $contentId)
                        ->avg('rating');
        
        Content::where('content_id', $contentId)
               ->update(['ratings' => $avgRating ?: 0]);
    }
}