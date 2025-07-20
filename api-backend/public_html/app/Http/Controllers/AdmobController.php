<?php

namespace App\Http\Controllers;

use App\Admob;
use Illuminate\Http\Request;

class AdmobController extends Controller
{
    function admob()
    {
        $admobAndroid = Admob::where('type', 1)->first();
        $admobiOS = Admob::where('type', 2)->first();
        return view('admob', [
            'admobAndroid' => $admobAndroid,
            'admobiOS' => $admobiOS,
        ]);
    }

    public function admobAndroid(Request $request)
    {
        $admobAndroid = Admob::where('type', $request->type)->first();

        if ($admobAndroid == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something went wrong',
            ]);
        }

        $admobAndroid->banner_id = $request->banner_id;
        $admobAndroid->intersial_id = $request->intersial_id;
        $admobAndroid->rewarded_id = $request->rewarded_id;
        $admobAndroid->save();

        return response()->json([
            'status' => true,
            'message' => 'Admob Updated Successfully',
        ]);
    }

    public function admobiOS(Request $request)
    {
        $admobAndroid = Admob::where('type', $request->type)->first();

        if ($admobAndroid == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something went wrong',
            ]);
        }


        $admobAndroid->banner_id = $request->banner_id;
        $admobAndroid->intersial_id = $request->intersial_id;
        $admobAndroid->rewarded_id = $request->rewarded_id;
        $admobAndroid->save();

        return response()->json([
            'status' => true,
            'message' => 'Admob Updated Successfully',
        ]);
    }

}
