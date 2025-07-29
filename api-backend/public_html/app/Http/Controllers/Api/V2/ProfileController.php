<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppUser;
use App\Models\V2\AppUserProfile;
use App\Models\V2\DefaultAvatar;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;
use App\GlobalFunction;

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
            'is_kids' => 'boolean'
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
            'avatar_type' => 'default',
            'avatar_id' => $request->avatar_id ?? 1,
            'is_kids' => $request->is_kids ?? false
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
            'is_kids' => 'boolean',
            'profile_image' => 'nullable|file|image|max:2048' // 2MB max
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

        // Handle profile image upload
        if ($request->hasFile('profile_image')) {
            // Delete old custom avatar if exists
            if ($profile->avatar_type === 'custom' && $profile->custom_avatar_url) {
                GlobalFunction::deleteFile($profile->custom_avatar_url);
            }

            // Upload new image
            $imagePath = GlobalFunction::uploadFileToPublic($request->file('profile_image'), 'profile_avatars');
            $profile->avatar_type = 'custom';
            $profile->custom_avatar_url = $imagePath;
        } elseif ($request->has('avatar_id')) {
            // Switch to default avatar
            $profile->avatar_type = 'default';
            $profile->avatar_id = $request->avatar_id;
            
            // Delete custom avatar if switching from custom to default
            if ($profile->custom_avatar_url) {
                GlobalFunction::deleteFile($profile->custom_avatar_url);
                $profile->custom_avatar_url = null;
            }
        }

        $profile->save();
        $profile->load('defaultAvatar');

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
            GlobalFunction::deleteFile($profile->custom_avatar_url);
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
     * Format profile response
     */
    private function formatProfileResponse($profile)
    {
        return [
            'profile_id' => $profile->profile_id,
            'name' => $profile->name,
            'avatar_type' => $profile->avatar_type,
            'avatar_url' => $profile->avatar_url,
            'avatar_color' => $profile->avatar_color,
            'is_kids' => (bool) $profile->is_kids,
            'created_at' => $profile->created_at,
            'updated_at' => $profile->updated_at
        ];
    }
}