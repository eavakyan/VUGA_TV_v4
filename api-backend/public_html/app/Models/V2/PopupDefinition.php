<?php

namespace App\Models\V2;

class PopupDefinition extends BaseModel
{
    protected $table = 'popup_definition';
    protected $primaryKey = 'popup_definition_id';
    
    protected $fillable = [
        'popup_key',
        'title',
        'content',
        'popup_type',
        'target_audience',
        'is_active',
        'priority'
    ];
    
    protected $casts = [
        'target_audience' => 'array',
        'is_active' => 'boolean',
        'priority' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get all popup statuses for this definition
     */
    public function userStatuses()
    {
        return $this->hasMany(AppUserPopupStatus::class, 'popup_definition_id');
    }
    
    /**
     * Get analytics for this popup
     */
    public function analytics()
    {
        return $this->hasOne(PopupAnalytics::class, 'popup_definition_id');
    }
    
    /**
     * Scope for active popups
     */
    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }
    
    /**
     * Scope for ordering by priority
     */
    public function scopeByPriority($query)
    {
        return $query->orderBy('priority', 'desc')->orderBy('created_at', 'desc');
    }
    
    /**
     * Check if user matches target audience
     */
    public function isTargetedToUser($user, $userContext = [])
    {
        if (!$this->target_audience) {
            return true; // No targeting rules = show to everyone
        }
        
        $rules = $this->target_audience;
        
        // Check user type (new vs existing)
        if (isset($rules['user_type'])) {
            $daysSinceRegistration = $user->created_at->diffInDays(now());
            $isNewUser = $daysSinceRegistration <= 7;
            
            if ($rules['user_type'] === 'new' && !$isNewUser) {
                return false;
            }
            if ($rules['user_type'] === 'existing' && $isNewUser) {
                return false;
            }
        }
        
        // Check subscription status
        if (isset($rules['subscription_status'])) {
            $hasActiveSubscription = isset($userContext['has_subscription']) ? $userContext['has_subscription'] : false;
            
            if ($rules['subscription_status'] === 'premium' && !$hasActiveSubscription) {
                return false;
            }
            if ($rules['subscription_status'] === 'free' && $hasActiveSubscription) {
                return false;
            }
        }
        
        // Check device type
        if (isset($rules['device_types']) && isset($userContext['device_type'])) {
            if (!in_array($userContext['device_type'], $rules['device_types'])) {
                return false;
            }
        }
        
        return true;
    }
}