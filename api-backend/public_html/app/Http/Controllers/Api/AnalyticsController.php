<?php

namespace App\Http\Controllers\Api;

use App\Services\AnalyticsService;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AnalyticsController extends Controller
{
    protected $analyticsService;

    public function __construct(AnalyticsService $analyticsService)
    {
        $this->analyticsService = $analyticsService;
    }

    /**
     * Increase content view count
     */
    public function increaseContentView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $result = $this->analyticsService->incrementContentView($request->content_id);

        if (!$result['success']) {
            return response()->json([
                'status' => false,
                'message' => $result['message']
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => $result['message'],
            'data' => $result['data']
        ]);
    }

    /**
     * Increase content download count
     */
    public function increaseContentDownload(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $result = $this->analyticsService->incrementContentDownload($request->content_id);

        if (!$result['success']) {
            return response()->json([
                'status' => false,
                'message' => $result['message']
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => $result['message'],
            'data' => $result['data']
        ]);
    }

    /**
     * Increase content share count
     */
    public function increaseContentShare(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $result = $this->analyticsService->incrementContentShare($request->content_id);

        if (!$result['success']) {
            return response()->json([
                'status' => false,
                'message' => $result['message']
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => $result['message'],
            'data' => $result['data']
        ]);
    }

    /**
     * Increase episode view count
     */
    public function increaseEpisodeView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $result = $this->analyticsService->incrementEpisodeView($request->episode_id);

        if (!$result['success']) {
            return response()->json([
                'status' => false,
                'message' => $result['message']
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => $result['message'],
            'data' => $result['data']
        ]);
    }

    /**
     * Increase episode download count
     */
    public function increaseEpisodeDownload(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $result = $this->analyticsService->incrementEpisodeDownload($request->episode_id);

        if (!$result['success']) {
            return response()->json([
                'status' => false,
                'message' => $result['message']
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => $result['message'],
            'data' => $result['data']
        ]);
    }
} 