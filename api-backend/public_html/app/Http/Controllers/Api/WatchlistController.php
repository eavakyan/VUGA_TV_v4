<?php

namespace App\Http\Controllers\Api;

use App\Services\WatchlistService;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class WatchlistController extends Controller
{
    protected $watchlistService;

    public function __construct(WatchlistService $watchlistService)
    {
        $this->watchlistService = $watchlistService;
    }

    /**
     * Get user's watchlist
     */
    public function getWatchlist(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
            'start' => 'required|integer',
            'limit' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $watchlistContent = $this->watchlistService->getUserWatchlist(
            $request->user_id, 
            $request->start, 
            $request->limit
        );

        return response()->json([
            'status' => true,
            'message' => 'Watchlist Retrieved Successfully',
            'data' => $watchlistContent
        ]);
    }

    /**
     * Add content to watchlist
     */
    public function addToWatchList(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $result = $this->watchlistService->addToWatchlist($request->user_id, $request->content_id);

        if (!$result['success']) {
            return response()->json([
                'status' => false,
                'message' => $result['message']
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => $result['message'],
            'data' => $result['user']
        ]);
    }

    /**
     * Remove content from watchlist
     */
    public function removeFromWatchList(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $result = $this->watchlistService->removeFromWatchlist($request->user_id, $request->content_id);

        if (!$result['success']) {
            return response()->json([
                'status' => false,
                'message' => $result['message']
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => $result['message'],
            'data' => $result['user']
        ]);
    }
} 