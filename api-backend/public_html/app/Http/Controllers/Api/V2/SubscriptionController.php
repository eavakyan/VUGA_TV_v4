<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Carbon\Carbon;
use App\Models\V2\SubscriptionPricing;
use App\Models\V2\UserBaseSubscription;
use App\Models\V2\UserDistributorAccess;
use App\Models\V2\PaymentTransaction;
use App\Models\V2\PromoCode;
use App\Models\V2\PromoCodeUsage;

class SubscriptionController extends Controller
{
    /**
     * Get available subscription plans
     */
    public function getPlans(Request $request)
    {
        try {
            $plans = DB::table('subscription_pricing as sp')
                ->leftJoin('content_distributor as cd', 'sp.content_distributor_id', '=', 'cd.content_distributor_id')
                ->select(
                    'sp.pricing_id',
                    'sp.pricing_type',
                    'sp.billing_period',
                    'sp.price',
                    'sp.currency',
                    'sp.display_name',
                    'sp.description',
                    'cd.name as distributor_name',
                    'cd.code as distributor_code',
                    'cd.logo_url as distributor_logo'
                )
                ->where('sp.is_active', 1)
                ->orderBy('sp.pricing_type')
                ->orderBy('sp.sort_order')
                ->get();

            // Group by type
            $grouped = [
                'base' => [],
                'distributors' => []
            ];

            foreach ($plans as $plan) {
                if ($plan->pricing_type === 'base') {
                    $grouped['base'][] = $plan;
                } else {
                    $grouped['distributors'][] = $plan;
                }
            }

            return response()->json([
                'status' => true,
                'message' => 'Subscription plans retrieved successfully',
                'data' => $grouped
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to retrieve subscription plans',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get user's active subscriptions
     */
    public function getMySubscriptions(Request $request)
    {
        try {
            $userId = $request->user_id ?? $request->app_user_id;
            
            if (!$userId) {
                return response()->json([
                    'status' => false,
                    'message' => 'User ID is required'
                ], 400);
            }

            // Get base subscription
            $baseSubscription = DB::table('user_base_subscription')
                ->where('app_user_id', $userId)
                ->where('is_active', 1)
                ->where(function($query) {
                    $query->whereNull('end_date')
                        ->orWhere('end_date', '>', Carbon::now());
                })
                ->first();

            // Get distributor subscriptions
            $distributorSubscriptions = DB::table('user_distributor_access as uda')
                ->join('content_distributor as cd', 'uda.content_distributor_id', '=', 'cd.content_distributor_id')
                ->select(
                    'uda.*',
                    'cd.name as distributor_name',
                    'cd.code as distributor_code',
                    'cd.logo_url as distributor_logo'
                )
                ->where('uda.app_user_id', $userId)
                ->where('uda.is_active', 1)
                ->where(function($query) {
                    $query->whereNull('uda.end_date')
                        ->orWhere('uda.end_date', '>', Carbon::now());
                })
                ->get();

            return response()->json([
                'status' => true,
                'message' => 'User subscriptions retrieved successfully',
                'data' => [
                    'base_subscription' => $baseSubscription,
                    'distributor_subscriptions' => $distributorSubscriptions,
                    'has_active_base' => !is_null($baseSubscription),
                    'active_distributor_count' => count($distributorSubscriptions)
                ]
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to retrieve subscriptions',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Validate promo code
     */
    public function validatePromoCode(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'code' => 'required|string',
                'pricing_id' => 'required|integer',
                'user_id' => 'required|integer'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'status' => false,
                    'message' => 'Validation failed',
                    'errors' => $validator->errors()
                ], 400);
            }

            $code = strtoupper($request->code);
            $pricingId = $request->pricing_id;
            $userId = $request->user_id;

            // Get promo code details
            $promo = DB::table('promo_code')
                ->where('code', $code)
                ->where('is_active', 1)
                ->first();

            if (!$promo) {
                return response()->json([
                    'status' => false,
                    'message' => 'Invalid promo code'
                ], 404);
            }

            // Check validity dates
            $now = Carbon::now();
            if ($now->lt(Carbon::parse($promo->valid_from)) || 
                ($promo->valid_until && $now->gt(Carbon::parse($promo->valid_until)))) {
                return response()->json([
                    'status' => false,
                    'message' => 'Promo code has expired'
                ], 400);
            }

            // Check usage limits
            if ($promo->usage_limit && $promo->usage_count >= $promo->usage_limit) {
                return response()->json([
                    'status' => false,
                    'message' => 'Promo code usage limit reached'
                ], 400);
            }

            // Check user usage limit
            $userUsageCount = DB::table('promo_code_usage')
                ->where('promo_code_id', $promo->promo_code_id)
                ->where('app_user_id', $userId)
                ->count();

            if ($userUsageCount >= $promo->user_limit) {
                return response()->json([
                    'status' => false,
                    'message' => 'You have already used this promo code'
                ], 400);
            }

            // Get pricing details
            $pricing = DB::table('subscription_pricing')
                ->where('pricing_id', $pricingId)
                ->first();

            if (!$pricing) {
                return response()->json([
                    'status' => false,
                    'message' => 'Invalid pricing plan'
                ], 404);
            }

            // Check if promo applies to this subscription type
            if ($promo->applicable_to !== 'all') {
                if ($promo->applicable_to !== $pricing->pricing_type) {
                    return response()->json([
                        'status' => false,
                        'message' => 'Promo code does not apply to this subscription type'
                    ], 400);
                }
            }

            // Calculate discount
            $discountAmount = 0;
            if ($promo->discount_type === 'percentage') {
                $discountAmount = ($pricing->price * $promo->discount_value) / 100;
            } elseif ($promo->discount_type === 'fixed_amount') {
                $discountAmount = min($promo->discount_value, $pricing->price);
            }

            $finalPrice = max(0, $pricing->price - $discountAmount);

            return response()->json([
                'status' => true,
                'message' => 'Promo code is valid',
                'data' => [
                    'promo_code_id' => $promo->promo_code_id,
                    'discount_type' => $promo->discount_type,
                    'discount_value' => $promo->discount_value,
                    'original_price' => $pricing->price,
                    'discount_amount' => round($discountAmount, 2),
                    'final_price' => round($finalPrice, 2),
                    'currency' => $pricing->currency
                ]
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to validate promo code',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Create subscription (stub for payment processing)
     */
    public function createSubscription(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'user_id' => 'required|integer',
                'pricing_id' => 'required|integer',
                'promo_code' => 'nullable|string',
                'payment_method' => 'required|string'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'status' => false,
                    'message' => 'Validation failed',
                    'errors' => $validator->errors()
                ], 400);
            }

            DB::beginTransaction();

            $userId = $request->user_id;
            $pricingId = $request->pricing_id;
            $promoCode = $request->promo_code ? strtoupper($request->promo_code) : null;

            // Get pricing details
            $pricing = DB::table('subscription_pricing')
                ->where('pricing_id', $pricingId)
                ->where('is_active', 1)
                ->first();

            if (!$pricing) {
                throw new \Exception('Invalid pricing plan');
            }

            // Calculate amounts
            $subtotal = $pricing->price;
            $discountAmount = 0;
            $promoCodeId = null;

            // Apply promo code if provided
            if ($promoCode) {
                $promoValidation = $this->validatePromoCodeInternal($promoCode, $pricingId, $userId);
                if ($promoValidation['valid']) {
                    $discountAmount = $promoValidation['discount_amount'];
                    $promoCodeId = $promoValidation['promo_code_id'];
                }
            }

            $totalAmount = max(0, $subtotal - $discountAmount);

            // Create payment transaction record
            $transactionId = DB::table('payment_transaction')->insertGetId([
                'app_user_id' => $userId,
                'transaction_type' => 'subscription',
                'subscription_type' => $pricing->pricing_type,
                'content_distributor_id' => $pricing->content_distributor_id,
                'pricing_id' => $pricingId,
                'promo_code_id' => $promoCodeId,
                'payment_method' => $request->payment_method,
                'payment_status' => 'pending',
                'currency' => $pricing->currency,
                'subtotal' => $subtotal,
                'discount_amount' => $discountAmount,
                'tax_amount' => 0, // TODO: Calculate tax based on location
                'total_amount' => $totalAmount,
                'billing_period' => $pricing->billing_period,
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now()
            ]);

            // For development with 100% discount, auto-complete the transaction
            if ($totalAmount == 0 && $promoCode === 'DEVTEST100') {
                $this->completeSubscription($transactionId);
            }

            DB::commit();

            return response()->json([
                'status' => true,
                'message' => 'Subscription created successfully',
                'data' => [
                    'transaction_id' => $transactionId,
                    'total_amount' => $totalAmount,
                    'requires_payment' => $totalAmount > 0
                ]
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'status' => false,
                'message' => 'Failed to create subscription',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Complete subscription after payment
     */
    public function completeSubscription($transactionId)
    {
        try {
            DB::beginTransaction();

            // Get transaction details
            $transaction = DB::table('payment_transaction')
                ->where('transaction_id', $transactionId)
                ->first();

            if (!$transaction) {
                throw new \Exception('Transaction not found');
            }

            // Calculate subscription dates
            $startDate = Carbon::now();
            $endDate = null;

            switch ($transaction->billing_period) {
                case 'monthly':
                    $endDate = $startDate->copy()->addMonth();
                    break;
                case 'quarterly':
                    $endDate = $startDate->copy()->addMonths(3);
                    break;
                case 'yearly':
                    $endDate = $startDate->copy()->addYear();
                    break;
                case 'lifetime':
                    $endDate = null; // No expiry
                    break;
            }

            // Create or update subscription
            if ($transaction->subscription_type === 'base') {
                // Deactivate existing base subscription
                DB::table('user_base_subscription')
                    ->where('app_user_id', $transaction->app_user_id)
                    ->update(['is_active' => 0]);

                // Create new base subscription
                DB::table('user_base_subscription')->insert([
                    'app_user_id' => $transaction->app_user_id,
                    'start_date' => $startDate,
                    'end_date' => $endDate,
                    'is_active' => 1,
                    'subscription_type' => $transaction->billing_period,
                    'created_at' => Carbon::now(),
                    'updated_at' => Carbon::now()
                ]);
            } else {
                // Create or update distributor subscription
                $existing = DB::table('user_distributor_access')
                    ->where('app_user_id', $transaction->app_user_id)
                    ->where('content_distributor_id', $transaction->content_distributor_id)
                    ->first();

                if ($existing) {
                    DB::table('user_distributor_access')
                        ->where('access_id', $existing->access_id)
                        ->update([
                            'start_date' => $startDate,
                            'end_date' => $endDate,
                            'is_active' => 1,
                            'subscription_type' => $transaction->billing_period,
                            'updated_at' => Carbon::now()
                        ]);
                } else {
                    DB::table('user_distributor_access')->insert([
                        'app_user_id' => $transaction->app_user_id,
                        'content_distributor_id' => $transaction->content_distributor_id,
                        'start_date' => $startDate,
                        'end_date' => $endDate,
                        'is_active' => 1,
                        'subscription_type' => $transaction->billing_period,
                        'created_at' => Carbon::now(),
                        'updated_at' => Carbon::now()
                    ]);
                }
            }

            // Update transaction status
            DB::table('payment_transaction')
                ->where('transaction_id', $transactionId)
                ->update([
                    'payment_status' => 'completed',
                    'subscription_start_date' => $startDate,
                    'subscription_end_date' => $endDate,
                    'updated_at' => Carbon::now()
                ]);

            // Record promo code usage if applicable
            if ($transaction->promo_code_id) {
                DB::table('promo_code_usage')->insert([
                    'promo_code_id' => $transaction->promo_code_id,
                    'app_user_id' => $transaction->app_user_id,
                    'transaction_id' => $transactionId,
                    'used_at' => Carbon::now()
                ]);

                // Increment usage count
                DB::table('promo_code')
                    ->where('promo_code_id', $transaction->promo_code_id)
                    ->increment('usage_count');
            }

            DB::commit();

            return true;
        } catch (\Exception $e) {
            DB::rollBack();
            throw $e;
        }
    }

    /**
     * Cancel subscription
     */
    public function cancelSubscription(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'user_id' => 'required|integer',
                'subscription_type' => 'required|in:base,distributor',
                'distributor_id' => 'required_if:subscription_type,distributor|integer'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'status' => false,
                    'message' => 'Validation failed',
                    'errors' => $validator->errors()
                ], 400);
            }

            DB::beginTransaction();

            $userId = $request->user_id;

            if ($request->subscription_type === 'base') {
                DB::table('user_base_subscription')
                    ->where('app_user_id', $userId)
                    ->where('is_active', 1)
                    ->update([
                        'is_active' => 0,
                        'updated_at' => Carbon::now()
                    ]);
            } else {
                DB::table('user_distributor_access')
                    ->where('app_user_id', $userId)
                    ->where('content_distributor_id', $request->distributor_id)
                    ->where('is_active', 1)
                    ->update([
                        'is_active' => 0,
                        'updated_at' => Carbon::now()
                    ]);
            }

            DB::commit();

            return response()->json([
                'status' => true,
                'message' => 'Subscription cancelled successfully'
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'status' => false,
                'message' => 'Failed to cancel subscription',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get payment history
     */
    public function getPaymentHistory(Request $request)
    {
        try {
            $userId = $request->user_id ?? $request->app_user_id;
            
            if (!$userId) {
                return response()->json([
                    'status' => false,
                    'message' => 'User ID is required'
                ], 400);
            }

            $transactions = DB::table('payment_transaction as pt')
                ->leftJoin('content_distributor as cd', 'pt.content_distributor_id', '=', 'cd.content_distributor_id')
                ->leftJoin('promo_code as pc', 'pt.promo_code_id', '=', 'pc.promo_code_id')
                ->select(
                    'pt.*',
                    'cd.name as distributor_name',
                    'pc.code as promo_code'
                )
                ->where('pt.app_user_id', $userId)
                ->orderBy('pt.created_at', 'desc')
                ->limit(50)
                ->get();

            return response()->json([
                'status' => true,
                'message' => 'Payment history retrieved successfully',
                'data' => $transactions
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to retrieve payment history',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Internal promo code validation
     */
    private function validatePromoCodeInternal($code, $pricingId, $userId)
    {
        $promo = DB::table('promo_code')
            ->where('code', $code)
            ->where('is_active', 1)
            ->first();

        if (!$promo) {
            return ['valid' => false];
        }

        // Get pricing details
        $pricing = DB::table('subscription_pricing')
            ->where('pricing_id', $pricingId)
            ->first();

        // Calculate discount
        $discountAmount = 0;
        if ($promo->discount_type === 'percentage') {
            $discountAmount = ($pricing->price * $promo->discount_value) / 100;
        } elseif ($promo->discount_type === 'fixed_amount') {
            $discountAmount = min($promo->discount_value, $pricing->price);
        }

        return [
            'valid' => true,
            'promo_code_id' => $promo->promo_code_id,
            'discount_amount' => round($discountAmount, 2)
        ];
    }
}