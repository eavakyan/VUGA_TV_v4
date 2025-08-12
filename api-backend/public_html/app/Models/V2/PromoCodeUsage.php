<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class PromoCodeUsage extends Model
{
    protected $table = 'promo_code_usage';
    protected $primaryKey = 'usage_id';
    
    protected $fillable = [
        'promo_code_id',
        'app_user_id',
        'transaction_id',
        'used_at'
    ];

    protected $casts = [
        'used_at' => 'datetime'
    ];

    public $timestamps = false;

    /**
     * Get the promo code associated with this usage
     */
    public function promoCode()
    {
        return $this->belongsTo(PromoCode::class, 'promo_code_id', 'promo_code_id');
    }

    /**
     * Get the user who used the promo code
     */
    public function user()
    {
        return $this->belongsTo(AppUser::class, 'app_user_id', 'app_user_id');
    }

    /**
     * Get the transaction associated with this usage
     */
    public function transaction()
    {
        return $this->belongsTo(PaymentTransaction::class, 'transaction_id', 'transaction_id');
    }
}