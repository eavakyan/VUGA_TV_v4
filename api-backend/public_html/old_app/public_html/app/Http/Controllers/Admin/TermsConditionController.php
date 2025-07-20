<?php

namespace App\Http\Controllers\admin;

use App\TermsCondition;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class TermsConditionController extends Controller
{
    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        $termsCondition = TermsCondition::first();
        return view('admin.termscondition.index')->with('termsCondition', $termsCondition);
    }

    public function viewTermsCondition()
    {
        $termsCondition = TermsCondition::first();
        return view('admin.termscondition.view')->with('termsCondition', $termsCondition);
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function UpdateTermscondition(Request $request)
    {
        $request->validate([
            'terms_condition' => 'required',
        ]);
        if(!empty($request->id)){
            TermsCondition::where('id', 1)->update(['terms_condition' => $request->terms_condition]);
        }else{
            $termscondition = new TermsCondition();
            $termscondition->terms_condition = $request->terms_condition;
            $termscondition->save();
        }
        $response['success'] = 1;
        $response['message'] = "Successfully Update Terms Condition";
        // flash()->success('Terms updated successfully.');
        echo json_encode($response);
    }
}
