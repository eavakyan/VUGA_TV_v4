<?php

namespace App\Services;

use App\Constants;
use App\Content;
use App\ContentCast;
use App\Genre;
use App\TopContent;
use App\User;
use App\Repositories\Contracts\ContentRepositoryInterface;
use Illuminate\Http\Request;

class ContentService
{
    protected $contentRepository;

    public function __construct(ContentRepositoryInterface $contentRepository)
    {
        $this->contentRepository = $contentRepository;
    }

    /**
     * Get home page content data
     */
    public function getHomePageData($userId)
    {
        $user = User::where('id', $userId)->first();
        if (!$user) {
            return null;
        }

        // Get user's watchlist content
        $userWatchListContent = [];
        if (!empty($user->watchlist_content_ids)) {
            $userWatchListContentsArray = explode(',', $user->watchlist_content_ids);
            $userWatchListContent = Content::where('is_show', Constants::showContent)
                                          ->whereIn('id', $userWatchListContentsArray)
                                          ->limit(5)
                                          ->get();
        }

        // Get featured content
        $featuredContent = $this->contentRepository->getFeaturedContent();

        // Get top contents
        $topContents = TopContent::whereHas('content', function ($query) {
            $query->where('is_show', Constants::showContent);
        })->with('content')->orderBy('content_index', 'ASC')->get();

        // Get genre-based content
        $genres = Genre::get();
        $genreContents = [];

        foreach ($genres as $genre) {
            $genreContent = $this->contentRepository->getRandomContentByGenre(
                $genre->id, 
                env('HOME_PAGE_GENRE_CONTENTS_LIMIT', 10)
            );

            if ($genreContent->isNotEmpty()) {
                $genre->contents = $genreContent;
                $genreContents[] = $genre;
            }
        }

        return [
            'user' => $user,
            'featured' => $featuredContent,
            'watchlist' => $userWatchListContent,
            'topContents' => $topContents,
            'genreContents' => $genreContents
        ];
    }

    /**
     * Get content with pagination
     */
    public function getContentList($filters = [])
    {
        $query = Content::where('is_show', Constants::showContent)
                       ->orderBy('created_at', 'DESC');

        if (isset($filters['type'])) {
            $query->where('type', $filters['type']);
        }

        if (isset($filters['genre_id'])) {
            $query->whereRaw('FIND_IN_SET(?, genre_ids)', [$filters['genre_id']]);
        }

        if (isset($filters['language_id'])) {
            $query->where('language_id', $filters['language_id']);
        }

        if (isset($filters['keyword'])) {
            $query->where('title', 'LIKE', '%' . $filters['keyword'] . '%');
        }

        $start = $filters['start'] ?? 0;
        $limit = $filters['limit'] ?? 10;

        return $query->offset($start)->limit($limit)->get();
    }

    /**
     * Get content details with related data
     */
    public function getContentDetails($contentId, $userId)
    {
        $content = Content::where('id', $contentId)
                         ->where('is_show', Constants::showContent)
                         ->first();

        if (!$content) {
            return null;
        }

        $user = User::where('id', $userId)->first();
        if (!$user) {
            return null;
        }

        // Check if content is in user's watchlist
        $content->is_watchlist = $this->isInWatchlist($contentId, $user->watchlist_content_ids);

        // Add content-specific data
        if ($content->type == Constants::movie) {
            $content = $this->addMovieData($content);
        } elseif ($content->type == Constants::series) {
            $content = $this->addSeriesData($content);
        }

        // Add "more like this" content
        $content->more_like_this = $this->getMoreLikeThis($content);

        return $content;
    }

    /**
     * Check if content is in user's watchlist
     */
    private function isInWatchlist($contentId, $watchlistContentIds)
    {
        if (empty($watchlistContentIds)) {
            return false;
        }

        $watchlistArray = explode(',', $watchlistContentIds);
        return in_array($contentId, $watchlistArray);
    }

    /**
     * Add movie-specific data
     */
    private function addMovieData($content)
    {
        $content->contentCast = ContentCast::with('actor')
                                          ->where('content_id', $content->id)
                                          ->get();
        $content->content_sources = $content->sources;
        $content->content_subtitles = $content->subtitles;

        return $content;
    }

    /**
     * Add series-specific data
     */
    private function addSeriesData($content)
    {
        $content->seasons = $content->seasons()->with([
            'episodes',
            'episodes.sources',
            'episodes.subtitles'
        ])->get();

        $content->contentCast = ContentCast::with('actor')
                                          ->where('content_id', $content->id)
                                          ->get();

        return $content;
    }

    /**
     * Get "more like this" content
     */
    private function getMoreLikeThis($content)
    {
        $genreIds = explode(',', $content->genre_ids);
        
        $moreLikeThis = Content::where('is_show', Constants::showContent)
                              ->where('id', '!=', $content->id)
                              ->where(function ($query) use ($genreIds) {
                                  foreach ($genreIds as $genreId) {
                                      $query->orWhereRaw('FIND_IN_SET(?, genre_ids)', [$genreId]);
                                  }
                              })
                              ->inRandomOrder()
                              ->limit(env('MORE_LIKE_RANDOM_LIST_COUNT', 10))
                              ->get();

        // Fallback to same type if no genre matches
        if ($moreLikeThis->isEmpty()) {
            $moreLikeThis = Content::where('is_show', Constants::showContent)
                                  ->where('type', $content->type)
                                  ->where('id', '!=', $content->id)
                                  ->inRandomOrder()
                                  ->limit(env('MORE_LIKE_RANDOM_LIST_COUNT', 10))
                                  ->get();
        }

        return $moreLikeThis;
    }

    /**
     * Update content analytics
     */
    public function updateContentAnalytics($contentId, $type)
    {
        $content = Content::find($contentId);
        
        if (!$content) {
            return false;
        }

        switch ($type) {
            case 'view':
                $content->total_view += 1;
                break;
            case 'download':
                $content->total_download += 1;
                break;
            case 'share':
                $content->total_share += 1;
                break;
        }

        return $content->save();
    }

    /**
     * Get content for admin listing
     */
    public function getContentForAdmin($filters = [])
    {
        $query = Content::with('language');

        if (isset($filters['search']) && !empty($filters['search'])) {
            $search = $filters['search'];
            $query->where(function($q) use ($search) {
                $q->where('title', 'LIKE', "%{$search}%")
                  ->orWhere('description', 'LIKE', "%{$search}%")
                  ->orWhere('release_year', 'LIKE', "%{$search}%");
            });
        }

        if (isset($filters['type']) && !empty($filters['type'])) {
            $query->where('type', $filters['type']);
        }

        return $query->orderBy('created_at', 'DESC');
    }

    /**
     * Create or update content
     */
    public function saveContent($data, $contentId = null)
    {
        if ($contentId) {
            $content = Content::find($contentId);
        } else {
            $content = new Content();
        }

        $content->title = $data['title'];
        $content->description = $data['description'];
        $content->type = $data['type'];
        $content->language_id = $data['language_id'];
        $content->genre_ids = $data['genre_ids'];
        $content->release_year = $data['release_year'];
        $content->ratings = $data['ratings'];

        return $content->save();
    }
} 