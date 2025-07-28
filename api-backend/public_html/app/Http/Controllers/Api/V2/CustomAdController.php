<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\CustomAd;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class CustomAdController extends Controller
{
    /**
     * Get custom ads
     */
    public function getCustomAds(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'type' => 'integer|in:1,2', // 1 = image, 2 = video
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $query = CustomAd::query();

        if ($request->has('type')) {
            $query->where('type', $request->type);
        }

        $ads = $query->get();

        return response()->json([
            'status' => true,
            'message' => 'Custom ads fetched successfully',
            'data' => $ads
        ]);
    }

    /**
     * Get single custom ad
     */
    public function getCustomAd($id)
    {
        $ad = CustomAd::find($id);

        if (!$ad) {
            return response()->json([
                'status' => false,
                'message' => 'Ad not found'
            ], 404);
        }

        return response()->json([
            'status' => true,
            'message' => 'Ad fetched successfully',
            'data' => $ad
        ]);
    }

    /**
     * Create custom ad (admin function - requires authentication in production)
     */
    public function createCustomAd(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:200',
            'redirectlink' => 'nullable|url|max:500',
            'ads_image' => 'nullable|string|max:500',
            'ads_video' => 'nullable|string|max:500',
            'type' => 'required|integer|in:1,2',
            'status' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        // Validate that either image or video is provided based on type
        if ($request->type == 1 && empty($request->ads_image)) {
            return response()->json([
                'status' => false,
                'message' => 'Image URL is required for image type ads'
            ], 400);
        }

        if ($request->type == 2 && empty($request->ads_video)) {
            return response()->json([
                'status' => false,
                'message' => 'Video URL is required for video type ads'
            ], 400);
        }

        $ad = new CustomAd;
        $ad->title = $request->title;
        $ad->redirectlink = $request->redirectlink;
        $ad->ads_image = $request->ads_image;
        $ad->ads_video = $request->ads_video;
        $ad->type = $request->type;
        $ad->status = $request->status ?? 1;
        $ad->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom ad created successfully',
            'data' => $ad
        ], 201);
    }

    /**
     * Update custom ad (admin function - requires authentication in production)
     */
    public function updateCustomAd(Request $request, $id)
    {
        $ad = CustomAd::find($id);

        if (!$ad) {
            return response()->json([
                'status' => false,
                'message' => 'Ad not found'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'title' => 'string|max:200',
            'redirectlink' => 'nullable|url|max:500',
            'ads_image' => 'nullable|string|max:500',
            'ads_video' => 'nullable|string|max:500',
            'type' => 'integer|in:1,2',
            'status' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        // Update only provided fields
        if ($request->has('title')) $ad->title = $request->title;
        if ($request->has('redirectlink')) $ad->redirectlink = $request->redirectlink;
        if ($request->has('ads_image')) $ad->ads_image = $request->ads_image;
        if ($request->has('ads_video')) $ad->ads_video = $request->ads_video;
        if ($request->has('type')) $ad->type = $request->type;
        if ($request->has('status')) $ad->status = $request->status;

        // Validate consistency
        if ($ad->type == 1 && empty($ad->ads_image)) {
            return response()->json([
                'status' => false,
                'message' => 'Image URL is required for image type ads'
            ], 400);
        }

        if ($ad->type == 2 && empty($ad->ads_video)) {
            return response()->json([
                'status' => false,
                'message' => 'Video URL is required for video type ads'
            ], 400);
        }

        $ad->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom ad updated successfully',
            'data' => $ad
        ]);
    }

    /**
     * Delete custom ad (admin function - requires authentication in production)
     */
    public function deleteCustomAd($id)
    {
        $ad = CustomAd::find($id);

        if (!$ad) {
            return response()->json([
                'status' => false,
                'message' => 'Ad not found'
            ], 404);
        }

        $ad->delete();

        return response()->json([
            'status' => true,
            'message' => 'Custom ad deleted successfully'
        ]);
    }

    /**
     * Get active custom ads for display
     */
    public function getActiveAds(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'type' => 'integer|in:1,2',
            'limit' => 'integer|min:1|max:50'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $query = CustomAd::where('status', 1);

        if ($request->has('type')) {
            $query->where('type', $request->type);
        }

        $limit = $request->limit ?? 10;
        $ads = $query->inRandomOrder()
                     ->limit($limit)
                     ->get();

        return response()->json([
            'status' => true,
            'message' => 'Active ads fetched successfully',
            'data' => $ads
        ]);
    }
}