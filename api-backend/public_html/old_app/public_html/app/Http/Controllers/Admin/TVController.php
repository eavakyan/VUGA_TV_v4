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
use App\TVCategory;
use App\TVChannel;
use App\TVChannelSource;
use App\Common;
use App\GlobalFunction;

class TVController extends Controller
{

    public function viewListTVCategory()
	{
		$total_category = TVCategory::count();
		return view('admin.tv.tv_category_list')->with('total_category',$total_category);
    }

    public function CheckExistTVCategory(Request $request)
	{
		$category_name = $request->input('category_name');
		$category_id = $request->input('category_id');

		if(!empty($category_id)){
			$checkTVCategory = TVCategory::selectRaw('*')->where('category_name',$category_name)->where('category_id','!=',$category_id)->first();
		}else{
			$checkTVCategory = TVCategory::selectRaw('*')->where('category_name',$category_name)->first();
		}

		if(!empty($checkTVCategory)) {
			return json_encode(FALSE);
		}else{
			return json_encode(TRUE);
		}
	}

    
	public function addUpdateTVCategory(Request $request){
		$category_id = $request->input('category_id');
		$category_name = $request->input('category_name');

		if ($request->hasfile('category_image')) {
			$data['category_image'] = GlobalFunction::saveFileAndGivePath($request->file('category_image'));
		}

		$data['category_name'] = $category_name;

		if(!empty($category_id)){
			$result =  TVCategory::where('category_id',$category_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		}else{
			$result =  TVCategory::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_category = TVCategory::count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully ".$msg." TVCategory";
			$response['total_category'] = $total_category;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While ".$msg." TVCategory";
			$response['total_category'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteTVCategory(Request $request){

		$category_id = $request->input('category_id');
		$categoryData = TVCategory::where('category_id',$category_id)->first();
		if($categoryData && $categoryData->category_image && file_exists(public_path('uploads/').$categoryData->category_image)){
			unlink(public_path('uploads/').$categoryData->category_image);
		}

		$result = TVCategory::where('category_id',$category_id)->delete();
		$total_category = TVCategory::count();

		if ($result) {
			$response['success'] = 1;
			$response['total_category'] = $total_category;
		} else {
			$response['success'] = 0;
			$response['total_category'] = 0;
		}
		echo json_encode($response);

	}

	public function showTVCategoryList(Request $request)
    {

		$columns = array( 
            0=>'category_id',
            1=>'category_name',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');

		if(empty($request->input('search.value')))
		{      
			$TVCategoryData = TVCategory::offset($start)
					->limit($limit)
					->orderBy($order,$dir)
					->get();
            $totalData = $totalFiltered = TVCategory::count();
		}
		else {
			$search = $request->input('search.value'); 
			$TVCategoryData =  TVCategory::where('category_id','LIKE',"%{$search}%")
							->orWhere('category_name', 'LIKE',"%{$search}%")
							->offset($start)
							->limit($limit)
							->orderBy($order,$dir)
							->get();

            $totalData = $totalFiltered = TVCategory::where('category_id','LIKE',"%{$search}%")
                        ->orWhere('category_name', 'LIKE',"%{$search}%")                 
                        ->count();
		}

		$data = array();
		if(!empty($TVCategoryData))
		{
			foreach ($TVCategoryData as $rows)
			{

                if(!empty($rows->category_image))
                {
                    $profile = '<img height="60" width="60" src="'.url(env('DEFAULT_IMAGE_URL').$rows->category_image).'">';
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
                    $rows->category_name,
                    '<a class="UpdateTVCategory" data-toggle="modal" data-target="#categoryModal" data-id="'.$rows->category_id.'" data-category_name="'.$rows->category_name.'" data-image="'.url(env('DEFAULT_IMAGE_URL').$rows->category_image).'" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a>
					<a class="delete DeleteTVCategory" data-id="'.$rows->category_id.'" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
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



	public function viewListTVChannel()
	{
		$total_channel = TVChannel::count();
		return view('admin.tv.tv_channel_list')->with('total_channel',$total_channel);
    }


	public function viewTVChannel($id="")
	{
		$categoryData = TVCategory::count();
		$data = TVChannel::where('channel_id',$id)->first();
		return view('admin.tv.channel_view')->with('data',$data);
	}

    public function viewAddTVChannel()
	{
		$categoryData = TVCategory::get();
		return view('admin.tv.tv_channel_addupdate')->with('categoryData',$categoryData)->with('data',[])->with('sourceData',[])->with('channel_id',0)->with('channel_title','')->with('title','Add');
		
	}
	
	public function viewUpdateTVChannel($id="")
	{
		$data = TVChannel::where('channel_id',$id)->first();
		$sourceData = TVChannelSource::where('channel_id',$id)->get();
		$categoryData = TVCategory::get();
		if($data['total_view']){
			$data['total_view'] = Common::number_format_short($data['total_view']);
		}
		if($data['total_share']){
			$data['total_share'] = Common::number_format_short($data['total_share']);
		}
		return view('admin.tv.tv_channel_addupdate')->with('categoryData',$categoryData)->with('data',$data)->with('sourceData',$sourceData)->with('channel_id',$id)->with('channel_title',$data['channel_title'])->with('title','Edit');
	}


    public function CheckExistTVChannel(Request $request)
	{
		$channel_title = $request->input('channel_title');
		$channel_id = $request->input('channel_id');

		if(!empty($channel_id)){
			$checkTVChannel = TVChannel::selectRaw('*')->where('channel_title',$channel_title)->where('channel_id','!=',$channel_id)->first();
		}else{
			$checkTVChannel = TVChannel::selectRaw('*')->where('channel_title',$channel_title)->first();
		}

		if(!empty($checkTVChannel)) {
			return json_encode(FALSE);
		}else{
			return json_encode(TRUE);
		}
	}

	public function addUpdateTVChannel(Request $request){
		$action = $request->input('action');
		$channel_id = $request->input('channel_id');
		$channel_title = $request->input('channel_title');
		$access_type = $request->input('access_type');
		$category_id = $request->input('category_id');
		$source_id = $request->input('source_id');
		$source_type = $request->input('source_type');
		$source = $request->input('source');
		$source_file = $request->input('source_file');
		
        $imageFileName = "";
		if ($request->hasfile('channel_thumb')) {
			$data['channel_thumb'] = GlobalFunction::saveFileAndGivePath($request->file('channel_thumb'));
		}

		$data['channel_title'] = $channel_title;
		$data['access_type'] = $access_type;
		$data['category_id'] = implode(',',$category_id);
		
		$data['source_type'] = $source_type;
		$data['source'] = $source;
		
		if(empty($channel_id)){
			$channel_id = TVChannel::get_random_string();
		}
		$data['channel_id'] = $channel_id;
	
		if($action == 'update'){
			$result =  TVChannel::where('channel_id',$channel_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		}else{
			$result =  TVChannel::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}

		// $i=0;
		// foreach($source_id as $k => $value){

		// 	$data1['channel_id'] = $channel_id;
		// 	$data1['source_type'] =  $source_type[$i];
		// 	if($source_type[$i] == 7){
		// 		$data1['source'] = $source_file[$i];
		// 	}else{
		// 		$data1['source'] = $source[$i];
		// 	}

		// 	$TVChannelSourceData = TVChannelSource::where('source_id',$value)->first();
		// 	if($TVChannelSourceData){
		// 		TVChannelSource::where('source_id',$value)->update($data1);
		// 	}else{
		// 		if($data1['source']){
		// 			TVChannelSource::insert($data1);
		// 		}
		// 	}
		// 	$i++;			
		// }

		$total_channel = TVChannel::count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully ".$msg." TV Channel";
			$response['total_channel'] = $total_channel;
			$response['channel_id'] = $channel_id;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While ".$msg." TV Channel";
			$response['total_channel'] = 0;
			$response['channel_id'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteTVChannel(Request $request){

		$channel_id = $request->input('channel_id');
		$channelData = TVChannel::where('channel_id',$channel_id)->first();
		if($channelData && file_exists(public_path('uploads/').$channelData->channel_thumb)){
			unlink(public_path('uploads/').$channelData->channel_thumb);
		}
		$channelSourceData = TVChannelSource::where('channel_id',$channel_id)->first();
		if($channelSourceData){
			foreach($channelSourceData as $value){
				if($value->source_type == 7 && file_exists(public_path('uploads/').$value->source)){
					unlink(public_path('uploads/').$value->source);
				}
			}
		}

		$result = TVChannel::where('channel_id',$channel_id)->delete();
		$total_channel = TVChannel::count();

		if ($result) {
			$response['success'] = 1;
			$response['total_channel'] = $total_channel;
		} else {
			$response['success'] = 0;
			$response['total_channel'] = 0;
		}
		echo json_encode($response);

	}

	public function deleteChannelSource(Request $request){
		$source_id = $request->input('source_id');
		TVChannelSource::where('source_id',$source_id)->delete();
		echo 1;
	}

	public function showTVChannelList(Request $request)
    {

		$columns = array( 
            0=>'channel_id',
            1=>'channel_title',
			2=>'category_id',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');

		if(empty($request->input('search.value')))
		{      
			$TVChannelData = TVChannel::offset($start)
					->limit($limit)
					->orderBy($order,$dir)
					->get();
            $totalData = $totalFiltered = TVChannel::count();
		}
		else {
			$search = $request->input('search.value'); 
			$TVChannelData =  TVChannel::where('channel_id','LIKE',"%{$search}%")
							->orWhere('channel_title', 'LIKE',"%{$search}%")
							->orWhere('category_id', 'LIKE',"%{$search}%")
							->offset($start)
							->limit($limit)
							->orderBy($order,$dir)
							->get();

            $totalData = $totalFiltered = TVChannel::where('channel_id','LIKE',"%{$search}%")
                        ->orWhere('channel_title', 'LIKE',"%{$search}%")  
						->orWhere('category_id', 'LIKE',"%{$search}%")               
                        ->count();
		}

		$data = array();
		if(!empty($TVChannelData))
		{
			foreach ($TVChannelData as $rows)
			{

                if(!empty($rows->channel_thumb))
                {
                    $profile = '<img height="60" width="60" src="'.url(env('DEFAULT_IMAGE_URL').$rows->channel_thumb).'">';
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
				$categoryData = TVCategory::select(DB::raw('group_concat(category_name) as category_name'))->whereIn('category_id', explode(',',$rows->category_id))->first();
				// $view =  route('channel/view',$rows->channel_id);
				$edit =  route('channel/edit',$rows->channel_id);	

				$data[]= array(
					$profile,
                    $rows->channel_title,
					$categoryData['category_name'],
                    '<a href="'.$edit.'" class="edit" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a>
					<a class="delete DeleteTVChannel" data-id="'.$rows->channel_id.'" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
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


	public function viewTvChannelSource($id)
	{
		$data = TVChannel::where('channel_id',$id)->first();
		$sourceData = TVChannelSource::where('channel_id',$id)->get();
		$total_source = TVChannelSource::where('channel_id',$id)->count();
		if($data['total_view']){
			$data['total_view'] = Common::number_format_short($data['total_view']);
		}
		if($data['total_share']){
			$data['total_share'] = Common::number_format_short($data['total_share']);
		}
		if(Session::get('admin_id') == 1){ 
			return view('admin.tv.tv_channel_source_addupdate')->with('total_source',$total_source)->with('data',$data)->with('sourceData',$sourceData)->with('channel_id',$id)->with('channel_title',$data['channel_title'])->with('title','Edit');
		}else{
			return Redirect::route('dashboard');
		}

	}

	public function UploadSourceMedia(Request $request){

        $imageFileName = "";
		if ($request->hasfile('source_video')) {
			$imageFileName = GlobalFunction::saveFileAndGivePath($request->file('source_video'));
		}
		if ($imageFileName) {
            $response['success'] = 1;
            $response['default_path'] = url(env('DEFAULT_IMAGE_URL'));
			$response['source_video'] = $imageFileName;
		} else {
            $response['success'] = 0;
            $response['default_path'] = "";
			$response['source_video'] = "";
		}
		echo json_encode($response);
	}
	
	public function addUpdateTVChannelSource(Request $request){
		$source_id = $request->input('source_id');
		$channel_id = $request->input('channel_id');
		// $source_title = $request->input('source_title');
		// $source_quality = $request->input('source_quality');
		// $source_size = $request->input('source_size');
		// $downloadable = $request->input('downloadable');
		// $access_type = $request->input('access_type');
		$source_type = $request->input('source_type');
		$source = $request->input('source');
		$source_file = $request->input('source_file');

		if($source_type == 7){
			$data['source'] = $source_file;
		}else{
			$data['source'] = $source;
		}

		$data['channel_id'] = $channel_id;
		// $data['source_title'] = $source_title;
		// $data['source_quality'] = $source_quality;
		// $data['source_size'] = $source_size;
		// $data['downloadable'] = ($downloadable == 'on') ? 1 : 0;
		// $data['access_type'] = $access_type;
		$data['source_type'] = $source_type;

		if(!empty($source_id)){
			$result =  TVChannelSource::where('source_id',$source_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		}else{
			$result =  TVChannelSource::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_source = TVChannelSource::where('channel_id',$channel_id)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully ".$msg." Content Source";
			$response['total_source'] = $total_source;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While ".$msg." Content Source";
			$response['total_source'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteTVChannelSource(Request $request){
		$source_id = $request->input('source_id');
		$channel_id = $request->input('channel_id');
		$sourceData = TVChannelSource::where('source_id',$source_id)->first();
		if($sourceData && $sourceData->source_type == 7 && file_exists(public_path('uploads/').$sourceData->source)){
			unlink(public_path('uploads/').$sourceData->source);
		}

		$result = TVChannelSource::where('source_id',$source_id)->delete();
		$total_source = TVChannelSource::where('channel_id',$channel_id)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_source'] = $total_source;
		} else {
			$response['success'] = 0;
			$response['total_source'] = 0;
		}
		echo json_encode($response);

	}

	
	public function showTVChannelSourceList(Request $request)
    {

		$columns = array( 
			0 => 'source_type',
			// 1 => 'source_title',
			1 => 'source',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$channel_id= $request->input("channel_id");

		if(empty($request->input('search.value')))
		{      
			$TVChannelSourceData  = TVChannelSource::where('channel_id',$channel_id)->offset($start)
					->limit($limit)
					->orderBy($order,$dir)
					->get();

            $totalData = $totalFiltered = TVChannelSource::where('channel_id',$channel_id)->count();
		}
		else {
			$search = $request->input('search.value'); 
			$query =  TVChannelSource::where('channel_id',$channel_id)->where('source_id','LIKE',"%{$search}%")
                            // ->orWhere('source_title', 'LIKE',"%{$search}%")
                            ->orWhere('source_type', 'LIKE',"%{$search}%")
                            ->orWhere('source', 'LIKE',"%{$search}%");
               
                $TVChannelSourceData = $query->offset($start)
                            ->limit($limit)
                            ->orderBy($order,$dir)
                            ->get();

                $query =  TVChannelSource::where('channel_id',$channel_id)->where('source_id','LIKE',"%{$search}%")
							// ->orWhere('source_title', 'LIKE',"%{$search}%")
                            ->orWhere('source_type', 'LIKE',"%{$search}%")
                            ->orWhere('source', 'LIKE',"%{$search}%");
                
                $totalData = $totalFiltered = $query->count();
		}

		$data = array();
		if(!empty($TVChannelSourceData))
		{
			foreach ($TVChannelSourceData as $rows)
			{

				if(Session::get('admin_id') == 2){ 
					$disabled = "disabled";
				}else{
					$disabled = "";
				}
				
				if($rows->source_type == 1){
                    $source_label = 'Youtube Id';
					$source = $source_html = $rows->source;
				}
				if($rows->source_type == 2){
					$source_label = 'M3u8 Url';
					$source = $source_html = $rows->source;
				}
				if($rows->source_type == 3){
					$source_label = 'Mov Url';
					$source = $source_html = $rows->source;
				}
				if($rows->source_type == 4){
					$source_label = 'Mp4 Url';
					$source = $source_html = $rows->source;
				}
				if($rows->source_type == 5){
					$source_label = 'Mkv Url';
					$source = $source_html = $rows->source;
				}
				if($rows->source_type == 6){
					$source_label = 'Webm Url';
					$source = $source_html = $rows->source;
				}
				if($rows->source_type == 7){
					$source_label = 'File';
					$source = url(env('DEFAULT_IMAGE_URL').$rows->source);
					$source_html = '<button data-toggle="modal" data-target="#modal-video" data-src="'.url(env('DEFAULT_IMAGE_URL').$rows->source).'" class="btn btn-success text-white" id="playvideomdl" title="Play Video"><i class="fa fa-play" style="font-size: 14px;" ></i></button>';
				}
				
                $data[]= array(
					$source_label,
					// $rows->source_title,
					$source_html,
					'<a class="updateTVChannelSource"  data-toggle="modal" data-target="#SourceModal" data-id="'.$rows->source_id.'" data-source_type="'.$rows->source_type.'" data-source="'.$rows->source.'"  ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a> <a class="delete DeleteTVChannelSource" data-id="'.$rows->source_id.'" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
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
