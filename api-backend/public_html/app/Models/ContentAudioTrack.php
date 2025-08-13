<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ContentAudioTrack extends Model
{
    protected $table = 'content_audio_tracks';
    protected $primaryKey = 'id';
    
    protected $fillable = [
        'content_id',
        'content_source_id',
        'language_id',
        'title',
        'language_code',
        'audio_url',
        'audio_format',
        'audio_channels',
        'audio_bitrate',
        'is_primary',
        'is_default',
        'sort_order'
    ];

    protected $casts = [
        'is_primary' => 'boolean',
        'is_default' => 'boolean',
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