<?php

namespace App\Http\Controllers\Api\V2\Admin;

use App\Http\Controllers\Controller;
use App\Models\V2\TvChannel;
use App\Models\V2\TvCategory;
use App\Models\V2\LiveTvSchedule;
use App\Models\V2\LiveTvViewAnalytics;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class LiveTvAdminController extends Controller
{
    // =========================================================================
    // CHANNEL MANAGEMENT
    // =========================================================================

    /**
     * Get all channels for admin
     */
    public function getChannels(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
            'search' => 'string|max:255',
            'category_id' => 'integer|exists:tv_category,tv_category_id',
            'is_active' => 'boolean',
            'language' => 'string|max:10'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $perPage = $request->per_page ?? 20;
        
        $query = TvChannel::with(['categories']);

        if ($request->has('search')) {
            $query->where('title', 'LIKE', '%' . $request->search . '%');
        }

        if ($request->has('category_id')) {
            $query->whereHas('categories', function($q) use ($request) {
                $q->where('tv_category.tv_category_id', $request->category_id);
            });
        }

        if ($request->has('is_active')) {
            $query->where('is_active', $request->boolean('is_active'));
        }

        if ($request->has('language')) {
            $query->byLanguage($request->language);
        }

        $channels = $query->orderBy('channel_number')->paginate($perPage);

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
     * Create new channel
     */
    public function createChannel(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:100',
            'channel_number' => 'nullable|integer|unique:tv_channel,channel_number',
            'description' => 'nullable|string',
            'thumbnail' => 'nullable|string|max:500',
            'logo_url' => 'nullable|string|max:500',
            'stream_url' => 'nullable|string|max:500',
            'access_type' => 'required|integer|in:1,2,3',
            'type' => 'required|integer|in:1,2',
            'source' => 'nullable|string',
            'epg_url' => 'nullable|string|max:500',
            'language' => 'required|string|max:10',
            'country_code' => 'nullable|string|max:5',
            'streaming_qualities' => 'nullable|array',
            'category_ids' => 'required|array',
            'category_ids.*' => 'integer|exists:tv_category,tv_category_id',
            'is_active' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            DB::beginTransaction();

            $channel = TvChannel::create([
                'title' => $request->title,
                'channel_number' => $request->channel_number,
                'description' => $request->description,
                'thumbnail' => $request->thumbnail,
                'logo_url' => $request->logo_url,
                'stream_url' => $request->stream_url,
                'access_type' => $request->access_type,
                'type' => $request->type,
                'source' => $request->source,
                'epg_url' => $request->epg_url,
                'language' => $request->language,
                'country_code' => $request->country_code,
                'streaming_qualities' => $request->streaming_qualities,
                'category_ids' => implode(',', $request->category_ids),
                'is_active' => $request->boolean('is_active', true)
            ]);

            // Attach categories
            $channel->categories()->attach($request->category_ids);

            DB::commit();

            return response()->json([
                'status' => true,
                'message' => 'Channel created successfully',
                'data' => $channel->load('categories')
            ]);
        } catch (\Exception $e) {
            DB::rollback();
            return response()->json([
                'status' => false,
                'message' => 'Failed to create channel: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Update channel
     */
    public function updateChannel(Request $request, $channelId)
    {
        $validator = Validator::make(array_merge($request->all(), ['channel_id' => $channelId]), [
            'channel_id' => 'required|integer|exists:tv_channel,tv_channel_id',
            'title' => 'required|string|max:100',
            'channel_number' => 'nullable|integer|unique:tv_channel,channel_number,' . $channelId . ',tv_channel_id',
            'description' => 'nullable|string',
            'thumbnail' => 'nullable|string|max:500',
            'logo_url' => 'nullable|string|max:500',
            'stream_url' => 'nullable|string|max:500',
            'access_type' => 'required|integer|in:1,2,3',
            'type' => 'required|integer|in:1,2',
            'source' => 'nullable|string',
            'epg_url' => 'nullable|string|max:500',
            'language' => 'required|string|max:10',
            'country_code' => 'nullable|string|max:5',
            'streaming_qualities' => 'nullable|array',
            'category_ids' => 'required|array',
            'category_ids.*' => 'integer|exists:tv_category,tv_category_id',
            'is_active' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            DB::beginTransaction();

            $channel = TvChannel::find($channelId);
            $channel->update([
                'title' => $request->title,
                'channel_number' => $request->channel_number,
                'description' => $request->description,
                'thumbnail' => $request->thumbnail,
                'logo_url' => $request->logo_url,
                'stream_url' => $request->stream_url,
                'access_type' => $request->access_type,
                'type' => $request->type,
                'source' => $request->source,
                'epg_url' => $request->epg_url,
                'language' => $request->language,
                'country_code' => $request->country_code,
                'streaming_qualities' => $request->streaming_qualities,
                'category_ids' => implode(',', $request->category_ids),
                'is_active' => $request->boolean('is_active', true)
            ]);

            // Sync categories
            $channel->categories()->sync($request->category_ids);

            DB::commit();

            return response()->json([
                'status' => true,
                'message' => 'Channel updated successfully',
                'data' => $channel->load('categories')
            ]);
        } catch (\Exception $e) {
            DB::rollback();
            return response()->json([
                'status' => false,
                'message' => 'Failed to update channel: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Delete channel
     */
    public function deleteChannel($channelId)
    {
        $channel = TvChannel::find($channelId);
        
        if (!$channel) {
            return response()->json([
                'status' => false,
                'message' => 'Channel not found'
            ], 404);
        }

        try {
            DB::beginTransaction();
            
            // Detach categories
            $channel->categories()->detach();
            
            // Delete channel (schedules and analytics will be deleted by cascade)
            $channel->delete();
            
            DB::commit();

            return response()->json([
                'status' => true,
                'message' => 'Channel deleted successfully'
            ]);
        } catch (\Exception $e) {
            DB::rollback();
            return response()->json([
                'status' => false,
                'message' => 'Failed to delete channel: ' . $e->getMessage()
            ], 500);
        }
    }

    // =========================================================================
    // SCHEDULE MANAGEMENT
    // =========================================================================

    /**
     * Get schedule for a channel
     */
    public function getChannelSchedule($channelId, Request $request)
    {
        $validator = Validator::make(array_merge($request->all(), ['channel_id' => $channelId]), [
            'channel_id' => 'required|integer|exists:tv_channel,tv_channel_id',
            'date_from' => 'date_format:Y-m-d',
            'date_to' => 'date_format:Y-m-d',
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $perPage = $request->per_page ?? 50;
        
        $query = LiveTvSchedule::forChannel($channelId);

        if ($request->has('date_from') && $request->has('date_to')) {
            $dateFrom = Carbon::parse($request->date_from)->startOfDay();
            $dateTo = Carbon::parse($request->date_to)->endOfDay();
            $query->withinTimeRange($dateFrom, $dateTo);
        }

        $schedules = $query->orderBy('start_time')->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Channel schedule fetched successfully',
            'data' => $schedules->items(),
            'pagination' => [
                'current_page' => $schedules->currentPage(),
                'last_page' => $schedules->lastPage(),
                'per_page' => $schedules->perPage(),
                'total' => $schedules->total()
            ]
        ]);
    }

    /**
     * Create schedule entry
     */
    public function createScheduleEntry(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_channel_id' => 'required|integer|exists:tv_channel,tv_channel_id',
            'program_title' => 'required|string|max:200',
            'description' => 'nullable|string',
            'thumbnail_url' => 'nullable|string|max:500',
            'genre' => 'nullable|string|max:100',
            'start_time' => 'required|date_format:Y-m-d H:i:s',
            'end_time' => 'required|date_format:Y-m-d H:i:s|after:start_time',
            'is_repeat' => 'boolean',
            'episode_number' => 'nullable|string|max:20',
            'season_number' => 'nullable|string|max:20',
            'original_air_year' => 'nullable|integer|min:1900|max:' . (date('Y') + 10),
            'rating' => 'nullable|string|max:10',
            'metadata' => 'nullable|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        // Check for overlapping schedules
        $overlapping = LiveTvSchedule::forChannel($request->tv_channel_id)
            ->withinTimeRange($request->start_time, $request->end_time)
            ->count();

        if ($overlapping > 0) {
            return response()->json([
                'status' => false,
                'message' => 'Schedule overlaps with existing program for this channel'
            ], 400);
        }

        try {
            $schedule = LiveTvSchedule::create($request->all());

            return response()->json([
                'status' => true,
                'message' => 'Schedule entry created successfully',
                'data' => $schedule
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to create schedule entry: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Bulk import schedule from CSV/JSON
     */
    public function bulkImportSchedule(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_channel_id' => 'required|integer|exists:tv_channel,tv_channel_id',
            'format' => 'required|in:csv,json',
            'data' => 'required_if:format,json|array',
            'file' => 'required_if:format,csv|file|mimes:csv,txt',
            'clear_existing' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            DB::beginTransaction();

            $channelId = $request->tv_channel_id;
            $scheduleData = [];

            if ($request->format === 'csv') {
                // Process CSV file
                $file = $request->file('file');
                $csv = array_map('str_getcsv', file($file->getPathname()));
                $header = array_shift($csv);
                
                foreach ($csv as $row) {
                    $scheduleData[] = array_combine($header, $row);
                }
            } else {
                // Use JSON data
                $scheduleData = $request->data;
            }

            // Clear existing schedule if requested
            if ($request->boolean('clear_existing')) {
                LiveTvSchedule::forChannel($channelId)->delete();
            }

            $imported = 0;
            $errors = [];

            foreach ($scheduleData as $index => $entry) {
                try {
                    $entry['tv_channel_id'] = $channelId;
                    
                    // Validate each entry
                    $entryValidator = Validator::make($entry, [
                        'program_title' => 'required|string|max:200',
                        'start_time' => 'required|date_format:Y-m-d H:i:s',
                        'end_time' => 'required|date_format:Y-m-d H:i:s|after:start_time',
                        'description' => 'nullable|string',
                        'genre' => 'nullable|string|max:100',
                        'rating' => 'nullable|string|max:10'
                    ]);

                    if ($entryValidator->fails()) {
                        $errors[] = "Row " . ($index + 1) . ": " . $entryValidator->errors()->first();
                        continue;
                    }

                    LiveTvSchedule::create($entry);
                    $imported++;
                } catch (\Exception $e) {
                    $errors[] = "Row " . ($index + 1) . ": " . $e->getMessage();
                }
            }

            DB::commit();

            return response()->json([
                'status' => true,
                'message' => "Schedule import completed. Imported {$imported} entries.",
                'data' => [
                    'imported_count' => $imported,
                    'total_count' => count($scheduleData),
                    'errors' => $errors
                ]
            ]);
        } catch (\Exception $e) {
            DB::rollback();
            return response()->json([
                'status' => false,
                'message' => 'Failed to import schedule: ' . $e->getMessage()
            ], 500);
        }
    }

    // =========================================================================
    // ANALYTICS
    // =========================================================================

    /**
     * Get analytics overview
     */
    public function getAnalyticsOverview(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'date_from' => 'date',
            'date_to' => 'date|after_or_equal:date_from',
            'channel_id' => 'integer|exists:tv_channel,tv_channel_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $dateFrom = $request->date_from ? Carbon::parse($request->date_from) : Carbon::now()->subDays(30);
        $dateTo = $request->date_to ? Carbon::parse($request->date_to) : Carbon::now();

        $baseQuery = LiveTvViewAnalytics::byDateRange($dateFrom, $dateTo);
        
        if ($request->has('channel_id')) {
            $baseQuery->byChannel($request->channel_id);
        }

        $totalViews = (clone $baseQuery)->views()->count();
        $totalShares = (clone $baseQuery)->shares()->count();
        $uniqueUsers = (clone $baseQuery)->whereNotNull('app_user_id')->distinct('app_user_id')->count();

        // Top channels by views
        $topChannels = (clone $baseQuery)
            ->views()
            ->select('tv_channel_id', DB::raw('COUNT(*) as view_count'))
            ->with('channel:tv_channel_id,title,channel_number')
            ->groupBy('tv_channel_id')
            ->orderBy('view_count', 'desc')
            ->limit(10)
            ->get();

        // Views by device type
        $deviceStats = (clone $baseQuery)
            ->views()
            ->select('device_type', DB::raw('COUNT(*) as view_count'))
            ->groupBy('device_type')
            ->orderBy('view_count', 'desc')
            ->get();

        // Daily view trends
        $dailyViews = (clone $baseQuery)
            ->views()
            ->select(DB::raw('DATE(created_at) as date'), DB::raw('COUNT(*) as views'))
            ->groupBy(DB::raw('DATE(created_at)'))
            ->orderBy('date')
            ->get();

        return response()->json([
            'status' => true,
            'message' => 'Analytics overview fetched successfully',
            'data' => [
                'summary' => [
                    'total_views' => $totalViews,
                    'total_shares' => $totalShares,
                    'unique_users' => $uniqueUsers,
                    'date_range' => [
                        'from' => $dateFrom->format('Y-m-d'),
                        'to' => $dateTo->format('Y-m-d')
                    ]
                ],
                'top_channels' => $topChannels,
                'device_stats' => $deviceStats,
                'daily_trends' => $dailyViews
            ]
        ]);
    }
}