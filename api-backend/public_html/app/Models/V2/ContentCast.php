<?php

namespace App\Models\V2;

class ContentCast extends BaseModel
{
    protected $table = 'content_cast';
    protected $primaryKey = 'content_cast_id';
    
    protected $fillable = [
        'content_id',
        'actor_id',
        'character_name'
    ];
    
    protected $casts = [
        'content_id' => 'integer',
        'actor_id' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the cast record's content
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id');
    }
    
    /**
     * Get the cast record's actor
     */
    public function actor()
    {
        return $this->belongsTo(Actor::class, 'actor_id');
    }
}