<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppUser;
use App\Models\V2\AppUserProfile;
use App\Models\V2\DefaultAvatar;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class ProfileController extends Controller
{
    /**
     * Get all profiles for a user
     */
    public function getUserProfiles(Request $request)
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

        $profiles = AppUserProfile::with('defaultAvatar')
            ->where('app_user_id', $request->user_id)
            ->where('is_active', 1)
            ->get()
            ->map(function ($profile) {
                return $this->formatProfileResponse($profile);
            });

        return response()->json([
            'status' => true,
            'message' => 'Profiles fetched successfully',
            'profiles' => $profiles
        ]);
    }

    /**
     * Create a new profile
     */
    public function createProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
            'name' => 'required|string|max:100',
            'avatar_id' => 'nullable|integer|exists:default_avatar,avatar_id',
            'avatar_type' => 'nullable|string|in:default,custom,color',
            'avatar_color' => 'nullable|string|max:7',
            'is_kids' => 'boolean',
            'age' => 'nullable|integer|min:1|max:150',
            'is_kids_profile' => 'nullable|boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        // Check if user has reached profile limit
        $profileCount = AppUserProfile::where('app_user_id', $request->user_id)
            ->where('is_active', 1)
            ->count();

        if ($profileCount >= AppUserProfile::MAX_PROFILES_PER_USER) {
            return response()->json([
                'status' => false,
                'message' => 'Maximum number of profiles reached (limit: ' . AppUserProfile::MAX_PROFILES_PER_USER . ')'
            ]);
        }

        // Create profile
        $profile = AppUserProfile::create([
            'app_user_id' => $request->user_id,
            'name' => $request->name,
            'avatar_type' => $request->avatar_type ?? 'color',
            'avatar_id' => $request->avatar_id ?? 1,
            'avatar_color' => $request->avatar_color ?? '#FF5252',
            'is_kids' => $request->is_kids ?? false,
            'is_kids_profile' => $request->is_kids_profile ?? $request->is_kids ?? false,
            'age' => $request->age
        ]);

        $profile->load('defaultAvatar');

        return response()->json([
            'status' => true,
            'message' => 'Profile created successfully',
            'profile' => $this->formatProfileResponse($profile)
        ]);
    }

    /**
     * Update a profile
     */
    public function updateProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'user_id' => 'required|integer|exists:app_user,app_user_id',
            'name' => 'string|max:100',
            'avatar_id' => 'integer|exists:default_avatar,avatar_id',
            'avatar_type' => 'nullable|string|in:default,custom,color',
            'avatar_color' => 'nullable|string|max:7',
            'is_kids' => 'boolean',
            'age' => 'nullable|integer|min:1|max:150',
            'is_kids_profile' => 'nullable|boolean',
            'profile_image' => 'nullable|file|image|max:10240' // 10MB max
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $profile = AppUserProfile::where('profile_id', $request->profile_id)
            ->where('app_user_id', $request->user_id)
            ->first();

        if (!$profile) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found'
            ]);
        }

        // Update basic fields
        if ($request->has('name')) {
            $profile->name = $request->name;
        }
        if ($request->has('is_kids')) {
            $profile->is_kids = $request->is_kids;
        }
        if ($request->has('age')) {
            $profile->age = $request->age;
        }
        if ($request->has('is_kids_profile')) {
            $profile->is_kids_profile = $request->is_kids_profile;
            // If setting as kids profile, also set is_kids for backward compatibility
            if ($request->is_kids_profile) {
                $profile->is_kids = true;
            }
        }

        // Handle avatar updates
        \Log::info('Profile update - avatar params', [
            'profile_id' => $profile->profile_id,
            'has_avatar_type' => $request->has('avatar_type'),
            'avatar_type' => $request->avatar_type,
            'avatar_color' => $request->avatar_color,
            'avatar_id' => $request->avatar_id,
            'current_avatar_type' => $profile->avatar_type
        ]);
        
        if ($request->hasFile('profile_image')) {
            // Delete old custom avatar if exists
            if ($profile->avatar_type === 'custom' && $profile->custom_avatar_url) {
                \App\GlobalFunction::deleteFile($profile->custom_avatar_url);
            }

            // Upload new image
            $imagePath = \App\GlobalFunction::saveFileAndGivePath($request->file('profile_image'));
            $profile->avatar_type = 'custom';
            $profile->custom_avatar_url = $imagePath;
        } elseif ($request->has('avatar_type')) {
            // Handle avatar type changes
            if ($request->avatar_type === 'color' || $request->avatar_type === 'default') {
                // Switch to color or default avatar
                $profile->avatar_type = $request->avatar_type;
                
                if ($request->has('avatar_id')) {
                    $profile->avatar_id = $request->avatar_id;
                }
                
                if ($request->has('avatar_color')) {
                    $profile->avatar_color = $request->avatar_color;
                }
                
                // Delete custom avatar if switching from custom
                if ($profile->custom_avatar_url) {
                    \App\GlobalFunction::deleteFile($profile->custom_avatar_url);
                    $profile->custom_avatar_url = null;
                }
                
                \Log::info('Profile avatar updated to color/default', [
                    'profile_id' => $profile->profile_id,
                    'new_avatar_type' => $profile->avatar_type,
                    'new_avatar_color' => $profile->avatar_color
                ]);
            }
        } elseif ($request->has('avatar_id')) {
            // Legacy support: just avatar_id means switch to default
            $profile->avatar_type = 'default';
            $profile->avatar_id = $request->avatar_id;
            
            // Delete custom avatar if switching from custom to default
            if ($profile->custom_avatar_url) {
                \App\GlobalFunction::deleteFile($profile->custom_avatar_url);
                $profile->custom_avatar_url = null;
            }
        }

        $profile->save();
        $profile->load('defaultAvatar');
        
        \Log::info('Profile after save', [
            'profile_id' => $profile->profile_id,
            'avatar_type' => $profile->avatar_type,
            'avatar_color' => $profile->avatar_color,
            'avatar_id' => $profile->avatar_id,
            'custom_avatar_url' => $profile->custom_avatar_url
        ]);

        return response()->json([
            'status' => true,
            'message' => 'Profile updated successfully',
            'profile' => $this->formatProfileResponse($profile)
        ]);
    }

    /**
     * Delete a profile
     */
    public function deleteProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'user_id' => 'required|integer|exists:app_user,app_user_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        // Check if user has at least 2 profiles (can't delete last profile)
        $profileCount = AppUserProfile::where('app_user_id', $request->user_id)
            ->where('is_active', 1)
            ->count();

        if ($profileCount <= 1) {
            return response()->json([
                'status' => false,
                'message' => 'Cannot delete the last profile'
            ]);
        }

        $profile = AppUserProfile::where('profile_id', $request->profile_id)
            ->where('app_user_id', $request->user_id)
            ->first();

        if (!$profile) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found'
            ]);
        }

        // Delete custom avatar if exists
        if ($profile->avatar_type === 'custom' && $profile->custom_avatar_url) {
            \App\GlobalFunction::deleteFile($profile->custom_avatar_url);
        }

        // Soft delete by setting is_active to 0
        $profile->is_active = 0;
        $profile->save();

        // If this was the last active profile, set another as active
        $user = AppUser::find($request->user_id);
        if ($user->last_active_profile_id == $profile->profile_id) {
            $newActiveProfile = AppUserProfile::where('app_user_id', $request->user_id)
                ->where('is_active', 1)
                ->first();
            
            if ($newActiveProfile) {
                $user->last_active_profile_id = $newActiveProfile->profile_id;
                $user->save();
            }
        }

        return response()->json([
            'status' => true,
            'message' => 'Profile deleted successfully'
        ]);
    }

    /**
     * Select/switch to a profile
     */
    public function selectProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'user_id' => 'required|integer|exists:app_user,app_user_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $profile = AppUserProfile::where('profile_id', $request->profile_id)
            ->where('app_user_id', $request->user_id)
            ->where('is_active', 1)
            ->first();

        if (!$profile) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found'
            ]);
        }

        // Update user's last active profile
        $user = AppUser::find($request->user_id);
        $user->last_active_profile_id = $profile->profile_id;
        $user->save();

        $profile->load('defaultAvatar');

        return response()->json([
            'status' => true,
            'message' => 'Profile selected successfully',
            'profile' => $this->formatProfileResponse($profile)
        ]);
    }

    /**
     * Get default avatars
     */
    public function getDefaultAvatars(Request $request)
    {
        $avatars = DefaultAvatar::where('is_active', 1)->get();

        return response()->json([
            'status' => true,
            'message' => 'Default avatars fetched successfully',
            'avatars' => $avatars
        ]);
    }

    /**
     * Update profile age settings
     */
    public function updateAgeSettings(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'user_id' => 'required|integer|exists:app_user,app_user_id',
            'age' => 'nullable|integer|min:1|max:150',
            'is_kids_profile' => 'nullable|boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $profile = AppUserProfile::where('profile_id', $request->profile_id)
            ->where('app_user_id', $request->user_id)
            ->first();

        if (!$profile) {
            return response()->json([
                'status' => false,
                'message' => 'Profile not found'
            ]);
        }
        
        // Update age if provided
        if ($request->has('age')) {
            $profile->age = $request->age;
        }
        
        // Update kids profile setting if provided
        if ($request->has('is_kids_profile')) {
            $profile->is_kids_profile = $request->is_kids_profile;
            // If setting as kids profile, also set is_kids for backward compatibility
            if ($request->is_kids_profile) {
                $profile->is_kids = true;
            }
        }
        
        $profile->save();

        return response()->json([
            'status' => true,
            'message' => 'Profile age settings updated successfully',
            'profile' => $this->formatProfileResponse($profile)
        ]);
    }

    /**
     * Get available age ratings
     */
    public function getAgeRatings(Request $request)
    {
        $ageRatings = \App\Models\V2\AgeLimit::orderBy('min_age')->get();

        return response()->json([
            'status' => true,
            'message' => 'Age ratings fetched successfully',
            'age_ratings' => $ageRatings
        ]);
    }

    /**
     * Format profile response
     */
    private function formatProfileResponse($profile)
    {
        return [
            'profile_id' => $profile->profile_id,
            'name' => $profile->name,
            'avatar_type' => $profile->avatar_type,
            'avatar_url' => $profile->avatar_url,
            'avatar_color' => $profile->display_color, // Always return a color for backward compatibility
            'avatar_id' => $profile->avatar_id,
            'is_kids' => (bool) $profile->is_kids,
            'is_kids_profile' => (bool) $profile->is_kids_profile,
            'age' => $profile->age,
            'created_at' => $profile->created_at,
            'updated_at' => $profile->updated_at
        ];
    }
}