<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Content extends Model
{
    use HasFactory;

    protected $table = 'contents';

    public function language()
    {
        return $this->hasOne(Language::class, 'id', 'language_id');
    }

    public function sources()
    {
        return $this->hasMany(ContentSource::class, 'content_id');
    }

    public function casts()
    {
        return $this->hasMany(ContentCast::class, 'content_id');
    }

    public function subtitles()
    {
        return $this->hasMany(Subtitle::class, 'content_id');
    }

    public function seasons()
    {
        return $this->hasMany(Season::class, 'content_id');
    }

 
}
