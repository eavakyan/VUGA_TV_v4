<?php

namespace App\Http\Controllers\admin;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Redirect;
use URL;
use Hash;
use File;
use Session;
use DB;
use App\Admin;
use App\Actor;
use App\GlobalFunction;
use App\MovieCast;

class ActorController extends Controller
{

    public function viewListActor()
	{
		$total_actor = Actor::count();
		return view('admin.actor.actor_list')->with('total_actor',$total_actor);
    }

    public function CheckExistActor(Request $request)
	{
		$actor_name = $request->input('actor_name');
		$actor_id = $request->input('actor_id');

		if(!empty($actor_id)){
			$checkActor = Actor::selectRaw('*')->where('actor_name',$actor_name)->where('actor_id','!=',$actor_id)->first();
		}else{
			$checkActor = Actor::selectRaw('*')->where('actor_name',$actor_name)->first();
		}

		if(!empty($checkActor)) {
			return json_encode(FALSE);
		}else{
			return json_encode(TRUE);
		}
	}

    
	public function addUpdateActor(Request $request){
		$actor_id = $request->input('actor_id');
		$actor_name = $request->input('actor_name');

        $imageFileName = "";
		if ($request->hasfile('actor_image')) {
			$data['actor_image'] = GlobalFunction::saveFileAndGivePath($request->file('actor_image'));
		}

		$data['actor_name'] = $actor_name;

		if(!empty($actor_id)){
			$result =  Actor::where('actor_id',$actor_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		}else{
			$result =  Actor::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_actor = Actor::count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully ".$msg." Actor";
			$response['total_actor'] = $total_actor;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While ".$msg." Actor";
			$response['total_actor'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteActor(Request $request){

		$actor_id = $request->input('actor_id');
		$actorData = Actor::where('actor_id',$actor_id)->first();
		if($actorData && $actorData->actor_image && file_exists(public_path('uploads/').$actorData->actor_image)){
			unlink(public_path('uploads/').$actorData->actor_image);
		}

		$result = Actor::where('actor_id',$actor_id)->delete();
		$total_actor = Actor::count();
		MovieCast::where('actor_id',$actor_id)->delete();

		if ($result) {
			$response['success'] = 1;
			$response['total_actor'] = $total_actor;
		} else {
			$response['success'] = 0;
			$response['total_actor'] = 0;
		}
		echo json_encode($response);

	}

	public function showActorList(Request $request)
    {

		$columns = array( 
            0=>'actor_id',
            1=>'actor_name',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');

		if(empty($request->input('search.value')))
		{      
			$ActorData = Actor::offset($start)
					->limit($limit)
					->orderBy($order,$dir)
					->get();
            $totalData = $totalFiltered = Actor::count();
		}
		else {
			$search = $request->input('search.value'); 
			$ActorData =  Actor::where('actor_id','LIKE',"%{$search}%")
							->orWhere('actor_name', 'LIKE',"%{$search}%")
							->offset($start)
							->limit($limit)
							->orderBy($order,$dir)
							->get();

            $totalData = $totalFiltered = Actor::where('actor_id','LIKE',"%{$search}%")
                        ->orWhere('actor_name', 'LIKE',"%{$search}%")                 
                        ->count();
		}

		$data = array();
		if(!empty($ActorData))
		{
			foreach ($ActorData as $rows)
			{

                if(!empty($rows->actor_image))
                {
                    $profile = '<img height="60" width="60" src="'.url(env('DEFAULT_IMAGE_URL').$rows->actor_image).'">';
                }
                else
                {
                    $profile = '<img height="60px;" width="60px;" src="'.asset('assets/dist/img/default.png').'">';
                }
				
				if(Session::get('admin_id') == 2){ 
					$disabled = "disabled";
				}else{
					$disabled = "";
				}

				$data[]= array(
					$profile,
                    $rows->actor_name,
                    '<a class="UpdateActor" data-toggle="modal" data-target="#actorModal" data-id="'.$rows->actor_id.'" data-actor_name="'.$rows->actor_name.'" data-image="'.url(env('DEFAULT_IMAGE_URL').$rows->actor_image).'" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a>
					<a class="delete DeleteActor" data-id="'.$rows->actor_id.'" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
				); 
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
