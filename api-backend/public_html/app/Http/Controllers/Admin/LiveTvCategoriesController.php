<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\V2\TvCategory;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class LiveTvCategoriesController extends Controller
{
    public function index()
    {
        return view('admin.live-tv.categories.index');
    }

    public function fetchCategoriesList(Request $request)
    {
        $columns = array(
            0 => 'tv_category_id',
            1 => 'image',
            2 => 'title',
            3 => 'channels_count',
            4 => 'sort_order',
            5 => 'is_active',
            6 => 'action',
        );

        $totalData = TvCategory::count();
        $totalFiltered = $totalData;

        $limit = $request->input('length');
        $start = $request->input('start');
        $order = $columns[$request->input('order.0.column')];
        $dir = $request->input('order.0.dir');

        $categoriesQuery = TvCategory::withCount('channels');

        if (!empty($request->input('search.value'))) {
            $search = $request->input('search.value');
            $categoriesQuery->where('title', 'LIKE', "%{$search}%")
                           ->orWhere('description', 'LIKE', "%{$search}%");
            $totalFiltered = $categoriesQuery->count();
        }

        // Status filter
        if ($request->filled('status_filter') && $request->status_filter != 'all') {
            $categoriesQuery->where('is_active', $request->status_filter == 'active' ? 1 : 0);
        }

        $categories = $categoriesQuery->offset($start)
                                    ->limit($limit)
                                    ->orderBy($order, $dir)
                                    ->get();

        $data = array();
        if (!empty($categories)) {
            foreach ($categories as $category) {
                $image = $category->image ? 
                    '<img src="' . $category->image . '" alt="' . $category->title . '" class="category-image" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">' : 
                    '<div class="no-image">No Image</div>';

                $status = $category->is_active ? 
                    '<span class="badge badge-success">Active</span>' : 
                    '<span class="badge badge-secondary">Inactive</span>';

                $actions = '
                    <div class="btn-group" role="group">
                        <button type="button" class="btn btn-sm btn-primary" onclick="editCategory(' . $category->tv_category_id . ')" title="Edit">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-info" onclick="viewChannels(' . $category->tv_category_id . ')" title="View Channels">
                            <i class="fas fa-list"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-' . ($category->is_active ? 'warning" onclick="toggleCategory(' . $category->tv_category_id . ', 0)" title="Deactivate">
                            <i class="fas fa-eye-slash"></i>' : 'success" onclick="toggleCategory(' . $category->tv_category_id . ', 1)" title="Activate">
                            <i class="fas fa-eye"></i>') . '
                        </button>
                        <button type="button" class="btn btn-sm btn-danger" onclick="deleteCategory(' . $category->tv_category_id . ')" title="Delete">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>';

                $nestedData['tv_category_id'] = $category->tv_category_id;
                $nestedData['image'] = $image;
                $nestedData['title'] = $category->title;
                $nestedData['channels_count'] = $category->channels_count . ' channels';
                $nestedData['sort_order'] = '<span class="sort-handle badge badge-info" data-id="' . $category->tv_category_id . '">' . ($category->sort_order ?: 0) . '</span>';
                $nestedData['is_active'] = $status;
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

    public function getSortedCategories()
    {
        $categories = TvCategory::ordered()->get(['tv_category_id', 'title', 'sort_order', 'is_active']);
        return response()->json([
            'success' => true,
            'data' => $categories
        ]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255|unique:tv_category,title',
            'slug' => 'nullable|string|max:255|unique:tv_category,slug',
            'image' => 'nullable|url',
            'icon_url' => 'nullable|url',
            'description' => 'nullable|string',
            'sort_order' => 'nullable|integer|min:0',
            'is_active' => 'boolean',
            'metadata' => 'nullable|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $categoryData = $request->only([
                'title', 'slug', 'image', 'icon_url', 'description', 
                'sort_order', 'metadata'
            ]);

            // Auto-generate slug if not provided
            if (empty($categoryData['slug'])) {
                $categoryData['slug'] = Str::slug($categoryData['title']);
            }

            $categoryData['is_active'] = $request->has('is_active');

            // Set default sort order if not provided
            if (!isset($categoryData['sort_order'])) {
                $maxOrder = TvCategory::max('sort_order') ?: 0;
                $categoryData['sort_order'] = $maxOrder + 10;
            }

            $category = TvCategory::create($categoryData);

            return response()->json([
                'success' => true,
                'message' => 'Category created successfully',
                'data' => $category
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to create category: ' . $e->getMessage()
            ], 500);
        }
    }

    public function show($id)
    {
        try {
            $category = TvCategory::withCount('channels', 'activeChannels')->findOrFail($id);
            return response()->json([
                'success' => true,
                'data' => $category
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Category not found'
            ], 404);
        }
    }

    public function update(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255|unique:tv_category,title,' . $id . ',tv_category_id',
            'slug' => 'nullable|string|max:255|unique:tv_category,slug,' . $id . ',tv_category_id',
            'image' => 'nullable|url',
            'icon_url' => 'nullable|url',
            'description' => 'nullable|string',
            'sort_order' => 'nullable|integer|min:0',
            'is_active' => 'boolean',
            'metadata' => 'nullable|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $category = TvCategory::findOrFail($id);
            
            $categoryData = $request->only([
                'title', 'slug', 'image', 'icon_url', 'description', 
                'sort_order', 'metadata'
            ]);

            // Auto-generate slug if not provided
            if (empty($categoryData['slug'])) {
                $categoryData['slug'] = Str::slug($categoryData['title']);
            }

            $categoryData['is_active'] = $request->has('is_active');

            $category->update($categoryData);

            return response()->json([
                'success' => true,
                'message' => 'Category updated successfully',
                'data' => $category->fresh()
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update category: ' . $e->getMessage()
            ], 500);
        }
    }

    public function destroy($id)
    {
        try {
            $category = TvCategory::withCount('channels')->findOrFail($id);
            
            if ($category->channels_count > 0) {
                return response()->json([
                    'success' => false,
                    'message' => 'Cannot delete category that has channels assigned to it. Please reassign or remove channels first.'
                ], 422);
            }

            $category->delete();

            return response()->json([
                'success' => true,
                'message' => 'Category deleted successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to delete category: ' . $e->getMessage()
            ], 500);
        }
    }

    public function toggleStatus(Request $request, $id)
    {
        try {
            $category = TvCategory::findOrFail($id);
            $category->is_active = $request->status == 1;
            $category->save();

            return response()->json([
                'success' => true,
                'message' => 'Category status updated successfully',
                'status' => $category->is_active
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update category status: ' . $e->getMessage()
            ], 500);
        }
    }

    public function updateSortOrder(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'categories' => 'required|array',
            'categories.*.id' => 'required|exists:tv_category,tv_category_id',
            'categories.*.sort_order' => 'required|integer|min:0'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            foreach ($request->categories as $categoryData) {
                TvCategory::where('tv_category_id', $categoryData['id'])
                         ->update(['sort_order' => $categoryData['sort_order']]);
            }

            return response()->json([
                'success' => true,
                'message' => 'Category sort order updated successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update sort order: ' . $e->getMessage()
            ], 500);
        }
    }

    public function bulkAction(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'action' => 'required|in:activate,deactivate,delete',
            'category_ids' => 'required|array',
            'category_ids.*' => 'exists:tv_category,tv_category_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $categoryIds = $request->category_ids;
            $action = $request->action;
            $affectedCount = 0;

            switch ($action) {
                case 'activate':
                    $affectedCount = TvCategory::whereIn('tv_category_id', $categoryIds)
                                              ->update(['is_active' => true]);
                    break;
                
                case 'deactivate':
                    $affectedCount = TvCategory::whereIn('tv_category_id', $categoryIds)
                                              ->update(['is_active' => false]);
                    break;
                
                case 'delete':
                    // Check if any categories have channels
                    $categoriesWithChannels = TvCategory::whereIn('tv_category_id', $categoryIds)
                                                      ->withCount('channels')
                                                      ->get()
                                                      ->where('channels_count', '>', 0);

                    if ($categoriesWithChannels->count() > 0) {
                        $categoryNames = $categoriesWithChannels->pluck('title')->implode(', ');
                        return response()->json([
                            'success' => false,
                            'message' => "Cannot delete categories with assigned channels: {$categoryNames}. Please reassign channels first."
                        ], 422);
                    }

                    $affectedCount = TvCategory::whereIn('tv_category_id', $categoryIds)->delete();
                    break;
            }

            return response()->json([
                'success' => true,
                'message' => "Successfully {$action}d {$affectedCount} category(ies)",
                'affected_count' => $affectedCount
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Bulk action failed: ' . $e->getMessage()
            ], 500);
        }
    }

    public function getCategoryChannels($id)
    {
        try {
            $category = TvCategory::with(['channels' => function($query) {
                $query->select('tv_channel.tv_channel_id', 'title', 'channel_number', 'thumbnail', 'is_active', 'total_views')
                      ->orderBy('channel_number')
                      ->orderBy('title');
            }])->findOrFail($id);

            return response()->json([
                'success' => true,
                'data' => [
                    'category' => $category->only(['tv_category_id', 'title', 'description']),
                    'channels' => $category->channels
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Category not found'
            ], 404);
        }
    }

    public function getCategoriesForSelect()
    {
        $categories = TvCategory::active()
                               ->ordered()
                               ->get(['tv_category_id', 'title']);

        return response()->json([
            'success' => true,
            'data' => $categories
        ]);
    }

    public function duplicate(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255|unique:tv_category,title'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $originalCategory = TvCategory::findOrFail($id);
            
            $duplicateData = $originalCategory->toArray();
            unset($duplicateData['tv_category_id'], $duplicateData['created_at'], $duplicateData['updated_at']);
            
            $duplicateData['title'] = $request->title;
            $duplicateData['slug'] = Str::slug($request->title);
            $duplicateData['is_active'] = false; // New duplicate starts as inactive
            
            // Set new sort order
            $maxOrder = TvCategory::max('sort_order') ?: 0;
            $duplicateData['sort_order'] = $maxOrder + 10;

            $duplicate = TvCategory::create($duplicateData);

            return response()->json([
                'success' => true,
                'message' => 'Category duplicated successfully',
                'data' => $duplicate
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to duplicate category: ' . $e->getMessage()
            ], 500);
        }
    }

    public function getCategoryStats($id)
    {
        try {
            $category = TvCategory::withCount([
                'channels',
                'activeChannels' => function($query) {
                    $query->where('is_active', true);
                }
            ])->findOrFail($id);

            // Get total views for all channels in this category
            $totalViews = $category->channels()->sum('total_views');
            
            // Get channels with most views in this category
            $topChannels = $category->channels()
                                   ->orderBy('total_views', 'desc')
                                   ->limit(5)
                                   ->get(['tv_channel_id', 'title', 'channel_number', 'total_views']);

            $stats = [
                'total_channels' => $category->channels_count,
                'active_channels' => $category->active_channels_count,
                'inactive_channels' => $category->channels_count - $category->active_channels_count,
                'total_views' => $totalViews,
                'avg_views_per_channel' => $category->channels_count > 0 ? round($totalViews / $category->channels_count) : 0,
                'top_channels' => $topChannels
            ];

            return response()->json([
                'success' => true,
                'data' => $stats
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to get category stats: ' . $e->getMessage()
            ], 500);
        }
    }
}