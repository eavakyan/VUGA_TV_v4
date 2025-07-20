<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\TVCategory;
use App\TVChannel;
use Illuminate\Http\Request;

class TVController extends Controller
{
    // TV Category Methods
    public function viewListTVCategory()
    {
        return view('admin.tv.category.list');
    }

    public function showTVCategoryList(Request $request)
    {
        $query = TVCategory::query();
        
        if ($request->has('search') && !empty($request->search)) {
            $query->where('title', 'LIKE', "%{$request->search}%");
        }

        $categories = $query->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $categories->items(),
            'recordsTotal' => $categories->total(),
            'recordsFiltered' => $categories->total()
        ]);
    }

    public function addUpdateTVCategory(Request $request)
    {
        if ($request->id) {
            $category = TVCategory::find($request->id);
        } else {
            $category = new TVCategory();
        }
        
        $category->title = $request->title;
        $category->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Category saved successfully'
        ]);
    }

    public function deleteTVCategory(Request $request)
    {
        $category = TVCategory::find($request->id);
        if ($category) {
            $category->delete();
            return response()->json(['status' => true, 'message' => 'TV Category deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'TV Category not found']);
    }

    public function CheckExistTVCategory(Request $request)
    {
        $exists = TVCategory::where('title', $request->title)
                           ->when($request->id, function($q) use ($request) {
                               $q->where('id', '!=', $request->id);
                           })
                           ->exists();
        
        return response()->json(['exists' => $exists]);
    }

    // TV Channel Methods
    public function viewListTVChannel()
    {
        return view('admin.tv.channel.list');
    }

    public function viewAddTVChannel()
    {
        $categories = TVCategory::all();
        return view('admin.tv.channel.add', compact('categories'));
    }

    public function viewUpdateTVChannel($id)
    {
        $channel = TVChannel::findOrFail($id);
        $categories = TVCategory::all();
        return view('admin.tv.channel.edit', compact('channel', 'categories'));
    }

    public function viewTVChannel($id)
    {
        $channel = TVChannel::findOrFail($id);
        return view('admin.tv.channel.view', compact('channel'));
    }

    public function showTVChannelList(Request $request)
    {
        $query = TVChannel::with('category');
        
        if ($request->has('search') && !empty($request->search)) {
            $query->where('title', 'LIKE', "%{$request->search}%");
        }

        $channels = $query->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $channels->items(),
            'recordsTotal' => $channels->total(),
            'recordsFiltered' => $channels->total()
        ]);
    }

    public function addUpdateTVChannel(Request $request)
    {
        if ($request->id) {
            $channel = TVChannel::find($request->id);
        } else {
            $channel = new TVChannel();
        }
        
        $channel->title = $request->title;
        $channel->tv_category_id = $request->tv_category_id;
        $channel->description = $request->description;
        $channel->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Channel saved successfully'
        ]);
    }

    public function deleteTVChannel(Request $request)
    {
        $channel = TVChannel::find($request->id);
        if ($channel) {
            $channel->delete();
            return response()->json(['status' => true, 'message' => 'TV Channel deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'TV Channel not found']);
    }

    public function CheckExistTVChannel(Request $request)
    {
        $exists = TVChannel::where('title', $request->title)
                          ->when($request->id, function($q) use ($request) {
                              $q->where('id', '!=', $request->id);
                          })
                          ->exists();
        
        return response()->json(['exists' => $exists]);
    }

    // Placeholder methods for other TV channel operations
    public function viewTvChannelSource($channelId)
    {
        $channel = TVChannel::findOrFail($channelId);
        return view('admin.tv.channel.source.list', compact('channel'));
    }

    public function showTVChannelSourceList(Request $request)
    {
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function addUpdateTVChannelSource(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'TV Channel Source saved successfully']);
    }

    public function deleteTVChannelSource(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'TV Channel Source deleted successfully']);
    }

    public function deleteChannelSource(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Channel Source deleted successfully']);
    }

    public function UploadSourceMedia(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Source media uploaded successfully']);
    }
} 