<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;


class Genre extends Model
{
    use HasFactory;
    protected $table = 'genre';
    protected $primaryKey = 'genre_id';
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->genre_id;
    }
}
