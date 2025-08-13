<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ContentSubtitleTrack extends Model
{
    protected $table = 'content_subtitle_tracks';
    protected $primaryKey = 'id';
    
    protected $fillable = [
        'content_id',
        'content_source_id',
        'language_id',
        'title',
        'language_code',
        'subtitle_url',
        'subtitle_format',
        'subtitle_type',
        'is_default',
        'is_forced',
        'is_sdh',
        'sort_order'
    ];

    protected $casts = [
        'is_default' => 'boolean',
        'is_forced' => 'boolean',
        'is_sdh' => 'boolean',
    ];

    public function content()
    {
        return $this->belongsTo(\App\Content::class, 'content_id', 'content_id');
    }

    public function contentSource()
    {
        return $this->belongsTo(\App\ContentSource::class, 'content_source_id', 'content_source_id');
    }

    public function language()
    {
        return $this->belongsTo(\App\Language::class, 'language_id', 'app_language_id');
    }
}