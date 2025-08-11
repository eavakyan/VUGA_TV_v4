<?php

namespace App\Models\V2;

class ContentDistributor extends BaseModel
{
    protected $table = 'content_distributor';
    protected $primaryKey = 'content_distributor_id';
    
    protected $fillable = [
        'name',
        'code',
        'description',
        'logo_url',
        'is_base_included',
        'is_premium',
        'display_order',
        'is_active'
    ];
    
    protected $casts = [
        'is_base_included' => 'boolean',
        'is_premium' => 'boolean',
        'is_active' => 'boolean',
        'display_order' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the content associated with this distributor
     */
    public function content()
    {
        return $this->hasMany(Content::class, 'content_distributor_id');
    }
    
    /**
     * Get the pricing plans for this distributor
     */
    public function pricingPlans()
    {
        return $this->hasMany(SubscriptionPricing::class, 'content_distributor_id');
    }
    
    /**
     * Get users who have access to this distributor
     */
    public function userAccess()
    {
        return $this->hasMany(UserDistributorAccess::class, 'content_distributor_id');
    }
    
    /**
     * Get revenue share configuration
     */
    public function revenueShareConfig()
    {
        return $this->hasOne(RevenueShareConfig::class, 'content_distributor_id');
    }
    
    /**
     * Scope for active distributors
     */
    public function scopeActive($query)
    {
        return $query->where('is_active', 1);
    }
    
    /**
     * Scope for premium distributors
     */
    public function scopePremium($query)
    {
        return $query->where('is_premium', 1);
    }
    
    /**
     * Scope for base included distributors
     */
    public function scopeBaseIncluded($query)
    {
        return $query->where('is_base_included', 1);
    }
    
    /**
     * Check if a user has access to this distributor
     */
    public function userHasAccess($userId)
    {
        // If included in base subscription
        if ($this->is_base_included) {
            $hasBase = \DB::table('user_base_subscription')
                ->where('app_user_id', $userId)
                ->where('is_active', 1)
                ->where(function($q) {
                    $q->whereNull('end_date')
                      ->orWhere('end_date', '>', now());
                })
                ->exists();
                
            if ($hasBase) {
                return true;
            }
        }
        
        // Check specific distributor access
        if ($this->is_premium) {
            return \DB::table('user_distributor_access')
                ->where('app_user_id', $userId)
                ->where('content_distributor_id', $this->content_distributor_id)
                ->where('is_active', 1)
                ->where(function($q) {
                    $q->whereNull('end_date')
                      ->orWhere('end_date', '>', now());
                })
                ->exists();
        }
        
        return false;
    }
}