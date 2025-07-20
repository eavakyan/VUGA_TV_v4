<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Subtitle extends Model
{
    use HasFactory;
    protected $table = 'subtitles';

    public function language()
    {
        return $this->hasOne(Language::class, 'id', 'language_id');
    }
}
