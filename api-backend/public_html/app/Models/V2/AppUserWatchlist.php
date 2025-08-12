<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class AppUserWatchlist extends Model
{
    protected $table = 'app_user_watchlist';
    
    protected $fillable = [
        'profile_id',
        'content_id'
    ];
    
    /**
     * Get the profile that owns the watchlist item.
     */
    public function profile()
    {
        return $this->belongsTo(AppUserProfile::class, 'profile_id', 'profile_id');
    }
    
    /**
     * Get the content associated with the watchlist item.
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id', 'content_id');
    }
}