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

    /**
     * V1 Compatible: Fetch custom ads
     */
    public function fetchCustomAds(Request $request)
    {
        $ads = CustomAd::where('status', 1)->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Fetch Custom Ads Successfully',
            'data' => $ads
        ]);
    }

    /**
     * V1 Compatible: Increase ad metric
     */
    public function increaseAdMetric(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'custom_ad_id' => 'required|integer|exists:custom_ad,custom_ad_id',
            'metric_type' => 'required|in:total_views,total_clicks'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $ad = CustomAd::find($request->custom_ad_id);
        
        if ($request->metric_type === 'total_views') {
            $ad->increment('total_views');
        } else {
            $ad->increment('total_clicks');
        }

        return response()->json([
            'status' => true,
            'message' => 'Ad metric increased successfully'
        ]);
    }

    /**
     * V1 Compatible: Increase ad view
     */
    public function increaseAdView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'custom_ad_id' => 'required|integer|exists:custom_ad,custom_ad_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        CustomAd::where('custom_ad_id', $request->custom_ad_id)
            ->increment('total_views');

        return response()->json([
            'status' => true,
            'message' => 'Increase Ad View Successfully'
        ]);
    }

    /**
     * V1 Compatible: Increase ad click
     */
    public function increaseAdClick(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'custom_ad_id' => 'required|integer|exists:custom_ad,custom_ad_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        CustomAd::where('custom_ad_id', $request->custom_ad_id)
            ->increment('total_clicks');

        return response()->json([
            'status' => true,
            'message' => 'Increase Ad Click Successfully'
        ]);
    }
}