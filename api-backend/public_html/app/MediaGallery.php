<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class MediaGallery extends Model
{
    use HasFactory;
    protected $table = 'media_gallery';
    protected $primaryKey = 'media_gallery_id';
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->media_gallery_id;
    }
}
