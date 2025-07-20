<?php

namespace App\Http\Controllers\Admin;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Redirect;
use URL;
use Hash;
use Session;
use DB;
use File;
use App\Admin;
use App\User;
use App\Subscription;
use App\SubscriptionPackage;

class SubscriptionController extends Controller
{

	public function viewListSubscription()
	{
		$total_subscription = Subscription::count();
		return view('admin.subscription.subscription_list')->with('data',[])->with('total_subscription',$total_subscription);
	}

	public function deleteSubscription(Request $request){

		$subscription_id = $request->input('subscription_id');
        $data['is_delete'] = 1;		
		$result =  Subscription::where('subscription_id',$subscription_id)->delete();

		if ($result) {
			$response['success'] = 1;
		} else {
			$response['success'] = 0;
		}
		echo json_encode($response);

	}
    
	public function showSubscriptionList(Request $request)
    {
        $status= $request->input("status");
        $user_id= $request->input("user_id");
        if($status == 1){
            $columns = array( 
                0 => 'payment_type',
                1 => 'subscription_id',
                2 => 'subscription_id',
                3 => 'start_date',
                4 => 'expired_date',
                5 => 'amount',
               
            );
        }else{
            $columns = array( 
                0 => 'user_id',
                1 => 'payment_type',
                2 => 'subscription_id',
                3 => 'subscription_id',
                4 => 'start_date',
                5 => 'expired_date',
                6 => 'amount',
            );
        }

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');

		if(empty($request->input('search.value')))
		{      
			$query =  Subscription::select('tbl_subscription.*','u.fullname')->leftjoin('tbl_users as u', 'u.user_id', 'tbl_subscription.user_id');
            if($status == 1){
				$query->where('tbl_subscription.user_id',$user_id);
			}
            $SubscriptionData = $query->offset($start)
					->limit($limit)
					->orderBy($order,$dir)
					->get();

            $query =  Subscription::select('tbl_subscription.*','u.fullname')->leftjoin('tbl_users as u', 'u.user_id', 'tbl_subscription.user_id');
            if($status == 1){
				$query->where('tbl_subscription.user_id',$user_id);
			}
            $totalData = $totalFiltered = $query->count();
		}
		else {
			$search = $request->input('search.value'); 

			$query = Subscription::select('tbl_subscription.*','u.fullname')->leftjoin('tbl_users as u', 'u.user_id', 'tbl_subscription.user_id');
            if($status == 1){
                $query->where(function ($query1) use ($user_id) {
                    $query1->where('tbl_subscription.user_id',$user_id);;
                });
            }
            $query->where(function($query1) use ($search) {
                $query1->orWhere('tbl_subscription.subscription_id','LIKE',"%{$search}%")
                ->orWhere('tbl_subscription.start_date', 'LIKE',"%{$search}%")
                ->orWhere('tbl_subscription.expired_date', 'LIKE',"%{$search}%")
                ->orWhere('tbl_subscription.amount', 'LIKE',"%{$search}%")
                ->orWhere('u.fullname', 'LIKE',"%{$search}%");
            });

            $SubscriptionData = $query->offset($start)
                ->limit($limit)
                ->orderBy($order,$dir)
                ->get();

            $query = Subscription::select('tbl_subscription.*','u.fullname')->leftjoin('tbl_users as u', 'u.user_id', 'tbl_subscription.user_id');
            if($status == 1){
                $query->where(function ($query1) use ($user_id) {
                    $query1->where('tbl_subscription.user_id',$user_id);;
                });
            }
            $query->where(function($query1) use ($search) {
                $query1->orWhere('tbl_subscription.subscription_id','LIKE',"%{$search}%")
                ->orWhere('tbl_subscription.start_date', 'LIKE',"%{$search}%")
                ->orWhere('tbl_subscription.expired_date', 'LIKE',"%{$search}%")
                ->orWhere('tbl_subscription.amount', 'LIKE',"%{$search}%")
                ->orWhere('u.fullname', 'LIKE',"%{$search}%");
            });
     
            $totalData	= $totalFiltered =  $query->count();
		}

		$data = array();
		if(!empty($SubscriptionData))
		{
			foreach ($SubscriptionData as $rows)
			{
				if(Session::get('admin_id') == 2){ 
					$disabled = "disabled";
				}else{
					$disabled = "";
				}
                $packageData = SubscriptionPackage::where('package_id',$rows->package_id)->first();
                if($packageData['duration'] == 1){
                    $pcak = 'Monthly';
                    $duration = '30 Day(s)';
                }
                if($packageData['duration'] == 2){
                    $pcak = 'Yearly';
                    $duration = '365 Day(s)';
                }
                if($rows->payment_type == 1){
                    $payment_type = 'Cash';
                }else{
                    $payment_type = 'Card';
                }
                if($status == 1){
                    $data[]= array(
                        // $rows->subscription_id,
                        $payment_type,
                        $pcak,
                        $duration,
                        date('d-m-Y',strtotime($rows->start_date)),
                        date('d-m-Y',strtotime($rows->expired_date)),
                        '$'.$rows->amount,
                        '<button class="delete DeleteSubscription btn btn-danger" data-id="'.$rows->subscription_id.'" >Delete</button>'
                    ); 
                }else{
                    $view =  route('user/view',$rows->user_id);	

                    $data[]= array(
                        // $rows->subscription_id,
                        '<a href="'.$view.'">'.$rows->fullname.' '.$rows->last_name.'</a>',
                        $payment_type,
                        $pcak,
                        $duration,
                        date('d-m-Y',strtotime($rows->start_date)),
                        date('d-m-Y',strtotime($rows->expired_date)),
                        '$'.$rows->amount,
                        '<button class="delete DeleteSubscription btn btn-danger" data-id="'.$rows->subscription_id.'" >Delete</button>'
                    ); 
                }
               
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),  
			"recordsTotal"    => intval($totalData),  
			"recordsFiltered" => intval($totalFiltered), 
			"data"            => $data   
			);

		echo json_encode($json_data); 
        exit();
	}	

}


 