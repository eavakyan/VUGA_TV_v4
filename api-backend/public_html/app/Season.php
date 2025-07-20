<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Season extends Model
{
    use HasFactory;
    protected $table = 'seasons';

    public function episodes()
    {
        return $this->hasMany(Episode::class, 'season_id', 'id')->orderBy('number');
    }
}
