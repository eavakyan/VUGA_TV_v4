<?php

namespace App\Models\V2;

class TvCategory extends BaseModel
{
    protected $table = 'tv_category';
    protected $primaryKey = 'tv_category_id';
    
    protected $fillable = [
        'title',
        'slug',
        'image',
        'icon_url',
        'sort_order',
        'is_active',
        'description',
        'metadata'
    ];
    
    protected $casts = [
        'is_active' => 'boolean',
        'sort_order' => 'integer',
        'metadata' => 'array',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get channels in this category
     */
    public function channels()
    {
        return $this->belongsToMany(TvChannel::class, 'tv_channel_category', 'tv_category_id', 'tv_channel_id');
    }
    
    /**
     * Get active channels in this category
     */
    public function activeChannels()
    {
        return $this->belongsToMany(TvChannel::class, 'tv_channel_category', 'tv_category_id', 'tv_channel_id')
                   ->where('tv_channel.is_active', true);
    }
    
    /**
     * Scope to get only active categories
     */
    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }
    
    /**
     * Scope to get categories ordered by sort_order
     */
    public function scopeOrdered($query)
    {
        return $query->orderBy('sort_order')->orderBy('title');
    }
    
    /**
     * Scope to search categories by title
     */
    public function scopeSearch($query, $searchTerm)
    {
        return $query->where('title', 'LIKE', '%' . $searchTerm . '%');
    }
    
    /**
     * Get channels count attribute
     */
    public function getChannelsCountAttribute()
    {
        return $this->channels()->count();
    }
    
    /**
     * Get active channels count attribute
     */
    public function getActiveChannelsCountAttribute()
    {
        return $this->activeChannels()->count();
    }
    
    /**
     * Get icon URL with fallback to image
     */
    public function getIconAttribute()
    {
        return $this->icon_url ?: $this->image;
    }
    
    /**
     * Generate slug from title if not provided
     */
    public function setTitleAttribute($value)
    {
        $this->attributes['title'] = $value;
        
        if (empty($this->attributes['slug'])) {
            $this->attributes['slug'] = \Str::slug($value);
        }
    }
}