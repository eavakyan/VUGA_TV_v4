<?php

namespace App\Services;

use App\Content;
use App\Constants;

class QueryOptimizationService
{
    /**
     * Get optimized content query with eager loading
     */
    public function getOptimizedContentQuery(array $relations = [])
    {
        $defaultRelations = ['genres', 'language'];
        $relations = array_merge($defaultRelations, $relations);
        
        return Content::with($relations)->where('is_show', Constants::showContent);
    }

    /**
     * Get content with all necessary relationships for details page
     */
    public function getContentWithDetails()
    {
        return Content::with([
            'genres',
            'language',
            'contentCast.actor',
            'sources',
            'subtitles.language',
            'seasons.episodes.sources',
            'seasons.episodes.subtitles.language'
        ])->where('is_show', Constants::showContent);
    }

    /**
     * Get content with basic relationships for listing
     */
    public function getContentWithBasics()
    {
        return Content::with([
            'genres', 
            'language'
        ])->where('is_show', Constants::showContent);
    }

    /**
     * Get featured content with relationships
     */
    public function getFeaturedContent()
    {
        return $this->getContentWithBasics()
                   ->where('is_featured', Constants::featured)
                   ->orderBy('created_at', 'DESC');
    }

    /**
     * Get content by type with optimization
     */
    public function getContentByType($type, $limit = null, $offset = null)
    {
        $query = $this->getContentWithBasics()->where('type', $type);
        
        if ($offset !== null) {
            $query->offset($offset);
        }
        
        if ($limit !== null) {
            $query->limit($limit);
        }
        
        return $query->orderBy('created_at', 'DESC');
    }

    /**
     * Search content with optimization
     */
    public function searchContentOptimized(array $filters)
    {
        $query = $this->getContentWithBasics();
        
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
        
        if (isset($filters['start'])) {
            $query->offset($filters['start']);
        }
        
        if (isset($filters['limit'])) {
            $query->limit($filters['limit']);
        }
        
        return $query->orderBy('created_at', 'DESC');
    }

    /**
     * Get similar content based on genres (optimized)
     */
    public function getSimilarContent($content, $limit = 10)
    {
        if (empty($content->genre_ids)) {
            return $this->getContentWithBasics()
                       ->where('type', $content->type)
                       ->where('id', '!=', $content->id)
                       ->inRandomOrder()
                       ->limit($limit);
        }

        $genreIds = explode(',', $content->genre_ids);
        
        return $this->getContentWithBasics()
                   ->where(function ($query) use ($genreIds) {
                       foreach ($genreIds as $genreId) {
                           $query->orWhereRaw('FIND_IN_SET(?, genre_ids)', [$genreId]);
                       }
                   })
                   ->where('id', '!=', $content->id)
                   ->inRandomOrder()
                   ->limit($limit);
    }

    /**
     * Get content by IDs with optimization
     */
    public function getContentByIds(array $ids)
    {
        if (empty($ids)) {
            return collect();
        }

        return $this->getContentWithBasics()
                   ->whereIn('id', $ids)
                   ->orderByRaw('FIELD(id, ' . implode(',', $ids) . ')');
    }
} 