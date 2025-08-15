<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;
use App\Models\Profile;

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
            $profile = Profile::findOrFail($request->profile_id);
            
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

            // Process image using native PHP functions
            $sourceImage = imagecreatefromstring($imageData);
            if (!$sourceImage) {
                return response()->json([
                    'status' => false,
                    'message' => 'Invalid image format'
                ], 400);
            }
            
            // Get original dimensions
            $width = imagesx($sourceImage);
            $height = imagesy($sourceImage);
            
            // Calculate crop dimensions for square
            $size = min($width, $height);
            $x = ($width - $size) / 2;
            $y = ($height - $size) / 2;
            
            // Create 500x500 image
            $targetSize = 500;
            $targetImage = imagecreatetruecolor($targetSize, $targetSize);
            
            // Resample the image
            imagecopyresampled(
                $targetImage, $sourceImage,
                0, 0, $x, $y,
                $targetSize, $targetSize,
                $size, $size
            );
            
            // Capture the image as JPEG
            ob_start();
            imagejpeg($targetImage, null, 80);
            $processedImageData = ob_get_clean();
            
            // Clean up
            imagedestroy($sourceImage);
            imagedestroy($targetImage);

            // Generate filename and path
            $fileName = 'avatar-' . $profile->profile_id . '-' . time() . '.jpg';
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
            
            return response()->json([
                'status' => true,
                'message' => 'Avatar uploaded successfully',
                'avatar_url' => $profile->avatar_url, // Use the accessor to ensure consistency
                'profile' => $profile
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
            $profile = Profile::findOrFail($request->profile_id);
            
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
}