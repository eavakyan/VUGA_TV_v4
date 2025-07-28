<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;


class Language extends Model
{
    use HasFactory;

    protected $table = 'app_language';
    protected $primaryKey = 'app_language_id';
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->app_language_id;
    }
}
