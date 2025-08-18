<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class User extends Model
{
    use HasFactory;
    protected $table = 'app_user';
    protected $primaryKey = 'app_user_id';
    
    protected $fillable = [
        'fullname',
        'email',
        'login_type',
        'identity',
        'profile_image',
        'watchlist_content_ids',
        'device_type',
        'device_token',
        'status',
        'is_premium',
        'email_consent',
        'sms_consent',
        'consent_updated_at'
    ];
    
    protected $casts = [
        'login_type' => 'integer',
        'device_type' => 'integer',
        'status' => 'integer',
        'is_premium' => 'integer',
        'email_consent' => 'boolean',
        'sms_consent' => 'boolean',
        'consent_updated_at' => 'datetime'
    ];
    
    // Add accessor for backward compatibility with 'id' field
    public function getIdAttribute()
    {
        return $this->app_user_id;
    }
}
