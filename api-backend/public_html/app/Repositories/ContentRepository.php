<?php

namespace App\Repositories;

use App\Constants;
use App\Content;
use App\Genre;
use App\TopContent;
use App\Repositories\Contracts\ContentRepositoryInterface;

class ContentRepository implements ContentRepositoryInterface
{
    /**
     * Get content by ID
     */
    public function findById($id)
    {
        return Content::find($id);
    }

    /**
     * Get content by ID with relationships
     */
    public function findByIdWithRelations($id, $relations = [])
    {
        return Content::with($relations)->find($id);
    }

    /**
     * Get active content by ID
     */
    public function findActiveById($id)
    {
        return Content::where('id', $id)
                     ->where('is_show', Constants::showContent)
                     ->first();
    }

    /**
     * Get featured content
     */
    public function getFeaturedContent()
    {
        return Content::where('is_featured', Constants::featured)
                     ->where('is_show', Constants::showContent)
                     ->get();
    }

    /**
     * Get content by type
     */
    public function getContentByType($type, $start = 0, $limit = 10)
    {
        return Content::where('type', $type)
                     ->where('is_show', Constants::showContent)
                     ->orderBy('created_at', 'DESC')
                     ->offset($start)
                     ->limit($limit)
                     ->get();
    }

    /**
     * Get content by genre
     */
    public function getContentByGenre($genreId, $start = 0, $limit = 10)
    {
        return Content::where('is_show', Constants::showContent)
                     ->whereRaw('FIND_IN_SET(?, genre_ids)', [$genreId])
                     ->orderBy('created_at', 'DESC')
                     ->offset($start)
                     ->limit($limit)
                     ->get();
    }

    /**
     * Get content by genre (random order)
     */
    public function getRandomContentByGenre($genreId, $limit = 10)
    {
        return Content::where('is_show', Constants::showContent)
                     ->whereRaw('FIND_IN_SET(?, genre_ids)', [$genreId])
                     ->inRandomOrder()
                     ->limit($limit)
                     ->get();
    }

    /**
     * Search content
     */
    public function searchContent($filters = [], $start = 0, $limit = 10)
    {
        $query = Content::where('is_show', Constants::showContent);

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

        return $query->orderBy('created_at', 'DESC')
                    ->offset($start)
                    ->limit($limit)
                    ->get();
    }

    /**
     * Get similar content based on genres
     */
    public function getSimilarContent($content, $limit = 10)
    {
        $genreIds = explode(',', $content->genre_ids);
        
        $query = Content::where('is_show', Constants::showContent)
                       ->where('id', '!=', $content->id);

        if (!empty($genreIds)) {
            $query->where(function ($q) use ($genreIds) {
                foreach ($genreIds as $genreId) {
                    $q->orWhereRaw('FIND_IN_SET(?, genre_ids)', [$genreId]);
                }
            });
        }

        $similarContent = $query->inRandomOrder()->limit($limit)->get();

        // Fallback to same type if no genre matches
        if ($similarContent->isEmpty()) {
            $similarContent = Content::where('is_show', Constants::showContent)
                                    ->where('type', $content->type)
                                    ->where('id', '!=', $content->id)
                                    ->inRandomOrder()
                                    ->limit($limit)
                                    ->get();
        }

        return $similarContent;
    }

    /**
     * Get content for admin with search and filters
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
     * Create new content
     */
    public function create($data)
    {
        return Content::create($data);
    }

    /**
     * Update content
     */
    public function update($id, $data)
    {
        $content = Content::find($id);
        if ($content) {
            $content->update($data);
            return $content;
        }
        return null;
    }

    /**
     * Delete content
     */
    public function delete($id)
    {
        $content = Content::find($id);
        if ($content) {
            return $content->delete();
        }
        return false;
    }

    /**
     * Update content feature status
     */
    public function updateFeatureStatus($id, $status)
    {
        return Content::where('id', $id)->update(['is_featured' => $status]);
    }

    /**
     * Update content analytics
     */
    public function incrementAnalytics($id, $field)
    {
        return Content::where('id', $id)->increment($field);
    }

    /**
     * Get content count by type
     */
    public function getCountByType($type)
    {
        return Content::where('type', $type)->count();
    }

    /**
     * Get top viewed content
     */
    public function getTopViewedContent($limit = 10, $type = null)
    {
        $query = Content::where('is_show', Constants::showContent)
                       ->orderBy('total_view', 'DESC');

        if ($type) {
            $query->where('type', $type);
        }

        return $query->limit($limit)->get();
    }

    /**
     * Get content by IDs
     */
    public function getContentByIds($ids)
    {
        return Content::where('is_show', Constants::showContent)
                     ->whereIn('id', $ids)
                     ->orderBy('created_at', 'DESC')
                     ->get();
    }

    /**
     * Get all content for sitemap or export
     */
    public function getAllActiveContent()
    {
        return Content::where('is_show', Constants::showContent)
                     ->orderBy('created_at', 'DESC')
                     ->get();
    }
} 