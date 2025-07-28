<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Content;
use App\Models\V2\ContentGenre;
use App\Models\V2\Genre;
use App\Models\V2\AppLanguage;
use App\Models\V2\TopContent;
use App\Models\V2\AppUserWatchHistory;
use App\Models\V2\Episode;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

class ContentController extends Controller
{
    /**
     * V1 Compatible: Fetch home page data
     */
    public function fetchHomePageData(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $userId = $request->user_id;
        $user = null;
        $watchlistIds = [];
        
        // Only fetch user if user_id is not 0
        if ($userId > 0) {
            $user = DB::table('app_user')->where('app_user_id', $userId)->first();
            // Get user's watchlist
            $watchlistIds = $user && $user->watchlist_content_ids ? explode(',', $user->watchlist_content_ids) : [];
        }
        
        $watchlistContent = [];
        
        if (!empty($watchlistIds)) {
            $watchlistContent = Content::with(['language', 'genres'])
                ->whereIn('content_id', $watchlistIds)
                ->visible()
                ->limit(5)
                ->get();
        }

        // Featured content
        $featuredContent = Content::with(['language', 'genres'])
            ->featured()
            ->visible()
            ->get();

        // Top content
        $topContents = TopContent::with(['content.language', 'content.genres'])
            ->join('content', 'top_content.content_id', '=', 'content.content_id')
            ->where('content.is_show', 1)
            ->orderBy('top_content.content_index')
            ->get()
            ->map(function ($topContent) {
                return [
                    'top_content_id' => $topContent->top_content_id,
                    'content_index' => $topContent->content_index,
                    'content_id' => $topContent->content_id,
                    'content' => $this->formatContent($topContent->content)
                ];
            });

        // Genre content
        $genres = Genre::all();
        $genreContents = [];
        
        foreach ($genres as $genre) {
            $contents = DB::table('content')
                ->where('is_show', 1)
                ->whereRaw('FIND_IN_SET(?, genre_ids)', [$genre->genre_id])
                ->inRandomOrder()
                ->limit(10)
                ->get();
                
            if ($contents->isNotEmpty()) {
                $contentModels = Content::whereIn('content_id', $contents->pluck('content_id'))->get();
                $genre->contents = $this->formatContentList($contentModels);
                $genreContents[] = $genre;
            }
        }

        return response()->json([
            'status' => true,
            'message' => 'Fetch Home Page Data Successfully',
            'featured' => $this->formatContentList($featuredContent),
            'watchlist' => $this->formatContentList($watchlistContent),
            'topContents' => $topContents,
            'genreContents' => $genreContents
        ]);
    }

    /**
     * V1 Compatible: Fetch content details
     */
    public function fetchContentDetail(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
            'user_id' => 'required|integer'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $content = Content::with([
            'language',
            'genres',
            'sources',
            'casts.actor',
            'subtitles',
            'seasons.episodes.sources',
            'seasons.episodes.subtitles'
        ])->find($request->content_id);

        if (!$content || !$content->is_show) {
            return response()->json([
                'status' => false,
                'message' => 'Content not found'
            ]);
        }

        // Format content for V1 compatibility
        $formattedContent = $this->formatContentDetail($content);
        
        // Check if content is in user's watchlist
        $watchlistIds = [];
        $formattedContent['is_watchlist'] = false;
        
        if ($request->user_id > 0) {
            $user = DB::table('app_user')->where('app_user_id', $request->user_id)->first();
            if ($user) {
                $watchlistIds = $user->watchlist_content_ids ? explode(',', $user->watchlist_content_ids) : [];
                $formattedContent['is_watchlist'] = in_array($request->content_id, $watchlistIds);
            }
        }
        
        // Get more like this
        $genreIds = $content->genres->pluck('genre_id');
        $moreLikeThis = Content::with(['language', 'genres'])
            ->visible()
            ->where('content_id', '!=', $content->content_id)
            ->whereHas('genres', function($q) use ($genreIds) {
                $q->whereIn('genre.genre_id', $genreIds);
            })
            ->limit(10)
            ->get();
        
        $formattedContent['more_like_this'] = $this->formatContentList($moreLikeThis);

        return response()->json([
            'status' => true,
            'message' => 'Fetch Content Details Successfully',
            'data' => $formattedContent
        ]);
    }

    /**
     * Get home page data
     */
    public function getHomePageData(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'app_user_id' => 'nullable|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        // Featured content
        $featuredContent = Content::with(['language', 'genres', 'sources'])
            ->featured()
            ->visible()
            ->orderBy('updated_at', 'desc')
            ->limit(10)
            ->get();

        // Top content
        $topContent = TopContent::with(['content.language', 'content.genres'])
            ->join('content', 'top_content.content_id', '=', 'content.content_id')
            ->where('content.is_show', 1)
            ->orderBy('top_content.content_index')
            ->limit(10)
            ->get()
            ->pluck('content');

        // Continue watching (if user is logged in)
        $continueWatching = [];
        if ($request->app_user_id) {
            $continueWatching = $this->getContinueWatching($request->app_user_id);
        }

        // New releases
        $newReleases = Content::with(['language', 'genres'])
            ->visible()
            ->orderBy('created_at', 'desc')
            ->limit(20)
            ->get();

        // Content by genres
        $genreContent = Genre::with(['contents' => function($query) {
                $query->visible()
                      ->with(['language', 'genres'])
                      ->limit(10);
            }])
            ->get()
            ->map(function($genre) {
                return [
                    'genre_id' => $genre->genre_id,
                    'genre_title' => $genre->title,
                    'contents' => $genre->contents
                ];
            })
            ->filter(function($item) {
                return $item['contents']->isNotEmpty();
            })
            ->values();

        return response()->json([
            'status' => true,
            'message' => 'Home page data fetched successfully',
            'data' => [
                'featured_content' => $this->formatContentList($featuredContent),
                'top_content' => $this->formatContentList($topContent),
                'continue_watching' => $continueWatching,
                'new_releases' => $this->formatContentList($newReleases),
                'genre_content' => $genreContent
            ]
        ]);
    }

    /**
     * Get all content with pagination
     */
    public function getAllContent(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
            'type' => 'nullable|integer|in:1,2', // 1=movie, 2=series
            'language_id' => 'nullable|integer|exists:app_language,app_language_id',
            'genre_id' => 'nullable|integer|exists:genre,genre_id',
            'sort_by' => 'nullable|in:latest,oldest,popular,rating',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $query = Content::with(['language', 'genres', 'sources'])
                        ->visible();

        // Apply filters
        if ($request->type) {
            $query->where('type', $request->type);
        }

        if ($request->language_id) {
            $query->where('language_id', $request->language_id);
        }

        if ($request->genre_id) {
            $query->whereHas('genres', function($q) use ($request) {
                $q->where('genre.genre_id', $request->genre_id);
            });
        }

        // Apply sorting
        switch ($request->sort_by) {
            case 'oldest':
                $query->orderBy('created_at', 'asc');
                break;
            case 'popular':
                $query->orderBy('total_view', 'desc');
                break;
            case 'rating':
                $query->orderBy('ratings', 'desc');
                break;
            default: // latest
                $query->orderBy('created_at', 'desc');
        }

        $perPage = $request->per_page ?? 20;
        $contents = $query->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Content fetched successfully',
            'data' => $this->formatContentList($contents->items()),
            'pagination' => [
                'current_page' => $contents->currentPage(),
                'last_page' => $contents->lastPage(),
                'per_page' => $contents->perPage(),
                'total' => $contents->total()
            ]
        ]);
    }

    /**
     * Get content by ID
     */
    public function getContentById(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
            'app_user_id' => 'nullable|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $content = Content::with([
            'language',
            'genres',
            'sources',
            'casts.actor',
            'subtitles.language',
            'seasons.episodes.sources',
            'seasons.episodes.subtitles.language'
        ])->find($request->content_id);

        if (!$content || !$content->is_show) {
            return response()->json([
                'status' => false,
                'message' => 'Content not found'
            ], 404);
        }

        // Get user-specific data if logged in
        $userData = null;
        if ($request->app_user_id) {
            $userData = $this->getUserContentData($request->app_user_id, $request->content_id);
        }

        // Get recommendations
        $recommendations = $this->getRecommendations($content);

        return response()->json([
            'status' => true,
            'message' => 'Content details fetched successfully',
            'data' => [
                'content' => $this->formatContentDetail($content),
                'user_data' => $userData,
                'recommendations' => $this->formatContentList($recommendations)
            ]
        ]);
    }

    /**
     * Search content
     */
    public function searchContent(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'search' => 'required|string|min:1',
            'type' => 'nullable|integer|in:1,2',
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $query = Content::with(['language', 'genres'])
                        ->visible()
                        ->where(function($q) use ($request) {
                            $q->where('title', 'LIKE', '%' . $request->search . '%')
                              ->orWhere('description', 'LIKE', '%' . $request->search . '%');
                        });

        if ($request->type) {
            $query->where('type', $request->type);
        }

        $perPage = $request->per_page ?? 20;
        $contents = $query->orderBy('total_view', 'desc')
                         ->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Search results fetched successfully',
            'data' => $this->formatContentList($contents->items()),
            'pagination' => [
                'current_page' => $contents->currentPage(),
                'last_page' => $contents->lastPage(),
                'per_page' => $contents->perPage(),
                'total' => $contents->total()
            ]
        ]);
    }

    /**
     * Get featured content
     */
    public function getFeaturedContent(Request $request)
    {
        $contents = Content::with(['language', 'genres', 'sources'])
                          ->featured()
                          ->visible()
                          ->orderBy('updated_at', 'desc')
                          ->get();

        return response()->json([
            'status' => true,
            'message' => 'Featured content fetched successfully',
            'data' => $this->formatContentList($contents)
        ]);
    }

    /**
     * Get trending content
     */
    public function getTrendingContent(Request $request)
    {
        $contents = Content::with(['language', 'genres'])
                          ->visible()
                          ->where('created_at', '>=', now()->subDays(30))
                          ->orderBy('total_view', 'desc')
                          ->limit(20)
                          ->get();

        return response()->json([
            'status' => true,
            'message' => 'Trending content fetched successfully',
            'data' => $this->formatContentList($contents)
        ]);
    }

    /**
     * Get new content
     */
    public function getNewContent(Request $request)
    {
        $contents = Content::with(['language', 'genres'])
                          ->visible()
                          ->orderBy('created_at', 'desc')
                          ->limit(20)
                          ->get();

        return response()->json([
            'status' => true,
            'message' => 'New content fetched successfully',
            'data' => $this->formatContentList($contents)
        ]);
    }

    /**
     * Get content by genre
     */
    public function getContentByGenre(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'genre_id' => 'required|integer|exists:genre,genre_id',
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $genre = Genre::find($request->genre_id);
        
        $perPage = $request->per_page ?? 20;
        $contents = $genre->contents()
                         ->with(['language', 'genres'])
                         ->visible()
                         ->orderBy('created_at', 'desc')
                         ->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Content by genre fetched successfully',
            'genre' => $genre->title,
            'data' => $this->formatContentList($contents->items()),
            'pagination' => [
                'current_page' => $contents->currentPage(),
                'last_page' => $contents->lastPage(),
                'per_page' => $contents->perPage(),
                'total' => $contents->total()
            ]
        ]);
    }

    /**
     * Increase content view count
     */
    public function increaseContentView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        Content::where('content_id', $request->content_id)
               ->increment('total_view');

        return response()->json([
            'status' => true,
            'message' => 'View count increased'
        ]);
    }

    /**
     * Increase content share count
     */
    public function increaseContentShare(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        Content::where('content_id', $request->content_id)
               ->increment('total_share');

        return response()->json([
            'status' => true,
            'message' => 'Share count increased'
        ]);
    }

    /**
     * Increase content download count
     */
    public function increaseContentDownload(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        Content::where('content_id', $request->content_id)
               ->increment('total_download');

        return response()->json([
            'status' => true,
            'message' => 'Download count increased'
        ]);
    }

    /**
     * Increase episode view count
     */
    public function increaseEpisodeView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required|integer|exists:episode,episode_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        Episode::where('episode_id', $request->episode_id)
               ->increment('total_view');

        return response()->json([
            'status' => true,
            'message' => 'Episode view count increased'
        ]);
    }

    /**
     * Increase episode download count
     */
    public function increaseEpisodeDownload(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required|integer|exists:episode,episode_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        Episode::where('episode_id', $request->episode_id)
               ->increment('total_download');

        return response()->json([
            'status' => true,
            'message' => 'Episode download count increased'
        ]);
    }

    /**
     * Get continue watching content for user
     */
    private function getContinueWatching($userId)
    {
        $watchHistory = AppUserWatchHistory::with(['content.language', 'content.genres', 'episode.season'])
            ->where('app_user_id', $userId)
            ->where('completed', 0)
            ->where('last_watched_position', '>', 0)
            ->orderBy('updated_at', 'desc')
            ->limit(10)
            ->get();

        return $watchHistory->map(function($history) {
            $data = $this->formatContent($history->content);
            $data['watch_history'] = [
                'last_watched_position' => $history->last_watched_position,
                'total_duration' => $history->total_duration,
                'episode_id' => $history->episode_id,
                'episode' => $history->episode ? [
                    'episode_id' => $history->episode->episode_id,
                    'title' => $history->episode->title,
                    'season_title' => $history->episode->season->title,
                    'number' => $history->episode->number
                ] : null
            ];
            return $data;
        });
    }

    /**
     * Get user-specific content data
     */
    private function getUserContentData($userId, $contentId)
    {
        $user = \App\Models\V2\AppUser::find($userId);
        
        return [
            'is_in_watchlist' => $user->watchlist()->where('content_id', $contentId)->exists(),
            'is_favorite' => $user->favorites()->where('content_id', $contentId)->exists(),
            'user_rating' => $user->ratings()->where('content_id', $contentId)->value('rating'),
            'watch_history' => AppUserWatchHistory::where('app_user_id', $userId)
                                                  ->where('content_id', $contentId)
                                                  ->whereNull('episode_id')
                                                  ->first()
        ];
    }

    /**
     * Get content recommendations
     */
    private function getRecommendations($content)
    {
        // Get content with similar genres
        $genreIds = $content->genres->pluck('genre_id');
        
        return Content::with(['language', 'genres'])
            ->visible()
            ->where('content_id', '!=', $content->content_id)
            ->where(function($query) use ($genreIds, $content) {
                $query->whereHas('genres', function($q) use ($genreIds) {
                    $q->whereIn('genre.genre_id', $genreIds);
                })
                ->orWhere('type', $content->type);
            })
            ->orderBy('ratings', 'desc')
            ->limit(10)
            ->get();
    }

    /**
     * Format content list for response
     */
    private function formatContentList($contents)
    {
        return collect($contents)->map(function($content) {
            return $this->formatContent($content);
        });
    }

    /**
     * V1 Compatible: Fetch contents by genre
     */
    public function fetchContentsByGenre(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'genre_id' => 'required|integer|exists:genre,genre_id',
            'start' => 'required|integer',
            'limit' => 'required|integer',
            'type' => 'nullable|integer'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $query = DB::table('content')
            ->where('is_show', 1)
            ->whereRaw('FIND_IN_SET(?, genre_ids)', [$request->genre_id]);
        
        // Filter by type if provided
        if ($request->has('type') && $request->type != 0) {
            $query->where('type', $request->type);
        }
        
        // Apply pagination
        $contents = $query->offset($request->start)
            ->limit($request->limit)
            ->get();
        
        // Get content models
        $contentModels = Content::whereIn('content_id', $contents->pluck('content_id'))->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Fetch Contents By Genre Successfully',
            'data' => $this->formatContentList($contentModels)
        ]);
    }

    /**
     * Format single content for response
     */
    private function formatContent($content)
    {
        if (!$content) return null;
        
        $data = $content->toArray();
        
        // Add genre IDs as comma-separated string for backward compatibility
        if ($content->relationLoaded('genres')) {
            $data['genre_ids'] = $content->genres->pluck('genre_id')->implode(',');
        }
        
        // Convert duration back to string if needed for backward compatibility
        if (isset($data['duration']) && is_numeric($data['duration'])) {
            $data['duration_seconds'] = $data['duration'];
            $data['duration'] = (string) $data['duration'];
        }
        
        // Convert boolean fields to integers for backward compatibility
        if (isset($data['is_featured'])) {
            $data['is_featured'] = (int) $data['is_featured'];
        }
        if (isset($data['is_show'])) {
            $data['is_show'] = (int) $data['is_show'];
        }
        
        return $data;
    }

    /**
     * Format content detail for response
     */
    private function formatContentDetail($content)
    {
        $data = $this->formatContent($content);
        
        // Format cast data
        if ($content->relationLoaded('casts')) {
            $data['cast'] = $content->casts->map(function($cast) {
                return [
                    'content_cast_id' => $cast->content_cast_id,
                    'actor_id' => $cast->actor_id,
                    'character_name' => $cast->character_name,
                    'actor' => $cast->actor
                ];
            });
        }
        
        // Rename sources to content_sources for V1 compatibility
        if (isset($data['sources'])) {
            $data['content_sources'] = $data['sources'];
            unset($data['sources']);
        }
        
        // Rename subtitles to content_subtitles for V1 compatibility
        if (isset($data['subtitles'])) {
            $data['content_subtitles'] = $data['subtitles'];
            unset($data['subtitles']);
        }
        
        // Format cast to contentCast for V1 compatibility
        if (isset($data['cast'])) {
            $data['contentCast'] = $data['cast'];
        }
        
        return $data;
    }
}