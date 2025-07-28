<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;


class EpisodeSource extends Model
{
    use HasFactory;
    protected $table = 'episode_sources';

    public function media()
    {
        return $this->hasOne(MediaGallery::class, 'media_gallery_id', 'source');
    }

}
