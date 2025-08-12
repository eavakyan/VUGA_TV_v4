<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Carbon\Carbon;

class PromoCode extends Model
{
    protected $table = 'promo_code';
    protected $primaryKey = 'promo_code_id';
    
    protected $fillable = [
        'code',
        'description',
        'discount_type',
        'discount_value',
        'applicable_to',
        'content_distributor_id',
        'valid_from',
        'valid_until',
        'usage_limit',
        'usage_count',
        'user_limit',
        'is_active'
    ];

    protected $casts = [
        'discount_value' => 'decimal:2',
        'usage_limit' => 'integer',
        'usage_count' => 'integer',
        'user_limit' => 'integer',
        'is_active' => 'boolean',
        'valid_from' => 'datetime',
        'valid_until' => 'datetime'
    ];

    /**
     * Get the distributor associated with this promo code
     */
    public function distributor()
    {
        return $this->belongsTo(ContentDistributor::class, 'content_distributor_id', 'content_distributor_id');
    }

    /**
     * Get all transactions that used this promo code
     */
    public function transactions()
    {
        return $this->hasMany(PaymentTransaction::class, 'promo_code_id', 'promo_code_id');
    }

    /**
     * Get usage records for this promo code
     */
    public function usageRecords()
    {
        return $this->hasMany(PromoCodeUsage::class, 'promo_code_id', 'promo_code_id');
    }

    /**
     * Check if promo code is currently valid
     */
    public function isValid()
    {
        if (!$this->is_active) {
            return false;
        }

        $now = Carbon::now();

        // Check validity dates
        if ($now->lt($this->valid_from)) {
            return false;
        }

        if ($this->valid_until && $now->gt($this->valid_until)) {
            return false;
        }

        // Check usage limit
        if ($this->usage_limit && $this->usage_count >= $this->usage_limit) {
            return false;
        }

        return true;
    }

    /**
     * Check if user can use this promo code
     */
    public function canBeUsedByUser($userId)
    {
        if (!$this->isValid()) {
            return false;
        }

        $userUsageCount = $this->usageRecords()
            ->where('app_user_id', $userId)
            ->count();

        return $userUsageCount < $this->user_limit;
    }

    /**
     * Calculate discount amount for a given price
     */
    public function calculateDiscount($price)
    {
        if ($this->discount_type === 'percentage') {
            return ($price * $this->discount_value) / 100;
        } elseif ($this->discount_type === 'fixed_amount') {
            return min($this->discount_value, $price);
        }

        return 0;
    }

    /**
     * Scope for active promo codes
     */
    public function scopeActive($query)
    {
        return $query->where('is_active', 1);
    }

    /**
     * Scope for currently valid promo codes
     */
    public function scopeCurrentlyValid($query)
    {
        $now = Carbon::now();
        
        return $query->where('is_active', 1)
            ->where('valid_from', '<=', $now)
            ->where(function($q) use ($now) {
                $q->whereNull('valid_until')
                  ->orWhere('valid_until', '>=', $now);
            })
            ->where(function($q) {
                $q->whereNull('usage_limit')
                  ->orWhereRaw('usage_count < usage_limit');
            });
    }
}