<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class DefaultAvatar extends Model
{
    protected $table = 'default_avatar';
    protected $primaryKey = 'avatar_id';
    public $timestamps = false;
    
    protected $fillable = [
        'name',
        'image_url',
        'color',
        'is_active'
    ];
    
    protected $casts = [
        'is_active' => 'boolean'
    ];
    
    // Scopes
    public function scopeActive($query)
    {
        return $query->where('is_active', 1);
    }
}