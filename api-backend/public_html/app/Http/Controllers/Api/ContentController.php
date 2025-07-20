<?php

namespace App\Http\Controllers\Api;

use App\Constants;
use App\Services\ContentService;
use App\Services\CacheService;
use App\Http\Controllers\Controller;
use App\Http\Requests\GetHomeContentRequest;
use App\Http\Requests\GetContentListRequest;
use App\Http\Resources\ApiResponse;
use App\Http\Resources\ContentResource;
use App\Http\Resources\GenreResource;

class ContentController extends Controller
{
    protected $contentService;
    protected $cacheService;

    public function __construct(ContentService $contentService, CacheService $cacheService)
    {
        $this->contentService = $contentService;
        $this->cacheService = $cacheService;
    }

    /**
     * Get home page data for mobile app
     */
    public function getHomeContentList(GetHomeContentRequest $request)
    {
        $homeData = $this->contentService->getHomePageData($request->user_id);
        
        if (!$homeData) {
            return ApiResponse::error('User Not Found', 404);
        }

        // Transform data using resources
        $responseData = [
            'featured' => ContentResource::collection($homeData['featured']),
            'watchlist' => ContentResource::collection($homeData['watchlist']),
            'topContents' => $homeData['topContents'], // Already has content relationship
            'genreContents' => GenreResource::collection($homeData['genreContents']),
        ];

        return ApiResponse::homeData($responseData, 'Home page data retrieved successfully');
    }

    /**
     * Get all content list with pagination
     */
    public function getAllContentList(GetContentListRequest $request)
    {
        $filters = $request->getFilters();
        $contents = $this->contentService->getContentList($filters);

        return ApiResponse::success(
            ContentResource::collection($contents),
            'Content list retrieved successfully'
        );
    }

    /**
     * Get movies list
     */
    public function getMovieList(GetContentListRequest $request)
    {
        $filters = $request->getFilters();
        $filters['type'] = Constants::movie;
        
        $contents = $this->contentService->getContentList($filters);

        return ApiResponse::success(
            ContentResource::collection($contents),
            'Movies list retrieved successfully'
        );
    }

    /**
     * Get series list
     */
    public function getSeriesList(GetContentListRequest $request)
    {
        $filters = $request->getFilters();
        $filters['type'] = Constants::series;
        
        $contents = $this->contentService->getContentList($filters);

        return ApiResponse::success(
            ContentResource::collection($contents),
            'Series list retrieved successfully'
        );
    }

    /**
     * Get all genres
     */
    public function getAllGenreList()
    {
        $genres = $this->cacheService->getGenres();

        return ApiResponse::success(
            GenreResource::collection($genres),
            'Genres retrieved successfully'
        );
    }

    /**
     * Get content list by genre ID
     */
    public function getContentListByGenreID(GetContentListRequest $request)
    {
        $filters = $request->getFilters();
        
        $contents = $this->contentService->getContentList($filters);

        return ApiResponse::success(
            ContentResource::collection($contents),
            'Content by genre retrieved successfully'
        );
    }
} 