<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;


class Category extends Model
{
    use HasFactory;
    protected $table = 'category';
    protected $primaryKey = 'category_id';
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->category_id;
    }
    
    /**
     * Get the category's contents
     */
    public function contents()
    {
        return $this->belongsToMany('App\Content', 'content_category', 'category_id', 'content_id');
    }
}
