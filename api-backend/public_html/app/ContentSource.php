<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ContentSource extends Model
{
    use HasFactory;
    protected $table = 'content_sources';

    public function media()
    {
        return $this->hasOne(MediaGallery::class, 'id', 'source');
    }
}
