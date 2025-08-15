<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Profile extends Model
{
    use HasFactory;

    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'app_user_profile';

    /**
     * The primary key associated with the table.
     *
     * @var string
     */
    protected $primaryKey = 'profile_id';

    /**
     * Indicates if the model should be timestamped.
     *
     * @var bool
     */
    public $timestamps = true;

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'app_user_id',
        'name',
        'avatar_type',
        'avatar_id',
        'avatar_url',
        'avatar_color',
        'custom_avatar_url',
        'custom_avatar_uploaded_at',
        'age',
        'date_of_birth',
        'is_kids',
        'is_kids_profile',
        'is_active',
        'created_at',
        'updated_at'
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'custom_avatar_uploaded_at' => 'datetime',
        'date_of_birth' => 'date',
        'is_kids' => 'boolean',
        'is_kids_profile' => 'boolean',
        'is_active' => 'boolean',
        'age' => 'integer',
        'avatar_id' => 'integer',
        'app_user_id' => 'integer',
        'profile_id' => 'integer'
    ];
    
    /**
     * The accessors to append to the model's array form.
     *
     * @var array
     */
    protected $appends = ['avatar_url', 'avatar_color'];

    /**
     * Get the user that owns the profile.
     */
    public function user()
    {
        return $this->belongsTo(User::class, 'app_user_id', 'app_user_id');
    }
    
    /**
     * Get the avatar_url attribute - returns custom_avatar_url if avatar_type is custom
     */
    public function getAvatarUrlAttribute()
    {
        if ($this->avatar_type === 'custom' && $this->custom_avatar_url) {
            // Fix double slash issue in URLs
            return preg_replace('#(?<!:)//#', '/', $this->custom_avatar_url);
        }
        return null;
    }
    
    /**
     * Get the avatar_color attribute from default_avatar table
     */
    public function getAvatarColorAttribute()
    {
        if ($this->avatar_id) {
            $defaultAvatar = \DB::table('default_avatar')
                ->where('avatar_id', $this->avatar_id)
                ->first();
            return $defaultAvatar ? $defaultAvatar->color : '#4A90E2';
        }
        return '#4A90E2'; // Default blue color
    }
}