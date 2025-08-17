<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;
use App\Models\V2\AppUserProfile;

class ProfileAvatarController extends Controller
{
    /**
     * Upload avatar for a profile
     */
    public function uploadAvatar(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id',
            'image_data' => 'required|string'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $profile = AppUserProfile::findOrFail($request->profile_id);
            
            // Verify user owns this profile
            if ($profile->app_user_id != $request->user_id) {
                return response()->json([
                    'status' => false,
                    'message' => 'Unauthorized - Profile does not belong to user'
                ], 403);
            }

            // Decode base64 image
            $imageData = base64_decode($request->image_data);
            if (!$imageData) {
                return response()->json([
                    'status' => false,
                    'message' => 'Invalid image data'
                ], 400);
            }

            // Validate it's a valid image without processing it
            $finfo = finfo_open(FILEINFO_MIME_TYPE);
            $mimeType = finfo_buffer($finfo, $imageData);
            finfo_close($finfo);
            
            $allowedMimeTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
            if (!in_array($mimeType, $allowedMimeTypes)) {
                return response()->json([
                    'status' => false,
                    'message' => 'Invalid image format. Allowed formats: JPEG, PNG, GIF, WebP'
                ], 400);
            }
            
            // Use the original image data without any processing to preserve orientation
            $processedImageData = $imageData;
            
            // Determine file extension based on mime type
            $extension = 'jpg';
            switch ($mimeType) {
                case 'image/png':
                    $extension = 'png';
                    break;
                case 'image/gif':
                    $extension = 'gif';
                    break;
                case 'image/webp':
                    $extension = 'webp';
                    break;
            }

            // Generate filename and path
            $fileName = 'avatar-' . $profile->profile_id . '-' . time() . '.' . $extension;
            $path = 'profile-avatars/' . $profile->app_user_id . '/' . $profile->profile_id . '/' . $fileName;

            // Upload to Digital Ocean Spaces (S3-compatible)
            $disk = Storage::disk('spaces'); // Configure this in config/filesystems.php
            $uploaded = $disk->put($path, $processedImageData, 'public');

            if (!$uploaded) {
                return response()->json([
                    'status' => false,
                    'message' => 'Failed to upload image'
                ], 500);
            }

            // Get CDN URL (remove trailing slash if present)
            $baseUrl = rtrim(config('filesystems.disks.spaces.url'), '/');
            $cdnUrl = $baseUrl . '/' . $path;
            
            // Delete old custom avatar if exists
            if ($profile->custom_avatar_url) {
                $this->deleteOldAvatar($profile->custom_avatar_url);
            }

            // Update profile
            $profile->update([
                'avatar_type' => 'custom',
                'custom_avatar_url' => $cdnUrl,
                'custom_avatar_uploaded_at' => now()
            ]);

            // Load fresh profile data with avatar info
            $profile->refresh();
            $profile->load('defaultAvatar');
            
            return response()->json([
                'status' => true,
                'message' => 'Avatar uploaded successfully',
                'avatar_url' => $profile->avatar_url, // Use the accessor to ensure consistency
                'profile' => $this->formatProfileResponse($profile)
            ]);

        } catch (\Exception $e) {
            \Log::error('Avatar upload failed: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to upload avatar'
            ], 500);
        }
    }

    /**
     * Remove custom avatar for a profile
     */
    public function removeAvatar(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer|exists:app_user,app_user_id',
            'profile_id' => 'required|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $profile = AppUserProfile::findOrFail($request->profile_id);
            
            // Verify user owns this profile
            if ($profile->app_user_id != $request->user_id) {
                return response()->json([
                    'status' => false,
                    'message' => 'Unauthorized - Profile does not belong to user'
                ], 403);
            }

            // Delete from S3
            if ($profile->custom_avatar_url) {
                $this->deleteOldAvatar($profile->custom_avatar_url);
            }

            // Revert to color avatar
            $profile->update([
                'avatar_type' => 'color',
                'custom_avatar_url' => null,
                'custom_avatar_uploaded_at' => null
            ]);

            return response()->json([
                'status' => true,
                'message' => 'Avatar removed successfully'
            ]);

        } catch (\Exception $e) {
            \Log::error('Avatar removal failed: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to remove avatar'
            ], 500);
        }
    }

    /**
     * Delete old avatar from storage
     */
    private function deleteOldAvatar($url)
    {
        try {
            // Extract path from CDN URL (handle trailing slash)
            $baseUrl = rtrim(config('filesystems.disks.spaces.url'), '/');
            $cdnUrl = $baseUrl . '/';
            $path = str_replace($cdnUrl, '', $url);
            
            Storage::disk('spaces')->delete($path);
        } catch (\Exception $e) {
            \Log::warning('Failed to delete old avatar: ' . $e->getMessage());
        }
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