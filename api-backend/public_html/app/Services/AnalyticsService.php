<?php

namespace App\Services;

use App\Content;
use App\Episode;
use App\TVChannel;

class AnalyticsService
{
    /**
     * Increment content view count
     */
    public function incrementContentView($contentId)
    {
        $content = Content::find($contentId);
        
        if (!$content) {
            return ['success' => false, 'message' => 'Content not found'];
        }

        $content->total_view += 1;
        $content->save();

        return ['success' => true, 'message' => 'Content view incremented successfully', 'data' => $content];
    }

    /**
     * Increment content download count
     */
    public function incrementContentDownload($contentId)
    {
        $content = Content::find($contentId);
        
        if (!$content) {
            return ['success' => false, 'message' => 'Content not found'];
        }

        $content->total_download += 1;
        $content->save();

        return ['success' => true, 'message' => 'Content download incremented successfully', 'data' => $content];
    }

    /**
     * Increment content share count
     */
    public function incrementContentShare($contentId)
    {
        $content = Content::find($contentId);
        
        if (!$content) {
            return ['success' => false, 'message' => 'Content not found'];
        }

        $content->total_share += 1;
        $content->save();

        return ['success' => true, 'message' => 'Content share incremented successfully', 'data' => $content];
    }

    /**
     * Increment episode view count
     */
    public function incrementEpisodeView($episodeId)
    {
        $episode = Episode::find($episodeId);
        
        if (!$episode) {
            return ['success' => false, 'message' => 'Episode not found'];
        }

        $episode->total_view += 1;
        $episode->save();

        return ['success' => true, 'message' => 'Episode view incremented successfully', 'data' => $episode];
    }

    /**
     * Increment episode download count
     */
    public function incrementEpisodeDownload($episodeId)
    {
        $episode = Episode::find($episodeId);
        
        if (!$episode) {
            return ['success' => false, 'message' => 'Episode not found'];
        }

        $episode->total_download += 1;
        $episode->save();

        return ['success' => true, 'message' => 'Episode download incremented successfully', 'data' => $episode];
    }

    /**
     * Increment TV channel view count
     */
    public function incrementTVChannelView($channelId)
    {
        $channel = TVChannel::find($channelId);
        
        if (!$channel) {
            return ['success' => false, 'message' => 'TV Channel not found'];
        }

        $channel->total_view += 1;
        $channel->save();

        return ['success' => true, 'message' => 'TV Channel view incremented successfully', 'data' => $channel];
    }

    /**
     * Increment TV channel share count
     */
    public function incrementTVChannelShare($channelId)
    {
        $channel = TVChannel::find($channelId);
        
        if (!$channel) {
            return ['success' => false, 'message' => 'TV Channel not found'];
        }

        $channel->total_share += 1;
        $channel->save();

        return ['success' => true, 'message' => 'TV Channel share incremented successfully', 'data' => $channel];
    }

    /**
     * Get content analytics summary
     */
    public function getContentAnalytics($contentId)
    {
        $content = Content::find($contentId);
        
        if (!$content) {
            return null;
        }

        return [
            'content_id' => $content->id,
            'title' => $content->title,
            'total_views' => $content->total_view,
            'total_downloads' => $content->total_download,
            'total_shares' => $content->total_share
        ];
    }

    /**
     * Get episode analytics summary
     */
    public function getEpisodeAnalytics($episodeId)
    {
        $episode = Episode::find($episodeId);
        
        if (!$episode) {
            return null;
        }

        return [
            'episode_id' => $episode->id,
            'title' => $episode->title,
            'total_views' => $episode->total_view,
            'total_downloads' => $episode->total_download
        ];
    }

    /**
     * Get top viewed content
     */
    public function getTopViewedContent($limit = 10, $type = null)
    {
        $query = Content::where('is_show', \App\Constants::showContent)
                       ->orderBy('total_view', 'DESC');

        if ($type) {
            $query->where('type', $type);
        }

        return $query->limit($limit)->get();
    }

    /**
     * Get analytics dashboard data
     */
    public function getDashboardAnalytics()
    {
        $totalContent = Content::count();
        $totalMovies = Content::where('type', \App\Constants::movie)->count();
        $totalSeries = Content::where('type', \App\Constants::series)->count();
        $totalViews = Content::sum('total_view');
        $totalDownloads = Content::sum('total_download');
        $totalShares = Content::sum('total_share');

        return [
            'total_content' => $totalContent,
            'total_movies' => $totalMovies,
            'total_series' => $totalSeries,
            'total_views' => $totalViews,
            'total_downloads' => $totalDownloads,
            'total_shares' => $totalShares,
            'top_viewed_content' => $this->getTopViewedContent(5)
        ];
    }
} 