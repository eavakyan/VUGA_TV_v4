<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class EpisodeSubtitle extends Model
{
    use HasFactory;
    protected $table = 'episode_subtitles';

    public function language()
    {
        return $this->hasOne(Language::class, 'id', 'language_id');
    }
}
