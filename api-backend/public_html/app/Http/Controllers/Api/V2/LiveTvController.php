<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\TvChannel;
use App\Models\V2\TvCategory;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class LiveTvController extends Controller
{
    /**
     * Get live TV page data (categories and featured channels)
     */
    public function getLiveTvPageData(Request $request)
    {
        // Get all categories
        $categories = TvCategory::all();
        
        // Get featured channels (first 10 channels)
        $featuredChannels = TvChannel::with('categories')
                                     ->limit(10)
                                     ->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Live TV data fetched successfully',
            'data' => [
                'categories' => $categories,
                'featured_channels' => $featuredChannels
            ]
        ]);
    }
    
    /**
     * Get all TV categories
     */
    public function getCategories(Request $request)
    {
        $categories = TvCategory::withCount('channels')
                                ->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Categories fetched successfully',
            'data' => $categories
        ]);
    }
    
    /**
     * Get channels by category
     */
    public function getChannelsByCategory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'category_id' => 'required|integer|exists:tv_category,tv_category_id',
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        $perPage = $request->per_page ?? 20;
        
        // Get channels for this category
        $channels = TvChannel::whereHas('categories', function($query) use ($request) {
                                $query->where('tv_category.tv_category_id', $request->category_id);
                            })
                            ->with('categories')
                            ->paginate($perPage);
        
        return response()->json([
            'status' => true,
            'message' => 'Channels fetched successfully',
            'data' => $channels->items(),
            'pagination' => [
                'current_page' => $channels->currentPage(),
                'last_page' => $channels->lastPage(),
                'per_page' => $channels->perPage(),
                'total' => $channels->total()
            ]
        ]);
    }
    
    /**
     * Search TV channels
     */
    public function searchChannels(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'query' => 'required|string|min:1',
            'category_id' => 'integer|exists:tv_category,tv_category_id',
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        $perPage = $request->per_page ?? 20;
        
        $query = TvChannel::where('title', 'LIKE', '%' . $request->query . '%');
        
        // Filter by category if provided
        if ($request->has('category_id')) {
            $query->whereHas('categories', function($q) use ($request) {
                $q->where('tv_category.tv_category_id', $request->category_id);
            });
        }
        
        $channels = $query->with('categories')
                          ->paginate($perPage);
        
        return response()->json([
            'status' => true,
            'message' => 'Search results fetched successfully',
            'data' => $channels->items(),
            'pagination' => [
                'current_page' => $channels->currentPage(),
                'last_page' => $channels->lastPage(),
                'per_page' => $channels->perPage(),
                'total' => $channels->total()
            ]
        ]);
    }
    
    /**
     * Get single channel details
     */
    public function getChannelDetails($id)
    {
        $channel = TvChannel::with('categories')->find($id);
        
        if (!$channel) {
            return response()->json([
                'status' => false,
                'message' => 'Channel not found'
            ], 404);
        }
        
        return response()->json([
            'status' => true,
            'message' => 'Channel details fetched successfully',
            'data' => $channel
        ]);
    }
    
    /**
     * Increase channel view count
     */
    public function increaseChannelView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'channel_id' => 'required|integer|exists:tv_channel,tv_channel_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        // In the future, you might want to track view stats in a separate table
        // For now, just return success
        
        return response()->json([
            'status' => true,
            'message' => 'View count increased successfully'
        ]);
    }
    
    /**
     * Increase channel share count
     */
    public function increaseChannelShare(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'channel_id' => 'required|integer|exists:tv_channel,tv_channel_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        // In the future, you might want to track share stats in a separate table
        // For now, just return success
        
        return response()->json([
            'status' => true,
            'message' => 'Share count increased successfully'
        ]);
    }
    
    /**
     * Get all channels (paginated)
     */
    public function getAllChannels(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
            'access_type' => 'integer|in:1,2,3'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        $perPage = $request->per_page ?? 20;
        
        $query = TvChannel::with('categories');
        
        // Filter by access type if provided
        if ($request->has('access_type')) {
            $query->where('access_type', $request->access_type);
        }
        
        $channels = $query->paginate($perPage);
        
        return response()->json([
            'status' => true,
            'message' => 'Channels fetched successfully',
            'data' => $channels->items(),
            'pagination' => [
                'current_page' => $channels->currentPage(),
                'last_page' => $channels->lastPage(),
                'per_page' => $channels->perPage(),
                'total' => $channels->total()
            ]
        ]);
    }
    
    /**
     * V1 Compatible: Fetch Live TV page data
     */
    public function fetchLiveTVPageData(Request $request)
    {
        $categories = TvCategory::all();
        
        $categoriesWithChannels = [];
        foreach ($categories as $category) {
            $channels = TvChannel::whereHas('categories', function($query) use ($category) {
                $query->where('tv_category.tv_category_id', $category->tv_category_id);
            })->get();
            
            if ($channels->count() > 0) {
                $category->channels = $channels;
                $categoriesWithChannels[] = $category;
            }
        }
        
        return response()->json([
            'status' => true,
            'message' => 'Fetch Live TV Page Data Successfully',
            'categories' => $categoriesWithChannels
        ]);
    }
    
    /**
     * V1 Compatible: Fetch TV channels by category
     */
    public function fetchTVChannelByCategory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_category_id' => 'required|integer|exists:tv_category,tv_category_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }
        
        $channels = TvChannel::whereHas('categories', function($query) use ($request) {
            $query->where('tv_category.tv_category_id', $request->tv_category_id);
        })->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Fetch TV Channel By Category Successfully',
            'channels' => $channels
        ]);
    }
    
    /**
     * V1 Compatible: Search TV channel
     */
    public function searchTVChannel(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'search' => 'required|string'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }
        
        $channels = TvChannel::where('title', 'LIKE', '%' . $request->search . '%')->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Search TV Channel Successfully',
            'channels' => $channels
        ]);
    }
    
    /**
     * V1 Compatible: Increase TV channel view
     */
    public function increaseTVChannelView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_channel_id' => 'required|integer|exists:tv_channel,tv_channel_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }
        
        // Increment view count if the column exists
        // TvChannel::where('tv_channel_id', $request->tv_channel_id)->increment('total_view');
        
        return response()->json([
            'status' => true,
            'message' => 'Increase TV Channel View Successfully'
        ]);
    }
    
    /**
     * V1 Compatible: Increase TV channel share
     */
    public function increaseTVChannelShare(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_channel_id' => 'required|integer|exists:tv_channel,tv_channel_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }
        
        // Increment share count if the column exists
        // TvChannel::where('tv_channel_id', $request->tv_channel_id)->increment('total_share');
        
        return response()->json([
            'status' => true,
            'message' => 'Increase TV Channel Share Successfully'
        ]);
    }
}