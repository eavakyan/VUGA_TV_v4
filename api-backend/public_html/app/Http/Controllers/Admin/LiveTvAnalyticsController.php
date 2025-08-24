<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\V2\LiveTvViewAnalytics;
use App\Models\V2\TvChannel;
use App\Models\V2\TvCategory;
use App\Models\V2\LiveTvSchedule;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class LiveTvAnalyticsController extends Controller
{
    public function index()
    {
        $channels = TvChannel::active()->orderBy('channel_number')->get();
        $categories = TvCategory::active()->ordered()->get();
        
        return view('admin.live-tv.analytics.index', compact('channels', 'categories'));
    }

    public function getDashboardStats(Request $request)
    {
        $dateRange = $request->get('date_range', '30'); // days
        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        // Total views and unique viewers
        $totalViews = LiveTvViewAnalytics::views()
            ->byDateRange($startDate, $endDate)
            ->count();

        $uniqueViewers = LiveTvViewAnalytics::views()
            ->byDateRange($startDate, $endDate)
            ->distinct('app_user_id')
            ->count();

        // Channel stats
        $totalChannels = TvChannel::count();
        $activeChannels = TvChannel::active()->count();

        // Top channels by views
        $topChannels = LiveTvViewAnalytics::views()
            ->byDateRange($startDate, $endDate)
            ->select('tv_channel_id', DB::raw('count(*) as view_count'))
            ->with('channel:tv_channel_id,title,thumbnail,channel_number')
            ->groupBy('tv_channel_id')
            ->orderBy('view_count', 'desc')
            ->limit(10)
            ->get();

        // Device breakdown
        $deviceStats = LiveTvViewAnalytics::views()
            ->byDateRange($startDate, $endDate)
            ->select('device_type', DB::raw('count(*) as count'))
            ->groupBy('device_type')
            ->get()
            ->pluck('count', 'device_type');

        // Country breakdown
        $countryStats = LiveTvViewAnalytics::views()
            ->byDateRange($startDate, $endDate)
            ->select('country', DB::raw('count(*) as count'))
            ->whereNotNull('country')
            ->groupBy('country')
            ->orderBy('count', 'desc')
            ->limit(10)
            ->get()
            ->pluck('count', 'country');

        // Daily views trend
        $dailyViews = LiveTvViewAnalytics::views()
            ->byDateRange($startDate, $endDate)
            ->select(DB::raw('DATE(created_at) as date'), DB::raw('count(*) as views'))
            ->groupBy(DB::raw('DATE(created_at)'))
            ->orderBy('date')
            ->get()
            ->pluck('views', 'date');

        // Peak viewing hours
        $hourlyViews = LiveTvViewAnalytics::views()
            ->byDateRange($startDate, $endDate)
            ->select(DB::raw('HOUR(created_at) as hour'), DB::raw('count(*) as views'))
            ->groupBy(DB::raw('HOUR(created_at)'))
            ->orderBy('hour')
            ->get()
            ->pluck('views', 'hour');

        return response()->json([
            'success' => true,
            'data' => [
                'overview' => [
                    'total_views' => $totalViews,
                    'unique_viewers' => $uniqueViewers,
                    'total_channels' => $totalChannels,
                    'active_channels' => $activeChannels,
                    'avg_views_per_user' => $uniqueViewers > 0 ? round($totalViews / $uniqueViewers, 2) : 0
                ],
                'top_channels' => $topChannels,
                'device_stats' => $deviceStats,
                'country_stats' => $countryStats,
                'daily_views' => $dailyViews,
                'hourly_views' => $hourlyViews,
                'date_range' => $dateRange
            ]
        ]);
    }

    public function getChannelAnalytics(Request $request, $channelId)
    {
        $dateRange = $request->get('date_range', '30');
        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $channel = TvChannel::findOrFail($channelId);

        // Channel views
        $totalViews = LiveTvViewAnalytics::views()
            ->byChannel($channelId)
            ->byDateRange($startDate, $endDate)
            ->count();

        $uniqueViewers = LiveTvViewAnalytics::views()
            ->byChannel($channelId)
            ->byDateRange($startDate, $endDate)
            ->distinct('app_user_id')
            ->count();

        // Watch duration stats
        $durationStats = LiveTvViewAnalytics::byChannel($channelId)
            ->byDateRange($startDate, $endDate)
            ->whereNotNull('watch_duration')
            ->select(
                DB::raw('AVG(watch_duration) as avg_duration'),
                DB::raw('SUM(watch_duration) as total_duration'),
                DB::raw('MIN(watch_duration) as min_duration'),
                DB::raw('MAX(watch_duration) as max_duration')
            )
            ->first();

        // Daily trend
        $dailyTrend = LiveTvViewAnalytics::views()
            ->byChannel($channelId)
            ->byDateRange($startDate, $endDate)
            ->select(DB::raw('DATE(created_at) as date'), DB::raw('count(*) as views'))
            ->groupBy(DB::raw('DATE(created_at)'))
            ->orderBy('date')
            ->get()
            ->pluck('views', 'date');

        // Device breakdown
        $deviceBreakdown = LiveTvViewAnalytics::views()
            ->byChannel($channelId)
            ->byDateRange($startDate, $endDate)
            ->select('device_type', DB::raw('count(*) as count'))
            ->groupBy('device_type')
            ->get()
            ->pluck('count', 'device_type');

        // Top programs (if schedule data exists)
        $topPrograms = LiveTvSchedule::where('tv_channel_id', $channelId)
            ->select('program_title', 'genre', DB::raw('count(*) as episode_count'))
            ->groupBy('program_title', 'genre')
            ->orderBy('episode_count', 'desc')
            ->limit(10)
            ->get();

        return response()->json([
            'success' => true,
            'data' => [
                'channel' => $channel,
                'overview' => [
                    'total_views' => $totalViews,
                    'unique_viewers' => $uniqueViewers,
                    'avg_watch_duration' => $durationStats->avg_duration ? round($durationStats->avg_duration / 60, 2) : 0, // Convert to minutes
                    'total_watch_time' => $durationStats->total_duration ? round($durationStats->total_duration / 3600, 2) : 0, // Convert to hours
                ],
                'daily_trend' => $dailyTrend,
                'device_breakdown' => $deviceBreakdown,
                'top_programs' => $topPrograms,
                'duration_stats' => $durationStats
            ]
        ]);
    }

    public function getPopularPrograms(Request $request)
    {
        $dateRange = $request->get('date_range', '30');
        $channelId = $request->get('channel_id');
        $genre = $request->get('genre');
        $limit = $request->get('limit', 20);

        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $query = LiveTvSchedule::with('channel:tv_channel_id,title,channel_number')
            ->whereBetween('start_time', [$startDate, $endDate]);

        if ($channelId) {
            $query->where('tv_channel_id', $channelId);
        }

        if ($genre) {
            $query->where('genre', $genre);
        }

        $popularPrograms = $query->select(
                'program_title',
                'genre',
                'tv_channel_id',
                DB::raw('count(*) as air_count'),
                DB::raw('AVG(TIMESTAMPDIFF(MINUTE, start_time, end_time)) as avg_duration'),
                DB::raw('MIN(start_time) as first_aired'),
                DB::raw('MAX(start_time) as last_aired')
            )
            ->groupBy('program_title', 'genre', 'tv_channel_id')
            ->orderBy('air_count', 'desc')
            ->limit($limit)
            ->get();

        return response()->json([
            'success' => true,
            'data' => $popularPrograms
        ]);
    }

    public function getPeakViewingTimes(Request $request)
    {
        $dateRange = $request->get('date_range', '30');
        $channelId = $request->get('channel_id');
        
        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $query = LiveTvViewAnalytics::views()->byDateRange($startDate, $endDate);

        if ($channelId) {
            $query->byChannel($channelId);
        }

        // Hourly breakdown
        $hourlyStats = $query->select(
                DB::raw('HOUR(created_at) as hour'),
                DB::raw('count(*) as views'),
                DB::raw('count(distinct app_user_id) as unique_viewers')
            )
            ->groupBy(DB::raw('HOUR(created_at)'))
            ->orderBy('hour')
            ->get()
            ->map(function ($item) {
                $item->hour_label = Carbon::createFromTime($item->hour)->format('H:i');
                return $item;
            });

        // Day of week breakdown
        $dayOfWeekStats = $query->select(
                DB::raw('DAYOFWEEK(created_at) as day_of_week'),
                DB::raw('count(*) as views'),
                DB::raw('count(distinct app_user_id) as unique_viewers')
            )
            ->groupBy(DB::raw('DAYOFWEEK(created_at)'))
            ->orderBy('day_of_week')
            ->get()
            ->map(function ($item) {
                $dayNames = ['', 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
                $item->day_name = $dayNames[$item->day_of_week];
                return $item;
            });

        return response()->json([
            'success' => true,
            'data' => [
                'hourly_stats' => $hourlyStats,
                'day_of_week_stats' => $dayOfWeekStats
            ]
        ]);
    }

    public function getGeographicDistribution(Request $request)
    {
        $dateRange = $request->get('date_range', '30');
        $channelId = $request->get('channel_id');
        
        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $query = LiveTvViewAnalytics::views()->byDateRange($startDate, $endDate);

        if ($channelId) {
            $query->byChannel($channelId);
        }

        $countryStats = $query->select(
                'country',
                DB::raw('count(*) as views'),
                DB::raw('count(distinct app_user_id) as unique_viewers')
            )
            ->whereNotNull('country')
            ->groupBy('country')
            ->orderBy('views', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $countryStats
        ]);
    }

    public function getViewerRetention(Request $request)
    {
        $dateRange = $request->get('date_range', '30');
        $channelId = $request->get('channel_id');
        
        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $query = LiveTvViewAnalytics::byDateRange($startDate, $endDate);

        if ($channelId) {
            $query->byChannel($channelId);
        }

        // Average watch duration by user
        $retentionStats = $query->select(
                'app_user_id',
                DB::raw('count(*) as session_count'),
                DB::raw('AVG(watch_duration) as avg_duration'),
                DB::raw('SUM(watch_duration) as total_duration')
            )
            ->whereNotNull('watch_duration')
            ->whereNotNull('app_user_id')
            ->groupBy('app_user_id')
            ->get();

        // Categorize users by engagement level
        $engagementLevels = [
            'high' => $retentionStats->where('avg_duration', '>', 1800)->count(), // > 30 minutes
            'medium' => $retentionStats->whereBetween('avg_duration', [600, 1800])->count(), // 10-30 minutes
            'low' => $retentionStats->where('avg_duration', '<', 600)->count(), // < 10 minutes
        ];

        // Return rate (users who watched more than once)
        $totalUsers = $retentionStats->count();
        $returningUsers = $retentionStats->where('session_count', '>', 1)->count();
        $returnRate = $totalUsers > 0 ? round(($returningUsers / $totalUsers) * 100, 2) : 0;

        return response()->json([
            'success' => true,
            'data' => [
                'engagement_levels' => $engagementLevels,
                'return_rate' => $returnRate,
                'total_users' => $totalUsers,
                'returning_users' => $returningUsers,
                'avg_sessions_per_user' => $totalUsers > 0 ? round($retentionStats->avg('session_count'), 2) : 0,
                'avg_total_watch_time' => $retentionStats->avg('total_duration') ? round($retentionStats->avg('total_duration') / 3600, 2) : 0 // hours
            ]
        ]);
    }

    public function exportAnalytics(Request $request)
    {
        $dateRange = $request->get('date_range', '30');
        $channelId = $request->get('channel_id');
        $format = $request->get('format', 'csv'); // csv or json
        
        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $query = LiveTvViewAnalytics::with('channel:tv_channel_id,title,channel_number')
            ->byDateRange($startDate, $endDate);

        if ($channelId) {
            $query->byChannel($channelId);
        }

        $analytics = $query->select([
                'tv_channel_id',
                'app_user_id',
                'action_type',
                'device_type',
                'country',
                'watch_duration',
                'created_at'
            ])
            ->get();

        $filename = 'live_tv_analytics_' . $startDate->format('Y-m-d') . '_to_' . $endDate->format('Y-m-d');

        if ($format === 'csv') {
            $headers = [
                'Channel ID', 'Channel Name', 'User ID', 'Action Type', 
                'Device Type', 'Country', 'Watch Duration (seconds)', 'Timestamp'
            ];
            
            $filename .= '.csv';
            $handle = fopen('php://temp', 'r+');
            fputcsv($handle, $headers);
            
            foreach ($analytics as $row) {
                fputcsv($handle, [
                    $row->tv_channel_id,
                    $row->channel ? $row->channel->title : 'Unknown',
                    $row->app_user_id,
                    $row->action_type,
                    $row->device_type,
                    $row->country,
                    $row->watch_duration,
                    $row->created_at->format('Y-m-d H:i:s')
                ]);
            }
            
            rewind($handle);
            $content = stream_get_contents($handle);
            fclose($handle);
            
            return response($content)
                ->header('Content-Type', 'text/csv')
                ->header('Content-Disposition', "attachment; filename=\"{$filename}\"");
                
        } else {
            $filename .= '.json';
            $data = $analytics->toArray();
            
            return response(json_encode($data, JSON_PRETTY_PRINT))
                ->header('Content-Type', 'application/json')
                ->header('Content-Disposition', "attachment; filename=\"{$filename}\"");
        }
    }

    public function getGenreAnalytics(Request $request)
    {
        $dateRange = $request->get('date_range', '30');
        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $genreStats = LiveTvSchedule::whereBetween('start_time', [$startDate, $endDate])
            ->select(
                'genre',
                DB::raw('count(*) as program_count'),
                DB::raw('AVG(TIMESTAMPDIFF(MINUTE, start_time, end_time)) as avg_duration'),
                DB::raw('count(distinct tv_channel_id) as channel_count')
            )
            ->whereNotNull('genre')
            ->groupBy('genre')
            ->orderBy('program_count', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $genreStats
        ]);
    }

    public function getChannelComparison(Request $request)
    {
        $dateRange = $request->get('date_range', '30');
        $channelIds = $request->get('channel_ids', []); // Array of channel IDs to compare
        
        if (empty($channelIds)) {
            // Default to top 5 channels if none specified
            $channelIds = TvChannel::active()
                ->orderBy('total_views', 'desc')
                ->limit(5)
                ->pluck('tv_channel_id')
                ->toArray();
        }

        $startDate = Carbon::now()->subDays($dateRange);
        $endDate = Carbon::now();

        $comparisonData = [];

        foreach ($channelIds as $channelId) {
            $channel = TvChannel::find($channelId);
            if (!$channel) continue;

            $views = LiveTvViewAnalytics::views()
                ->byChannel($channelId)
                ->byDateRange($startDate, $endDate)
                ->count();

            $uniqueViewers = LiveTvViewAnalytics::views()
                ->byChannel($channelId)
                ->byDateRange($startDate, $endDate)
                ->distinct('app_user_id')
                ->count();

            $avgDuration = LiveTvViewAnalytics::byChannel($channelId)
                ->byDateRange($startDate, $endDate)
                ->whereNotNull('watch_duration')
                ->avg('watch_duration');

            $comparisonData[] = [
                'channel_id' => $channelId,
                'channel_name' => $channel->title,
                'channel_number' => $channel->channel_number,
                'views' => $views,
                'unique_viewers' => $uniqueViewers,
                'avg_duration_minutes' => $avgDuration ? round($avgDuration / 60, 2) : 0,
                'engagement_rate' => $uniqueViewers > 0 ? round($views / $uniqueViewers, 2) : 0
            ];
        }

        return response()->json([
            'success' => true,
            'data' => collect($comparisonData)->sortBy('views', SORT_REGULAR, true)->values()
        ]);
    }
}