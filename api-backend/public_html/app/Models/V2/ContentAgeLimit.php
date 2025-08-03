<?php

namespace App\Models\V2;

class ContentAgeLimit extends BaseModel
{
    protected $table = 'content_age_limit';
    protected $primaryKey = 'content_age_limit_id';
    
    protected $fillable = [
        'content_id',
        'age_limit_id'
    ];
    
    protected $casts = [
        'content_id' => 'integer',
        'age_limit_id' => 'integer',
        'created_at' => 'datetime'
    ];
    
    /**
     * Get the content
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id', 'content_id');
    }
    
    /**
     * Get the age limit
     */
    public function ageLimit()
    {
        return $this->belongsTo(AgeLimit::class, 'age_limit_id', 'age_limit_id');
    }
}