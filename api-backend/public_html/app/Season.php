<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Season extends Model
{
    use HasFactory;
    protected $table = 'season';
    protected $primaryKey = 'season_id';

    public function episodes()
    {
        return $this->hasMany(Episode::class, 'season_id', 'season_id')->orderBy('number');
    }
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->season_id;
    }
}
