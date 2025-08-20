<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Content;
use App\Models\V2\ContentCategory;
use App\Models\V2\ContentTrailer;
use App\Models\V2\Category;
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
     * Fetch multiple contents by IDs for Recently Watched
     */
    public function getContentsByIds(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_ids' => 'required|array',
            'content_ids.*' => 'required|integer',
            'user_id' => 'nullable|integer',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $contentIds = $request->content_ids;
        $userId = $request->user_id;
        $profileId = $request->profile_id;

        // Get contents without aliases first
        $contents = Content::whereIn('content_id', $contentIds)
            ->where('is_show', 1)
            ->with(['genres', 'distributor', 'seasons.episodes'])
            ->get();

        // Transform the data to match the expected format
        $transformedContents = $contents->map(function($content) use ($userId, $profileId) {
            // Map fields to expected names
            $contentArray = [
                'content_id' => $content->content_id,
                'content_name' => $content->title,
                'horizontal_poster' => $content->horizontal_poster,
                'vertical_poster' => $content->vertical_poster,
                'content_type' => $content->type,
                'release_year' => $content->release_year,
                'ratings' => $content->ratings,
                'duration' => $content->duration,
                'genres' => $content->genres,
                'seasons' => $content->seasons ? $content->seasons->map(function($season) {
                    return [
                        'season_id' => $season->season_id,
                        'content_id' => $season->content_id,
                        'season_title' => $season->title,
                        'season_thumbnail' => $season->thumbnail,
                        'episodes' => $season->episodes ? $season->episodes->map(function($episode) {
                            return [
                                'episode_id' => $episode->episode_id,
                                'season_id' => $episode->season_id,
                                'episode_title' => $episode->title,
                                'episode_thumbnail' => $episode->thumbnail,
                                'episode_duration' => $episode->duration,
                                'episode_number' => $episode->number
                            ];
                        }) : []
                    ];
                }) : []
            ];
            
            // Add additional fields
            $contentArray['is_watchlist'] = false;
            if ($profileId && $profileId > 0) {
                $contentArray['is_watchlist'] = \App\Models\V2\AppUserWatchlist::where('profile_id', $profileId)
                    ->where('content_id', $content->content_id)
                    ->exists();
            }

            // Add user rating if available
            // TODO: Implement user rating when AppUserRating model is available
            // if ($userId && $userId > 0) {
            //     $userRating = \App\Models\V2\AppUserRating::where('user_id', $userId)
            //         ->where('content_id', $content->content_id)
            //         ->where(function($query) use ($profileId) {
            //             if ($profileId) {
            //                 $query->where('profile_id', $profileId);
            //             }
            //         })
            //         ->first();
                
            //     $contentArray['user_rating'] = $userRating ? $userRating->rating : null;
            // }
            $contentArray['user_rating'] = null; // Placeholder until rating model is implemented

            return $contentArray;
        });

        return response()->json([
            'status' => true,
            'message' => 'Contents fetched successfully',
            'data' => $transformedContents
        ]);
    }

    /**
     * V1 Compatible: Fetch home page data
     */
    public function fetchHomePageData(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required|integer',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $userId = $request->user_id;
        $profileId = $request->profile_id;
        $user = null;
        $watchlistIds = [];
        
        // Only fetch user if user_id is not 0
        if ($userId > 0) {
            $user = \App\Models\V2\AppUser::find($userId);
            
            // If no profile_id provided, use last active profile
            if ($user && !$profileId) {
                $profileId = $user->last_active_profile_id;
            }
            
            // Get profile's watchlist
            if ($profileId) {
                $profile = \App\Models\V2\AppUserProfile::find($profileId);
                if ($profile && $profile->app_user_id == $userId) {
                    $watchlistIds = $profile->watchlist()->pluck('content.content_id')->toArray();
                }
            } else {
                // Fallback to user-level watchlist for backward compatibility
                $watchlistIds = $user && $user->watchlist_content_ids ? explode(',', $user->watchlist_content_ids) : [];
            }
        }
        
        $watchlistContent = [];
        
        if (!empty($watchlistIds)) {
            $query = Content::with(['language', 'genres'])
                ->whereIn('content_id', $watchlistIds)
                ->visible()
                ->limit(5);
            $watchlistContent = $this->filterContentByAge($query, $profileId)->get();
        }

        // Featured content
        $query = Content::with(['language', 'genres'])
            ->featured()
            ->visible();
        $featuredContent = $this->filterContentByAge($query, $profileId)->get();

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
        $categorys = Category::all();
        $categoryContents = [];
        
        foreach ($categorys as $category) {
            $query = Content::visible()
                ->whereHas('genres', function($q) use ($category) {
                    $q->where('genre.category_id', $category->category_id);
                })
                ->inRandomOrder()
                ->limit(10);
                
            $contentModels = $this->filterContentByAge($query, $profileId)->get();
            
            if ($contentModels->isNotEmpty()) {
                $category->contents = $this->formatContentList($contentModels);
                $categoryContents[] = $category;
            }
        }

        return response()->json([
            'status' => true,
            'message' => 'Fetch Home Page Data Successfully',
            'featured' => $this->formatContentList($featuredContent),
            'watchlist' => $this->formatContentList($watchlistContent),
            'topContents' => $topContents,
            'genreContents' => $categoryContents
        ]);
    }

    /**
     * V1 Compatible: Fetch content details
     */
    public function fetchContentDetail(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
            'user_id' => 'required|integer',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
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
            'trailers',
            'casts.actor',
            'subtitles',
            // 'audioTracks',  // Model doesn't exist yet
            // 'subtitleTracks',  // Model doesn't exist yet
            'seasons.episodes.sources',
            'seasons.episodes.subtitles',
            // 'seasons.episodes.audioTracks',  // Model doesn't exist yet
            // 'seasons.episodes.subtitleTracks',  // Model doesn't exist yet
            'ageLimits'
        ])->find($request->content_id);

        if (!$content || !$content->is_show) {
            return response()->json([
                'status' => false,
                'message' => 'Content not found'
            ]);
        }
        
        // Check age restrictions
        $profileId = $request->profile_id;
        if (!$profileId && $request->user_id > 0) {
            $user = \App\Models\V2\AppUser::find($request->user_id);
            $profileId = $user ? $user->last_active_profile_id : null;
        }
        
        if (!$this->canProfileAccessContent($profileId, $request->content_id)) {
            return response()->json([
                'status' => false,
                'message' => 'This content is not available for your profile due to age restrictions'
            ]);
        }

        // Format content for V1 compatibility
        $formattedContent = $this->formatContentDetail($content, $request->user_id);
        
        // Check if content is in user's watchlist
        $formattedContent['is_watchlist'] = false;
        $formattedContent['is_favorite'] = false;
        
        if ($request->user_id > 0) {
            $user = \App\Models\V2\AppUser::find($request->user_id);
            $profileId = $request->profile_id;
            
            // If no profile_id provided, use last active profile
            if ($user && !$profileId) {
                $profileId = $user->last_active_profile_id;
            }
            
            if ($user && $profileId) {
                $profile = \App\Models\V2\AppUserProfile::find($profileId);
                if ($profile && $profile->app_user_id == $request->user_id) {
                    $formattedContent['is_watchlist'] = $profile->watchlist()->where('app_user_watchlist.content_id', $request->content_id)->exists();
                    $formattedContent['is_favorite'] = $profile->favorites()->where('app_profile_favorite.content_id', $request->content_id)->exists();
                }
            } else if ($user) {
                // If no profile specified, use last active profile
                if ($user->last_active_profile_id) {
                    $profile = \App\Models\V2\AppUserProfile::find($user->last_active_profile_id);
                    if ($profile) {
                        $formattedContent['is_watchlist'] = $profile->watchlist()->where('app_user_watchlist.content_id', $request->content_id)->exists();
                        $formattedContent['is_favorite'] = $profile->favorites()->where('app_profile_favorite.content_id', $request->content_id)->exists();
                    }
                }
            }
        }
        
        // Get more like this
        $categoryIds = $content->genres->pluck('category_id');
        $query = Content::with(['language', 'genres'])
            ->visible()
            ->where('content_id', '!=', $content->content_id)
            ->whereHas('genres', function($q) use ($categoryIds) {
                $q->whereIn('genre.category_id', $categoryIds);
            })
            ->limit(10);
        $moreLikeThis = $this->filterContentByAge($query, $profileId)->get();
        
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
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        // Get profile ID
        $profileId = $request->profile_id;
        if (!$profileId && $request->app_user_id) {
            $user = \App\Models\V2\AppUser::find($request->app_user_id);
            $profileId = $user ? $user->last_active_profile_id : null;
        }

        // Featured content
        $query = Content::with(['language', 'genres', 'sources', 'ageLimits'])
            ->featured()
            ->visible()
            ->orderBy('updated_at', 'desc')
            ->limit(10);
        $featuredContent = $this->filterContentByAge($query, $profileId)->get();

        // Top content - filter after fetching
        $topContentQuery = TopContent::with(['content.language', 'content.genres', 'content.ageLimits'])
            ->join('content', 'top_content.content_id', '=', 'content.content_id')
            ->where('content.is_show', 1)
            ->orderBy('top_content.content_index')
            ->limit(20) // Get more to account for filtering
            ->get();
            
        $topContent = collect();
        foreach ($topContentQuery as $topItem) {
            if ($this->canProfileAccessContent($profileId, $topItem->content->content_id)) {
                $topContent->push($topItem->content);
                if ($topContent->count() >= 10) break;
            }
        }

        // Continue watching (if user is logged in)
        $continueWatching = [];
        if ($request->app_user_id) {
            $continueWatching = $this->getContinueWatching($request->app_user_id, $profileId);
        }

        // New releases
        $query = Content::with(['language', 'genres', 'ageLimits'])
            ->visible()
            ->orderBy('created_at', 'desc')
            ->limit(20);
        $newReleases = $this->filterContentByAge($query, $profileId)->get();

        // Content by genres
        $categoryContent = Category::all()
            ->map(function($category) use ($profileId) {
                $query = Content::visible()
                    ->with(['language', 'genres', 'ageLimits'])
                    ->whereHas('genres', function($q) use ($category) {
                        $q->where('genre.category_id', $category->category_id);
                    })
                    ->limit(10);
                    
                $contents = $this->filterContentByAge($query, $profileId)->get();
                
                if ($contents->isEmpty()) {
                    return null;
                }
                
                return [
                    'category_id' => $category->category_id,
                    'genre_title' => $category->title,
                    'contents' => $contents
                ];
            })
            ->filter()
            ->values();

        return response()->json([
            'status' => true,
            'message' => 'Home page data fetched successfully',
            'data' => [
                'featured_content' => $this->formatContentList($featuredContent),
                'top_content' => $this->formatContentList($topContent),
                'continue_watching' => $continueWatching,
                'new_releases' => $this->formatContentList($newReleases),
                'genre_content' => $categoryContent
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
            'category_id' => 'nullable|integer|exists:category,category_id',
            'sort_by' => 'nullable|in:latest,oldest,popular,rating',
            'app_user_id' => 'nullable|integer|exists:app_user,app_user_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        // Get profile ID
        $profileId = $request->profile_id;
        if (!$profileId && $request->app_user_id) {
            $user = \App\Models\V2\AppUser::find($request->app_user_id);
            $profileId = $user ? $user->last_active_profile_id : null;
        }

        $query = Content::with(['language', 'genres', 'sources', 'ageLimits'])
                        ->visible();

        // Apply filters
        if ($request->type) {
            $query->where('type', $request->type);
        }

        if ($request->language_id) {
            $query->where('language_id', $request->language_id);
        }

        if ($request->category_id) {
            $query->whereHas('genres', function($q) use ($request) {
                $q->where('genre.category_id', $request->category_id);
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
        
        // Apply age filtering
        $query = $this->filterContentByAge($query, $profileId);

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
            'trailers',
            'casts.actor',
            'subtitles.language',
            'seasons.episodes.sources',
            'seasons.episodes.subtitles.language',
            'ageLimits'
        ])->find($request->content_id);

        if (!$content || !$content->is_show) {
            return response()->json([
                'status' => false,
                'message' => 'Content not found'
            ], 404);
        }
        
        // Check age restrictions
        $profileId = $request->profile_id;
        if (!$profileId && $request->app_user_id) {
            $user = \App\Models\V2\AppUser::find($request->app_user_id);
            $profileId = $user ? $user->last_active_profile_id : null;
        }
        
        if (!$this->canProfileAccessContent($profileId, $request->content_id)) {
            return response()->json([
                'status' => false,
                'message' => 'This content is not available for your profile due to age restrictions'
            ], 403);
        }

        // Get user-specific data if logged in
        $userData = null;
        if ($request->app_user_id) {
            $profileId = $request->profile_id;
            if (!$profileId) {
                $user = \App\Models\V2\AppUser::find($request->app_user_id);
                $profileId = $user ? $user->last_active_profile_id : null;
            }
            $userData = $this->getUserContentData($request->app_user_id, $profileId, $request->content_id);
        }

        // Get recommendations
        $recommendations = $this->getRecommendations($content);

        return response()->json([
            'status' => true,
            'message' => 'Content details fetched successfully',
            'data' => [
                'content' => $this->formatContentDetail($content, $request->app_user_id),
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
            'search_type' => 'nullable|string|in:title,cast',  // New parameter for search type
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
            'app_user_id' => 'nullable|integer|exists:app_user,app_user_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }
        
        // Get profile ID
        $profileId = $request->profile_id;
        if (!$profileId && $request->app_user_id) {
            $user = \App\Models\V2\AppUser::find($request->app_user_id);
            $profileId = $user ? $user->last_active_profile_id : null;
        }

        $searchType = $request->search_type ?? 'title';  // Default to title search
        
        $query = Content::with(['language', 'genres', 'ageLimits'])
                        ->visible();
        
        if ($searchType === 'cast') {
            // Search by actor/cast name
            $query->whereHas('actors', function($q) use ($request) {
                $q->where('fullname', 'LIKE', '%' . $request->search . '%');
            });
        } else {
            // Search by title (default)
            $query->where('title', 'LIKE', '%' . $request->search . '%');
        }

        if ($request->type) {
            $query->where('type', $request->type);
        }
        
        // Apply age filtering
        $query = $this->filterContentByAge($query, $profileId);

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
    public function getContentByCategory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'category_id' => 'required|integer|exists:category,category_id',
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $category = Category::find($request->category_id);
        
        $perPage = $request->per_page ?? 20;
        $contents = $category->contents()
                         ->with(['language', 'genres'])
                         ->visible()
                         ->orderBy('created_at', 'desc')
                         ->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Content by genre fetched successfully',
            'genre' => $category->title,
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
    private function getContinueWatching($userId, $profileId = null)
    {
        // If no profile ID provided, get last active profile
        if (!$profileId) {
            $user = \App\Models\V2\AppUser::find($userId);
            if ($user) {
                $profileId = $user->last_active_profile_id;
            }
        }
        
        // Use profile-specific watch history
        if ($profileId) {
            $watchHistory = AppUserWatchHistory::with(['content.language', 'content.genres', 'episode.season'])
                ->where('profile_id', $profileId)
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
        } else {
            // No profile, return empty array
            return [];
        }
    }

    /**
     * Get user-specific content data
     */
    private function getUserContentData($userId, $profileId, $contentId)
    {
        $user = \App\Models\V2\AppUser::find($userId);
        
        // If profile is provided, use profile-specific data
        if ($profileId) {
            $profile = \App\Models\V2\AppUserProfile::find($profileId);
            if ($profile && $profile->app_user_id == $userId) {
                return [
                    'is_in_watchlist' => $profile->watchlist()->where('app_user_watchlist.content_id', $contentId)->exists(),
                    'is_favorite' => $profile->favorites()->where('app_profile_favorite.content_id', $contentId)->exists(),
                    'user_rating' => $profile->ratings()->where('app_profile_rating.content_id', $contentId)->value('rating'),
                    'watch_history' => AppUserWatchHistory::where('profile_id', $profileId)
                                                          ->where('content_id', $contentId)
                                                          ->whereNull('episode_id')
                                                          ->first()
                ];
            }
        }
        
        // Fallback to empty data if no valid profile
        return [
            'is_in_watchlist' => false,
            'is_favorite' => false,
            'user_rating' => null,
            'watch_history' => null
        ];
    }

    /**
     * Get content recommendations
     */
    private function getRecommendations($content)
    {
        // Get content with similar genres
        $categoryIds = $content->genres->pluck('category_id');
        
        return Content::with(['language', 'genres'])
            ->visible()
            ->where('content_id', '!=', $content->content_id)
            ->where(function($query) use ($categoryIds, $content) {
                $query->whereHas('genres', function($q) use ($categoryIds) {
                    $q->whereIn('genre.category_id', $categoryIds);
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
    public function fetchContentsByCategory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'category_id' => 'required|integer|exists:category,category_id',
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
            ->whereRaw('FIND_IN_SET(?, category_ids)', [$request->category_id]);
        
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
            $data['category_ids'] = $content->genres->pluck('category_id')->implode(',');
        }
        
        // Add distributor information
        if ($content->content_distributor_id) {
            $distributor = \DB::table('content_distributor')
                ->where('content_distributor_id', $content->content_distributor_id)
                ->first();
            
            if ($distributor) {
                $data['distributor'] = [
                    'id' => $distributor->content_distributor_id,
                    'name' => $distributor->name,
                    'code' => $distributor->code,
                    'logo_url' => $distributor->logo_url,
                    'is_premium' => (bool) $distributor->is_premium,
                    'is_base_included' => (bool) $distributor->is_base_included
                ];
            }
        } else {
            $data['distributor'] = null; // Legacy content
        }
        
        // Add subscription requirements
        $data['requires_subscription'] = $content->content_distributor_id ? true : false;
        $data['subscription_type'] = 'free'; // Default to free
        
        if ($content->content_distributor_id && isset($distributor)) {
            if ($distributor->is_base_included) {
                $data['subscription_type'] = 'base';
            } else if ($distributor->is_premium) {
                $data['subscription_type'] = 'premium';
            }
        }
        
        // Add age limit information if loaded
        if ($content->relationLoaded('ageLimits') && $content->ageLimits->isNotEmpty()) {
            $ageLimit = $content->ageLimits->first();
            $data['age_rating'] = $ageLimit->code;
            $data['age_rating_display'] = $ageLimit->display_name;
            $data['min_age'] = $content->ageLimits->max('min_age');
            $data['age_limits'] = $content->ageLimits->map(function($limit) {
                return [
                    'age_limit_id' => $limit->age_limit_id,
                    'name' => $limit->display_name ?? $limit->name ?? $limit->code ?? '',  // iOS expects 'name' field, fallback to various fields
                    'code' => $limit->code,
                    'display_name' => $limit->display_name,
                    'icon' => $limit->icon,
                    'display_color' => $limit->display_color,
                    'min_age' => $limit->min_age,
                    'max_age' => $limit->max_age,
                    'description' => $limit->description
                ];
            });
        }
        
        // Add trailer information for backward compatibility
        if ($content->relationLoaded('trailers')) {
            // Get primary trailer for backward compatibility
            $primaryTrailer = $content->trailers->where('is_primary', true)->first();
            if ($primaryTrailer) {
                $data['trailer_url'] = $primaryTrailer->trailer_url;
                $data['trailer_youtube_id'] = $primaryTrailer->youtube_id;
            } else {
                // If no primary trailer, get first trailer
                $firstTrailer = $content->trailers->first();
                if ($firstTrailer) {
                    $data['trailer_url'] = $firstTrailer->trailer_url;
                    $data['trailer_youtube_id'] = $firstTrailer->youtube_id;
                }
            }
            // Include all trailers in detailed format
            $data['trailers'] = $content->trailers->map(function($trailer) {
                return [
                    'content_trailer_id' => $trailer->content_trailer_id,
                    'title' => $trailer->title,
                    'youtube_id' => $trailer->youtube_id,
                    'trailer_url' => $trailer->trailer_url,
                    'embed_url' => $trailer->embed_url,
                    'thumbnail_url' => $trailer->thumbnail_url,
                    'is_primary' => $trailer->is_primary,
                    'sort_order' => $trailer->sort_order
                ];
            });
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
    private function formatContentDetail($content, $userId = null)
    {
        $data = $this->formatContent($content);
        
        // Add user access information
        if ($userId) {
            $data['user_has_access'] = $content->userHasAccess($userId);
        } else {
            $data['user_has_access'] = !$content->content_distributor_id; // Only free content
        }
        
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
        
        // Add audio tracks if loaded
        if ($content->relationLoaded('audioTracks')) {
            $data['audio_tracks'] = $content->audioTracks->map(function($track) {
                return [
                    'track_id' => $track->content_audio_track_id,
                    'language_id' => $track->language_id,
                    'language_code' => $track->language_code,
                    'title' => $track->title,
                    'audio_url' => $track->audio_url,
                    'audio_format' => $track->audio_format,
                    'audio_channels' => $track->audio_channels,
                    'audio_bitrate' => $track->audio_bitrate,
                    'is_primary' => (bool) $track->is_primary,
                    'is_default' => (bool) $track->is_default,
                    'sort_order' => $track->sort_order
                ];
            });
        }
        
        // Add subtitle tracks if loaded
        if ($content->relationLoaded('subtitleTracks')) {
            $data['subtitle_tracks'] = $content->subtitleTracks->map(function($track) {
                return [
                    'track_id' => $track->content_subtitle_track_id,
                    'language_id' => $track->language_id,
                    'language_code' => $track->language_code,
                    'title' => $track->title,
                    'subtitle_url' => $track->subtitle_url,
                    'subtitle_format' => $track->subtitle_format,
                    'is_forced' => (bool) $track->is_forced,
                    'is_default' => (bool) $track->is_default,
                    'sort_order' => $track->sort_order
                ];
            });
        }
        
        return $data;
    }
    
    /**
     * Filter content based on profile age restrictions
     */
    private function filterContentByAge($query, $profileId = null)
    {
        if (!$profileId) {
            return $query;
        }
        
        $profile = \App\Models\V2\AppUserProfile::find($profileId);
        if (!$profile) {
            return $query;
        }
        
        // If kids profile, only show content for ages 12 and under
        if ($profile->is_kids_profile || $profile->is_kids) {
            return $query->whereHas('ageLimits', function($q) {
                $q->where(function($subQ) {
                    $subQ->whereNotNull('age_limit.max_age')
                         ->where('age_limit.max_age', '<=', 12);
                });
            })->orWhereDoesntHave('ageLimits');
        }
        
        // If profile has age set, filter based on age
        if ($profile->age) {
            return $query->where(function($q) use ($profile) {
                // Include content without age limits
                $q->whereDoesntHave('ageLimits')
                  // Or content where profile age meets minimum age requirement
                  ->orWhereHas('ageLimits', function($ageQuery) use ($profile) {
                      $ageQuery->where('age_limit.min_age', '<=', $profile->age);
                  });
            });
        }
        
        return $query;
    }
    
    /**
     * Check if profile can access content based on age restrictions
     */
    private function canProfileAccessContent($profileId, $contentId)
    {
        if (!$profileId) {
            return true; // No restrictions if no profile
        }
        
        $profile = \App\Models\V2\AppUserProfile::find($profileId);
        if (!$profile) {
            return true; // Allow if profile not found
        }
        
        $content = Content::with('ageLimits')->find($contentId);
        if (!$content) {
            return false; // Content not found
        }
        
        // If content has no age limits, allow access
        if ($content->ageLimits->isEmpty()) {
            return true;
        }
        
        // Get the highest age limit for the content
        $maxAgeLimit = $content->ageLimits->max('min_age');
        
        // Kids profiles can only access content for ages 12 and under
        if ($profile->is_kids_profile || $profile->is_kids) {
            return $content->ageLimits->where('max_age', '<=', 12)->isNotEmpty();
        }
        
        // Check if profile age meets the requirement
        if ($profile->age) {
            return $profile->age >= $maxAgeLimit;
        }
        
        // If no age is set and not a kids profile, allow access
        return true;
    }
}