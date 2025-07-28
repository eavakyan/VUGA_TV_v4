<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ContentSource extends Model
{
    use HasFactory;
    protected $table = 'content_source';
    protected $primaryKey = 'content_source_id';

    public function media()
    {
        return $this->hasOne(MediaGallery::class, 'media_gallery_id', 'source');
    }
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->content_source_id;
    }
}
