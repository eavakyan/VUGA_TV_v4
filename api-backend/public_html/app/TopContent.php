<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class TopContent extends Model
{
    use HasFactory;
    protected $table = 'top_contents';

    public function content()
    {
        return $this->hasOne(Content::class, 'id', 'content_id');
    }
}
