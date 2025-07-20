<?php

namespace App\Http\Controllers\API;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rule;
use Validator;
use Hash;
use DB;
use File;
use Log;
use App\Admin;
use App\User;
use App\TVCategory;
use App\TVChannel;
use App\TVChannelSource;
use App\Common;
use Laravel\Passport\Token;

class TVController extends Controller
{

    public function GetTvCategoryist(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();
                
            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }
                       
            $rules = [
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;

            
            $Categoryata =  TVCategory::select('category_id','category_name','category_image')->offset($start)->limit($limit)->get();
            
            if (count($Categoryata) > 0) {
                return response()->json(['status' => 200, 'message' => "Category List Get Successfully.", 'data' => $Categoryata]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
        
       
    }

    public function getAllTvChannelList(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();
                
            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }
            
            $rules = [
                'user_id' => 'required',
            ];
    
            $validator = Validator::make($request->all(), $rules);
    
            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $user_id = $request->get('user_id'); 

            $User =  User::where('user_id', $user_id)->first();
            if (empty($User)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }

            $CategoryData =  TVCategory::orderBy('category_id','DESC') ->get();
                        
            if (count($CategoryData) > 0) {
                $TvChannelData = Common::GetTVChannelDataByCategory($CategoryData,$user_id);
                $TvChannelData = array_values($TvChannelData);
            }
            
            if (count($TvChannelData) > 0) {
                return response()->json(['status' => 200, 'message' => "TV Channel List Get Successfully.", 'data' => $TvChannelData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
        
        
    }

    
    public function getTvChannelListByCategoryID(Request $request)
    {
       
         
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }

            $headers = $request->headers->all();
                
            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'category_id' => 'required',
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }
            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;
            $category_id = $request->get('category_id');

            $TVChannelData =  TVChannel::select('*')
                        ->whereRaw("FIND_IN_SET( ".$category_id." , category_id) ")
                        ->orderBy('id','DESC')
                        ->offset($start)
                        ->limit($limit)
                        ->get();
            $TVChannellist = [];
            if($TVChannelData){
                $i=0;
                foreach($TVChannelData as $key => $value){
                    // $sourceData = TVChannelSource::where('channel_id',$value['channel_id'])->first();

                    $TVChannellist[$i]['id'] = $value['id'];
                    $TVChannellist[$i]['channel_id'] = $value['channel_id'];
                    $TVChannellist[$i]['channel_title'] = $value['channel_title'];
                    $TVChannellist[$i]['channel_thumb'] = $value['channel_thumb'];
                    $TVChannellist[$i]['access_type'] = $value['access_type'];
                    $TVChannellist[$i]['source_type'] = $value['source_type'];
                    $TVChannellist[$i]['source'] = $value['source'];
                    $i++;
                }
            }      
               
            return response()->json(['status' => 200, 'message' => "TV Channel Data Get Successfully.", 'data' => $TVChannellist]);

       
    }

    public function increaseTVChannelView(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();
                
            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }
                       
            $rules = [
                'channel_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $channel_id = $request->get('channel_id');

            TVChannel::where('channel_id',$channel_id)->increment('total_view',1);
            
            return response()->json(['status' => 200, 'message' => "TV Channel View Successfully."]);
        
    
    }

    public function increaseTVChannelShare(Request $request)
    {
      
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();
                
            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }
                       
            $rules = [
                'channel_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $channel_id = $request->get('channel_id');

            TVChannel::where('channel_id',$channel_id)->increment('total_share',1);
            
            return response()->json(['status' => 200, 'message' => "TV Channel Share Successfully."]);
        
       
    }

}

?>