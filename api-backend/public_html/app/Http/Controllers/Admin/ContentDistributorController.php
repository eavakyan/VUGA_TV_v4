<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\V2\ContentDistributor;
use App\Models\V2\Content;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

class ContentDistributorController extends Controller
{
    /**
     * Display list of distributors
     */
    public function index()
    {
        $distributors = ContentDistributor::with(['content' => function($q) {
            $q->select('content_id', 'title', 'content_distributor_id');
        }])
        ->withCount('content')
        ->orderBy('display_order')
        ->orderBy('name')
        ->get();
        
        return view('admin.distributors.index', compact('distributors'));
    }
    
    /**
     * Show create distributor form
     */
    public function create()
    {
        return view('admin.distributors.create');
    }
    
    /**
     * Store new distributor
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:100',
            'code' => 'required|string|max:50|unique:content_distributor,code',
            'description' => 'nullable|string',
            'logo_url' => 'nullable|string|max:500',
            'is_base_included' => 'boolean',
            'is_premium' => 'boolean',
            'display_order' => 'integer',
            'is_active' => 'boolean'
        ]);
        
        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }
        
        ContentDistributor::create($request->all());
        
        return redirect()->route('distributors.index')
            ->with('success', 'Distributor created successfully');
    }
    
    /**
     * Show edit distributor form
     */
    public function edit($id)
    {
        $distributor = ContentDistributor::findOrFail($id);
        return view('admin.distributors.edit', compact('distributor'));
    }
    
    /**
     * Update distributor
     */
    public function update(Request $request, $id)
    {
        $distributor = ContentDistributor::findOrFail($id);
        
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:100',
            'code' => 'required|string|max:50|unique:content_distributor,code,' . $id . ',content_distributor_id',
            'description' => 'nullable|string',
            'logo_url' => 'nullable|string|max:500',
            'is_base_included' => 'boolean',
            'is_premium' => 'boolean',
            'display_order' => 'integer',
            'is_active' => 'boolean'
        ]);
        
        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }
        
        $distributor->update($request->all());
        
        return redirect()->route('distributors.index')
            ->with('success', 'Distributor updated successfully');
    }
    
    /**
     * Delete distributor
     */
    public function destroy($id)
    {
        $distributor = ContentDistributor::findOrFail($id);
        
        // Check if distributor has content
        if ($distributor->content()->count() > 0) {
            return redirect()->back()
                ->with('error', 'Cannot delete distributor with associated content');
        }
        
        $distributor->delete();
        
        return redirect()->route('distributors.index')
            ->with('success', 'Distributor deleted successfully');
    }
    
    /**
     * Manage content distributor assignments
     */
    public function manageContent()
    {
        $distributors = ContentDistributor::active()->orderBy('name')->get();
        $content = Content::with('distributor')
            ->visible()
            ->orderBy('title')
            ->paginate(50);
            
        return view('admin.distributors.manage-content', compact('distributors', 'content'));
    }
    
    /**
     * Bulk update content distributor assignments
     */
    public function updateContentDistributors(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'assignments' => 'required|array',
            'assignments.*' => 'nullable|exists:content_distributor,content_distributor_id'
        ]);
        
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }
        
        DB::beginTransaction();
        
        try {
            foreach ($request->assignments as $contentId => $distributorId) {
                Content::where('content_id', $contentId)
                    ->update(['content_distributor_id' => $distributorId ?: null]);
            }
            
            DB::commit();
            
            return response()->json([
                'success' => true,
                'message' => 'Content distributor assignments updated successfully'
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            
            return response()->json([
                'success' => false,
                'message' => 'Failed to update assignments: ' . $e->getMessage()
            ], 500);
        }
    }
    
    /**
     * Revenue share configuration
     */
    public function revenueShare()
    {
        $distributors = ContentDistributor::with('revenueShareConfig')
            ->premium()
            ->orderBy('name')
            ->get();
            
        return view('admin.distributors.revenue-share', compact('distributors'));
    }
    
    /**
     * Update revenue share configuration
     */
    public function updateRevenueShare(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'revenue_share_percentage' => 'required|numeric|min:0|max:100',
            'minimum_payout' => 'required|numeric|min:0',
            'payment_terms_days' => 'required|integer|min:1',
            'notes' => 'nullable|string'
        ]);
        
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }
        
        DB::table('revenue_share_config')->updateOrInsert(
            ['content_distributor_id' => $id],
            array_merge($request->all(), [
                'is_active' => 1,
                'updated_at' => now()
            ])
        );
        
        return response()->json([
            'success' => true,
            'message' => 'Revenue share configuration updated'
        ]);
    }
    
    /**
     * Promo codes management
     */
    public function promoCodes()
    {
        $promoCodes = DB::table('promo_code as pc')
            ->leftJoin('content_distributor as cd', 'pc.content_distributor_id', '=', 'cd.content_distributor_id')
            ->leftJoin('admin_user as au', 'pc.created_by', '=', 'au.admin_user_id')
            ->select(
                'pc.*',
                'cd.name as distributor_name',
                'au.user_name as created_by_name'
            )
            ->orderBy('pc.created_at', 'desc')
            ->get();
            
        $distributors = ContentDistributor::active()->orderBy('name')->get();
        
        return view('admin.distributors.promo-codes', compact('promoCodes', 'distributors'));
    }
    
    /**
     * Create promo code
     */
    public function createPromoCode(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'code' => 'required|string|max:50|unique:promo_code,code',
            'description' => 'nullable|string|max:255',
            'discount_type' => 'required|in:percentage,fixed_amount,free_period',
            'discount_value' => 'required|numeric|min:0',
            'applicable_to' => 'required|in:base,distributor,all',
            'content_distributor_id' => 'nullable|exists:content_distributor,content_distributor_id',
            'minimum_purchase' => 'nullable|numeric|min:0',
            'usage_limit' => 'nullable|integer|min:1',
            'user_limit' => 'required|integer|min:1',
            'valid_from' => 'required|date',
            'valid_until' => 'nullable|date|after:valid_from'
        ]);
        
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }
        
        $data = $request->all();
        $data['code'] = strtoupper($data['code']);
        $data['created_by'] = auth()->guard('admin')->id();
        $data['is_active'] = 1;
        
        DB::table('promo_code')->insert($data);
        
        return response()->json([
            'success' => true,
            'message' => 'Promo code created successfully'
        ]);
    }
    
    /**
     * Toggle promo code status
     */
    public function togglePromoCodeStatus($id)
    {
        $promoCode = DB::table('promo_code')->where('promo_code_id', $id)->first();
        
        if (!$promoCode) {
            return response()->json([
                'success' => false,
                'message' => 'Promo code not found'
            ], 404);
        }
        
        DB::table('promo_code')
            ->where('promo_code_id', $id)
            ->update([
                'is_active' => !$promoCode->is_active,
                'updated_at' => now()
            ]);
            
        return response()->json([
            'success' => true,
            'message' => 'Promo code status updated'
        ]);
    }
    
    /**
     * Get pricing for a distributor
     */
    public function getPricing($id)
    {
        $pricing = DB::table('subscription_pricing')
            ->where('content_distributor_id', $id)
            ->orderBy('sort_order')
            ->orderBy('billing_period')
            ->get();
            
        return response()->json([
            'success' => true,
            'data' => $pricing
        ]);
    }
    
    /**
     * Store new pricing option
     */
    public function storePricing(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_distributor_id' => 'required|exists:content_distributor,content_distributor_id',
            'billing_period' => 'required|in:daily,weekly,monthly,quarterly,yearly,lifetime',
            'price' => 'required|numeric|min:0',
            'display_name' => 'required|string|max:100',
            'description' => 'nullable|string',
            'is_active' => 'boolean'
        ]);
        
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => $validator->errors()->first()
            ], 422);
        }
        
        // Check if this billing period already exists for this distributor
        $exists = DB::table('subscription_pricing')
            ->where('content_distributor_id', $request->content_distributor_id)
            ->where('billing_period', $request->billing_period)
            ->exists();
            
        if ($exists) {
            return response()->json([
                'success' => false,
                'message' => 'A pricing option for this billing period already exists'
            ], 422);
        }
        
        $pricingId = DB::table('subscription_pricing')->insertGetId([
            'pricing_type' => 'distributor',
            'content_distributor_id' => $request->content_distributor_id,
            'billing_period' => $request->billing_period,
            'price' => $request->price,
            'currency' => 'USD',
            'display_name' => $request->display_name,
            'description' => $request->description,
            'is_active' => $request->is_active ?? 1,
            'sort_order' => 0,
            'created_at' => now(),
            'updated_at' => now()
        ]);
        
        return response()->json([
            'success' => true,
            'message' => 'Pricing option added successfully',
            'data' => ['pricing_id' => $pricingId]
        ]);
    }
    
    /**
     * Update pricing option
     */
    public function updatePricing(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'billing_period' => 'required|in:daily,weekly,monthly,quarterly,yearly,lifetime',
            'price' => 'required|numeric|min:0',
            'display_name' => 'required|string|max:100',
            'description' => 'nullable|string',
            'is_active' => 'boolean'
        ]);
        
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => $validator->errors()->first()
            ], 422);
        }
        
        DB::table('subscription_pricing')
            ->where('pricing_id', $id)
            ->update([
                'billing_period' => $request->billing_period,
                'price' => $request->price,
                'display_name' => $request->display_name,
                'description' => $request->description,
                'is_active' => $request->is_active ?? 1,
                'updated_at' => now()
            ]);
            
        return response()->json([
            'success' => true,
            'message' => 'Pricing option updated successfully'
        ]);
    }
    
    /**
     * Delete pricing option
     */
    public function destroyPricing($id)
    {
        DB::table('subscription_pricing')
            ->where('pricing_id', $id)
            ->delete();
            
        return response()->json([
            'success' => true,
            'message' => 'Pricing option deleted successfully'
        ]);
    }
    
    /**
     * Show base subscription pricing page
     */
    public function basePricing()
    {
        return view('admin.distributors.base-pricing');
    }
    
    /**
     * Get base subscription pricing
     */
    public function getBasePricing()
    {
        $pricing = DB::table('subscription_pricing')
            ->where('pricing_type', 'base')
            ->whereNull('content_distributor_id')
            ->orderBy('sort_order')
            ->orderBy('billing_period')
            ->get();
            
        return response()->json([
            'success' => true,
            'data' => $pricing
        ]);
    }
    
    /**
     * Store base subscription pricing
     */
    public function storeBasePricing(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'billing_period' => 'required|in:daily,weekly,monthly,quarterly,yearly,lifetime',
            'price' => 'required|numeric|min:0',
            'display_name' => 'required|string|max:100',
            'description' => 'nullable|string',
            'is_active' => 'boolean'
        ]);
        
        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => $validator->errors()->first()
            ], 422);
        }
        
        // Check if this billing period already exists for base subscription
        $exists = DB::table('subscription_pricing')
            ->where('pricing_type', 'base')
            ->whereNull('content_distributor_id')
            ->where('billing_period', $request->billing_period)
            ->exists();
            
        if ($exists) {
            return response()->json([
                'success' => false,
                'message' => 'A base pricing option for this billing period already exists'
            ], 422);
        }
        
        $pricingId = DB::table('subscription_pricing')->insertGetId([
            'pricing_type' => 'base',
            'content_distributor_id' => null,
            'billing_period' => $request->billing_period,
            'price' => $request->price,
            'currency' => 'USD',
            'display_name' => $request->display_name,
            'description' => $request->description,
            'is_active' => $request->is_active ?? 1,
            'sort_order' => 0,
            'created_at' => now(),
            'updated_at' => now()
        ]);
        
        return response()->json([
            'success' => true,
            'message' => 'Base pricing option added successfully',
            'data' => ['pricing_id' => $pricingId]
        ]);
    }
    
    /**
     * Subscription analytics
     */
    public function analytics()
    {
        // Base subscription stats
        $baseStats = DB::table('user_base_subscription')
            ->selectRaw('
                COUNT(DISTINCT app_user_id) as total_subscribers,
                SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_subscribers,
                SUM(CASE WHEN subscription_type = "monthly" THEN 1 ELSE 0 END) as monthly_subscribers,
                SUM(CASE WHEN subscription_type = "yearly" THEN 1 ELSE 0 END) as yearly_subscribers
            ')
            ->first();
            
        // Distributor subscription stats
        $distributorStats = DB::table('user_distributor_access as uda')
            ->join('content_distributor as cd', 'uda.content_distributor_id', '=', 'cd.content_distributor_id')
            ->select(
                'cd.name',
                'cd.code',
                DB::raw('COUNT(DISTINCT uda.app_user_id) as total_subscribers'),
                DB::raw('SUM(CASE WHEN uda.is_active = 1 THEN 1 ELSE 0 END) as active_subscribers')
            )
            ->groupBy('cd.content_distributor_id', 'cd.name', 'cd.code')
            ->get();
            
        // Revenue stats
        $revenueStats = DB::table('payment_transaction')
            ->where('payment_status', 'completed')
            ->selectRaw('
                DATE_FORMAT(created_at, "%Y-%m") as month,
                subscription_type,
                SUM(total_amount) as revenue,
                COUNT(*) as transaction_count
            ')
            ->groupBy('month', 'subscription_type')
            ->orderBy('month', 'desc')
            ->limit(12)
            ->get();
            
        return view('admin.distributors.analytics', compact('baseStats', 'distributorStats', 'revenueStats'));
    }
}