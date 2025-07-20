<?php

namespace App\Http\Controllers\Api;

use App\TVCategory;
use App\TVChannel;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class TVController extends Controller
{
    /**
     * Get TV categories list
     */
    public function GetTvCategoryist()
    {
        $categories = TVCategory::orderBy('title', 'ASC')->get();

        return response()->json([
            'status' => true,
            'message' => 'TV Categories Retrieved Successfully',
            'data' => $categories
        ]);
    }

    /**
     * Get all TV channels
     */
    public function getAllTvChannelList(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'start' => 'required|integer',
            'limit' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $channels = TVChannel::where('is_show', 1)
                             ->orderBy('created_at', 'DESC')
                             ->offset($request->start)
                             ->limit($request->limit)
                             ->get();

        return response()->json([
            'status' => true,
            'message' => 'TV Channels Retrieved Successfully',
            'data' => $channels
        ]);
    }

    /**
     * Get TV channels by category ID
     */
    public function getTvChannelListByCategoryID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'category_id' => 'required|integer',
            'start' => 'required|integer',
            'limit' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $channels = TVChannel::where('is_show', 1)
                             ->where('tv_category_id', $request->category_id)
                             ->orderBy('created_at', 'DESC')
                             ->offset($request->start)
                             ->limit($request->limit)
                             ->get();

        return response()->json([
            'status' => true,
            'message' => 'TV Channels by Category Retrieved Successfully',
            'data' => $channels
        ]);
    }

    /**
     * Increase TV channel view count
     */
    public function increaseTVChannelView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'channel_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $channel = TVChannel::where('id', $request->channel_id)->first();
        
        if (!$channel) {
            return response()->json([
                'status' => false,
                'message' => 'TV Channel Not Found'
            ]);
        }

        $channel->total_view += 1;
        $channel->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Channel View Increased Successfully',
            'data' => $channel
        ]);
    }

    /**
     * Increase TV channel share count
     */
    public function increaseTVChannelShare(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'channel_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $channel = TVChannel::where('id', $request->channel_id)->first();
        
        if (!$channel) {
            return response()->json([
                'status' => false,
                'message' => 'TV Channel Not Found'
            ]);
        }

        $channel->total_share += 1;
        $channel->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Channel Share Increased Successfully',
            'data' => $channel
        ]);
    }
} 