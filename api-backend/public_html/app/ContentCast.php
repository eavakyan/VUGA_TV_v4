<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ContentCast extends Model
{
    use HasFactory;

    protected $table = 'content_cast';
    protected $primaryKey = 'content_cast_id';

    public function actor()
    {
        return $this->hasOne(Actor::class, 'actor_id', 'actor_id');
    }
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->content_cast_id;
    }
}
