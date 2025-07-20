<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\PrivacyPolicy;
use Illuminate\Http\Request;

class PrivacyPolicyController extends Controller
{
    public function index()
    {
        $privacyPolicy = PrivacyPolicy::first();
        return view('admin.privacy_policy', compact('privacyPolicy'));
    }

    public function viewPrivacyPolicy()
    {
        $privacyPolicy = PrivacyPolicy::first();
        return view('privacy_policy', compact('privacyPolicy'));
    }

    public function UpdatePrivacypolicy(Request $request)
    {
        $privacyPolicy = PrivacyPolicy::first();
        if (!$privacyPolicy) {
            $privacyPolicy = new PrivacyPolicy();
        }
        $privacyPolicy->content = $request->content;
        $privacyPolicy->save();

        return response()->json([
            'status' => true,
            'message' => 'Privacy Policy updated successfully'
        ]);
    }
} 