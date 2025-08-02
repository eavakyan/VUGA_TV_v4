<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

class AppUserProfile extends Model
{
    protected $table = 'app_user_profile';
    protected $primaryKey = 'profile_id';
    
    protected $fillable = [
        'app_user_id',
        'name',
        'avatar_type',
        'avatar_id',
        'custom_avatar_url',
        'is_kids',
        'is_active'
    ];
    
    protected $casts = [
        'is_kids' => 'boolean',
        'is_active' => 'boolean',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    // Maximum profiles per user
    const MAX_PROFILES_PER_USER = 4;
    
    // Relationships
    public function user(): BelongsTo
    {
        return $this->belongsTo(AppUser::class, 'app_user_id', 'app_user_id');
    }
    
    public function defaultAvatar(): BelongsTo
    {
        return $this->belongsTo(DefaultAvatar::class, 'avatar_id', 'avatar_id');
    }
    
    public function watchlist(): BelongsToMany
    {
        return $this->belongsToMany(Content::class, 'app_user_watchlist', 'profile_id', 'content_id')
            ->withTimestamps();
    }
    
    public function favorites(): BelongsToMany
    {
        return $this->belongsToMany(Content::class, 'app_profile_favorite', 'profile_id', 'content_id')
            ->withTimestamps();
    }
    
    public function ratings(): BelongsToMany
    {
        return $this->belongsToMany(Content::class, 'app_profile_rating', 'profile_id', 'content_id')
            ->withPivot('rating')
            ->withTimestamps();
    }
    
    public function watchHistory(): HasMany
    {
        return $this->hasMany(AppUserWatchHistory::class, 'profile_id', 'profile_id');
    }
    
    public function downloads(): HasMany
    {
        return $this->hasMany(ProfileDownload::class, 'profile_id', 'profile_id');
    }
    
    // Methods
    public function getAvatarUrlAttribute()
    {
        if ($this->avatar_type === 'custom' && $this->custom_avatar_url) {
            return $this->custom_avatar_url;
        }
        
        return $this->defaultAvatar ? $this->defaultAvatar->image_url : null;
    }
    
    public function getAvatarColorAttribute()
    {
        return $this->defaultAvatar ? $this->defaultAvatar->color : null;
    }
    
    // Scopes
    public function scopeActive($query)
    {
        return $query->where('is_active', 1);
    }
    
    public function scopeAdult($query)
    {
        return $query->where('is_kids', 0);
    }
    
    public function scopeKids($query)
    {
        return $query->where('is_kids', 1);
    }
}