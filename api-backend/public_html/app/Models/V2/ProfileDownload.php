<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ProfileDownload extends Model
{
    protected $table = 'profile_download';
    protected $primaryKey = 'download_id';
    const UPDATED_AT = null;
    
    protected $fillable = [
        'profile_id',
        'content_id',
        'episode_id',
        'source_id',
        'download_path',
        'file_size',
        'status',
        'progress',
        'started_at',
        'completed_at'
    ];
    
    protected $casts = [
        'file_size' => 'integer',
        'progress' => 'integer',
        'started_at' => 'datetime',
        'completed_at' => 'datetime',
        'created_at' => 'datetime'
    ];
    
    // Status constants
    const STATUS_PENDING = 'pending';
    const STATUS_DOWNLOADING = 'downloading';
    const STATUS_COMPLETED = 'completed';
    const STATUS_FAILED = 'failed';
    const STATUS_DELETED = 'deleted';
    
    // Relationships
    public function profile(): BelongsTo
    {
        return $this->belongsTo(AppUserProfile::class, 'profile_id', 'profile_id');
    }
    
    public function content(): BelongsTo
    {
        return $this->belongsTo(Content::class, 'content_id', 'content_id');
    }
    
    public function episode(): BelongsTo
    {
        return $this->belongsTo(Episode::class, 'episode_id', 'episode_id');
    }
    
    public function source(): BelongsTo
    {
        return $this->belongsTo(ContentSource::class, 'source_id', 'content_source_id');
    }
    
    // Methods
    public function getFileSizeFormattedAttribute()
    {
        $bytes = $this->file_size;
        if ($bytes >= 1073741824) {
            return number_format($bytes / 1073741824, 2) . ' GB';
        } elseif ($bytes >= 1048576) {
            return number_format($bytes / 1048576, 2) . ' MB';
        } elseif ($bytes >= 1024) {
            return number_format($bytes / 1024, 2) . ' KB';
        } elseif ($bytes > 1) {
            return $bytes . ' bytes';
        } elseif ($bytes == 1) {
            return $bytes . ' byte';
        } else {
            return '0 bytes';
        }
    }
    
    // Scopes
    public function scopeByStatus($query, $status)
    {
        return $query->where('status', $status);
    }
    
    public function scopeCompleted($query)
    {
        return $query->where('status', self::STATUS_COMPLETED);
    }
    
    public function scopeDownloading($query)
    {
        return $query->where('status', self::STATUS_DOWNLOADING);
    }
    
    public function scopePending($query)
    {
        return $query->where('status', self::STATUS_PENDING);
    }
}