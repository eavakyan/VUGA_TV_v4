<?php

namespace App\Models\V2;

use Carbon\Carbon;

class LiveTvSchedule extends BaseModel
{
    protected $table = 'live_tv_schedule';
    protected $primaryKey = 'schedule_id';

    protected $fillable = [
        'tv_channel_id',
        'program_title',
        'description',
        'thumbnail_url',
        'genre',
        'start_time',
        'end_time',
        'is_repeat',
        'episode_number',
        'season_number',
        'original_air_year',
        'rating',
        'metadata'
    ];

    protected $casts = [
        'start_time' => 'datetime',
        'end_time' => 'datetime',
        'is_repeat' => 'boolean',
        'original_air_year' => 'integer',
        'metadata' => 'array',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];

    /**
     * Get the channel this schedule belongs to
     */
    public function channel()
    {
        return $this->belongsTo(TvChannel::class, 'tv_channel_id', 'tv_channel_id');
    }

    /**
     * Check if program is currently airing
     */
    public function getIsCurrentlyAiringAttribute()
    {
        $now = Carbon::now();
        return $now->between($this->start_time, $this->end_time);
    }

    /**
     * Get program duration in minutes
     */
    public function getDurationInMinutesAttribute()
    {
        return $this->start_time->diffInMinutes($this->end_time);
    }

    /**
     * Get formatted air time
     */
    public function getFormattedAirTimeAttribute()
    {
        return $this->start_time->format('H:i') . ' - ' . $this->end_time->format('H:i');
    }

    /**
     * Check if program has ended
     */
    public function getHasEndedAttribute()
    {
        return Carbon::now()->gt($this->end_time);
    }

    /**
     * Get time until program starts (in minutes)
     */
    public function getMinutesUntilStartAttribute()
    {
        $now = Carbon::now();
        if ($now->gt($this->start_time)) {
            return 0;
        }
        return $now->diffInMinutes($this->start_time);
    }

    /**
     * Get progress percentage for currently airing program
     */
    public function getProgressPercentageAttribute()
    {
        if (!$this->is_currently_airing) {
            return 0;
        }

        $now = Carbon::now();
        $totalDuration = $this->start_time->diffInMinutes($this->end_time);
        $elapsed = $this->start_time->diffInMinutes($now);
        
        return min(100, ($elapsed / $totalDuration) * 100);
    }

    /**
     * Scope to get programs for a specific channel
     */
    public function scopeForChannel($query, $channelId)
    {
        return $query->where('tv_channel_id', $channelId);
    }

    /**
     * Scope to get programs within a time range
     */
    public function scopeWithinTimeRange($query, $startTime, $endTime)
    {
        return $query->where(function ($q) use ($startTime, $endTime) {
            $q->whereBetween('start_time', [$startTime, $endTime])
              ->orWhereBetween('end_time', [$startTime, $endTime])
              ->orWhere(function ($q2) use ($startTime, $endTime) {
                  $q2->where('start_time', '<=', $startTime)
                     ->where('end_time', '>=', $endTime);
              });
        });
    }

    /**
     * Scope to get currently airing programs
     */
    public function scopeCurrentlyAiring($query)
    {
        $now = Carbon::now();
        return $query->where('start_time', '<=', $now)
                    ->where('end_time', '>', $now);
    }

    /**
     * Scope to get upcoming programs
     */
    public function scopeUpcoming($query, $hours = 24)
    {
        $now = Carbon::now();
        $endTime = $now->copy()->addHours($hours);
        
        return $query->where('start_time', '>', $now)
                    ->where('start_time', '<=', $endTime)
                    ->orderBy('start_time');
    }

    /**
     * Scope to search programs by title
     */
    public function scopeSearchByTitle($query, $searchTerm)
    {
        return $query->where('program_title', 'LIKE', '%' . $searchTerm . '%');
    }

    /**
     * Scope to filter by genre
     */
    public function scopeByGenre($query, $genre)
    {
        return $query->where('genre', $genre);
    }
}