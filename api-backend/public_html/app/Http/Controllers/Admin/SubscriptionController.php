<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Subscription;
use Illuminate\Http\Request;

class SubscriptionController extends Controller
{
    public function viewListSubscription()
    {
        return view('admin.subscription.list');
    }

    public function showSubscriptionList(Request $request)
    {
        $query = Subscription::with(['user', 'package']);
        
        if ($request->has('search') && !empty($request->search)) {
            $search = $request->search;
            $query->whereHas('user', function($q) use ($search) {
                $q->where('fullname', 'LIKE', "%{$search}%")
                  ->orWhere('email', 'LIKE', "%{$search}%");
            });
        }

        $subscriptions = $query->orderBy('created_at', 'DESC')
                              ->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $subscriptions->items(),
            'recordsTotal' => $subscriptions->total(),
            'recordsFiltered' => $subscriptions->total()
        ]);
    }

    public function viewSubscription($id)
    {
        $subscription = Subscription::with(['user', 'package'])->findOrFail($id);
        return view('admin.subscription.view', compact('subscription'));
    }

    public function deleteSubscription(Request $request)
    {
        $subscription = Subscription::find($request->id);
        if ($subscription) {
            $subscription->delete();
            return response()->json(['status' => true, 'message' => 'Subscription deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Subscription not found']);
    }
} 