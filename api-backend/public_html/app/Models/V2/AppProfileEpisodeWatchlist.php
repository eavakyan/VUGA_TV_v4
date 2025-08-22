<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class AppProfileEpisodeWatchlist extends Model
{
    protected $table = 'app_profile_episode_watchlist';
    
    // Use composite primary key
    protected $primaryKey = ['profile_id', 'episode_id'];
    public $incrementing = false;
    
    protected $fillable = [
        'profile_id',
        'episode_id'
    ];
    
    protected $casts = [
        'profile_id' => 'integer',
        'episode_id' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the profile that owns this watchlist item.
     */
    public function profile()
    {
        return $this->belongsTo(AppUserProfile::class, 'profile_id', 'profile_id');
    }
    
    /**
     * Get the episode associated with this watchlist item.
     */
    public function episode()
    {
        return $this->belongsTo(Episode::class, 'episode_id', 'episode_id');
    }
    
    /**
     * Override the setKeysForSaveQuery method to handle composite key
     */
    protected function setKeysForSaveQuery($query)
    {
        $keys = $this->getKeyName();
        if (!is_array($keys)) {
            return parent::setKeysForSaveQuery($query);
        }

        foreach ($keys as $keyName) {
            $query->where($keyName, '=', $this->getKeyForSaveQuery($keyName));
        }

        return $query;
    }
    
    /**
     * Get the value of the model's primary key for save queries.
     */
    protected function getKeyForSaveQuery($keyName = null)
    {
        if (is_null($keyName)) {
            $keyName = $this->getKeyName();
        }

        if (isset($this->original[$keyName])) {
            return $this->original[$keyName];
        }

        return $this->getAttribute($keyName);
    }
}