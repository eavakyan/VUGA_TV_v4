<?php

namespace App\Models\V2;

class Subtitle extends BaseModel
{
    protected $table = 'subtitle';
    protected $primaryKey = 'subtitle_id';
    
    protected $fillable = [
        'content_id',
        'language_id',
        'file'
    ];
    
    protected $casts = [
        'content_id' => 'integer',
        'language_id' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the subtitle's content
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id');
    }
    
    /**
     * Get the subtitle's language
     */
    public function language()
    {
        return $this->belongsTo(AppLanguage::class, 'language_id', 'app_language_id');
    }
}