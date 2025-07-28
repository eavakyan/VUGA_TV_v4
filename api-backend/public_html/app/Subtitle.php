<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Subtitle extends Model
{
    use HasFactory;
    protected $table = 'subtitle';
    protected $primaryKey = 'subtitle_id';

    public function language()
    {
        return $this->hasOne(Language::class, 'app_language_id', 'language_id');
    }
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->subtitle_id;
    }
}
