<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class PaymentTransaction extends Model
{
    protected $table = 'payment_transaction';
    protected $primaryKey = 'transaction_id';
    
    protected $fillable = [
        'app_user_id',
        'transaction_type',
        'subscription_type',
        'content_distributor_id',
        'pricing_id',
        'promo_code_id',
        'payment_method',
        'payment_status',
        'external_transaction_id',
        'currency',
        'subtotal',
        'discount_amount',
        'tax_amount',
        'total_amount',
        'billing_period',
        'subscription_start_date',
        'subscription_end_date',
        'payment_metadata'
    ];

    protected $casts = [
        'subtotal' => 'decimal:2',
        'discount_amount' => 'decimal:2',
        'tax_amount' => 'decimal:2',
        'total_amount' => 'decimal:2',
        'subscription_start_date' => 'datetime',
        'subscription_end_date' => 'datetime',
        'payment_metadata' => 'array'
    ];

    /**
     * Get the user associated with this transaction
     */
    public function user()
    {
        return $this->belongsTo(AppUser::class, 'app_user_id', 'app_user_id');
    }

    /**
     * Get the distributor associated with this transaction
     */
    public function distributor()
    {
        return $this->belongsTo(ContentDistributor::class, 'content_distributor_id', 'content_distributor_id');
    }

    /**
     * Get the pricing plan associated with this transaction
     */
    public function pricing()
    {
        return $this->belongsTo(SubscriptionPricing::class, 'pricing_id', 'pricing_id');
    }

    /**
     * Get the promo code used in this transaction
     */
    public function promoCode()
    {
        return $this->belongsTo(PromoCode::class, 'promo_code_id', 'promo_code_id');
    }

    /**
     * Scope for completed transactions
     */
    public function scopeCompleted($query)
    {
        return $query->where('payment_status', 'completed');
    }

    /**
     * Scope for pending transactions
     */
    public function scopePending($query)
    {
        return $query->where('payment_status', 'pending');
    }

    /**
     * Scope for failed transactions
     */
    public function scopeFailed($query)
    {
        return $query->where('payment_status', 'failed');
    }

    /**
     * Check if transaction is completed
     */
    public function isCompleted()
    {
        return $this->payment_status === 'completed';
    }

    /**
     * Check if transaction is pending
     */
    public function isPending()
    {
        return $this->payment_status === 'pending';
    }
}