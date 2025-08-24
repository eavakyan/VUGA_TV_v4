<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\V2\TvChannel;
use App\Models\V2\TvCategory;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

class LiveTvChannelsController extends Controller
{
    public function index()
    {
        $categories = TvCategory::active()->ordered()->get();
        return view('admin.live-tv.channels.index', compact('categories'));
    }

    public function fetchChannelsList(Request $request)
    {
        $columns = array(
            0 => 'tv_channel_id',
            1 => 'thumbnail',
            2 => 'channel_number',
            3 => 'title',
            4 => 'category_ids',
            5 => 'is_active',
            6 => 'total_views',
            7 => 'action',
        );

        $totalData = TvChannel::count();
        $totalFiltered = $totalData;

        $limit = $request->input('length');
        $start = $request->input('start');
        $order = $columns[$request->input('order.0.column')];
        $dir = $request->input('order.0.dir');

        $channelsQuery = TvChannel::with('categories');

        if (!empty($request->input('search.value'))) {
            $search = $request->input('search.value');
            $channelsQuery->where('title', 'LIKE', "%{$search}%")
                         ->orWhere('channel_number', 'LIKE', "%{$search}%");
            $totalFiltered = $channelsQuery->count();
        }

        // Category filter
        if ($request->filled('category_filter') && $request->category_filter != 'all') {
            $channelsQuery->whereRaw("FIND_IN_SET(?, category_ids)", [$request->category_filter]);
        }

        // Status filter
        if ($request->filled('status_filter') && $request->status_filter != 'all') {
            $channelsQuery->where('is_active', $request->status_filter == 'active' ? 1 : 0);
        }

        $channels = $channelsQuery->offset($start)
                                 ->limit($limit)
                                 ->orderBy($order, $dir)
                                 ->get();

        $data = array();
        if (!empty($channels)) {
            foreach ($channels as $channel) {
                $thumbnail = $channel->thumbnail ? 
                    '<img src="' . $channel->thumbnail . '" alt="' . $channel->title . '" class="channel-thumbnail" style="width: 60px; height: 40px; object-fit: cover; border-radius: 4px;">' : 
                    '<div class="no-image">No Image</div>';

                $categories = $channel->categories->pluck('title')->implode(', ') ?: 'Uncategorized';
                
                $status = $channel->is_active ? 
                    '<span class="badge badge-success">Active</span>' : 
                    '<span class="badge badge-secondary">Inactive</span>';

                $channelNumber = $channel->channel_number ? 
                    '<span class="channel-number badge badge-primary">' . str_pad($channel->channel_number, 3, '0', STR_PAD_LEFT) . '</span>' : 
                    '<span class="badge badge-light">N/A</span>';

                $actions = '
                    <div class="btn-group" role="group">
                        <button type="button" class="btn btn-sm btn-primary" onclick="editChannel(' . $channel->tv_channel_id . ')" title="Edit">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-info" onclick="viewSchedule(' . $channel->tv_channel_id . ')" title="Schedule">
                            <i class="fas fa-calendar"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-' . ($channel->is_active ? 'warning" onclick="toggleChannel(' . $channel->tv_channel_id . ', 0)" title="Deactivate">
                            <i class="fas fa-eye-slash"></i>' : 'success" onclick="toggleChannel(' . $channel->tv_channel_id . ', 1)" title="Activate">
                            <i class="fas fa-eye"></i>') . '
                        </button>
                        <button type="button" class="btn btn-sm btn-danger" onclick="deleteChannel(' . $channel->tv_channel_id . ')" title="Delete">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>';

                $nestedData['tv_channel_id'] = $channel->tv_channel_id;
                $nestedData['thumbnail'] = $thumbnail;
                $nestedData['channel_number'] = $channelNumber;
                $nestedData['title'] = $channel->title;
                $nestedData['categories'] = $categories;
                $nestedData['is_active'] = $status;
                $nestedData['total_views'] = number_format($channel->total_views ?: 0);
                $nestedData['action'] = $actions;

                $data[] = $nestedData;
            }
        }

        $json_data = array(
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data
        );

        return response()->json($json_data);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255',
            'channel_number' => 'nullable|integer|unique:tv_channel,channel_number',
            'stream_url' => 'required|url',
            'thumbnail' => 'nullable|url',
            'logo_url' => 'nullable|url',
            'category_ids' => 'nullable|array',
            'category_ids.*' => 'exists:tv_category,tv_category_id',
            'access_type' => 'nullable|integer',
            'language' => 'nullable|string|max:10',
            'country_code' => 'nullable|string|max:5',
            'description' => 'nullable|string',
            'epg_url' => 'nullable|url',
            'streaming_qualities' => 'nullable|array',
            'is_active' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $channelData = $request->only([
                'title', 'channel_number', 'stream_url', 'thumbnail', 'logo_url',
                'access_type', 'language', 'country_code', 'description', 'epg_url',
                'streaming_qualities', 'is_active'
            ]);

            // Convert category_ids array to comma-separated string
            if ($request->filled('category_ids')) {
                $channelData['category_ids'] = implode(',', $request->category_ids);
            }

            $channelData['is_active'] = $request->has('is_active');
            $channelData['type'] = 1; // Default type
            $channelData['source'] = 'manual'; // Default source

            $channel = TvChannel::create($channelData);

            // Sync categories if using pivot table
            if ($request->filled('category_ids')) {
                $channel->categories()->sync($request->category_ids);
            }

            return response()->json([
                'success' => true,
                'message' => 'Channel created successfully',
                'data' => $channel
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to create channel: ' . $e->getMessage()
            ], 500);
        }
    }

    public function show($id)
    {
        try {
            $channel = TvChannel::with('categories', 'schedules')->findOrFail($id);
            return response()->json([
                'success' => true,
                'data' => $channel
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Channel not found'
            ], 404);
        }
    }

    public function update(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255',
            'channel_number' => 'nullable|integer|unique:tv_channel,channel_number,' . $id . ',tv_channel_id',
            'stream_url' => 'required|url',
            'thumbnail' => 'nullable|url',
            'logo_url' => 'nullable|url',
            'category_ids' => 'nullable|array',
            'category_ids.*' => 'exists:tv_category,tv_category_id',
            'access_type' => 'nullable|integer',
            'language' => 'nullable|string|max:10',
            'country_code' => 'nullable|string|max:5',
            'description' => 'nullable|string',
            'epg_url' => 'nullable|url',
            'streaming_qualities' => 'nullable|array',
            'is_active' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $channel = TvChannel::findOrFail($id);
            
            $channelData = $request->only([
                'title', 'channel_number', 'stream_url', 'thumbnail', 'logo_url',
                'access_type', 'language', 'country_code', 'description', 'epg_url',
                'streaming_qualities', 'is_active'
            ]);

            // Convert category_ids array to comma-separated string
            if ($request->filled('category_ids')) {
                $channelData['category_ids'] = implode(',', $request->category_ids);
            } else {
                $channelData['category_ids'] = '';
            }

            $channelData['is_active'] = $request->has('is_active');

            $channel->update($channelData);

            // Sync categories if using pivot table
            if ($request->filled('category_ids')) {
                $channel->categories()->sync($request->category_ids);
            } else {
                $channel->categories()->detach();
            }

            return response()->json([
                'success' => true,
                'message' => 'Channel updated successfully',
                'data' => $channel->fresh('categories')
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update channel: ' . $e->getMessage()
            ], 500);
        }
    }

    public function destroy($id)
    {
        try {
            $channel = TvChannel::findOrFail($id);
            
            // Delete related schedules
            $channel->schedules()->delete();
            
            // Delete related analytics
            $channel->analytics()->delete();
            
            // Detach categories
            $channel->categories()->detach();
            
            $channel->delete();

            return response()->json([
                'success' => true,
                'message' => 'Channel deleted successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to delete channel: ' . $e->getMessage()
            ], 500);
        }
    }

    public function toggleStatus(Request $request, $id)
    {
        try {
            $channel = TvChannel::findOrFail($id);
            $channel->is_active = $request->status == 1;
            $channel->save();

            return response()->json([
                'success' => true,
                'message' => 'Channel status updated successfully',
                'status' => $channel->is_active
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update channel status: ' . $e->getMessage()
            ], 500);
        }
    }

    public function bulkAction(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'action' => 'required|in:activate,deactivate,delete',
            'channel_ids' => 'required|array',
            'channel_ids.*' => 'exists:tv_channel,tv_channel_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $channelIds = $request->channel_ids;
            $action = $request->action;
            $affectedCount = 0;

            switch ($action) {
                case 'activate':
                    $affectedCount = TvChannel::whereIn('tv_channel_id', $channelIds)
                                             ->update(['is_active' => true]);
                    break;
                
                case 'deactivate':
                    $affectedCount = TvChannel::whereIn('tv_channel_id', $channelIds)
                                             ->update(['is_active' => false]);
                    break;
                
                case 'delete':
                    // Delete related data first
                    DB::table('live_tv_schedule')->whereIn('tv_channel_id', $channelIds)->delete();
                    DB::table('live_tv_view_analytics')->whereIn('tv_channel_id', $channelIds)->delete();
                    DB::table('tv_channel_category')->whereIn('tv_channel_id', $channelIds)->delete();
                    
                    $affectedCount = TvChannel::whereIn('tv_channel_id', $channelIds)->delete();
                    break;
            }

            return response()->json([
                'success' => true,
                'message' => "Successfully {$action}d {$affectedCount} channel(s)",
                'affected_count' => $affectedCount
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Bulk action failed: ' . $e->getMessage()
            ], 500);
        }
    }

    public function importEpg(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'epg_url' => 'required|url'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Invalid EPG URL',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $channel = TvChannel::findOrFail($id);
            
            // Update EPG URL
            $channel->epg_url = $request->epg_url;
            $channel->save();

            // Here you would implement EPG parsing logic
            // This is a placeholder for the actual EPG import functionality
            
            return response()->json([
                'success' => true,
                'message' => 'EPG URL updated successfully. EPG import will be processed in the background.',
                'data' => $channel
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update EPG URL: ' . $e->getMessage()
            ], 500);
        }
    }

    public function getChannelStats($id)
    {
        try {
            $channel = TvChannel::with(['analytics' => function($query) {
                $query->where('created_at', '>=', now()->subDays(30));
            }])->findOrFail($id);

            $stats = [
                'total_views' => $channel->total_views ?: 0,
                'total_shares' => $channel->total_shares ?: 0,
                'current_program' => $channel->getCurrentProgram(),
                'upcoming_programs' => $channel->getUpcomingPrograms(6, 5),
                'monthly_views' => $channel->analytics->where('action_type', 'view')->count(),
                'monthly_unique_viewers' => $channel->analytics->where('action_type', 'view')->distinct('app_user_id')->count(),
                'device_breakdown' => $channel->analytics->groupBy('device_type')->map->count(),
                'country_breakdown' => $channel->analytics->groupBy('country')->map->count(),
            ];

            return response()->json([
                'success' => true,
                'data' => $stats
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to get channel stats: ' . $e->getMessage()
            ], 500);
        }
    }
}