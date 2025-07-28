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
        return $this->hasOne(Actor::class, 'id', 'actor_id');
    }
}
