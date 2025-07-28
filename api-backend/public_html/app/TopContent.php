<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class TopContent extends Model
{
    use HasFactory;
    protected $table = 'top_content';
    protected $primaryKey = 'top_content_id';

    public function content()
    {
        return $this->hasOne(Content::class, 'id', 'content_id');
    }
}
