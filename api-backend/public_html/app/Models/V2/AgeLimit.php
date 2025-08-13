<?php

namespace App\Models\V2;

class AgeLimit extends BaseModel
{
    protected $table = 'age_limit';
    protected $primaryKey = 'age_limit_id';
    
    protected $fillable = [
        'name',
        'min_age',
        'max_age',
        'description',
        'code',
        'display_name',
        'icon',
        'display_color'
    ];
    
    protected $casts = [
        'min_age' => 'integer',
        'max_age' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the contents that have this age limit
     */
    public function contents()
    {
        return $this->belongsToMany(Content::class, 'content_age_limit', 'age_limit_id', 'content_id')
                    ->withTimestamps();
    }
    
    /**
     * Check if this age limit is appropriate for kids
     */
    public function isKidsFriendly()
    {
        return $this->max_age !== null && $this->max_age <= 12;
    }
    
    /**
     * Check if a given age can access this content
     */
    public function canAccessByAge($age)
    {
        if ($age === null) {
            return true; // No age restriction if age not set
        }
        
        return $age >= $this->min_age;
    }
    
    /**
     * Get display color for age group
     * Returns stored display_color or falls back to computed color
     */
    public function getDisplayColorAttribute()
    {
        // Return database value if set
        if (!empty($this->attributes['display_color'])) {
            return $this->attributes['display_color'];
        }
        
        // Fallback to computed value
        switch ($this->code) {
            case 'AG_0_6':
                return '#4CAF50'; // Green
            case 'AG_7_12':
                return '#8BC34A'; // Light Green
            case 'AG_13_16':
                return '#FF9800'; // Orange
            case 'AG_17_18':
                return '#F44336'; // Red
            case 'AG_18_PLUS':
                return '#9C27B0'; // Purple
            default:
                return '#757575'; // Gray
        }
    }
    
    /**
     * Get user-friendly display name
     * Returns stored display_name or falls back to computed name
     */
    public function getDisplayNameAttribute()
    {
        // Return database value if set
        if (!empty($this->attributes['display_name'])) {
            return $this->attributes['display_name'];
        }
        
        // Fallback to computed value
        switch ($this->code) {
            case 'AG_0_6':
                return 'All Ages';
            case 'AG_7_12':
                return '7+';
            case 'AG_13_16':
                return '13+';
            case 'AG_17_18':
                return '17+';
            case 'AG_18_PLUS':
                return '18+';
            default:
                return $this->name ?? $this->code;
        }
    }
    
    /**
     * Get icon identifier for the age rating
     */
    public function getIconAttribute()
    {
        // Return database value if set
        if (!empty($this->attributes['icon'])) {
            return $this->attributes['icon'];
        }
        
        // Fallback to computed value
        switch ($this->code) {
            case 'AG_0_6':
                return 'family';
            case 'AG_7_12':
                return 'child';
            case 'AG_13_16':
                return 'teen';
            case 'AG_17_18':
                return 'mature';
            case 'AG_18_PLUS':
                return 'adult';
            default:
                return 'general';
        }
    }
}