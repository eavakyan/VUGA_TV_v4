<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;
use Carbon\Carbon;

class UserDistributorAccess extends Model
{
    protected $table = 'user_distributor_access';
    protected $primaryKey = 'access_id';
    
    protected $fillable = [
        'app_user_id',
        'content_distributor_id',
        'start_date',
        'end_date',
        'is_active',
        'subscription_type',
        'auto_renew',
        'payment_transaction_id'
    ];

    protected $casts = [
        'is_active' => 'boolean',
        'auto_renew' => 'boolean',
        'start_date' => 'datetime',
        'end_date' => 'datetime'
    ];

    /**
     * Get the user associated with this access
     */
    public function user()
    {
        return $this->belongsTo(AppUser::class, 'app_user_id', 'app_user_id');
    }

    /**
     * Get the distributor associated with this access
     */
    public function distributor()
    {
        return $this->belongsTo(ContentDistributor::class, 'content_distributor_id', 'content_distributor_id');
    }

    /**
     * Get the payment transaction for this access
     */
    public function paymentTransaction()
    {
        return $this->belongsTo(PaymentTransaction::class, 'payment_transaction_id', 'transaction_id');
    }

    /**
     * Check if access is currently active
     */
    public function isCurrentlyActive()
    {
        if (!$this->is_active) {
            return false;
        }

        if ($this->end_date === null) {
            return true; // Lifetime access
        }

        return Carbon::now()->lte($this->end_date);
    }

    /**
     * Scope for active access
     */
    public function scopeActive($query)
    {
        return $query->where('is_active', 1)
            ->where(function($q) {
                $q->whereNull('end_date')
                  ->orWhere('end_date', '>', Carbon::now());
            });
    }

    /**
     * Get access status text
     */
    public function getStatusAttribute()
    {
        if (!$this->is_active) {
            return 'Inactive';
        }

        if ($this->end_date && Carbon::now()->gt($this->end_date)) {
            return 'Expired';
        }

        return 'Active';
    }
}