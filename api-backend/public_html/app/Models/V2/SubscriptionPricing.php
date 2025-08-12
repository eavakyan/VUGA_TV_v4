<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class SubscriptionPricing extends Model
{
    protected $table = 'subscription_pricing';
    protected $primaryKey = 'pricing_id';
    
    protected $fillable = [
        'pricing_type',
        'content_distributor_id',
        'billing_period',
        'price',
        'currency',
        'display_name',
        'description',
        'is_active',
        'sort_order',
        'features'
    ];

    protected $casts = [
        'price' => 'decimal:2',
        'is_active' => 'boolean',
        'features' => 'array'
    ];

    /**
     * Get the distributor associated with this pricing
     */
    public function distributor()
    {
        return $this->belongsTo(ContentDistributor::class, 'content_distributor_id', 'content_distributor_id');
    }

    /**
     * Scope for active pricing plans
     */
    public function scopeActive($query)
    {
        return $query->where('is_active', 1);
    }

    /**
     * Scope for base subscription pricing
     */
    public function scopeBase($query)
    {
        return $query->where('pricing_type', 'base');
    }

    /**
     * Scope for distributor subscription pricing
     */
    public function scopeDistributor($query)
    {
        return $query->where('pricing_type', 'distributor');
    }
}