<?php

namespace App\Models\V2;

class TvAuthSession extends BaseModel
{
    protected $table = 'tv_auth_session';
    protected $primaryKey = 'tv_auth_session_id';
    
    // This table doesn't have updated_at column
    const UPDATED_AT = null;
    
    protected $fillable = [
        'session_token',
        'qr_code',
        'app_user_id',
        'tv_device_id',
        'status',
        'expires_at',
        'authenticated_at'
    ];
    
    protected $casts = [
        'app_user_id' => 'integer',
        'created_at' => 'datetime',
        'expires_at' => 'datetime',
        'authenticated_at' => 'datetime'
    ];
    
    /**
     * Get the session's user
     */
    public function user()
    {
        return $this->belongsTo(AppUser::class, 'app_user_id');
    }
    
    /**
     * Scope for pending sessions
     */
    public function scopePending($query)
    {
        return $query->where('status', 'pending');
    }
    
    /**
     * Scope for authenticated sessions
     */
    public function scopeAuthenticated($query)
    {
        return $query->where('status', 'authenticated');
    }
    
    /**
     * Scope for expired sessions
     */
    public function scopeExpired($query)
    {
        return $query->where('status', 'expired')
                     ->orWhere('expires_at', '<', now());
    }
}