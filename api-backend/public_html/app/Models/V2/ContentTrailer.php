<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ContentTrailer extends Model
{
    protected $table = 'content_trailer';
    protected $primaryKey = 'content_trailer_id';
    
    protected $fillable = [
        'content_id',
        'title',
        'youtube_id',
        'trailer_url',
        'is_primary',
        'sort_order'
    ];

    protected $casts = [
        'is_primary' => 'boolean',
        'sort_order' => 'integer',
        'content_id' => 'integer'
    ];

    /**
     * Get the content that owns the trailer
     */
    public function content(): BelongsTo
    {
        return $this->belongsTo(Content::class, 'content_id', 'content_id');
    }

    /**
     * Get the primary trailer for content
     */
    public static function getPrimaryTrailer($contentId)
    {
        return self::where('content_id', $contentId)
                   ->where('is_primary', true)
                   ->orderBy('sort_order')
                   ->first();
    }

    /**
     * Get all trailers for content ordered by sort_order
     */
    public static function getContentTrailers($contentId)
    {
        return self::where('content_id', $contentId)
                   ->orderBy('is_primary', 'desc')
                   ->orderBy('sort_order')
                   ->get();
    }

    /**
     * Get YouTube embed URL (only for YouTube videos)
     */
    public function getEmbedUrlAttribute()
    {
        if ($this->youtube_id) {
            return "https://www.youtube.com/embed/{$this->youtube_id}";
        }
        return null;
    }

    /**
     * Get YouTube watch URL (only for YouTube videos)
     */
    public function getWatchUrlAttribute()
    {
        if ($this->youtube_id) {
            return "https://www.youtube.com/watch?v={$this->youtube_id}";
        }
        return null;
    }

    /**
     * Get YouTube thumbnail URL (only for YouTube videos)
     */
    public function getThumbnailUrlAttribute()
    {
        if ($this->youtube_id) {
            return "https://img.youtube.com/vi/{$this->youtube_id}/maxresdefault.jpg";
        }
        return null;
    }

    /**
     * Scope to get only primary trailers
     */
    public function scopePrimary($query)
    {
        return $query->where('is_primary', true);
    }

    /**
     * Scope to get trailers ordered by display order
     */
    public function scopeOrdered($query)
    {
        return $query->orderBy('is_primary', 'desc')
                     ->orderBy('sort_order');
    }

    /**
     * Set a trailer as primary (and unset others for the same content)
     */
    public function setPrimary()
    {
        // First, unset all primary flags for this content
        self::where('content_id', $this->content_id)
            ->update(['is_primary' => false]);
        
        // Then set this one as primary
        $this->is_primary = true;
        $this->save();
        
        return $this;
    }

    /**
     * Extract YouTube ID from various URL formats
     */
    public static function extractYouTubeId($url)
    {
        $patterns = [
            // Standard YouTube URL
            '/(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/)([a-zA-Z0-9_-]{11})/',
            // YouTube short URL
            '/youtu\.be\/([a-zA-Z0-9_-]{11})/',
            // YouTube embed URL
            '/youtube\.com\/embed\/([a-zA-Z0-9_-]{11})/'
        ];
        
        foreach ($patterns as $pattern) {
            if (preg_match($pattern, $url, $matches)) {
                return $matches[1];
            }
        }
        
        // If it's already just an ID (11 characters)
        if (strlen($url) === 11 && preg_match('/^[a-zA-Z0-9_-]{11}$/', $url)) {
            return $url;
        }
        
        return null;
    }

    /**
     * Create a new trailer from URL (supports YouTube, MP4, HLS, etc.)
     */
    public static function createFromUrl($contentId, $url, $title = null, $isPrimary = false, $sortOrder = 0)
    {
        // Extract YouTube ID if it's a YouTube URL, otherwise leave null
        $youtubeId = self::extractYouTubeId($url);
        
        // Validate that we have a valid URL
        if (empty($url) || !filter_var($url, FILTER_VALIDATE_URL)) {
            throw new \InvalidArgumentException('Invalid URL provided');
        }
        
        $trailer = new self([
            'content_id' => $contentId,
            'title' => $title ?: 'Trailer',
            'youtube_id' => $youtubeId, // Can be null for non-YouTube URLs
            'trailer_url' => $url,
            'is_primary' => $isPrimary,
            'sort_order' => $sortOrder
        ]);
        
        $trailer->save();
        
        if ($isPrimary) {
            $trailer->setPrimary();
        }
        
        return $trailer;
    }
    
    /**
     * Check if this trailer is a YouTube video
     */
    public function isYouTube()
    {
        return !empty($this->youtube_id);
    }
    
    /**
     * Get the type of trailer (youtube, mp4, hls, other)
     */
    public function getTrailerType()
    {
        if ($this->isYouTube()) {
            return 'youtube';
        }
        
        $extension = strtolower(pathinfo($this->trailer_url, PATHINFO_EXTENSION));
        
        if (in_array($extension, ['mp4', 'mov', 'avi', 'webm'])) {
            return 'video';
        }
        
        if (in_array($extension, ['m3u8', 'm3u'])) {
            return 'hls';
        }
        
        return 'other';
    }
}