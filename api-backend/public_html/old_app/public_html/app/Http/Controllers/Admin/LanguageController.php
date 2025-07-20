<?php

namespace App\Http\Controllers\admin;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Redirect;
use URL;
use Hash;
use Session;
use DB;
use App\Content;
use App\User;
use App\Language;

class LanguageController extends Controller
{

	public function viewListLanguage()
	{
		$total_language = Language::count();
		return view('admin.language.language_list')->with('total_language',$total_language);
    }

    public function CheckExistLanguage(Request $request)
	{
		$language_name = $request->input('language_name');
		$language_id = $request->input('language_id');

		if(!empty($language_id)){
			$checkLanguage = Language::selectRaw('*')->where('language_name',$language_name)->where('language_id','!=',$language_id)->first();
		}else{
			$checkLanguage = Language::selectRaw('*')->where('language_name',$language_name)->first();
		}

		if(!empty($checkLanguage)) {
			return json_encode(FALSE);
		}else{
			return json_encode(TRUE);
		}
	}

    
	public function addUpdateLanguage(Request $request){
		$language_id = $request->input('language_id');
		$language_name = $request->input('language_name');

		$data['language_name'] = $language_name;

		if(!empty($language_id)){
			$result =  Language::where('language_id',$language_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		}else{
			$result =  Language::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_language = Language::count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully ".$msg." Language";
			$response['total_language'] = $total_language;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While ".$msg." Language";
			$response['total_language'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteLanguage(Request $request){

		$language_id = $request->input('language_id');
		$result =  Language::where('language_id',$language_id)->delete();
		$total_language = Language::count();

		if ($result) {
			$response['success'] = 1;
			$response['total_language'] = $total_language;
		} else {
			$response['success'] = 0;
			$response['total_language'] = 0;
		}
		echo json_encode($response);

	}

	public function showLanguageList(Request $request)
    {

		$columns = array( 
            0=>'language_name',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');

		if(empty($request->input('search.value')))
		{      
			$LanguageData = Language::offset($start)
					->limit($limit)
					->orderBy($order,$dir)
					->get();
            $totalData = $totalFiltered = Language::count();
		}
		else {
			$search = $request->input('search.value'); 
			$LanguageData =  Language::where('language_id','LIKE',"%{$search}%")
							->orWhere('language_name', 'LIKE',"%{$search}%")
							->offset($start)
							->limit($limit)
							->orderBy($order,$dir)
							->get();

            $totalData = $totalFiltered = Language::where('language_id','LIKE',"%{$search}%")
                        ->orWhere('language_name', 'LIKE',"%{$search}%")                 
                        ->count();
		}

		$data = array();
		if(!empty($LanguageData))
		{
			foreach ($LanguageData as $rows)
			{

				if(Session::get('admin_id') == 2){ 
					$disabled = "disabled";
				}else{
					$disabled = "";
				}

				$data[]= array(
                    $rows->language_name,
                    '<a class="UpdateLanguage" data-toggle="modal" data-target="#languageModal" data-id="'.$rows->language_id.'" data-language_name="'.$rows->language_name.'" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a>
					<a class="delete DeleteLanguage" data-id="'.$rows->language_id.'" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
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
