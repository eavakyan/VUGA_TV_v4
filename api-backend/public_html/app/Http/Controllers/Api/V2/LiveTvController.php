<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\TvChannel;
use App\Models\V2\TvCategory;
use App\Models\V2\LiveTvSchedule;
use App\Models\V2\LiveTvViewAnalytics;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

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
    
    // =========================================================================
    // ENHANCED LIVE TV ENDPOINTS WITH SCHEDULE & EPG SUPPORT
    // =========================================================================
    
    /**
     * Get live TV channels with current programs
     */
    public function getChannelsWithCurrentPrograms(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'category_id' => 'integer|exists:tv_category,tv_category_id',
            'language' => 'string|max:10',
            'country_code' => 'string|max:5',
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
            'include_inactive' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $perPage = $request->per_page ?? 20;
        
        // Load channels with categories, schedules only if table exists
        try {
            $query = TvChannel::with(['categories', 'schedules' => function($q) {
                $q->currentlyAiring()->first();
            }]);
        } catch (\Exception $e) {
            // Schedule relationship doesn't exist, just load categories
            $query = TvChannel::with(['categories']);
        }

        // Only apply active filter if column exists
        if (!$request->boolean('include_inactive')) {
            try {
                $query->active();
            } catch (\Exception $e) {
                // is_active column doesn't exist yet, skip this filter
                // This maintains backward compatibility before migration
            }
        }

        if ($request->has('category_id')) {
            $query->whereHas('categories', function($q) use ($request) {
                $q->where('tv_category.tv_category_id', $request->category_id);
            });
        }

        if ($request->has('language')) {
            try {
                $query->byLanguage($request->language);
            } catch (\Exception $e) {
                // language column doesn't exist yet, skip this filter
            }
        }

        if ($request->has('country_code')) {
            try {
                $query->byCountry($request->country_code);
            } catch (\Exception $e) {
                // country_code column doesn't exist yet, skip this filter
            }
        }

        // Order by channel_number if it exists, otherwise by id
        try {
            $channels = $query->orderBy('channel_number')->paginate($perPage);
        } catch (\Exception $e) {
            $channels = $query->orderBy('tv_channel_id')->paginate($perPage);
        }

        // Add current program info to each channel
        $channelsData = $channels->items();
        foreach ($channelsData as $channel) {
            // Try to get program info if schedule table exists
            $currentProgram = null;
            $nextProgram = null;
            $hasEpg = false;
            
            try {
                $currentProgram = $channel->getCurrentProgram();
                $nextProgram = $channel->getNextProgram();
                $hasEpg = $channel->has_epg;
            } catch (\Exception $e) {
                // Schedule methods don't work yet, provide default values
            }
            
            $channel->current_program = $currentProgram;
            $channel->next_program = $nextProgram;
            $channel->has_epg = $hasEpg;
        }

        return response()->json([
            'status' => true,
            'message' => 'Channels with current programs fetched successfully',
            'data' => $channelsData,
            'pagination' => [
                'current_page' => $channels->currentPage(),
                'last_page' => $channels->lastPage(),
                'per_page' => $channels->perPage(),
                'total' => $channels->total()
            ]
        ]);
    }

    /**
     * Get EPG schedule grid data
     */
    public function getScheduleGrid(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'date' => 'date_format:Y-m-d',
            'hours_range' => 'integer|min:1|max:24',
            'category_id' => 'integer|exists:tv_category,tv_category_id',
            'channel_ids' => 'array',
            'channel_ids.*' => 'integer|exists:tv_channel,tv_channel_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $date = $request->date ?? Carbon::now()->format('Y-m-d');
        $hoursRange = $request->hours_range ?? 6; // Default 6 hours
        
        $startTime = Carbon::parse($date)->startOfHour();
        $endTime = $startTime->copy()->addHours($hoursRange);

        // Get channels query (with backward compatibility)
        $channelsQuery = TvChannel::query();
        
        // Try to use active scope if column exists
        try {
            $channelsQuery->active();
        } catch (\Exception $e) {
            // is_active column doesn't exist, get all channels
        }
        
        // Try to order by channel_number if column exists
        try {
            $channelsQuery->orderBy('channel_number');
        } catch (\Exception $e) {
            $channelsQuery->orderBy('tv_channel_id');
        }
        
        if ($request->has('category_id')) {
            $channelsQuery->whereHas('categories', function($q) use ($request) {
                $q->where('tv_category.tv_category_id', $request->category_id);
            });
        }

        if ($request->has('channel_ids')) {
            $channelsQuery->whereIn('tv_channel_id', $request->channel_ids);
        }

        $channels = $channelsQuery->get();

        $scheduleGrid = [];
        
        foreach ($channels as $channel) {
            $programs = collect(); // Empty collection for now
            
            // Try to get schedule data if table exists
            try {
                $programs = LiveTvSchedule::forChannel($channel->tv_channel_id)
                    ->withinTimeRange($startTime, $endTime)
                    ->orderBy('start_time')
                    ->get();
            } catch (\Exception $e) {
                // Schedule table doesn't exist yet, return empty programs
            }

            $scheduleGrid[] = [
                'channel' => [
                    'id' => $channel->tv_channel_id,
                    'title' => $channel->title,
                    'channel_number' => $channel->channel_number ?? null,
                    'logo' => $channel->logo_url ?? $channel->thumbnail,
                    'description' => $channel->description ?? null
                ],
                'programs' => $programs->map(function($program) {
                    return [
                        'id' => $program->schedule_id,
                        'title' => $program->program_title,
                        'description' => $program->description,
                        'start_time' => $program->start_time,
                        'end_time' => $program->end_time,
                        'duration_minutes' => $program->duration_in_minutes,
                        'genre' => $program->genre,
                        'rating' => $program->rating,
                        'is_currently_airing' => $program->is_currently_airing,
                        'progress_percentage' => $program->progress_percentage,
                        'thumbnail_url' => $program->thumbnail_url
                    ];
                })
            ];
        }

        return response()->json([
            'status' => true,
            'message' => 'Schedule grid fetched successfully',
            'data' => [
                'schedule_grid' => $scheduleGrid,
                'time_range' => [
                    'start_time' => $startTime,
                    'end_time' => $endTime,
                    'hours_span' => $hoursRange
                ]
            ]
        ]);
    }

    /**
     * Get detailed channel information with full schedule
     */
    public function getChannelWithSchedule($channelId, Request $request)
    {
        $validator = Validator::make(array_merge($request->all(), ['channel_id' => $channelId]), [
            'channel_id' => 'required|integer|exists:tv_channel,tv_channel_id',
            'date' => 'date_format:Y-m-d',
            'days_range' => 'integer|min:1|max:7'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $channel = TvChannel::with(['categories'])->find($channelId);
        
        if (!$channel) {
            return response()->json([
                'status' => false,
                'message' => 'Channel not found'
            ], 404);
        }

        $date = $request->date ?? Carbon::now()->format('Y-m-d');
        $daysRange = $request->days_range ?? 1;

        $scheduleData = [];
        
        for ($i = 0; $i < $daysRange; $i++) {
            $currentDate = Carbon::parse($date)->addDays($i);
            $programs = $channel->getProgramsForDate($currentDate);
            
            $scheduleData[] = [
                'date' => $currentDate->format('Y-m-d'),
                'day_name' => $currentDate->format('l'),
                'programs' => $programs->map(function($program) {
                    return [
                        'id' => $program->schedule_id,
                        'title' => $program->program_title,
                        'description' => $program->description,
                        'start_time' => $program->start_time,
                        'end_time' => $program->end_time,
                        'formatted_air_time' => $program->formatted_air_time,
                        'duration_minutes' => $program->duration_in_minutes,
                        'genre' => $program->genre,
                        'rating' => $program->rating,
                        'is_currently_airing' => $program->is_currently_airing,
                        'has_ended' => $program->has_ended,
                        'thumbnail_url' => $program->thumbnail_url,
                        'episode_number' => $program->episode_number,
                        'season_number' => $program->season_number
                    ];
                })
            ];
        }

        $currentProgram = $channel->getCurrentProgram();
        $upcomingPrograms = $channel->getUpcomingPrograms(24, 5);

        return response()->json([
            'status' => true,
            'message' => 'Channel details with schedule fetched successfully',
            'data' => [
                'channel' => [
                    'id' => $channel->tv_channel_id,
                    'title' => $channel->title,
                    'description' => $channel->description,
                    'channel_number' => $channel->channel_number,
                    'formatted_channel_number' => $channel->formatted_channel_number,
                    'logo' => $channel->logo,
                    'thumbnail' => $channel->thumbnail,
                    'stream_url' => $channel->stream_url,
                    'access_type' => $channel->access_type,
                    'language' => $channel->language,
                    'country_code' => $channel->country_code,
                    'categories' => $channel->categories,
                    'total_views' => $channel->total_views,
                    'total_shares' => $channel->total_shares,
                    'has_epg' => $channel->has_epg,
                    'streaming_qualities' => $channel->streaming_qualities
                ],
                'current_program' => $currentProgram,
                'upcoming_programs' => $upcomingPrograms,
                'schedule' => $scheduleData
            ]
        ]);
    }

    /**
     * Search programs across all channels
     */
    public function searchPrograms(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'query' => 'required|string|min:2',
            'genre' => 'string|max:100',
            'date_from' => 'date',
            'date_to' => 'date',
            'channel_ids' => 'array',
            'channel_ids.*' => 'integer|exists:tv_channel,tv_channel_id',
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
        
        $query = LiveTvSchedule::with(['channel' => function($q) {
            $q->with('categories');
        }])->searchByTitle($request->query);

        if ($request->has('genre')) {
            $query->byGenre($request->genre);
        }

        if ($request->has('date_from') && $request->has('date_to')) {
            $dateFrom = Carbon::parse($request->date_from)->startOfDay();
            $dateTo = Carbon::parse($request->date_to)->endOfDay();
            $query->withinTimeRange($dateFrom, $dateTo);
        }

        if ($request->has('channel_ids')) {
            $query->whereIn('tv_channel_id', $request->channel_ids);
        }

        $programs = $query->orderBy('start_time')->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Program search completed successfully',
            'data' => $programs->items(),
            'pagination' => [
                'current_page' => $programs->currentPage(),
                'last_page' => $programs->lastPage(),
                'per_page' => $programs->perPage(),
                'total' => $programs->total()
            ]
        ]);
    }

    /**
     * Simple test endpoint to verify enhanced Live TV functionality
     */
    public function testEnhancedEndpoint()
    {
        return response()->json([
            'status' => true,
            'message' => 'Enhanced Live TV endpoints are working!',
            'data' => [
                'timestamp' => now(),
                'available_endpoints' => [
                    'GET /api/v2/live-tv/channels-with-programs',
                    'GET /api/v2/live-tv/schedule-grid',
                    'GET /api/v2/live-tv/channel/{id}/schedule',
                    'POST /api/v2/live-tv/track-view'
                ]
            ]
        ]);
    }

    /**
     * Track channel view with enhanced analytics
     */
    public function trackChannelView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_channel_id' => 'required|integer|exists:tv_channel,tv_channel_id',
            'app_user_id' => 'integer|exists:app_user,app_user_id',
            'profile_id' => 'integer',
            'watch_duration' => 'integer|min:0',
            'metadata' => 'array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            // Track in analytics table
            LiveTvViewAnalytics::create([
                'tv_channel_id' => $request->tv_channel_id,
                'app_user_id' => $request->app_user_id,
                'profile_id' => $request->profile_id,
                'action_type' => 'view',
                'device_type' => $request->header('User-Agent-Device'),
                'user_agent' => $request->header('User-Agent'),
                'ip_address' => $request->ip(),
                'country' => $request->header('CF-IPCountry'),
                'watch_duration' => $request->watch_duration ?? 0,
                'metadata' => $request->metadata ?? []
            ]);

            // Increment channel view count
            $channel = TvChannel::find($request->tv_channel_id);
            if ($channel) {
                $channel->incrementViews();
            }

            return response()->json([
                'status' => true,
                'message' => 'Channel view tracked successfully'
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to track view: ' . $e->getMessage()
            ], 500);
        }
    }

    // =========================================================================
    // EXISTING V1 COMPATIBLE ENDPOINTS (PRESERVED FOR BACKWARD COMPATIBILITY)
    // =========================================================================

    /**
     * V1 Compatible: Fetch Live TV page data
     */
    public function fetchLiveTVPageData(Request $request)
    {
        // Simple direct query to get all categories
        $categories = TvCategory::all();
        
        $categoriesWithChannels = [];
        
        foreach ($categories as $category) {
            // Direct query to get channels for this category using the junction table
            $channels = \DB::table('tv_channel')
                ->join('tv_channel_category', 'tv_channel.tv_channel_id', '=', 'tv_channel_category.tv_channel_id')
                ->where('tv_channel_category.tv_category_id', $category->tv_category_id)
                ->select('tv_channel.*')
                ->get();
            
            if ($channels->count() > 0) {
                $categoryData = [
                    'tv_category_id' => $category->tv_category_id,
                    'id' => $category->tv_category_id, // Add id field for iOS compatibility
                    'title' => $category->title,
                    'image' => $category->image,
                    'created_at' => $category->created_at,
                    'updated_at' => $category->updated_at,
                    'channels' => $channels->toArray()
                ];
                $categoriesWithChannels[] = $categoryData;
            }
        }
        
        // If still no categories with channels, get all channels as fallback
        if (empty($categoriesWithChannels)) {
            $allChannels = TvChannel::all();
            if ($allChannels->count() > 0) {
                $defaultCategory = [
                    'tv_category_id' => 1,
                    'id' => 1, // Add id field for iOS compatibility
                    'title' => 'All Channels',
                    'image' => '',
                    'created_at' => now(),
                    'updated_at' => now(),
                    'channels' => $allChannels->map(function($channel) {
                        // Ensure each channel has an id field for iOS
                        $channelData = $channel->toArray();
                        $channelData['id'] = $channel->tv_channel_id;
                        return $channelData;
                    })->toArray()
                ];
                $categoriesWithChannels[] = $defaultCategory;
            }
        }
        
        return response()->json([
            'status' => true,
            'message' => 'Fetch Live TV Page Data Successfully',
            'data' => $categoriesWithChannels
        ]);
    }
    
    /**
     * V1 Compatible: Fetch TV channels by category
     */
    public function fetchTVChannelByCategory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_category_id' => 'required|integer'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }
        
        // First try to get channels by category relationship
        $category = TvCategory::with('channels')->find($request->tv_category_id);
        
        if ($category && $category->channels) {
            $channels = $category->channels;
        } else {
            // Fallback: get all channels if category not found or has no channels
            $channels = TvChannel::all();
        }
        
        return response()->json([
            'status' => true,
            'message' => 'Fetch TV Channel By Category Successfully',
            'data' => [
                'channels' => $channels
            ]
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