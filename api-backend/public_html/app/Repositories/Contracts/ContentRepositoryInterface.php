<?php

namespace App\Repositories\Contracts;

interface ContentRepositoryInterface
{
    /**
     * Get content by ID
     */
    public function findById($id);

    /**
     * Get content by ID with relationships
     */
    public function findByIdWithRelations($id, $relations = []);

    /**
     * Get active content by ID
     */
    public function findActiveById($id);

    /**
     * Get featured content
     */
    public function getFeaturedContent();

    /**
     * Get content by type
     */
    public function getContentByType($type, $start = 0, $limit = 10);

    /**
     * Get content by genre
     */
    public function getContentByGenre($genreId, $start = 0, $limit = 10);

    /**
     * Get content by genre (random order)
     */
    public function getRandomContentByGenre($genreId, $limit = 10);

    /**
     * Search content
     */
    public function searchContent($filters = [], $start = 0, $limit = 10);

    /**
     * Get similar content based on genres
     */
    public function getSimilarContent($content, $limit = 10);

    /**
     * Get content for admin with search and filters
     */
    public function getContentForAdmin($filters = []);

    /**
     * Create new content
     */
    public function create($data);

    /**
     * Update content
     */
    public function update($id, $data);

    /**
     * Delete content
     */
    public function delete($id);

    /**
     * Update content feature status
     */
    public function updateFeatureStatus($id, $status);

    /**
     * Update content analytics
     */
    public function incrementAnalytics($id, $field);

    /**
     * Get content count by type
     */
    public function getCountByType($type);

    /**
     * Get top viewed content
     */
    public function getTopViewedContent($limit = 10, $type = null);

    /**
     * Get content by IDs
     */
    public function getContentByIds($ids);

    /**
     * Get all content for sitemap or export
     */
    public function getAllActiveContent();
} 