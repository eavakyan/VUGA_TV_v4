<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\TermsCondition;
use Illuminate\Http\Request;

class TermsConditionController extends Controller
{
    public function index()
    {
        $termsCondition = TermsCondition::first();
        return view('admin.terms_condition', compact('termsCondition'));
    }

    public function viewTermsCondition()
    {
        $termsCondition = TermsCondition::first();
        return view('terms_condition', compact('termsCondition'));
    }

    public function UpdateTermscondition(Request $request)
    {
        $termsCondition = TermsCondition::first();
        if (!$termsCondition) {
            $termsCondition = new TermsCondition();
        }
        $termsCondition->content = $request->content;
        $termsCondition->save();

        return response()->json([
            'status' => true,
            'message' => 'Terms & Conditions updated successfully'
        ]);
    }
} 