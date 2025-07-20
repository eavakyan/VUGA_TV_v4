<?php

namespace App\Http\Controllers\admin;

use App\PrivacyPolicy;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class PrivacyPolicyController extends Controller
{
    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        $privacyPolicy = PrivacyPolicy::first();
        return view('admin.privacypolicy.index')->with('privacyPolicy', $privacyPolicy);
    }

    public function viewPrivacyPolicy()
    {
        $privacyPolicy = PrivacyPolicy::first();
        return view('admin.privacypolicy.view')->with('privacyPolicy', $privacyPolicy);
    }
    /**
     * Store a newly created resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function UpdatePrivacypolicy(Request $request)
    {
        $request->validate([
            'policy' => 'required',
        ]);
        if(!empty($request->id)){
            PrivacyPolicy::where('id', 1)->update(['policy' => $request->policy]);
        }else{
            $privacypolicy = new PrivacyPolicy();
            $privacypolicy->policy = $request->policy;
            $privacypolicy->save();
        }
        $response['success'] = 1;
        $response['message'] = "Successfully Update Privacy Policy";
        // flash()->success('Policy updated successfully.');
        echo json_encode($response);
    }
}
