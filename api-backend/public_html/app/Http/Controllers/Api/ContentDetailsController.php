<?php

namespace App\Http\Controllers\Api;

use App\Constants;
use App\ContentSource;
use App\Subtitle;
use App\Season;
use App\Episode;
use App\EpisodeSource;
use App\EpisodeSubtitle;
use App\Language;
use App\Services\ContentService;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ContentDetailsController extends Controller
{
    protected $contentService;

    public function __construct(ContentService $contentService)
    {
        $this->contentService = $contentService;
    }

    /**
     * Get content details by ID
     */
    public function getContentDetailsByID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer',
            'user_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $content = $this->contentService->getContentDetails($request->content_id, $request->user_id);

        if (!$content) {
            return response()->json([
                'status' => false,
                'message' => 'Content or User Not Found'
            ]);
        }

        // Clean up unnecessary relations
        unset($content->subtitles);
        unset($content->sources);

        return response()->json([
            'status' => true,
            'message' => 'Fetch Content Successfully',
            'data' => $content
        ]);
    }

    /**
     * Search content
     */
    public function searchContent(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'start' => 'required',
            'limit' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $filters = [
            'start' => $request->start,
            'limit' => $request->limit,
        ];

        if ($request->has('type')) {
            $filters['type'] = $request->type;
        }
        
        if ($request->has('genre_id')) {
            $filters['genre_id'] = $request->genre_id;
        }
        
        if ($request->has('language_id')) {
            $filters['language_id'] = $request->language_id;
        }
        
        if ($request->has('keyword')) {
            $filters['keyword'] = $request->keyword;
        }

        $contents = $this->contentService->getContentList($filters);

        return response()->json([
            'status' => true,
            'message' => 'Content Result',
            'data' => $contents
        ]);
    }

    /**
     * Get content sources by content ID
     */
    public function getSourceByContentID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $sources = ContentSource::where('content_id', $request->content_id)->get();

        return response()->json([
            'status' => true,
            'message' => 'Content Sources Retrieved Successfully',
            'data' => $sources
        ]);
    }

    /**
     * Get content subtitles by content ID
     */
    public function getSubtitlesByContentID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $subtitles = Subtitle::with('language')->where('content_id', $request->content_id)->get();

        return response()->json([
            'status' => true,
            'message' => 'Content Subtitles Retrieved Successfully',
            'data' => $subtitles
        ]);
    }

    /**
     * Get seasons by content ID
     */
    public function getSeasonByContentID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $seasons = Season::where('content_id', $request->content_id)->orderBy('id', 'ASC')->get();

        return response()->json([
            'status' => true,
            'message' => 'Seasons Retrieved Successfully',
            'data' => $seasons
        ]);
    }

    /**
     * Get episodes by season ID
     */
    public function getEpisodeBySeasonID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'season_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $episodes = Episode::where('season_id', $request->season_id)->orderBy('number', 'ASC')->get();

        return response()->json([
            'status' => true,
            'message' => 'Episodes Retrieved Successfully',
            'data' => $episodes
        ]);
    }

    /**
     * Get episode sources by episode ID
     */
    public function getSourceByEpisodeID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $sources = EpisodeSource::where('episode_id', $request->episode_id)->get();

        return response()->json([
            'status' => true,
            'message' => 'Episode Sources Retrieved Successfully',
            'data' => $sources
        ]);
    }

    /**
     * Get episode subtitles by episode ID
     */
    public function getSubtitlesByEpisodeID(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $subtitles = EpisodeSubtitle::with('language')->where('episode_id', $request->episode_id)->get();

        return response()->json([
            'status' => true,
            'message' => 'Episode Subtitles Retrieved Successfully',
            'data' => $subtitles
        ]);
    }

    /**
     * Get all languages
     */
    public function getAllLanguageList()
    {
        $languages = Language::orderBy('title', 'ASC')->get();

        return response()->json([
            'status' => true,
            'message' => 'Languages Retrieved Successfully',
            'data' => $languages
        ]);
    }
} 